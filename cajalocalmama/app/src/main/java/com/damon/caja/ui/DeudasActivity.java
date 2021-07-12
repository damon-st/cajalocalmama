package com.damon.caja.ui;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.damon.caja.R;
import com.damon.caja.adapters.DeudaAdapter;
import com.damon.caja.models.DeudaM;
import com.damon.caja.models.ValoresDeudas;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rey.material.widget.ProgressView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

public class DeudasActivity extends AppCompatActivity {

    private RecyclerView rcv_deudas;
    private MaterialTextView txt_total;
    private LinearLayoutManager linearLayoutManager;
    private DeudaAdapter deudaAdapter;
    private List<DeudaM> deudaMList = new ArrayList<>();
    private FirebaseFirestore db;
    private ProgressView progressView;

    private Dialog dialogCreateDeudas;
    private TextInputEditText txt_nombre_deudor, txt_valor_deudor,txt_search_deuda;
    private MaterialButton btn_create_deuda,btn_deuda_cancelar;
    private MaterialTextView tv_deuda_fecha_create;
    private Date date;
    private String fechaCreate;
    private ProgressView progress_view_create_deuda;

    private ImageView filter_deuda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deudas);

        dialogCreateDeudas = new Dialog(this);

        date = new Date();

        fechaCreate = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(date);

        db = FirebaseFirestore.getInstance();

        rcv_deudas = findViewById(R.id.rcy_deudas);
        txt_total = findViewById(R.id.total_deudas);
        progressView = findViewById(R.id.progress_linear);
        txt_search_deuda = findViewById(R.id.txt_search_deuda);
        filter_deuda = findViewById(R.id.filter_deuda);

        StaggeredGridLayoutManager staggeredGridLayoutManager  =new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);

        linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
//        staggeredGridLayoutManager.setReverseLayout(true);
        rcv_deudas.setLayoutManager(staggeredGridLayoutManager);
        rcv_deudas.setHasFixedSize(true);

        deudaAdapter = new DeudaAdapter(deudaMList,this, db);
        rcv_deudas.setAdapter(deudaAdapter);

        getDeudas();


        initialiceDialog();

        txt_search_deuda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                deudaAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (deudaMList.size()>0){
                    deudaAdapter.buscarDeduda(s.toString());
                }
            }
        });

        filter_deuda.setOnClickListener(v -> {
           showFilterMenu(v);
        });

    }

    private void showFilterMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(this,v);

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.deuda_cancelado){
                    deudaAdapter.filterForCancelado();
                }else if (id ==R.id.deuda_pendiente){
                    deudaAdapter.filterForPendiente();
                }

                return false;
            }
        });


        popupMenu.inflate(R.menu.menu_filter_deuda);
        popupMenu.show();
    }

    void initialiceDialog(){
        dialogCreateDeudas.setContentView(R.layout.dialog_create_deuda);
        txt_nombre_deudor = dialogCreateDeudas.findViewById(R.id.txt_deuda_nombre);
        txt_valor_deudor = dialogCreateDeudas.findViewById(R.id.txt_deuda_valor);
        btn_create_deuda = dialogCreateDeudas.findViewById(R.id.btn_deuda_crear);
        btn_deuda_cancelar = dialogCreateDeudas.findViewById(R.id.btn_deuda_cancelar);
        tv_deuda_fecha_create = dialogCreateDeudas.findViewById(R.id.tv_deuda_fecha_create);
        tv_deuda_fecha_create.setText(fechaCreate);
        progress_view_create_deuda = dialogCreateDeudas.findViewById(R.id.progress_linear_create_deuda);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialogCreateDeudas.create();
        }

        dialogCreateDeudas.setCanceledOnTouchOutside(false);


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
                                    txt_total.setText("Total = "+formatDouble(deudaAdapter.getTotalDeuda()));
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

    private String  formatDouble(double numero){
        double res = numero;
        NumberFormat format = NumberFormat.getInstance();
        format.setMaximumFractionDigits(3);
        return format.format(res);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.deudas_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.deudas_crear){
            crear();
        }else if (id ==R.id.deuda_refresh){
            recreate();
        }


        return super.onOptionsItemSelected(item);
    }

    void  crear (){
        dialogCreateDeudas.show();
       Executors.newSingleThreadExecutor().submit(new Runnable() {
           @Override
           public void run() {
               btn_deuda_cancelar.setOnClickListener(v -> dialogCreateDeudas.dismiss());
               btn_create_deuda.setOnClickListener(v -> {
                   if (TextUtils.isEmpty(txt_nombre_deudor.getText().toString()) ||
                           TextUtils.isEmpty(txt_valor_deudor.getText().toString())){
                       Toast.makeText(DeudasActivity.this, "LLene los campos", Toast.LENGTH_SHORT).show();
                   }else {
                       progress_view_create_deuda.setVisibility(View.VISIBLE);
                       btn_create_deuda.setEnabled(false);
                       String nombre = txt_nombre_deudor.getText().toString();
                       double valor = Double.parseDouble(txt_valor_deudor.getText().toString());


                       String idDeudor = UUID.randomUUID().toString();
                       DocumentReference reference = db.collection("Deudas").document(idDeudor);
                       DeudaM deudaM = new DeudaM();
                       deudaM.setId(idDeudor);
                       deudaM.setName(nombre);
                       deudaM.setPay(false);
                       deudaM.setDate(fechaCreate);
                       deudaM.setDateCreate(date);
                       reference.set(deudaM).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if (task.isSuccessful()){
                                   String id = UUID.randomUUID().toString();
                                   ValoresDeudas valoresDeudas = new ValoresDeudas();
                                   valoresDeudas.setId(id);
                                   valoresDeudas.setIdDeudor(idDeudor);
                                   valoresDeudas.setPay(false);
                                   valoresDeudas.setRegisterDate(date);
                                   valoresDeudas.setValor(valor);
                                   DocumentReference refValor = db.collection("Valores").document(id);
                                   refValor.set(valoresDeudas).addOnCompleteListener(new OnCompleteListener<Void>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task) {
                                           if (task.isSuccessful()){
                                               List<ValoresDeudas> list_valores = new ArrayList<>();
                                               list_valores.add(valoresDeudas);
                                               deudaM.setValor(list_valores);
                                               deudaMList.add(deudaM);
                                               deudaAdapter.notifyDataSetChanged();
                                               progress_view_create_deuda.setVisibility(View.GONE);
                                               txt_total.setText("Total = "+formatDouble(deudaAdapter.getTotalDeuda()));
                                               txt_nombre_deudor.setText("");
                                               txt_valor_deudor.setText("");
                                               btn_create_deuda.setEnabled(true);
                                               dialogCreateDeudas.dismiss();

                                           }
                                       }
                                   }).addOnFailureListener(new OnFailureListener() {
                                       @Override
                                       public void onFailure(@NonNull Exception e) {
                                           progress_view_create_deuda.setVisibility(View.GONE);
                                           Toast.makeText(DeudasActivity.this, "Errror al crear los valores" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                       }
                                   });
                               }
                           }
                       }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull  Exception e) {
                               progress_view_create_deuda.setVisibility(View.GONE);
                               Toast.makeText(DeudasActivity.this, "Error al crear " + e.getMessage(), Toast.LENGTH_SHORT).show();
                           }
                       });

                   }


               });
           }
       });
    }

}