package com.codebase.quicklocation.adapters;

import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.codebase.quicklocation.R;
import com.codebase.quicklocation.database.Favorites;
import com.codebase.quicklocation.database.dao.FavoritesDao;
import com.codebase.quicklocation.utils.Utils;

import java.util.List;

/**
 * Created by Rosendo on 07/06/2017.
 */

public class FavoritesItemAdapter extends RecyclerView.Adapter<FavoritesItemAdapter.ViewHolder> {
    private final List<Favorites> favorites;
    private final FavoritesItemAdapter.OnItemClickListener listener;

    public FavoritesItemAdapter(List<Favorites> favorites, FavoritesItemAdapter.OnItemClickListener listener) {
        this.favorites = favorites;
        this.listener = listener;
    }

    @Override
    public FavoritesItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite_item, parent, false);
        return new FavoritesItemAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(FavoritesItemAdapter.ViewHolder holder, int position) {
        holder.bind(favorites.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView favoriteName;
        private TextView favoriteCategory;
        private ImageView placeLogo;
        private TextView placeRating;
        private TextView favoriteComment;

        private ViewHolder(View itemView) {
            super(itemView);
            favoriteName = (TextView) itemView.findViewById(R.id.favorite_name);
            favoriteCategory = (TextView) itemView.findViewById(R.id.favorite_category);
            placeLogo = (ImageView) itemView.findViewById(R.id.favorite_logo);
            placeRating = (TextView) itemView.findViewById(R.id.favorite_rating);
            favoriteComment = (TextView) itemView.findViewById(R.id.favoriteComment);
        }

        public void bind(final Favorites item, final FavoritesItemAdapter.OnItemClickListener listener) {
            placeRating.setText(String.valueOf(item.getRating()));
            favoriteName.setText(item.getLocalName());
            favoriteCategory.setText(item.getCategory());
            placeLogo.setImageResource(Utils.getDrawableByName(placeLogo.getContext(), "mipmap", item.getCategory().toLowerCase()));
            favoriteComment.setText(item.getComment());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Animation anim = AnimationUtils.loadAnimation(v.getContext(),
                            R.anim.anim_alpha);
                    v.startAnimation(anim);
                    listener.onItemClick(item);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
                    builder
                            .setTitle("Eliminar favorito")
                            .setMessage("¿Desea eliminar este registro?")
                            .setCancelable(false)
                            .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    FavoritesDao dao = new FavoritesDao(itemView.getContext());
                                    if (dao.delete(item) > 0) {
                                        Snackbar.make(itemView, "Registro borrado", Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(itemView, "Registro no borrado" + item.getLocalName(), Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .show();

                    return true;
                }
            });
        }

    }

    public interface OnItemClickListener {
        void onItemClick(Favorites item);

    }
}
