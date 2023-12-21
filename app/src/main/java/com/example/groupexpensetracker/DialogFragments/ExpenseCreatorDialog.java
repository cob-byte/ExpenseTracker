package com.example.groupexpensetracker.DialogFragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.groupexpensetracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class ExpenseCreatorDialog extends AppCompatDialogFragment {

    private TextView expenseName, expenseCost;
    private Spinner categorySpinner;
    private ArrayAdapter<String> categoryAdapter;
    private int categoryPosition;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    private String current_user_id, current_trip_id, current_user_name, trip_currency;
    private DatabaseReference tripsRef;

    public ExpenseCreatorDialog(String current_trip_id, String current_user_id, String current_user_name, String trip_currency){
        this.current_trip_id = current_trip_id;
        this.current_user_id = current_user_id;
        this.current_user_name = current_user_name;
        this.trip_currency = trip_currency;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.expense_form_layout, null);

        builder.setView(view)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Add expense", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addExpenseToDatabase(view);
                    }
                });

        initViews(view);

        return builder.create();
    }



    private void addExpenseToDatabase(final View view) {
        String name = expenseName.getText().toString();
        String cost = expenseCost.getText().toString();
        String category = categorySpinner.getItemAtPosition(categoryPosition).toString();
        String type = null;
        if(radioButton != null){
            type = radioButton.getText().toString();
        }

        if(!validateForm(name, cost, category, type)){
            Toast.makeText(view.getContext(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd.MM. HH:mm");
        String date = currentDate.format(calForDate.getTime());

        final String[] parts = date.split(" ");

        HashMap<String, String> expense = new HashMap<>();
        expense.put("name", name);
        expense.put("cost", cost);
        expense.put("category", category);
        expense.put("type", type);
        expense.put("date", parts[0]);
        expense.put("time", parts[1]);
        expense.put("userId", current_user_id);
        expense.put("username", current_user_name);
        expense.put("currency", trip_currency);

        tripsRef = FirebaseDatabase.getInstance().getReference().child("Trips");
        tripsRef.child(current_trip_id)
                .child("expenses")
                .push()
                .setValue(expense)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(view.getContext(), "Something went wrong with saving data!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm(String name, String cost, String category, String type) {
        if(TextUtils.isEmpty(name)){
            return false;
        }
        if(TextUtils.isEmpty(cost)){
            return false;
        }
        if(TextUtils.isEmpty(category)){
            return false;
        }
        if(TextUtils.isEmpty(type)){
            return false;
        }
        return true;
    }


    private void initViews(final View view) {
        expenseName = (TextView) view.findViewById(R.id.expenseFormName);
        expenseCost = (TextView) view.findViewById(R.id.expenseFormPrice);

        //spinner setup
        categorySpinner = (Spinner) view.findViewById(R.id.expenseFormCategorySpinner);

        final ArrayList<String> category_list = new ArrayList<>();
        category_list.add("FOOD & DINING");
        category_list.add("TRANSPORT");
        category_list.add("ENTERTAINMENT");
        category_list.add("SHOPPING");
        category_list.add("OTHER");

        categoryAdapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_spinner_item, category_list);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //radio group
        radioGroup = (RadioGroup) view.findViewById(R.id.expenseFormRadioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioButton = (RadioButton) view.findViewById(checkedId);
            }
        });
    }
}
