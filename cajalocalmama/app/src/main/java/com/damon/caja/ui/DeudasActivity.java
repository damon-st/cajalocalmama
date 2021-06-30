package com.damon.caja.ui;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.damon.caja.R;
import com.damon.caja.adapters.DeudaAdapter;
import com.damon.caja.models.DeudaM;
import com.damon.caja.models.ValoresDeudas;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rey.material.widget.ProgressView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DeudasActivity extends AppCompatActivity {

    private RecyclerView rcv_deudas;
    private MaterialTextView txt_total;
    private LinearLayoutManager linearLayoutManager;
    private DeudaAdapter deudaAdapter;
    private List<DeudaM> deudaMList = new ArrayList<>();
    private FirebaseFirestore db;
    private ProgressView progressView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deudas);

        db = FirebaseFirestore.getInstance();

        rcv_deudas = findViewById(R.id.rcy_deudas);
        txt_total = findViewById(R.id.total_deudas);
        progressView = findViewById(R.id.progress_linear);

        linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        rcv_deudas.setLayoutManager(linearLayoutManager);
        rcv_deudas.setHasFixedSize(true);

        deudaAdapter = new DeudaAdapter(deudaMList,this);
        rcv_deudas.setAdapter(deudaAdapter);

        getDeudas();
    }

    private void getDeudas() {

        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {

                Query reference = db.collection("Deudas").orderBy("dateCreate", Query.Direction.ASCENDING);
                CollectionReference valores = db.collection("Valores");
                reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull  Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){

                            for (QueryDocumentSnapshot snapshot: task.getResult()){
                                DeudaM deudaM = snapshot.toObject(DeudaM.class);
                                valores.whereEqualTo("idDeudor",deudaM.getId()).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull  Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()){
                                                    List<ValoresDeudas> valoresList = new ArrayList<>();
                                                    for (QueryDocumentSnapshot documentSnapshot: task.getResult()){
                                                        ValoresDeudas valoresDeudas = documentSnapshot.toObject(ValoresDeudas.class);
                                                        valoresList.add(valoresDeudas);
                                                        System.out.println("Deuda " + valoresDeudas.getValor());
                                                    }
                                                    deudaM.setValor(valoresList);

                                                }
                                            }
                                        });
                                deudaMList.add(deudaM);
                            }

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    deudaAdapter.notifyDataSetChanged();
                                    progressView.setVisibility(View.GONE);
                                }
                            },1000);

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull  Exception e) {
                        Toast.makeText(DeudasActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressView.setVisibility(View.GONE);
                    }
                });

            }
        });
    }
}