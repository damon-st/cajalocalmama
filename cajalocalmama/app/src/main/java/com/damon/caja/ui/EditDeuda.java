package com.damon.caja.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.damon.caja.R;
import com.damon.caja.adapters.ValorDeudaAdapter;
import com.damon.caja.models.DeudaM;
import com.damon.caja.models.ValoresDeudas;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rey.material.widget.ProgressView;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class EditDeuda extends AppCompatActivity {


    private DeudaM deudaM;
    private MaterialTextView txt_fecha_create,nombre_deuda_create;
    private RecyclerView rcv_valores_deudas;
    private LinearLayoutManager layoutManager;
    private ValorDeudaAdapter valorDeudaAdapter;
    private List<ValoresDeudas> valoresDeudasList = new ArrayList<>();
    private FirebaseFirestore db;

    private Dialog dialogCreateDeudas,dialogEditName;
    private TextInputEditText txt_nombre_deudor, txt_valor_deudor,txt_nombre_edit;
    private TextInputLayout layout_txt_deuda_nombre;
    private MaterialButton btn_create_deuda,btn_deuda_cancelar,btn_confirm_name,btn_cancel_name;
    private MaterialTextView tv_deuda_fecha_create;
    private Date date;
    private String fechaCreate;
    private ProgressView progress_view_create_deuda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_deuda);

        dialogCreateDeudas = new Dialog(this);
        dialogEditName = new Dialog(this);
        date = new Date();

        fechaCreate = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(date);


        db = FirebaseFirestore.getInstance();

        txt_fecha_create = findViewById(R.id.fecha_deuda_create);
        nombre_deuda_create = findViewById(R.id.nombre_deuda_create);
        rcv_valores_deudas = findViewById(R.id.rcv_valores_deudas);

        layoutManager = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);

        rcv_valores_deudas.setLayoutManager(layoutManager);
        rcv_valores_deudas.setHasFixedSize(true);

        valorDeudaAdapter = new ValorDeudaAdapter(valoresDeudasList,this,db);

        rcv_valores_deudas.setAdapter(valorDeudaAdapter);
        rcv_valores_deudas.addItemDecoration(new StickyRecyclerHeadersDecoration(valorDeudaAdapter));

        deudaM = new DeudaM();
        Intent intent = getIntent();

        initialiceDialog();

        if (intent.getExtras() != null){
            Bundle bundle = intent.getExtras();
            deudaM =(DeudaM) bundle.getSerializable("deuda");
            txt_fecha_create.setText("Fecha de Registro: \n"+deudaM.getDate());
            nombre_deuda_create.setText("Nombre del Deudor: \n "+deudaM.getName());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                deudaM.getValor().sort((ca1,ca2)->ca1.getRegisterDate().compareTo(ca2.getRegisterDate()));
            }

            valoresDeudasList.addAll(deudaM.getValor());
            valorDeudaAdapter.notifyDataSetChanged();
        }

        nombre_deuda_create.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                updateName();
                return false;
            }
        });

    }



    void initialiceDialog(){
        dialogCreateDeudas.setContentView(R.layout.dialog_create_deuda);
        txt_nombre_deudor = dialogCreateDeudas.findViewById(R.id.txt_deuda_nombre);
        layout_txt_deuda_nombre = dialogCreateDeudas.findViewById(R.id.layout_txt_deuda_nombre);
        layout_txt_deuda_nombre.setVisibility(View.GONE);
        txt_valor_deudor = dialogCreateDeudas.findViewById(R.id.txt_deuda_valor);
        btn_create_deuda = dialogCreateDeudas.findViewById(R.id.btn_deuda_crear);
        btn_deuda_cancelar = dialogCreateDeudas.findViewById(R.id.btn_deuda_cancelar);
        tv_deuda_fecha_create = dialogCreateDeudas.findViewById(R.id.tv_deuda_fecha_create);
        tv_deuda_fecha_create.setText(fechaCreate);
        progress_view_create_deuda = dialogCreateDeudas.findViewById(R.id.progress_linear_create_deuda);



        dialogCreateDeudas.setCanceledOnTouchOutside(false);


        dialogEditName.setContentView(R.layout.edit_name_deudas);
        txt_nombre_edit = dialogEditName.findViewById(R.id.edit_name_deudor);
        btn_confirm_name = dialogEditName.findViewById(R.id.btn_confir_name);
        btn_cancel_name = dialogEditName.findViewById(R.id.btn_cancel);
        dialogEditName.setCanceledOnTouchOutside(false);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            dialogCreateDeudas.create();
            dialogEditName.create();
        }
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
        if (id == R.id.deuda_refresh){
            recreate();
        }else if (id == R.id.deudas_crear){
            createDeuda();
        }

        return super.onOptionsItemSelected(item);
    }

    void  createDeuda(){
        dialogCreateDeudas.show();
        btn_deuda_cancelar.setOnClickListener(v -> { dialogCreateDeudas.dismiss();});
        btn_create_deuda.setOnClickListener(v -> {
            if (TextUtils.isEmpty(txt_valor_deudor.getText().toString())){
                Toast.makeText(this, "Por favor ingresa el valor ", Toast.LENGTH_SHORT).show();
            }else {
                progress_view_create_deuda.setVisibility(View.VISIBLE);
                btn_create_deuda.setEnabled(false);
                double valor = Double.parseDouble(txt_valor_deudor.getText().toString());
                String id = UUID.randomUUID().toString();
                ValoresDeudas deudas = new ValoresDeudas();
                deudas.setId(id);
                deudas.setPay(false);
                deudas.setValor(valor);
                deudas.setRegisterDate(date);
                deudas.setIdDeudor(deudaM.getId());
                DocumentReference valorRef = db.collection("Valores").document(id);
                valorRef.set(deudas).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull  Task<Void> task) {
                        if (task.isSuccessful()){
                            HashMap<String,Object> deudaorMap = new HashMap<>();
                            deudaorMap.put("pay",false);
                            DocumentReference deudorRef = db.collection("Deudas").document(deudaM.getId());
                            deudorRef.update(deudaorMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull  Task<Void> task) {
                                    if (task.isSuccessful()){
                                        btn_create_deuda.setEnabled(true);
                                        progress_view_create_deuda.setVisibility(View.GONE);
                                        valoresDeudasList.add(deudas);
                                        valorDeudaAdapter.notifyDataSetChanged();
                                        dialogCreateDeudas.dismiss();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull  Exception e) {
                                    progress_view_create_deuda.setVisibility(View.GONE);
                                    Toast.makeText(EditDeuda.this, "Error al actualizar al deudor " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progress_view_create_deuda.setVisibility(View.GONE);
                        Toast.makeText(EditDeuda.this, "Error al crear el valor " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


    }

    void  updateName(){
        dialogEditName.show();
        DocumentReference deudorRef = db.collection("Deudas").document(deudaM.getId());
        txt_nombre_edit.setText(deudaM.getName());
        btn_cancel_name.setOnClickListener(v -> {dialogEditName.dismiss();});
        btn_confirm_name.setOnClickListener(v -> {
           if (TextUtils.isEmpty(txt_nombre_edit.getText().toString())){
               Toast.makeText(this, "Escribe el nombre", Toast.LENGTH_SHORT).show();
               return;
           }else {
               btn_confirm_name.setEnabled(false);
               btn_cancel_name.setEnabled(false);
               HashMap<String,Object> deudorMap = new HashMap<>();
               deudorMap.put("name",txt_nombre_edit.getText().toString());

               deudorRef.update(deudorMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull  Task<Void> task) {
                       if (task.isSuccessful()){
                           btn_confirm_name.setEnabled(true);
                           btn_cancel_name.setEnabled(true);
                           nombre_deuda_create.setText(txt_nombre_edit.getText().toString());
                           txt_nombre_edit.setText("");
                           dialogEditName.dismiss();
                       }
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull  Exception e) {
                       btn_confirm_name.setEnabled(true);
                       btn_cancel_name.setEnabled(true);
                       Toast.makeText(EditDeuda.this, "Error al actualizar nombre " + e.getMessage(), Toast.LENGTH_SHORT).show();
                   }
               });
           }
        });
    }
}