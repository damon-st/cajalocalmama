package com.damon.caja.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.damon.caja.holder.DeudasViewHolder;
import com.damon.caja.models.DeudaM;
import com.damon.caja.models.ValoresDeudas;
import com.damon.caja.ui.EditDeuda;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class DeudaAdapter extends RecyclerView.Adapter<DeudasViewHolder> {

    private List<DeudaM> deudaMList,deudaSoruceList;
    private Activity activity;
    private FirebaseFirestore db;
    private Timer timer;

    public DeudaAdapter(List<DeudaM> deudaMList, Activity activity,FirebaseFirestore db) {
        this.deudaMList = deudaMList;
        this.activity = activity;
        this.db = db;
        this.deudaSoruceList = deudaMList;
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

        holder.fecha.setText("Fecha del registro: \n"+deudaM.getDate());
        holder.nombre.setText("Nombre deudor: \n"+deudaM.getName());

        holder.valor.setText("Valor = $" + formatDouble(getValor(deudaM)));

        if (deudaM.isPay()){
            holder.colorDeuda.setBackgroundColor(activity.getResources().getColor(R.color.success));
        }else {

            holder.colorDeuda.setBackgroundColor(activity.getResources().getColor(R.color.warning));
        }

        holder.btn_options.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(activity,v);
            menu.inflate(R.menu.deudas_menu_options);

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (item.getItemId() == R.id.deuda_edit){

                        Bundle bundle = new Bundle();
                        Intent intent = new Intent(activity, EditDeuda.class);
                        bundle.putSerializable("deuda",deudaM);
                        intent.putExtras(bundle);
                        activity.startActivity(intent);

                    }else if (item.getItemId() == R.id.deuda_delete){
                        AlertDialog.Builder eliminar = new AlertDialog.Builder(activity);
                        eliminar.setTitle("Eliminar deuda");
                        eliminar.setMessage("Si eliminas no se podra recuperar");
                        eliminar.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteDeuda(deudaM,position);
                                dialog.dismiss();
                            }
                        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        eliminar.create().show();

                    }

                    return false;
                }
            });

            menu.show();
        });
    }

    private void deleteDeuda(DeudaM deudaM, int position) {
        DocumentReference documentReference = db.collection("Deudas").document(deudaM.getId());
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull  Task<Void> task) {
               if (task.isSuccessful()){
                   for (ValoresDeudas valoresDeudas : deudaM.getValor()){
                       DocumentReference reference = db.collection("Valores")
                               .document(valoresDeudas.getId());
                       reference.delete();
                   }
                   deudaMList.remove(position);
                   notifyDataSetChanged();

               }else {
                   Toast.makeText(activity, "Error al eliminar la deuda algo salio mal " , Toast.LENGTH_SHORT).show();

               }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Toast.makeText(activity, "Error al eliminar la deuda " + e.getMessage() , Toast.LENGTH_SHORT).show();
            }
        });
    }

    public double getValor( DeudaM deudaM){
        double valor =0;
        if (deudaM.getValor() != null){
            for (ValoresDeudas valores: deudaM.getValor()){
                if (!valores.isPay()){
                    valor += valores.getValor();
                }
            }
        }
        return valor;
    }

    public double getTotalDeuda(){
        double valor = 0;
        if (deudaMList.size()>0){
            for (int  i =0; i < deudaMList.size() ; i++){
                for (ValoresDeudas deuda: deudaMList.get(i).getValor()){
                    if (!deuda.isPay()){
                        valor+= deuda.getValor();
                    }
                }
            }
        }
        return valor;

    }

    private String  formatDouble(double numero){
        double res = numero;
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);
        return format.format(res);
    }


    public void buscarDeduda(String nombre){
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (nombre.trim().isEmpty()){
                    deudaMList = deudaSoruceList;
                }else {
                    ArrayList<DeudaM> deudaMArrayList = new ArrayList<>();
                    for (DeudaM deudaM : deudaSoruceList){
                        if (deudaM.getName().trim().toLowerCase().contains(nombre.trim().toLowerCase())){
                            deudaMArrayList.add(deudaM);
                        }
                    }

                    deudaMList = deudaMArrayList;
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
        if (timer !=null){
            timer.cancel();
        }
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
