package com.damon.caja.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.caja.R;
import com.damon.caja.holder.DeudasViewHolder;
import com.damon.caja.models.DeudaM;
import com.damon.caja.models.ValoresDeudas;

import java.util.List;

public class DeudaAdapter extends RecyclerView.Adapter<DeudasViewHolder> {

    private List<DeudaM> deudaMList;
    private Activity activity;


    public DeudaAdapter(List<DeudaM> deudaMList, Activity activity) {
        this.deudaMList = deudaMList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public DeudasViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.deuda_item,parent,false);
        return new DeudasViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull  DeudasViewHolder holder, int position) {
        DeudaM deudaM = deudaMList.get(position);

        holder.fecha.setText(deudaM.getDate());
        holder.nombre.setText(deudaM.getName());
        holder.valor.setText("Valor = " + getValor(deudaM));
    }

    public double getValor( DeudaM deudaM){
        double valor =0;
        if (deudaM.getValor() != null){
            for (ValoresDeudas valores: deudaM.getValor()){
                valor += valores.getValor();
            }
        }
        return valor;
    }


    @Override
    public int getItemCount() {
        if (deudaMList.size()>0){
            return  deudaMList.size();
        }else {
            return 0;
        }
    }
}
