package com.example.groupexpensetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.groupexpensetracker.DialogFragments.AddParticipantsDialog;
import com.example.groupexpensetracker.DialogFragments.ExpenseCreatorDialog;
import com.example.groupexpensetracker.DialogFragments.PieChartDialog;
import com.example.groupexpensetracker.Entities.Expense;
import com.example.groupexpensetracker.RecyclerAdapters.ExpensesAdapter;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CurrentTripActivity extends AppCompatActivity {

    private String current_trip_id, current_user_id, current_user_name, STATE, trip_currency;
    private FirebaseAuth mAuth;
    private DatabaseReference tripsRef, usersRef;

    private Toolbar toolbar;

    private TextView tripName, tripCountry, tripStartDate;

    private RecyclerView mRecyclerView;
    private ExpensesAdapter mAdapter;

    private ArrayList<Expense> expensesList;

    private Button pieChartButton, endTripButton;

    //Statistics
    private HashMap<String, Double> spendingPerCategoryMap;
    private ArrayList<PieEntry> pieEntries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_trip);

        STATE = getIntent().getExtras().get("state").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        current_trip_id = getIntent().getExtras().get("current_trip_id").toString();
        tripsRef = FirebaseDatabase.getInstance().getReference().child("Trips");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        setUsernameFromUserId();
        setTripCurrency();

        //toolbar setup
        toolbar = (Toolbar) findViewById(R.id.toolbarCurrentTrip);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Expenses tracker");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewInit();
        textViewsInit();
        fillExpensesList();

        pieChartButton = (Button) findViewById(R.id.currentTripChartButton);
        pieChartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPieChartDialog();
            }
        });

        endTripButton = (Button) findViewById(R.id.currentTripEndButton);
        endTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTripForEveryone();
            }
        });

        if(STATE.equals("finished")){
            getSupportActionBar().hide();
            endTripButton.setVisibility(View.GONE);
        }

        if(STATE.equals("live")){
            checkIfUserNotOnTrip();
        }

        //Statistics
        spendingPerCategoryMap = new HashMap<>();
        pieEntries = new ArrayList<>();

    }

    private void checkIfUserNotOnTrip() {
        usersRef.child(current_user_id)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            String on_trip = dataSnapshot.child("on_trip").getValue().toString();
                            if(on_trip.equals("false")){
                                sendUserToMainActivity();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void sendUserToMainActivity() {
        Intent intent = new Intent(CurrentTripActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void endTripForEveryone() {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String end_date = currentDate.format(calForDate.getTime());

        tripsRef.child(current_trip_id)
                .child("end_date").setValue(end_date)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            tripsRef.child(current_trip_id)
                                    .child("members")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                                    if(snapshot.exists()){
                                                        final String user_id = snapshot.getKey();
                                                        usersRef.child(user_id)
                                                                .child("trips")
                                                                .child(current_trip_id).setValue("finished")
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){
                                                                            usersRef.child(user_id)
                                                                                    .child("on_trip").setValue("false")
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful()){
                                                                                                usersRef.child(user_id)
                                                                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                            @Override
                                                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                                                                if(dataSnapshot.exists()){
                                                                                                                    int numberOfTrips = Integer.parseInt(dataSnapshot.child("numberOfTrips").getValue().toString());
                                                                                                                    numberOfTrips += 1;
                                                                                                                    usersRef.child(user_id)
                                                                                                                            .child("numberOfTrips").setValue(numberOfTrips)
                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                    if(task.isSuccessful()){
                                                                                                                                        sendUserToMainActivity();
                                                                                                                                    }
                                                                                                                                }
                                                                                                                            });
                                                                                                                }
                                                                                                            }

                                                                                                            @Override
                                                                                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                                                            }
                                                                                                        });
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }
                });

    }

    private void setUsernameFromUserId(){
        usersRef.child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    current_user_name = dataSnapshot.child("fullname").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setTripCurrency(){
        tripsRef.child(current_trip_id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            trip_currency = dataSnapshot.child("trip_currency").getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void fillExpensesList() {
        tripsRef.child(current_trip_id).child("expenses")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        expensesList.clear();
                        if(dataSnapshot.exists()){
                            for(final DataSnapshot snapshot : dataSnapshot.getChildren()){
                                if(snapshot.exists()){
                                    Expense expense = new Expense
                                            (
                                                    snapshot.child("date").getValue().toString(),
                                                    snapshot.child("time").getValue().toString(),
                                                    snapshot.child("name").getValue().toString(),
                                                    snapshot.child("type").getValue().toString(),
                                                    snapshot.child("category").getValue().toString(),
                                                    snapshot.child("cost"). getValue().toString(),
                                                    snapshot.child("username").getValue().toString(),
                                                    snapshot.child("currency").getValue().toString()
                                            );

                                    expensesList.add(expense);
                                }
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                        updateSpendingPerCategoryMap();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void recyclerViewInit() {
        expensesList = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.currentTripRecyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(CurrentTripActivity.this));
        mAdapter = new ExpensesAdapter(expensesList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void textViewsInit() {
        tripName = (TextView) findViewById(R.id.currentTripName);
        tripCountry = (TextView) findViewById(R.id.currentTripCountry);
        tripStartDate = (TextView) findViewById(R.id.currentTripStartDate);

        tripsRef.child(current_trip_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.hasChild("trip_name")){
                        tripName.setText("Trip name: " + dataSnapshot.child("trip_name").getValue().toString());
                    }
                    if(dataSnapshot.hasChild("trip_country")){
                        tripCountry.setText("Country: " + dataSnapshot.child("trip_country").getValue().toString());
                    }
                    if(dataSnapshot.hasChild("start_date")){
                        tripStartDate.setText("Starting date: " + dataSnapshot.child("start_date").getValue().toString());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.expenses_tracker_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuAddExpenseET:
                openAddExpenseDialog();
                return true;
            case R.id.menuInviteParticipantsET:
                openAddParticipantsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openPieChartDialog() {
        PieChartDialog pieChartDialog = new PieChartDialog(pieEntries);
        pieChartDialog.show(getSupportFragmentManager(), "pie chart dialog");
    }

    private void openAddParticipantsDialog() {
        AddParticipantsDialog addParticipantsDialog = new AddParticipantsDialog(current_user_id);
        addParticipantsDialog.show(getSupportFragmentManager(), "add participants dialog");
    }

    private void openAddExpenseDialog() {
        ExpenseCreatorDialog expenseCreatorDialog = new ExpenseCreatorDialog(current_trip_id, current_user_id, current_user_name, trip_currency);
        expenseCreatorDialog.show(getSupportFragmentManager(), "add expense dialog");
    }


    //Statistics

    private void initSpendingPerCategoryMap(){
        spendingPerCategoryMap.put("FOOD & DINING", 0.0);
        spendingPerCategoryMap.put("TRANSPORT", 0.0);
        spendingPerCategoryMap.put("ENTERTAINMENT", 0.0);
        spendingPerCategoryMap.put("SHOPPING", 0.0);
        spendingPerCategoryMap.put("OTHER", 0.0);
    }

    private void updateSpendingPerCategoryMap() {
        Log.d("U UPDATEU MAPE", "provjeravam prvi if...");
        if(expensesList != null){
            Log.d("U UPDATEU MAPE", "Ušo u prvi if, provjeravam drugi...");
            if(!expensesList.isEmpty()){
                Log.d("U UPDATEU MAPE", "Ušo u drugi, stavljam vrijednosti na nula");
                initSpendingPerCategoryMap();
                for(Expense expense : expensesList){
                    String category = expense.getCategory();
                    Log.d("U FOR-U", "category: " + category);
                    Double prev;
                    switch (category){
                        case "FOOD & DINING":
                            prev = spendingPerCategoryMap.get("FOOD & DINING");
                            spendingPerCategoryMap.put("FOOD & DINING", prev+Double.parseDouble(expense.getCost()));
                            break;
                        case "TRANSPORT":
                            prev = spendingPerCategoryMap.get("TRANSPORT");
                            spendingPerCategoryMap.put("TRANSPORT", prev+Double.parseDouble(expense.getCost()));
                            break;
                        case "ENTERTAINMENT":
                            prev = spendingPerCategoryMap.get("ENTERTAINMENT");
                            spendingPerCategoryMap.put("ENTERTAINMENT", prev+Double.parseDouble(expense.getCost()));
                            break;
                        case "SHOPPING":
                            prev = spendingPerCategoryMap.get("SHOPPING");
                            spendingPerCategoryMap.put("SHOPPING", prev+Double.parseDouble(expense.getCost()));
                            break;
                        case "OTHER":
                            prev = spendingPerCategoryMap.get("OTHER");
                            spendingPerCategoryMap.put("OTHER", prev+Double.parseDouble(expense.getCost()));
                            break;
                        default:
                            break;
                    }
                    Log.d("U FOR-U", "Nakon switcha : " + spendingPerCategoryMap.get("FOOD & DINING").toString());
                }
                //tu sam spreman
                fillPieEnteriesList();
            }
        }
    }

    private void fillPieEnteriesList() {
        pieEntries.clear();
        for (Map.Entry<String, Double> entry : spendingPerCategoryMap.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            Log.d("U FOR-U fillPieEnteries", "value: " + value.intValue() + "   key: " + key);
            PieEntry pieEntry = new PieEntry(value.intValue(), key);
            pieEntries.add(pieEntry);
        }
    }

}
