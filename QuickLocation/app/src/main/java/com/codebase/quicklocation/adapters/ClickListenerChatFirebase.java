package com.codebase.quicklocation.adapters;

import android.view.View;

import com.codebase.quicklocation.database.Favorites;

public interface ClickListenerChatFirebase {
    /**
     * Quando houver click na imagem do chat
     *
     * @param view
     * @param position
     */
    void clickImageChat(View view, int position, String nameUser, String urlPhotoUser, String urlPhotoClick);

    /**
     * Quando houver click na imagem de mapa
     *
     * @param view
     * @param position
     */
    void clickImageMapChat(View view, int position, String latitude, String longitude);

    void clickFavoritos(View view, Favorites favorites);
}
