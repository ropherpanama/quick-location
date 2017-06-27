package com.codebase.quicklocation.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.codebase.quicklocation.R;
import com.codebase.quicklocation.firebasedb.Group;

import java.util.List;

/**
 * Created by AUrriola on 6/23/17.
 */
@Deprecated
public class ChatsFirebaseAdapter extends RecyclerView.Adapter<ChatsFirebaseAdapter.ViewHolder>  {
    private final List<Group> groupList;
    private final ChatsFirebaseAdapter.OnItemClickListener listener;

    public ChatsFirebaseAdapter(List<Group> groups, OnItemClickListener listener) {
        this.groupList = groups;
        this.listener = listener;
    }

    @Override
    public ChatsFirebaseAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_list, parent, false);
        return new ChatsFirebaseAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ChatsFirebaseAdapter.ViewHolder holder, int position) {
        holder.bind(groupList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPerfil;
        private TextView txtTitle;
        private TextView txtDescription;
        public ViewHolder(View itemView) {
            super(itemView);
            imgPerfil = (ImageView)itemView.findViewById(R.id.imgPerfilchat);
            txtTitle = (TextView)itemView.findViewById(R.id.txtTituloGroupo);
            txtDescription = (TextView)itemView.findViewById(R.id.txtDescripcion);
        }

        public void bind(final Group group, final OnItemClickListener listener) {
            txtTitle.setText(group.getTitle());
            txtDescription.setText(group.getDescription());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Animation anim = AnimationUtils.loadAnimation(v.getContext(),
                            R.anim.anim_alpha);
                    v.startAnimation(anim);
                    listener.onItemClick(group);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Group item);


    }
}
