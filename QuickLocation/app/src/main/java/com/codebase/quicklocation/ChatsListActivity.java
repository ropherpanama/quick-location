package com.codebase.quicklocation;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;

import com.codebase.quicklocation.adapters.ChatsFirebaseAdapter;
import com.codebase.quicklocation.firebasedb.Group;
import com.codebase.quicklocation.firebasedb.TypeGroup;
import com.codebase.quicklocation.utils.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatsListActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView lstChatsFirebase;
    private RecyclerView recyclerView;
    private   Toolbar toolbar;
    private RecyclerView.Adapter adapter;
    private List<Group> groups = new ArrayList<>();
    private Context context;
    private DatabaseReference rootDataBase = FirebaseDatabase.getInstance().getReference().child(Utils.groups);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context= this;
        setContentView(R.layout.activity_chats_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //DatabaseReference mDatabaseChats = FirebaseDatabase.getInstance().getReference();
        //mDatabaseChats = mDatabaseChats.child(Utils.groups);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);


        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                maplist(dataSnapshot);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        rootDataBase.addValueEventListener(eventListener);
        adapter = new ChatsFirebaseAdapter(groups, new ChatsFirebaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Group item) {
                Log.d("Item... "," "+item.getTitle());
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.lstFirebaseChats);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatsListActivity.this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(adapter);

    }

    /**
     * Recorre la lista de child
     */
    private void maplist(DataSnapshot map) {
        groups.clear();
        for (DataSnapshot dataSnapshot: map.getChildren()) {
            Group group = new Group();
           // TypeGroup typeGroup = dataSnapshot.getValue(TypeGroup.class);
            group = dataSnapshot.getValue(Group.class);   //mainChildChat.setTitle(dataSnapshot.getKey());
            groups.add(group);
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
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.fab:
                showAlertDialog(v);
                break;
        }
    }

    /**
     * Muestra al usuario una ventana para llenar la informacion de grupos para chats.
     * @param v
     */
    private void showAlertDialog(final View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.user_input_dialog_box,null);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setView(view);

        final EditText txtTitle = (AutoCompleteTextView)view.findViewById(R.id.txtTituloGroupo);
        final EditText txtDescripcion = (AutoCompleteTextView)view.findViewById(R.id.txtDescripcion);

        builder.setCancelable(false).setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = txtTitle.getText().toString();
                String description = txtDescripcion.getText().toString();
                boolean cancel = false;
                View focusView = null;

                if (TextUtils.isEmpty(title))
                {
                    txtTitle.setError(getString(R.string.error_field_required));
                    focusView = txtTitle;
                    cancel = true;

                }
                if (TextUtils.isEmpty(description))
                {
                    txtDescripcion.setError(getString(R.string.error_field_required));
                    focusView = txtDescripcion;
                    cancel = true;

                }

                if (cancel)
                {
                 focusView.requestFocus();
                }else {
                    dialog.dismiss();
                    //Map<String, Object> params = new HashMap<>();
                    //params.put("-KnSpnnQOwRNP4I44Oms", true);
                    TypeGroup member = new TypeGroup("-KnSpnnQOwRNP4I44Oms",true);
                    String key_group = rootDataBase.push().getKey();
                    DatabaseReference group_refer = rootDataBase.child(key_group);
                    Group groupNew = new Group(title,description,member);
                    Map<String, Object> groupValue = groupNew.toMap();
                    group_refer.updateChildren(groupValue);

                    /*Map<String, Object> params = new HashMap<>();
                    params.put("title", title);
                    params.put("description", description);
                   // params.put("men",userName);
                   // group_refer.updateChildren(params);
                    UsersDao usersDao = new UsersDao(context);
                    //save menbers
                    //DatabaseReference menReference = group_refer.child(Utils.menbers);
                    Map<String, Object> menbers = new HashMap<>();
                    //menbers.put(usersDao.getAll().get(0).getEmail(),true);
                    menbers.put("/"+key_group+"/",params);
                    menbers.put("/"+Utils.menbers+"/"+usersDao.getAll().get(0).getEmail(), true);
                    group_refer.updateChildren(menbers);*/
                    //  group_refer.child("members").updateChildren(usersDao);

                }

            }
        })
                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                     dialog.cancel();
                    }
                });
        android.support.v7.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
