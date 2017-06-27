package com.codebase.quicklocation;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.codebase.quicklocation.adapters.ChatFirebaseAdapter;
import com.codebase.quicklocation.adapters.ClickListenerChatFirebase;
import com.codebase.quicklocation.firebasedb.ChatMessage;
import com.codebase.quicklocation.firebasedb.TypeGroup;
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

public class ChatFirebaseActivity extends AppCompatActivity implements View.OnClickListener,ClickListenerChatFirebase {

    FirebaseListAdapter<ChatMessage> adapter;
    FloatingActionButton fab;
    private EditText input;
    private String group_id;
    private String chats_node;
    private DatabaseReference mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Utils.messages);
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private UserModel userModel;

    //Views UI
    private RecyclerView rvListMessage;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageView btSendMessage;
    private View contentRoot;
    private EditText edMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //fab = (FloatingActionButton) findViewById(R.id.fab_send);
       // input = (EditText) findViewById(R.id.input);
        //fab.setOnClickListener(this);

        group_id = getIntent().getExtras().getString("group_id");

        addChatToGruop();
        viewShow();
        userAuthentication();
    }

    private void userAuthentication() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        userModel = new UserModel(mFirebaseUser.getEmail().toString(), "", mFirebaseUser.getUid() );

    }

    private void viewShow() {
        edMessage = (EditText)findViewById(R.id.editTextMessage);
        contentRoot = findViewById(R.id.contentRoot);
        btSendMessage = (ImageView)findViewById(R.id.buttonMessage);
        btSendMessage.setOnClickListener(this);
        rvListMessage = (RecyclerView)findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
    }





    private void lerMessagensFirebase(){
       // mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(mFirebaseDatabaseReference.child(chats_node),userModel.getName(),this);
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
                    for(DataSnapshot dsChild : dsChildData){
                        chats_node = dsChild.getKey();
                    }
                }else {
                    //Log.e("test","value = null");
                    chats_node =  mFirebaseDatabaseReference.child(Utils.groups).child(group_id).child(Utils.chats).push().getKey();
                    DatabaseReference rootDataBase = FirebaseDatabase.getInstance().getReference().child(Utils.groups).child(group_id).child(Utils.chats);
                    TypeGroup typeGroup = new TypeGroup(chats_node,true);
                    Map<String, Object> typeValue = typeGroup.toMap();
                    rootDataBase.updateChildren(typeValue);
                }

                lerMessagensFirebase();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*@Deprecated
    private void displayChatMessage()
    {
        ListView  listView = (ListView)findViewById(R.id.list_of_messages);


        adapter = new FirebaseListAdapter<ChatMessage>(this,ChatMessage.class, R.layout.message_right, msgDBRoot.child(chats_node)) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
//                if (model.getUserMessage().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {


                    TextView txtMessage, txtUser, txtTime;

                    isEnabled(position);
                    txtMessage = (TextView) v.findViewById(R.id.message_text);
                    txtUser = (TextView) v.findViewById(R.id.message_user);
                    txtTime = (TextView) v.findViewById(R.id.txtTime);

                    if (!"".equals(txtMessage.toString()) && !"".equals(txtUser.toString()) && !"".equals(txtTime.toString())) {
                        txtMessage.setText(model.getMessage());
                        txtUser.setText(model.getUserMessage());
                        txtTime.setText(getTimestamp(model.getTimeOfMessage()));
                    }

            }

            @Override
            public View getView(int position, View view, ViewGroup viewGroup) {

                return super.getView(position, view, viewGroup);

            }
        };
       // if(adapter.getCount()>0)
            listView.setAdapter(adapter);
    }*/
    public String getTimestamp(long timestamp)
    {
        Date date = new Date(timestamp*1000);
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy hh:mm", Locale.getDefault());
        dateFormat.setCalendar(calendar);
        calendar.setTime(date);
        return dateFormat.format(date);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.buttonMessage:
                sendMessagetoFirebase();
                /*String msg = input.getText().toString();
                if (!"".equals(msg)) {
                    msgDBRoot.child(chats_node).push().setValue(new ChatMessage(msg, FirebaseAuth.getInstance().getCurrentUser().getEmail(), System.currentTimeMillis() / 1000));
                    adapter.notifyDataSetChanged();
                    input.setText("");
                }*/
                break;
        }
    }

    private void sendMessagetoFirebase() {
        ChatModel model = new ChatModel(userModel,edMessage.getText().toString(), Calendar.getInstance().getTime().getTime()+"",null);
        //String msg = input.getText().toString();
        //if (!"".equals(msg)) {
            mFirebaseDatabaseReference.child(chats_node).push().setValue(model);
            // mFirebaseDatabaseReference.child(chats_node).push().setValue(new ChatMessage(msg, FirebaseAuth.getInstance().getCurrentUser().getEmail(), System.currentTimeMillis() / 1000));
            //adapter.notifyDataSetChanged();
            edMessage.setText(null);
       // }
    }

    @Override
    public void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick) {

    }

    @Override
    public void clickImageMapChat(View view, int position, String latitude, String longitude) {

    }
}
