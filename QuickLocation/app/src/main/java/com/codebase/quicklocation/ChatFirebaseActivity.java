package com.codebase.quicklocation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.codebase.quicklocation.adapters.ChatFirebaseAdapter;
import com.codebase.quicklocation.adapters.ClickListenerChatFirebase;
import com.codebase.quicklocation.database.Favorites;
import com.codebase.quicklocation.database.dao.FavoritesDao;
import com.codebase.quicklocation.firebasedb.ChatMessage;
import com.codebase.quicklocation.firebasedb.TypeGroup;
import com.codebase.quicklocation.firebasedb.UserStructure;
import com.codebase.quicklocation.model.ChatModel;
import com.codebase.quicklocation.model.UserModel;
import com.codebase.quicklocation.utils.Utils;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

public class ChatFirebaseActivity extends AppCompatActivity implements View.OnClickListener, ClickListenerChatFirebase {
    private static final int ADD_ACTIVITY_FAVORITE = 5;
    private static final int FROM_FAVORITE = 6;
    private String placeId;
    FirebaseListAdapter<ChatMessage> adapter;
    FloatingActionButton fab;
    private EditText input;
    private String group_id;
    private String create_by;
    private String chats_node;
    private DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Utils.messages);
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private UserModel userModel;
    private FavoritesDao dataDao = new FavoritesDao(this);
    //Views UI
    private RecyclerView rvListMessage;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageView btSendMessage;
    private View contentRoot;
    private EditText edMessage;
    private Context context;
    private String userUID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        context = this;
        group_id = getIntent().getExtras().getString("group_id");
        create_by = getIntent().getExtras().getString("create_by");

        userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        addChatToGruop();
        viewShow();
        userAuthentication();
        setTitle(getIntent().getExtras().getString("title"));

    }

    private void userAuthentication() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(Utils.users).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("User", dataSnapshot.getValue().toString());
                UserStructure userStructure = dataSnapshot.getValue(UserStructure.class);
                userModel = new UserModel(userStructure.getFullname(), "", mFirebaseUser.getUid());
                lerMessagensFirebase();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void viewShow() {
        edMessage = (EditText) findViewById(R.id.editTextMessage);
        contentRoot = findViewById(R.id.contentRoot);
        btSendMessage = (ImageView) findViewById(R.id.buttonMessage);
        btSendMessage.setOnClickListener(this);
        rvListMessage = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
    }

    private void lerMessagensFirebase() {
        final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(mFirebaseDatabaseReference.child(chats_node), userModel.getName(), this);
        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    rvListMessage.scrollToPosition(positionStart);
                }
            }
        });
        rvListMessage.setLayoutManager(mLinearLayoutManager);
        rvListMessage.setAdapter(firebaseAdapter);
    }

    private void addChatToGruop() {
        FirebaseDatabase.getInstance().getReference().child(Utils.groups).child(group_id).child("chats").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Iterable<DataSnapshot> dsChildData = dataSnapshot.getChildren();
                    for (DataSnapshot dsChild : dsChildData) {
                        chats_node = dsChild.getKey();
                    }
                } else {
                    chats_node = mFirebaseDatabaseReference.child(Utils.groups).child(group_id).child(Utils.chats).push().getKey();
                    DatabaseReference rootDataBase = FirebaseDatabase.getInstance().getReference().child(Utils.groups).child(group_id).child(Utils.chats);
                    TypeGroup typeGroup = new TypeGroup(chats_node, true);
                    Map<String, Object> typeValue = typeGroup.toMap();
                    rootDataBase.updateChildren(typeValue);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public String getTimestamp(long timestamp) {
        Date date = new Date(timestamp * 1000);
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy hh:mm", Locale.getDefault());
        dateFormat.setCalendar(calendar);
        calendar.setTime(date);
        return dateFormat.format(date);
    }

    @Override
    public void onClick(View v) {
        final Animation anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.anim_alpha);
        v.startAnimation(anim);
        switch (v.getId()) {
            case R.id.buttonMessage:
                sendMessagetoFirebase();
                break;
        }
    }

    private void sendMessagetoFirebase() {
        ChatModel model = new ChatModel(group_id,userModel, edMessage.getText().toString(), Calendar.getInstance().getTime().getTime() + "", null);
        if (!"".equals(edMessage.getText().toString())) {
            mFirebaseDatabaseReference.child(chats_node).push().setValue(model);
            edMessage.setText(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_favorite, menu);
        return true;
    }


    /**
     * Función para agregar favoritos.
     *
     * @param item
     */
    public void addFavorite(MenuItem item) {
        Intent intent = new Intent(context, FavoritesActivity.class);
        intent.putExtra("add_favorite", true);
        startActivityForResult(intent, ADD_ACTIVITY_FAVORITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_ACTIVITY_FAVORITE) {
            String favoriteJSON = data.getStringExtra("cdata");
            sendMyFavorite(favoriteJSON);
        }
    }

    /**
     * Agrega al chat el item de favorito
     *
     * @param favoriteJSON
     */
    private void sendMyFavorite(String favoriteJSON) {
        Favorites favorite = Utils.factoryGson().fromJson(favoriteJSON, Favorites.class);
        placeId = favorite.getPlaceId();
        ChatModel model = new ChatModel(group_id,userModel, edMessage.getText().toString(), Calendar.getInstance().getTime().getTime() + "", favorite);
        mFirebaseDatabaseReference.child(chats_node).push().setValue(model);
        edMessage.setText(null);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {}

    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {}

    @Override
    public void clickFavoritos(View view, Favorites favorites) {
        if(favorites != null) {
            Intent intent = new Intent(this, PlaceDetailActivity.class);
            intent.putExtra("placeId",favorites.getPlaceId());
            intent.putExtra("placeName",favorites.getLocalName());
            intent.putExtra("placeRating",favorites.getRating());
            intent.putExtra("app_categoria",favorites.getCategory());
            intent.putExtra("from_favorito", true);
            startActivityForResult(intent,FROM_FAVORITE);
        }
    }
}
