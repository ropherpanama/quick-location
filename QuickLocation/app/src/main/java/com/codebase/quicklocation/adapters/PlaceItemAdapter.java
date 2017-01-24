package com.codebase.quicklocation.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codebase.quicklocation.R;
import com.codebase.quicklocation.model.Place;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Spanky on 22/01/2017.
 */

public class PlaceItemAdapter extends RecyclerView.Adapter<PlaceItemAdapter.ViewHolder>{
    private final List<Place> places;
    private final PlaceItemAdapter.OnItemClickListener listener;


    public PlaceItemAdapter(List<Place> places, PlaceItemAdapter.OnItemClickListener listener) {
        this.places = places;
        this.listener = listener;
    }

    @Override
    public PlaceItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_item, parent, false);
        return new PlaceItemAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PlaceItemAdapter.ViewHolder holder, int position) {
        holder.bind(places.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView placeName;
        private TextView placeDirection;
        private ImageView placeLogo;

        private ViewHolder(View itemView) {
            super(itemView);
            placeName = (TextView) itemView.findViewById(R.id.place_name);
            placeDirection = (TextView) itemView.findViewById(R.id.place_direction);
            placeLogo = (ImageView) itemView.findViewById(R.id.place_logo);
        }

        public void bind(final Place item, final PlaceItemAdapter.OnItemClickListener listener) {
            placeName.setText(item.getName());
            placeDirection.setText(item.getVicinity());
            //Picasso.with(itemView.getContext()).load(item.imageUrl).into(image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Place item);
    }
}
