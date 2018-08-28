package com.restaurant.ad.application.view.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.restaurant.ad.application.R;
import com.restaurant.ad.application.widget.tree.Node;
import com.restaurant.ad.application.widget.tree.TreeRecyclerAdapter;

import java.util.List;

public class SimpleTreeRecyclerAdapter<T, E> extends TreeRecyclerAdapter {

    public SimpleTreeRecyclerAdapter(RecyclerView mTree, Context context, List<Node<T, E>> datas, int defaultExpandLevel, int iconExpand, int iconNoExpand) {
        super(mTree, context, datas, defaultExpandLevel, iconExpand, iconNoExpand);
    }

    public SimpleTreeRecyclerAdapter(RecyclerView mTree, Context context, List<Node> datas, int defaultExpandLevel) {
        super(mTree, context, datas, defaultExpandLevel);
    }

    @Override
    public void onBindViewHolder(Node node, RecyclerView.ViewHolder holder, int position) {
        final MyHolder viewHolder = (MyHolder) holder;
        viewHolder.label.setText(node.getName());
        if (node.getIcon() == -1) {
            viewHolder.icon.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.icon.setVisibility(View.VISIBLE);
            viewHolder.icon.setImageResource(node.getIcon());
        }
        if (node.getLevel() == 0) {
            viewHolder.itemBg.setBackgroundColor(Color.parseColor("#ffffff"));
        } else if (node.getLevel() == 1) {
            viewHolder.itemBg.setBackgroundColor(Color.parseColor("#f8f8f8"));
        } else {
            viewHolder.itemBg.setBackgroundColor(Color.parseColor("#f1f1f1"));
        }
        if (node.isChecked()) {

        } else {

        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyHolder(View.inflate(mContext, R.layout.item_city_list, null));
    }

    class MyHolder extends RecyclerView.ViewHolder {

        TextView label;
        LinearLayout itemBg;
        ImageView icon;

        MyHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.deviceName);
            itemBg = itemView.findViewById(R.id.ll_choose_device);
            icon = itemView.findViewById(R.id.iv_icon);
        }
    }

}
