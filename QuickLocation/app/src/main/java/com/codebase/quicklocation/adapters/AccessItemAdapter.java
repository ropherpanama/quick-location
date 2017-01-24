package com.codebase.quicklocation.adapters;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codebase.quicklocation.R;
import com.codebase.quicklocation.model.AccessItem;

import java.util.ArrayList;

/**
 * Created by Spanky on 10/01/2017.
 */

public class AccessItemAdapter extends RecyclerView.Adapter<AccessItemAdapter.ViewHolder> {
    private final ArrayList<AccessItem> elements;
    private final OnItemClickListener listener;


    public AccessItemAdapter(ArrayList<AccessItem> elements, OnItemClickListener listener) {
        this.elements = elements;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.access_items, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(elements.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textView;
        private ImageView imageView;

        private ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.item_title);
            imageView = (ImageView) itemView.findViewById(R.id.item_logo);
        }

        public void bind(final AccessItem item, final OnItemClickListener listener) {
            textView.setText(item.getItemName());
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
        void onItemClick(AccessItem item);
    }

}
