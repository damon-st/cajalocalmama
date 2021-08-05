package com.damon.caja.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.caja.R;
import com.damon.caja.holder.HeaderViewHolder;
import com.damon.caja.holder.ValorDeudaViewHolder;
import com.damon.caja.models.ValoresDeudas;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rey.material.widget.ProgressView;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class ValorDeudaAdapter extends RecyclerView.Adapter<ValorDeudaViewHolder>  implements StickyRecyclerHeadersAdapter<HeaderViewHolder> {

    List<ValoresDeudas> valoresDeudasList;
    Activity activity;
    FirebaseFirestore db;
    Date date;


    public ValorDeudaAdapter(List<ValoresDeudas> valoresDeudasList, Activity activity, FirebaseFirestore db) {
        this.valoresDeudasList = valoresDeudasList;
        this.activity = activity;
        this.db = db;
    }

    @NonNull
    @Override
    public ValorDeudaViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(activity).inflate(R.layout.valores_deuda_item,parent,false);
        date = new Date();
        return new ValorDeudaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ValorDeudaViewHolder holder, int position) {
        ValoresDeudas valoresDeudas = valoresDeudasList.get(position);

        holder.fechaRegistro.setText("Fecha Registro: \n"+formatFecha(valoresDeudas.getRegisterDate()));
        holder.valorDeuda.setText("Valor =  $"+formatDouble(valoresDeudas.getValor()));

        if (valoresDeudas.getPayDate() != null){
            holder.fechaPagado.setVisibility(View.VISIBLE);
            holder.fechaPagado.setText("Fecha Pagado: \n"+formatFecha(valoresDeudas.getPayDate()));
        }else {
            holder.fechaPagado.setVisibility(View.GONE);
        }

        if (valoresDeudas.isPay()){
            holder.checkPay.setChecked(true);
            holder.checkPay.setEnabled(false);
            holder.colorDeuda.setBackgroundColor(activity.getResources().getColor(R.color.success));
        }else {
            holder.checkPay.setChecked(false);
            holder.checkPay.setEnabled(true);
            holder.colorDeuda.setBackgroundColor(activity.getResources().getColor(R.color.warning));
        }
        
        holder.checkPay.setOnClickListener(v -> {
            confirmarPago(position,valoresDeudas,holder.progressView);
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                dialog.setTitle("Eliminar este valor");
                dialog.setMessage("¿Estas seguro? .Si eliminas nose podra recuperar");
                dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteValor(valoresDeudas,position,holder.progressView);
                        dialog.dismiss();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.create().show();
                return false;
            }
        });

    }

    private void deleteValor(ValoresDeudas valoresDeudas, int position,ProgressView progressView) {
        progressView.setVisibility(View.VISIBLE);
        DocumentReference reference = db.collection("Valores").document(valoresDeudas.getId());
        reference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull  Task<Void> task) {
                if (task.isSuccessful()){
                    progressView.setVisibility(View.GONE);
                    valoresDeudasList.remove(position);
                    notifyItemRemoved(position);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Toast.makeText(activity, "Error al eliminar el valor " + e.getMessage() , Toast.LENGTH_SHORT).show();
                progressView.setVisibility(View.GONE);
            }
        });
    }
     boolean isTotalPay = true;
    private void confirmarPago(int position, ValoresDeudas valoresDeudas, ProgressView progressView) {
        ;
        progressView.setVisibility(View.VISIBLE);
        DocumentReference reference = db.collection("Valores").document(valoresDeudas.getId());
        HashMap<String,Object> valoresMap = new HashMap<>();
        valoresMap.put("pay",true);
        valoresMap.put("payDate",date);
        reference.update(valoresMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull  Task<Void> task) {
                if (task.isSuccessful()){
                    DocumentReference refDeudor = db.collection("Deudas").document(valoresDeudas.getIdDeudor());
                    HashMap<String,Object> deudorMap = new HashMap<>();
                    deudorMap.put("pay",true);
                    valoresDeudasList.get(position).setPay(true);
                    for (int i =0; i < valoresDeudasList.size(); i++){
                        Log.d("pagado", " " + valoresDeudasList.get(i).isPay());
                        if (!valoresDeudasList.get(i).isPay()){
                            isTotalPay = false;
                        }
                    }

                    if (isTotalPay){
                        refDeudor.update(deudorMap).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull  Exception e) {
                                progressView.setVisibility(View.GONE);
                                Toast.makeText(activity, "Error al actualizar al deudor " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }



                    progressView.setVisibility(View.GONE);
                    valoresDeudas.setPay(true);
                    valoresDeudas.setPayDate(date);
                    try {
                        valoresDeudasList.set(position,valoresDeudas);
                        notifyItemChanged(position,valoresDeudas);
                    }catch (Exception e){
                        Toast.makeText(activity, "Error al actulizar el listado aqui "+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                progressView.setVisibility(View.GONE);
                Toast.makeText(activity, "Error al pagar "+ e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String  formatDouble(double numero){
        double res = numero;
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);
        return format.format(res);
    }

    private String formatFecha(Date date){
        return  new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(date);
    }

    @Override
    public long getHeaderId(int position) {
        return valoresDeudasList.get(position).getRegisterDate().getMonth();
    }

    @Override
    public HeaderViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_header,parent,false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(HeaderViewHolder holder, int i) {
        holder.item_header.setText(getMonth(valoresDeudasList.get(i).getRegisterDate().getMonth()));
    }

    private String getMonth(int position){
        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto","Septiembre","Octubre",
                "Noviembre","Diciembre"};

        return meses[position];
    }

    @Override
    public int getItemCount() {
        if (valoresDeudasList.size()>0){
            return valoresDeudasList.size();
        }else {
            return 0;
        }
    }
}
