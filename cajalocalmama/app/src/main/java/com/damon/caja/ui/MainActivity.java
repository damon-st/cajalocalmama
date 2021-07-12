package com.damon.caja.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.damon.caja.R;
import com.damon.caja.adapters.CajaAdapter;
import com.damon.caja.coneccion.CheckNetworkConnection;
import com.damon.caja.models.CajaM;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.rey.material.widget.ProgressView;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

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
    private TextView txt_search, total_numero_cajas;
    private boolean isScrolling, isLastItemReached;
    DocumentSnapshot lastVisible;

    boolean isSearch = false;
    private int totalCajasEchas;

    private SwipeRefreshLayout refresh_caja;
    private float newDx;
    private float newDy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        getSupportActionBar().setTitle("Listado de Cajas");

        cajaMList = new ArrayList<>();

        total_numero_cajas = findViewById(R.id.total_numero_cajas);
        txt_search = findViewById(R.id.txt_search);
        clearSearch = findViewById(R.id.delete_search);
        linearSearch = findViewById(R.id.linear_search);
        btn_move_down = findViewById(R.id.btn_move_down);

        refresh_caja = findViewById(R.id.refresh_caja);

        recyclerView = findViewById(R.id.rcy_main);
        progressView = findViewById(R.id.progress_linear);
        recyclerView.setHasFixedSize(true);


        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        cajaAdapter = new CajaAdapter(MainActivity.this, cajaMList, db);
        recyclerView.setAdapter(cajaAdapter);
        recyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(cajaAdapter));


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


        btn_move_down.setOnClickListener(v -> {
            recyclerView.smoothScrollToPosition(cajaAdapter.getItemCount());
        });

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (cajaAdapter.getItemCount() > 0)
//                    recyclerView.smoothScrollToPosition(cajaAdapter.getItemCount());
//            }
//        }, 2100);

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clearSearch();
            }
        });
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        linearSearch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:


                        newDx = event.getRawX() + dX;
                        newDy = event.getRawY() + dY;

                        if ((newDx <=0 || newDx>= width-view.getWidth()) ||
                                newDy <=0 || newDy>= height-view.getHeight() ){
                            return true;
                        }

                        view.animate()
                                .x(newDx)
                                .y(newDy)
                                .setDuration(0)
                                .start();

//                        view.setTranslationX(newDx);
//                        view.setTranslationY(newDy);

                        break;
                    default:
                        return false;
                }
                return true;
            }
        });


        refresh_caja.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLastItemReached){
                    progressView.setVisibility(View.VISIBLE);
                loadMoreCaja();
                }else {
                    refresh_caja.setRefreshing(false);
                }
            }
        });
    }

    private void clearSearch() {
        isSearch = false;
        linearSearch.setVisibility(View.GONE);
        cajaAdapter.searchCajaDate("");
        cancelTimer();
    }

    private float dX;
    private float dY;


    private void LoadCajasSinInternet() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                progressView.setVisibility(View.VISIBLE);
                CollectionReference reference = db.collection("Caja");

                // Source can be CACHE, SERVER, or DEFAULT.
                Source cache = Source.CACHE;
                // Get the document, forcing the SDK to use the offline cache
                reference.orderBy("fechaDate", Query.Direction.ASCENDING).get(cache).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            progressView.setVisibility(View.GONE);
                            cajaMList.clear();
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                CajaM cajaM = snapshot.toObject(CajaM.class);
                                cajaM.setId(snapshot.getId());
                                cajaMList.add(cajaM);
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                cajaMList.sort((c1, ca2) -> c1.getFechaDate().compareTo(ca2.getFechaDate()));
                            }

                            total_numero_cajas.setText("Total de Cajas Echas = " + task.getResult().size());

                            cajaAdapter.notifyDataSetChanged();


                        } else {
                            progressView.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Ocurrio un error al cargar los datos\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        progressView.setVisibility(View.GONE);
                    }
                });
            }
        }.start();
    }


    float y = 0;

    private void LoadCajas() {
        progressView.setVisibility(View.VISIBLE);
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                Query reference = db.collection("Caja")
                        .orderBy("fechaDate", Query.Direction.DESCENDING).limit(15);

                reference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            progressView.setVisibility(View.GONE);
                            cajaMList.clear();
                            for (QueryDocumentSnapshot snapshot : task.getResult()) {
                                CajaM cajaM = snapshot.toObject(CajaM.class);
                                cajaM.setId(snapshot.getId());
                                itemCaja++;

                                if (itemCaja ==1){
                                    String cajakey = snapshot.getId();
                                    mLastKey = cajakey;
                                    mPrevKey = cajakey;
                                }

                                cajaMList.add(cajaM);

                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                cajaMList.sort((c1,ca2) -> c1.getFechaDate().compareTo(ca2.getFechaDate()));
                            }
                            lastVisible = task.getResult().getDocuments().get(task.getResult().size() - 1);
                            cajaAdapter.notifyDataSetChanged();

                            totalCajasEchas = task.getResult().size();
                            total_numero_cajas.setText("Total de Cajas Echas = " + totalCajasEchas);

                            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                                    super.onScrollStateChanged(recyclerView, newState);
                                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                        isScrolling = true;
                                    }
                                }

                                @Override
                                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);

                                    int visibleItemCount = linearLayoutManager.getChildCount();
                                    int totalItemCount = linearLayoutManager.getItemCount();
                                    int pastVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

                                    if ((visibleItemCount + pastVisibleItem) >= totalItemCount) {
                                        btn_move_down.setVisibility(View.GONE);
                                    } else {
                                        btn_move_down.setVisibility(View.VISIBLE);
                                    }

                                    if (isScrolling && (visibleItemCount + pastVisibleItem == totalItemCount) && !isLastItemReached) {
                                        isScrolling = false;
//                                        progressView.setVisibility(View.VISIBLE);

                                    }
                                }
                            });

                            recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

                        } else {
                            progressView.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressView.setVisibility(View.GONE);
                        Toast.makeText(MainActivity.this, "Error Base de datos \n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private int itemCaja = 0;
    private String mLastKey,mPrevKey;

     void  loadMoreCaja(){
         Query nextQuery  = db.collection("Caja")
                 .orderBy("fechaDate", Query.Direction.DESCENDING)
                 .startAfter(lastVisible)
                 .limit(15);
         nextQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
             @Override
             public void onComplete(@NonNull Task<QuerySnapshot> task) {
                 for (QueryDocumentSnapshot snapshot : task.getResult()){
                     CajaM cajaM = snapshot.toObject(CajaM.class);
                     cajaM.setId(snapshot.getId());

                     String cajaKey = snapshot.getId();

                     if (!mPrevKey.equals(cajaKey)){


                         cajaMList.add(itemCaja++,cajaM);

                     }else {
                         mPrevKey = mLastKey;
                     }


                     if (itemCaja ==1){
                         mLastKey = cajaKey;
                     }


                 }
                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                     cajaMList.sort((c1,ca2) -> c1.getFechaDate().compareTo(ca2.getFechaDate()));
                 }
                 cajaAdapter.notifyDataSetChanged();
                 if(task.getResult().size() >0){

                     lastVisible = task.getResult().getDocuments().get(task.getResult().size()-1);
                 }
                 if (task.getResult().size() <15){
                     isLastItemReached = true;
                 }

                 totalCajasEchas += task.getResult().size();
                 total_numero_cajas.setText("Total de Cajas Echas = "+totalCajasEchas);

                 progressView.setVisibility(View.GONE);
                 refresh_caja.setRefreshing(false);
                 linearLayoutManager.scrollToPositionWithOffset(15,0);
             }
         });
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

                if (cajaMList.size()>0) {
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

                            isSearch = true;
                        }

                    }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

                    StartTime.show();

                }
            }else if (id == R.id.deudas ){
                Intent intent = new Intent(MainActivity.this, DeudasActivity.class);
                startActivity(intent);
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

    @Override
    public void onBackPressed() {
        if (isSearch){
            clearSearch();
        }else {
            super.onBackPressed();
        }
    }
}