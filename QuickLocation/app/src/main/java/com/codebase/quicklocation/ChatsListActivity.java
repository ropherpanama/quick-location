package com.codebase.quicklocation;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.codebase.quicklocation.adapters.ChatsFirebaseAdapter;
import com.codebase.quicklocation.model.MainChildChat;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatsListActivity extends AppCompatActivity {
    private ListView lstChatsFirebase;
    private RecyclerView recyclerView;
    private   Toolbar toolbar;
    private RecyclerView.Adapter adapter;
    private List<MainChildChat> mainChildChats = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        DatabaseReference mDatabaseChats = FirebaseDatabase.getInstance().getReference();
        mDatabaseChats = mDatabaseChats.child("messages/chats");

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });


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
        mDatabaseChats.addValueEventListener(eventListener);
        adapter = new ChatsFirebaseAdapter(mainChildChats, new ChatsFirebaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MainChildChat item) {
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
     * @param value
     */
    private void maplist(DataSnapshot map) {
        mainChildChats.clear();
        for (DataSnapshot dataSnapshot: map.getChildren()) {
            MainChildChat mainChildChat = new MainChildChat();
            mainChildChat.setTitle(dataSnapshot.getKey());
            mainChildChats.add(mainChildChat);
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

}
