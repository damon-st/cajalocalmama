package com.damon.caja.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.caja.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder{

    public TextView item_header;
    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        item_header = itemView.findViewById(R.id.item_header);
    }
}
