package com.damon.caja.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.damon.caja.R;
import com.damon.caja.adapters.CajaAdapter;
import com.damon.caja.coneccion.CheckNetworkConnection;
import com.damon.caja.models.CajaM;
import com.google.android.gms.common.util.concurrent.HandlerExecutor;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.rey.material.widget.ProgressView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableFuture;

public class MainActivity extends AppCompatActivity {


    ImageView btn_move_down;

    private List<CajaM> cajaMList;
    private RecyclerView recyclerView;
    private CajaAdapter cajaAdapter;
    private LinearLayoutManager linearLayoutManager;
    FirebaseFirestore db;
    private ProgressView progressView;

    private LinearLayout linearSearch;
    private ImageView clearSearch;
    private TextView txt_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        cajaMList = new ArrayList<>();

        txt_search = findViewById(R.id.txt_search);
        clearSearch = findViewById(R.id.delete_search);
        linearSearch = findViewById(R.id.linear_search);
        btn_move_down = findViewById(R.id.btn_move_down);
        recyclerView = findViewById(R.id.rcy_main);
        progressView = findViewById(R.id.progress_linear);
        recyclerView.setHasFixedSize(true);


        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        cajaAdapter = new CajaAdapter(MainActivity.this, cajaMList, db);
        recyclerView.setAdapter(cajaAdapter);


        new CheckNetworkConnection(MainActivity.this, new CheckNetworkConnection.OnConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                LoadCajas();
            }

            @Override
            public void onConnectionFail(String errorMsg) {
                Toast.makeText(MainActivity.this, "No ay conexcion a internet \n Mostremos datos en cache", Toast.LENGTH_LONG).show();
                LoadCajasSinInternet();
                btn_move_down.setVisibility(View.GONE);
            }
        }).execute();


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int pastVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount+pastVisibleItem) >= totalItemCount){
                    btn_move_down.setVisibility(View.GONE);
                }else {
                    btn_move_down.setVisibility(View.VISIBLE);
                }
            }
        });

        btn_move_down.setOnClickListener(v -> {
            recyclerView.smoothScrollToPosition(cajaAdapter.getItemCount());
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (cajaAdapter.getItemCount() >0)
                recyclerView.smoothScrollToPosition(cajaAdapter.getItemCount());
            }
        },2100);

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearSearch.setVisibility(View.GONE);
                cancelTimer();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setVisivilityLiner();
                    }
                },2000);
            }
        });

        clearSearch.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                linearSearch.setVisibility(View.GONE);
                cajaAdapter.searchCajaDate("");
                cancelTimer();
                return false;
            }
        });

    }



    private void LoadCajasSinInternet(){
       new Thread(){
           @Override
           public void run() {
               super.run();
               progressView.setVisibility(View.VISIBLE);
               CollectionReference reference = db.collection("Caja");

               // Source can be CACHE, SERVER, or DEFAULT.
               Source cache = Source.CACHE;
               // Get the document, forcing the SDK to use the offline cache
               reference.get(cache).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                   @Override
                   public void onComplete(@NonNull Task<QuerySnapshot> task) {
                       if (task.isSuccessful()){
                           progressView.setVisibility(View.GONE);
                           cajaMList.clear();
                           for (QueryDocumentSnapshot snapshot : task.getResult()){
                               CajaM cajaM = snapshot.toObject(CajaM.class);
                               cajaM.setId(snapshot.getId());
                               cajaMList.add(cajaM);
                           }

                           if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                               cajaMList.sort((c1,ca2) -> c1.getFechaDate().compareTo(ca2.getFechaDate()));
                           }


                           cajaAdapter.notifyDataSetChanged();
                       }else {
                           progressView.setVisibility(View.GONE);
                       }
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(MainActivity.this, "Ocurrio un error al cargar los datos\n"+e.getMessage() , Toast.LENGTH_SHORT).show();
                       progressView.setVisibility(View.GONE);
                   }
               });
           }
       }.start();
    }



    private void LoadCajas(){
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                progressView.setVisibility(View.VISIBLE);
                CollectionReference reference  = db.collection("Caja");

                reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            progressView.setVisibility(View.GONE);
                            cajaMList.clear();
                            for (QueryDocumentSnapshot snapshot : task.getResult()){
                                CajaM cajaM = snapshot.toObject(CajaM.class);
                                cajaM.setId(snapshot.getId());
                                cajaMList.add(cajaM);
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                cajaMList.sort((c1,ca2) -> c1.getFechaDate().compareTo(ca2.getFechaDate()));
                            }
                            cajaAdapter.notifyDataSetChanged();
                        }else {
                            progressView.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressView.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Error Base de datos \n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadCajas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh){
            recreate();
        }else if (id == R.id.create_Caja) {
            Intent intent = new Intent(MainActivity.this,CreateActivity.class);
            startActivity(intent);
        }else if (id == R.id.buscarfecha){

            final Calendar newCalendar = Calendar.getInstance();
            DatePickerDialog StartTime = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    Calendar newDate = Calendar.getInstance();
                    newDate.set(year, monthOfYear, dayOfMonth);
//                inputSearch.setText(dateFormatter.format(newDate.getTime()));
                    String searchDate = new SimpleDateFormat("EEEE, dd MMMM yyyy ")
                            .format(newDate.getTime());

                    setVisivilityLiner();
                    txt_search.setText(searchDate);

                    cajaAdapter.searchCajaDate(searchDate);
                    cancelTimer();
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

            StartTime.show();

        }
        return super.onOptionsItemSelected(item);
    }

    private void setVisivilityLiner() {
        linearSearch.setVisibility(View.VISIBLE);
        linearSearch.setAnimation(AnimationUtils.loadAnimation(MainActivity.this,R.anim.scale_animation));
    }

    void cancelTimer(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cajaAdapter.cancelTimer();
            }
        },700);
    }
}