package com.damon.caja.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.damon.caja.R;
import com.damon.caja.holder.CajaViewHolder;
import com.damon.caja.models.CajaM;
import com.damon.caja.ui.CreateActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CajaAdapter extends RecyclerView.Adapter<CajaViewHolder> {

    private Activity activity;
    private List<CajaM> cajaMList;
    private List<CajaM> searchCajaList;
    private FirebaseFirestore firebaseFirestore;
    private Timer timer;

    public CajaAdapter(Activity activity, List<CajaM> cajaMList,FirebaseFirestore firebaseFirestore) {
        this.activity = activity;
        this.cajaMList = cajaMList;
        this.searchCajaList = cajaMList;
        this.firebaseFirestore = firebaseFirestore;
    }

    @NonNull
    @Override
    public CajaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_caja,parent,false);
        return new CajaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CajaViewHolder holder, int position) {
        CajaM cajaM = cajaMList.get(position);

        holder.fecha_Texto.setText(cajaM.getFecha());

        holder.valor_caja.setText("" + cajaM.getValorCaja());
        holder.valor_casa.setText("" + cajaM.getValorCasa());
        double suma_casa_caja = suma_casa_caja(cajaM.getValorCasa(), cajaM.getValorCaja());
        holder.suma_casa_caja.setText("" + suma_casa_caja);
        holder.valor_internet.setText("-" + cajaM.getInternetAnotado());

        double resta_internet_suma_caja_casa = resta_internet_menos_suma_casa_caja(suma_casa_caja, cajaM.getInternetAnotado());
        holder.total_suma_caja_casa.setText(formatDouble(resta_internet_suma_caja_casa));

        holder.valor_base_mio.setText("" + cajaM.getBaseRecargaMio());
        holder.valor_base_evelyn_vendido.setText("" + cajaM.getBaseRecargaEvelynVendido());
        holder.valor_base_evelyn_saldo.setText("" + cajaM.getBaseRecargaEvelynBase());
        holder.valor_base_movilway.setText("" + cajaM.getBaseRecargaMovilway());

        double suma_bases_regarcas = suma_bases_recargas(cajaM.getBaseRecargaMio(), cajaM.getBaseRecargaEvelynVendido(), cajaM.getBaseRecargaEvelynBase(), cajaM.getBaseRecargaMovilway());
        holder.suma_valores_bases.setText(formatDouble(suma_bases_regarcas));
        holder.suma_valores_bases_mas_total_anterior.setText("+" + formatDouble(resta_internet_suma_caja_casa));

        double sumaTotal = suma_valores_bases_mas_suma_total_casa_caja(resta_internet_suma_caja_casa, suma_bases_regarcas);

        holder.gran_total.setText("" + formatDouble(sumaTotal));
        holder.suma_valor_total.setText("" + formatDouble(sumaTotal));

        holder.valor_allegar.setText("" + cajaM.getValorTotalAllegar());

        double saldoFaltanteOSobrante = valorFaltanteoSobrante(sumaTotal, cajaM.getValorTotalAllegar());

        if (sumaTotal == cajaM.getValorTotalAllegar()) {
            holder.suma_valor_total.setTextColor(activity.getResources().getColor(R.color.success));
        } else {
            holder.saldo_faltante_osobrante.setVisibility(View.GONE);
        }

        if (sumaTotal < cajaM.getValorTotalAllegar()) {
            holder.suma_valor_total.setTextColor(activity.getResources().getColor(R.color.warning));
            holder.saldo_faltante_osobrante.setVisibility(View.VISIBLE);
            holder.saldo_faltante_osobrante.setText(formatDouble(saldoFaltanteOSobrante));
            holder.saldo_faltante_osobrante.setTextColor(activity.getResources().getColor(R.color.warning));

        } else if (sumaTotal > cajaM.getValorTotalAllegar()){
            holder.suma_valor_total.setTextColor(activity.getResources().getColor(R.color.alert));
            holder.saldo_faltante_osobrante.setVisibility(View.VISIBLE);
            holder.saldo_faltante_osobrante.setText("+"+formatDouble(saldoFaltanteOSobrante));
            holder.saldo_faltante_osobrante.setTextColor(activity.getResources().getColor(R.color.alert));
        }else {
            holder.saldo_faltante_osobrante.setVisibility(View.GONE);
        }

        holder.fecha_Texto.setOnClickListener(v -> {
            cajaM.setCollapse(!cajaM.isCollapse());
            notifyItemChanged(position);
        });

        boolean isExpandle = cajaM.isCollapse();
        holder.layout_details.setVisibility(isExpandle ? View.VISIBLE : View.GONE);


        holder.btn_options.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(activity,v);
            popupMenu.inflate(R.menu.menu_caja_options);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.delete_caja){
                        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
                        alert.setTitle("Eliminar Caja");
                        alert.setMessage("Estas seguro de eLiminar si lo ases jamas lo recuperaras");
                        alert.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCaja(cajaM.getId(),position);
                                dialog.dismiss();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
                    }else if (item.getItemId() == R.id.update_caja){
                        Intent intent = new Intent(activity, CreateActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("caja",cajaM);
                        intent.putExtras(bundle);
                        activity.startActivity(intent);
                    }
                    return false;
                }
            });
            popupMenu.show();
        });

    }

    private double valorFaltanteoSobrante(double sumaTotal, double valorTotalAllegar) {
        return  sumaTotal-valorTotalAllegar;
    }

    private void deleteCaja(String id,int position) {
        CollectionReference reference = firebaseFirestore.collection("Caja");
        reference.document(id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                cajaMList.remove(position);
                notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "ERROR AL ELIMINAR \n"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double suma_valores_bases_mas_suma_total_casa_caja(double resta_internet_suma_caja_casa, double suma_bases_regarcas) {
        return resta_internet_suma_caja_casa+suma_bases_regarcas;
    }

    private double suma_bases_recargas(double baseRecargaMio, double baseRecargaEvelynVendido, double baseRecargaEvelynBase, double baseRecargaMovilway) {
        return  baseRecargaMio+baseRecargaEvelynVendido+baseRecargaEvelynBase+baseRecargaMovilway;
    }

    private double resta_internet_menos_suma_casa_caja(double suma_casa_caja, double internetAnotado) {
        return suma_casa_caja-internetAnotado;
    }

    private double suma_casa_caja(double valorCasa, double valorCaja) {
        return valorCasa+valorCaja;
    }

    private String  formatDouble(double numero){
        double res = numero;
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);
        return format.format(res);
    }

    public void searchCajaDate(String date){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (date.trim().isEmpty()){
                    cajaMList = searchCajaList;
                }else {
                    ArrayList<CajaM> temp = new ArrayList<>();
                    for (CajaM cajaM : searchCajaList){
                        if (cajaM.getFecha().trim().toLowerCase().contains(date.trim().toLowerCase())){
                            temp.add(cajaM);
                        }
                    }
                    cajaMList = temp;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                            notifyDataSetChanged();
                    }
                });
            }
        },500);
    }

    public void cancelTimer(){
        if (timer!= null){
            timer.cancel();
        }
    }


    @Override
    public int getItemCount() {
        if (cajaMList.size() >0){
            return  cajaMList.size();
        }else {
            return 0;
        }
    }


}
