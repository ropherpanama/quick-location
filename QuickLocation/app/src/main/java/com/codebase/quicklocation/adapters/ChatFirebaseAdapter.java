package com.codebase.quicklocation.adapters;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codebase.quicklocation.R;
import com.codebase.quicklocation.model.ChatModel;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import me.himanshusoni.chatmessageview.ChatMessageView;

/**
 * Created by fgcanga on 6/26/17.
 */

public class ChatFirebaseAdapter extends FirebaseRecyclerAdapter<ChatModel, ChatFirebaseAdapter.MyChatViewHolder> {

    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;

    private ClickListenerChatFirebase mClickListenerChatFirebase;

    private String nameUser;


    public ChatFirebaseAdapter(DatabaseReference ref, String nameUser, ClickListenerChatFirebase mClickListenerChatFirebase) {
        super(ChatModel.class, R.layout.item_message_left, ChatFirebaseAdapter.MyChatViewHolder.class, ref);
        this.nameUser = nameUser;
        this.mClickListenerChatFirebase = mClickListenerChatFirebase;
    }

    @Override
    public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
            return new MyChatViewHolder(view);
        } else if (viewType == LEFT_MSG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
            return new MyChatViewHolder(view);
        } else if (viewType == RIGHT_MSG_IMG) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_img, parent, false);
            return new MyChatViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_img, parent, false);
            return new MyChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model = getItem(position);
        if (model.getMapModel() != null) {
            if (model.getUserModel().getName().equals(nameUser)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (model.getFavorites() != null) {
            // if (model.getFavorites().getType().equals("img") && model.getUserModel().getName().equals(nameUser)) {
            if (model.getUserModel().getName().equals(nameUser)) {
                return RIGHT_MSG_IMG;
            } else {
                return LEFT_MSG_IMG;
            }
        } else if (model.getUserModel().getName().equals(nameUser)) {
            return RIGHT_MSG;
        } else {
            return LEFT_MSG;
        }
    }

    @Override
    protected void populateViewHolder(MyChatViewHolder viewHolder, ChatModel model, int position) {
        viewHolder.setIvUser(model.getUserModel().getPhoto_profile());
        viewHolder.setTxtMessage(model.getMessage());
        viewHolder.setTvTimestamp(model.getTimeStamp());
        //viewHolder.tvIsLocation(View.GONE);
        //if (position == LEFT_MSG)
        //{
        if (model.getUserModel() != null)
            viewHolder.settxtsendusername(model.getUserModel().getName());
        if (model.getFavorites() != null) {
            viewHolder.setDetallFavorito(model.getFavorites().getCategory());
            viewHolder.settNombreFavorito(model.getFavorites().getLocalName());
        }

        //}
       /* if (model.getFavorites() != null) {
            viewHolder.tvIsLocation(View.GONE);
            viewHolder.setIvChatPhoto(model.getFavorites().getFavorito_categoria());
        } else if (model.getMapModel() != null) {
            viewHolder.setIvChatPhoto(alessandro.firebaseandroid.util.Util.local(model.getMapModel().getLatitude(), model.getMapModel().getLongitude()));
            viewHolder.tvIsLocation(View.VISIBLE);
        }*/
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTimestamp, txtNombreFavorito;
        TextView txtMessage, txtsendusername, txtDestallesFavorito;
        ImageView ivUser, ivChatPhoto;
        ChatMessageView messageView;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            tvTimestamp = (TextView) itemView.findViewById(R.id.timestamp);
            txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);
            txtsendusername = (TextView) itemView.findViewById(R.id.txtsendusername);
            //tvLocation = (TextView) itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView) itemView.findViewById(R.id.img_chat);
            ivUser = (ImageView) itemView.findViewById(R.id.ivUserChat);
            txtDestallesFavorito = (TextView) itemView.findViewById(R.id.txtDestallesFavorito);
            txtNombreFavorito = (TextView) itemView.findViewById(R.id.txtNombreFavorito);
            messageView = (ChatMessageView) itemView.findViewById(R.id.contentMessageChat);
            messageView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            ChatModel model = getItem(position);
            Log.d("detall", model.toString());

            if (model.getFavorites() != null) {
                mClickListenerChatFirebase.clickFavoritos(view, model.getFavorites());
            }
            /*if (model.getMapModel() != null) {
                mClickListenerChatFirebase.clickImageMapChat(view, position, model.getMapModel().getLatitude(), model.getMapModel().getLongitude());
            } else {
                mClickListenerChatFirebase.clickImageChat(view, position, model.getUserModel().getName(), model.getUserModel().getPhoto_profile(), model.getFavorites().getCategory());
            }*/
        }

        public void setTxtMessage(String message) {
            if (txtMessage == null) return;
            txtMessage.setText(message);
        }

        public void setIvUser(String urlPhotoUser) {
            if (ivUser == null) return;
            //  Glide.with(ivUser.getContext()).load(urlPhotoUser).centerCrop().transform(new CircleTransform(ivUser.getContext())).override(40,40).into(ivUser);
        }

        public void setTvTimestamp(String timestamp) {
            if (tvTimestamp == null) return;
            tvTimestamp.setText(converteTimestamp(timestamp));
        }

        public void setIvChatPhoto(String url) {
            if (ivChatPhoto == null) return;
            Glide.with(ivChatPhoto.getContext()).load(url)
                    .override(100, 100)
                    .fitCenter()
                    .into(ivChatPhoto);
            ivChatPhoto.setOnClickListener(this);
        }

        /*public void tvIsLocation(int visible) {
            if (tvLocation == null) return;
            tvLocation.setVisibility(visible);
        }*/
        public void settxtsendusername(String fullname) {
            if (txtsendusername == null) return;
            txtsendusername.setText(fullname);
        }

        public void settNombreFavorito(String nombre) {
            if (txtNombreFavorito == null) return;

            txtNombreFavorito.setText(nombre);

        }

        public void setDetallFavorito(String detalle) {
            if (txtDestallesFavorito == null) return;

            txtDestallesFavorito.setText(detalle);
        }


    }

    private CharSequence converteTimestamp(String mileSegundos) {
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }
}

