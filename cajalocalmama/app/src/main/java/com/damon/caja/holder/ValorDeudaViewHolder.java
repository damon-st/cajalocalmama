package com.damon.caja.holder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.caja.R;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textview.MaterialTextView;
import com.rey.material.widget.ProgressView;

public class ValorDeudaViewHolder  extends RecyclerView.ViewHolder {

    public MaterialTextView fechaRegistro,fechaPagado,valorDeuda;
    public MaterialCheckBox checkPay;
    public ImageView colorDeuda;
    public ProgressView progressView;

    public ValorDeudaViewHolder(@NonNull View itemView) {
        super(itemView);

        fechaPagado =itemView.findViewById(R.id.valor_fecha_pagado);
        fechaRegistro = itemView.findViewById(R.id.valor_fecha_registro);
        valorDeuda = itemView.findViewById(R.id.valor_deuda);
        checkPay = itemView.findViewById(R.id.check_pay);
        colorDeuda = itemView.findViewById(R.id.colorDeudaValor);
        progressView = itemView.findViewById(R.id.progress_valor_deuda);
    }
}
