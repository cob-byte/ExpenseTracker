package com.example.groupexpensetracker.DialogFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.groupexpensetracker.CountryJsonReader;
import com.example.groupexpensetracker.CurrentTripActivity;
import com.example.groupexpensetracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

public class TripCreatorDialog extends AppCompatDialogFragment {

    private FirebaseAuth mAuth;
    private String current_user_id;
    private DatabaseReference tripsRef, usersRef;
    private String trip_unique_id;

    private EditText tripName;
    private Spinner countrySpinner, currencySpinner;
    private int countryPosition, currencyPosition = 0;
    private ArrayAdapter<String> countryAdapter, currencyAdapter;





    private CountryJsonReader jsonReader;

    private boolean TRIP_CREATED = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {



        //Firebase
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        tripsRef = FirebaseDatabase.getInstance().getReference().child("Trips");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.trip_creator_dialog_layout, null);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Create trip", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createTripInDatabase(view.getContext());
                        if(TRIP_CREATED){
                            //sendUserToCurrentTripActivity(view.getContext(), trip_unique_id);
                        }
                    }
                });

        tripName = (EditText) view.findViewById(R.id.tripCreatorDialogTripName);

        initJSONReader(view.getContext());

        initCountrySpinner(view);

        initCurrencySpinner(view);

        return builder.create();
    }

    private void initCurrencySpinner(View view) {
        TreeSet<String> currency_set = readCurrencySetFromJSON();
        ArrayList<String> currency_list = new ArrayList<>(currency_set);
        currency_list.remove(0);
        currency_list.remove(currency_list.size()-1);
        currencySpinner = (Spinner) view.findViewById(R.id.tripCreatorDialogTripCurrency);

        currencyAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, currency_list);
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(currencyAdapter);
        currencySpinner.setSelection(55);

        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currencyPosition = position;
                Log.d("CURRENCY_POSITION", Integer.toString(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initCountrySpinner(View view) {
        List<String> countries = readCountriesFromJSON();
        countrySpinner = (Spinner) view.findViewById(R.id.tripCreatorDialogTripCountry);
        List<String> country_list = new ArrayList<String>();
        country_list.add(0, "Choose country:");
        country_list.addAll(countries);

        countryAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, country_list);
        countryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countrySpinner.setAdapter(countryAdapter);

        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!parent.getItemAtPosition(position).equals("Choose country:")){
                    countryPosition = position;
                }else{
                    countryPosition = 0;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void sendUserToCurrentTripActivity(Context context, String trip_id) {
        Intent intent = new Intent(context, CurrentTripActivity.class);
        intent.putExtra("current_trip_id", trip_id);
        intent.putExtra("state", "live");
        startActivity(intent);
    }

    private void createTripInDatabase(final Context context) {
        String trip_name = tripName.getText().toString();
        String trip_country = countrySpinner.getItemAtPosition(countryPosition).toString();
        String trip_currency = currencySpinner.getItemAtPosition(currencyPosition).toString();

        if(!validateForm(trip_name, trip_country)){
            Toast.makeText(context, "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        String start_date = currentDate.format(calForDate.getTime());

        HashMap<String, String> tripInfoMap = new HashMap<>();
        tripInfoMap.put("trip_name", trip_name);
        tripInfoMap.put("trip_country", trip_country);
        tripInfoMap.put("start_date", start_date);
        tripInfoMap.put("trip_currency", trip_currency);

        trip_unique_id = tripsRef.push().getKey();
        TRIP_CREATED = true;
        tripsRef.child(trip_unique_id)
                .setValue(tripInfoMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    tripsRef.child(trip_unique_id).child("members").child(current_user_id)
                            .setValue("master")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        usersRef.child(current_user_id).child("on_trip")
                                                .setValue(trip_unique_id)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Toast.makeText(context, "Trip created successfully", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });

    }

    private boolean validateForm(String trip_name, String trip_country) {
        if(TextUtils.isEmpty(trip_name)){
            return false;
        }
        if(trip_country.equals("Choose country:")){
            return false;
        }
        return true;
    }

    private void initJSONReader(Context context){
        try {
            jsonReader = new CountryJsonReader(context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> readCountriesFromJSON(){
        ArrayList<String> countries = new ArrayList<>();
        try {
            countries = jsonReader.getCountryNameList();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return countries;
    }

    private TreeSet<String> readCurrencySetFromJSON(){
        TreeSet<String> currencies = new TreeSet<>();
        try{
            currencies = jsonReader.getCurrencySet();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return currencies;
    }
}
