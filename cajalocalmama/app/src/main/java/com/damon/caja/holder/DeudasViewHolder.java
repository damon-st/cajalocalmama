package com.damon.caja.holder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.caja.R;
import com.google.android.material.textview.MaterialTextView;

public class DeudasViewHolder extends RecyclerView.ViewHolder {

    public MaterialTextView fecha,valor,nombre;
    public ImageView colorDeuda;
    public AppCompatImageButton btn_options;


    public DeudasViewHolder(@NonNull  View itemView) {
        super(itemView);

        fecha = itemView.findViewById(R.id.txt_fecha_deuda);
        valor = itemView.findViewById(R.id.deuda_valor);
        nombre = itemView.findViewById(R.id.deuda_nombre);
        colorDeuda = itemView.findViewById(R.id.colorDeuda);
        btn_options = itemView.findViewById(R.id.deudas_more_options);

    }
}
