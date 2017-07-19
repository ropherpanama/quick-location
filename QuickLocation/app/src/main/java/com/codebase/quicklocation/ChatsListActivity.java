package com.codebase.quicklocation;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.codebase.quicklocation.model.LastLocation;
import com.codebase.quicklocation.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
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
    private Toolbar toolbar;
    private RecyclerView.Adapter adapter;
    private List<Group> groups = new ArrayList<>();
    private Context context;
    private DatabaseReference rootDataBase = FirebaseDatabase.getInstance().getReference().child(Utils.groups);
    private String user_ui = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private String key_group="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        Log.d("ChatsListActivity", user_ui);
        setContentView(R.layout.activity_chats_list);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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
                Log.d("Item... ", " " + item.getTitle());
                Intent intent = new Intent(ChatsListActivity.this, ChatFirebaseActivity.class);
                intent.putExtra("group_id", item.getGruop_id());
                intent.putExtra("create_by", item.getCreate_by());
                intent.putExtra("title",item.getTitle());
                startActivity(intent);
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.lstFirebaseChats);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatsListActivity.this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setAdapter(adapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(createHelperCallback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private ItemTouchHelper.Callback createHelperCallback() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback =
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                        deleteItem(viewHolder.getAdapterPosition());
                    }
                };
        return simpleItemTouchCallback;    }

    private void deleteItem(int adapterPosition) {
        Group group = groups.get(adapterPosition);

        TypeGroup member = new TypeGroup(user_ui, false);
        Map<String, Object> typeValue = member.toMap();

        DatabaseReference salir_grupo = rootDataBase.child(group.getGruop_id()).child("members").child(user_ui);
        salir_grupo.removeValue();
        //salir_grupo.updateChildren(typeValue);
        groups.remove(adapterPosition);
        adapter.notifyDataSetChanged();
        Snackbar.make(toolbar, "Saliste del grupo!", Snackbar.LENGTH_SHORT).show();

        //DatabaseReference group_refer = rootDataBase.child(key_group);

    }

    private void moveItem(int adapterPosition, int adapterPosition1) {

    }

    /**
     * Recorre la lista de child
     */
    private void maplist(DataSnapshot map) {
        groups.clear();
        String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (DataSnapshot dataSnapshot : map.getChildren()) {
            Group group = dataSnapshot.getValue(Group.class);
            //Log.e("......",userUID+ " --- "+dataSnapshot.child("members"));
            DataSnapshot snapshot = dataSnapshot.child("members");
            for (DataSnapshot auz : snapshot.getChildren()) {
                Log.e("DataSnapshot",""+auz.getKey());
                if (userUID.equals(auz.getKey()))
                {
                    groups.add(group);
                }
            }


            /*if (addGroup)
            {
                groups.add(group);
            }*/

        }
        /*for (DataSnapshot dataSnapshot : map.getChildren()) {
            Group group = dataSnapshot.getValue(Group.class);
            if (group.getSalir()!= null) {
                Map<String, Object> map1 = group.getSalir();
                for (Object o : map1.keySet()) {
                    String key = o.toString();
                    if (!(Boolean) map1.get(key)) {
                        Log.e("ChatsListActivity", "No se agrega al listado de grupos... " + key + "  --  " + map1.get(key));
                    } else {
                        groups.add(group);
                    }
                }
            }else
            {
                groups.add(group);

            }
        }*/
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
        switch (v.getId()) {
            case R.id.fab:
                showAlertDialog(v);
                break;
        }
    }

    /**
     * Muestra al usuario una ventana para llenar la informacion de grupos para chats.
     *
     * @param v
     */
    private void showAlertDialog(final View v) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.user_input_dialog_box, null);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(context);
        builder.setView(view);

        final EditText txtTitle = (AutoCompleteTextView) view.findViewById(R.id.txtTituloGroupo);
        final EditText txtDescripcion = (AutoCompleteTextView) view.findViewById(R.id.txtDescripcion);

        builder.setCancelable(false).setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String title = txtTitle.getText().toString();
                String description = txtDescripcion.getText().toString();
                boolean cancel = false;
                View focusView = null;

                if (TextUtils.isEmpty(title)) {
                    txtTitle.setError(getString(R.string.error_field_required));
                    focusView = txtTitle;
                    cancel = true;

                }
                if (TextUtils.isEmpty(description)) {
                    txtDescripcion.setError(getString(R.string.error_field_required));
                    focusView = txtDescripcion;
                    cancel = true;
                }

                if (cancel) {
                    focusView.requestFocus();
                } else {
                    dialog.dismiss();
                    //TypeGroup member = new TypeGroup(user_ui, true);
                    //Map<String, Object> typeValue = member.toMap();
                    key_group = rootDataBase.push().getKey();

                    DatabaseReference group_refer = rootDataBase.child(key_group);
                    Group groupNew = new Group(title, description, key_group, user_ui);
                    Map<String, Object> groupValue = groupNew.toMap();

                    /**/
                    String lastLocation = Utils.getSavedLocation(context);
                    if(!"no_location".equals(lastLocation)) {
                        LastLocation userLocation = Utils.factoryGson().fromJson(lastLocation, LastLocation.class);
                        groupValue.put("latitude",userLocation.getLatitude()+"");
                        groupValue.put("longitude",userLocation.getLongitude()+"");
                    }
                    /**/

                    group_refer.updateChildren(groupValue);
                    //group_refer.child(Utils.menbers).child(key_group).setValue(true);
                    addGruopToUser(key_group);
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

    private void addGruopToUser(String title) {
        DatabaseReference rootDataBase = FirebaseDatabase.getInstance().getReference().child(Utils.users).child(user_ui).child(Utils.groups);
        TypeGroup typeGroup = new TypeGroup(title, true);
        Map<String, Object> typeValue = typeGroup.toMap();
        rootDataBase.updateChildren(typeValue);
        sendToAllUsers();
    }

    /**
     * Método para enviar notifiaciones a todas las pesonas.
     */
    private void sendToAllUsers() {

    }
}
