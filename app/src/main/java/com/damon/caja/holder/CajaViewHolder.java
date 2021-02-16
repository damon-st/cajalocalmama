package com.damon.caja.holder;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.caja.R;
import com.google.android.material.textview.MaterialTextView;

public class CajaViewHolder  extends RecyclerView.ViewHolder {

    public MaterialTextView fecha_Texto,valor_casa,valor_caja,suma_casa_caja,valor_internet,total_suma_caja_casa,
            valor_base_mio,valor_base_evelyn_vendido,valor_base_evelyn_saldo,valor_base_movilway,suma_valores_bases,
            suma_valores_bases_mas_total_anterior,gran_total,valor_allegar,suma_valor_total,saldo_faltante_osobrante;

    public ImageView btn_options;

    public CajaViewHolder(@NonNull View itemView) {
        super(itemView);

        fecha_Texto = itemView.findViewById(R.id.fecha_texto);
        valor_casa  =itemView.findViewById(R.id.valor_casa);
        valor_caja = itemView.findViewById(R.id.valor_caja);
        suma_casa_caja = itemView.findViewById(R.id.suma_casa_caja);
        valor_internet = itemView.findViewById(R.id.valor_internet);
        total_suma_caja_casa = itemView.findViewById(R.id.total_suma_caja_casa);
        valor_base_mio = itemView.findViewById(R.id.valor_base_mio);
        valor_base_evelyn_vendido = itemView.findViewById(R.id.valor_base_evelyn_vendido);
        valor_base_evelyn_saldo = itemView.findViewById(R.id.valor_base_evelyn_saldo);
        valor_base_movilway = itemView.findViewById(R.id.valor_base_movilway);
        suma_valores_bases = itemView.findViewById(R.id.suma_valores_bases);
        suma_valores_bases_mas_total_anterior = itemView.findViewById(R.id.suma_valores_bases_mas_total_anterior);
        gran_total = itemView.findViewById(R.id.gran_total);
        valor_allegar = itemView.findViewById(R.id.valor_allegar);
        suma_valor_total = itemView.findViewById(R.id.suma_valor_total);
        saldo_faltante_osobrante = itemView.findViewById(R.id.saldo_faltante_osobrante);

        btn_options = itemView.findViewById(R.id.btn_options);

    }
}
