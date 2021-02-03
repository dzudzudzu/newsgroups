package com.dzvd.newsgroupapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class MainParseAdapter extends RecyclerView.Adapter<MainParseAdapter.ViewHolder> {
    /*
    Used to parse "main" page, and allows us to enter threads of specific newsgroups
     */
    private ArrayList<MainParseItem> mainParseItems;
    private Context context;

    public MainParseAdapter(ArrayList<MainParseItem> parseItems, Context context) {
        this.mainParseItems = parseItems;
        this.context = context;
    }

    @NonNull
    @Override
    public MainParseAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.parsed_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainParseItem mainParseItem = mainParseItems.get(position);
        holder.textView.setText(mainParseItem.getTitle());
    }


    @Override
    public int getItemCount() {
        return mainParseItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.txtView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            MainParseItem parseItem = mainParseItems.get(position);

            Intent intent = new Intent(context, MsgActivity.class);
            intent.putExtra("url", parseItem.getUrl());
            context.startActivity(intent);
        }
    }
}
