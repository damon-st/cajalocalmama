package com.damon.caja.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.damon.caja.R;
import com.damon.caja.models.CajaM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rey.material.widget.ProgressView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.logging.SimpleFormatter;

public class CreateActivity extends AppCompatActivity {


    private MaterialTextView fechaTv;
    private TextInputEditText txt_valor_casa,txt_valor_caja,txt_base_mio,txt_base_evelyn_vendido,txt_base_evelyn_base,
            txt_base_movilway,txt_internet_anotado,txt_valor_a_llegar;

    private MaterialButton btn_create;

    private long cajaFecha;

    private String fecha;

    private FirebaseFirestore db;

    private ProgressView progressLinear;

    private Date date;

    private boolean isUpdate;
    private String pathID="1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);


        getSupportActionBar().setTitle("Crear Caja");

        initializeComponets();
        date = new Date();
        fecha = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(date);
        fechaTv.setText(fecha);


        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            cajaFecha = date.getTime();
            String salidaFecha = dateFormat.format(cajaFecha);
            cajaFecha = Long.parseLong(salidaFecha.replace("-",""));
        }catch (Exception e){
            e.printStackTrace();
        }



        btn_create.setOnClickListener(v -> {
            btn_create.setEnabled(false);
            CreateCaja();
            progressLinear.setVisibility(View.VISIBLE);
        });


        try {
            Intent intent = getIntent();
            if (intent != null && intent.getExtras() != null){

                isUpdate =true;

                Bundle bundle = getIntent().getExtras();
                CajaM cajaM = (CajaM)bundle.getSerializable("caja");

                pathID = cajaM.getId();
                txt_valor_caja.setText(String.valueOf(cajaM.getValorCaja()));
                txt_valor_casa.setText(String.valueOf(cajaM.getValorCasa()));
                txt_base_mio.setText(String.valueOf(cajaM.getBaseRecargaMio()));
                txt_base_evelyn_base.setText(String.valueOf(cajaM.getBaseRecargaEvelynBase()));
                txt_base_evelyn_vendido.setText(String.valueOf(cajaM.getBaseRecargaEvelynVendido()));
                txt_base_movilway.setText(String.valueOf(cajaM.getBaseRecargaMovilway()));
                txt_internet_anotado.setText(String.valueOf(cajaM.getInternetAnotado()));
                txt_valor_a_llegar.setText(String.valueOf(cajaM.getValorTotalAllegar()));

                fecha = new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a",Locale.getDefault()).format(cajaM.getFechaDate());
                fechaTv.setText(fecha);

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                cajaFecha = cajaM.getFechaDate().getTime();
                String salidaFechaUpdateOrigin = dateFormat.format(cajaFecha);
                cajaFecha = Long.parseLong(salidaFechaUpdateOrigin.replace("-",""));

                btn_create.setText("ACTUALIZAR CAJA");

            }

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void CreateCaja(){
        //Creo mi objeto de mi clase
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    if (TextUtils.isEmpty(txt_valor_casa.getText().toString())||
                            TextUtils.isEmpty(txt_valor_caja.getText().toString())||
                            TextUtils.isEmpty(txt_base_mio.getText().toString())||
                            TextUtils.isEmpty(txt_base_evelyn_base.getText().toString())||
                            TextUtils.isEmpty(txt_base_evelyn_vendido.getText().toString())||
                            TextUtils.isEmpty(txt_base_movilway.getText().toString())||
                            TextUtils.isEmpty(txt_internet_anotado.getText().toString())||
                            TextUtils.isEmpty(txt_valor_a_llegar.getText().toString())){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CreateActivity.this, "LLena todos los campos porfavor", Toast.LENGTH_SHORT).show();
                                btn_create.setEnabled(true);
                                progressLinear.setVisibility(View.GONE);
                            }
                        });
                        return;
                    }else {

                        CajaM cajaM = new CajaM();
                        cajaM.setValorCaja(getValor(txt_valor_caja.getText().toString()));
                        cajaM.setValorCasa(getValor(txt_valor_casa.getText().toString()));
                        cajaM.setBaseRecargaMio(getValor(txt_base_mio.getText().toString()));
                        cajaM.setBaseRecargaEvelynBase(getValor(txt_base_evelyn_base.getText().toString()));
                        cajaM.setBaseRecargaEvelynVendido(getValor(txt_base_evelyn_vendido.getText().toString()));
                        cajaM.setBaseRecargaMovilway(getValor(txt_base_movilway.getText().toString()));
                        cajaM.setInternetAnotado(getValor(txt_internet_anotado.getText().toString()));
                        cajaM.setFechaCreado(cajaFecha);
                        cajaM.setFechaDate(date);
                        cajaM.setFecha(fecha);
                        cajaM.setValorTotalAllegar(getValor(txt_valor_a_llegar.getText().toString()));

                        if (isUpdate){
                            db.collection("Caja").document(pathID).set(cajaM).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(CreateActivity.this, "Actulizado exitosamente", Toast.LENGTH_SHORT).show();
                                        onBackPressed();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.getMessage();
                                    Toast.makeText(CreateActivity.this, "Error al Actualizar " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            db.collection("Caja").add(cajaM)
                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                progressLinear.setVisibility(View.GONE);
                                                Toast.makeText(CreateActivity.this, "Creado la caja ", Toast.LENGTH_SHORT).show();
                                                onBackPressed();
                                            }else {
                                                progressLinear.setVisibility(View.GONE);
                                                Toast.makeText(CreateActivity.this, "Error al crear", Toast.LENGTH_SHORT).show();
                                                btn_create.setEnabled(true);

                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressLinear.setVisibility(View.GONE);
                                    btn_create.setEnabled(true);
                                    Toast.makeText(CreateActivity.this, "Errror al crear \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateActivity.this, "Errores " +e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }.start();
    }

    private Double getValor(String valor){
        try {
            return Double.parseDouble(valor);
        }catch (Exception e){
            Toast.makeText(CreateActivity.this, "Error " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return 0.0;
        }
    }


    private void initializeComponets() {
        fechaTv = findViewById(R.id.tv_fecha);
        txt_valor_casa = findViewById(R.id.txt_valor_casa);
        txt_valor_caja = findViewById(R.id.txt_valor_caja);
        txt_base_mio = findViewById(R.id.txt_base_mio);
        txt_base_evelyn_vendido = findViewById(R.id.txt_base_evelyn_vendido);
        txt_base_evelyn_base = findViewById(R.id.txt_base_evelyn_base);
        txt_base_movilway = findViewById(R.id.txt_base_movilway);
        txt_internet_anotado = findViewById(R.id.txt_internet_anotado);
        txt_valor_a_llegar = findViewById(R.id.txt_valor_a_llegar);

        progressLinear = findViewById(R.id.progress_linear);
        progressLinear.setVisibility(View.GONE);
        btn_create = findViewById(R.id.btn_create_caja);

        db = FirebaseFirestore.getInstance();
    }
}