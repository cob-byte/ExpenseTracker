package com.example.groupexpensetracker.RecyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupexpensetracker.Entities.Expense;
import com.example.groupexpensetracker.R;

import java.util.ArrayList;

public class ExpensesAdapter extends RecyclerView.Adapter<ExpensesAdapter.ExpenseViewHolder> {

    ArrayList<Expense> expensesList;

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }
        public void setDate(String date) {
            TextView myDate = (TextView) mView.findViewById(R.id.expenseItemDate);
            myDate.setText(date);
        }
        public void seteName(String eName) {
            TextView myeName = (TextView) mView.findViewById(R.id.expenseItemExpenseName);
            myeName.setText(eName);
        }
        public void setCost(String cost) {
            TextView myCost = (TextView) mView.findViewById(R.id.expenseItemCost);
            myCost.setText(cost);
        }
        public void setTime(String time) {
            TextView myTime = (TextView) mView.findViewById(R.id.expenseItemTime);
            myTime.setText(time);
        }
        public void setUsername(String username) {
            TextView myUsername = (TextView) mView.findViewById(R.id.expenseItemUserName);
            myUsername.setText(username);
        }
        public void setCategory(String category) {
            TextView myCategory = (TextView) mView.findViewById(R.id.expenseItemCategory);
            myCategory.setText(category);
        }
        public void setCurrency(String currency){
            TextView myCurrency = (TextView) mView.findViewById(R.id.expenseItemCurrency);
            myCurrency.setText(currency);
        }
    }

    public ExpensesAdapter(ArrayList<Expense> expensesList){
        this.expensesList = expensesList;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item_layout, parent, false);
        ExpensesAdapter.ExpenseViewHolder expenseViewHolder = new ExpensesAdapter.ExpenseViewHolder(view);
        return expenseViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expensesList.get(position);

        holder.setDate(expense.getDate());
        holder.seteName(expense.geteName());
        holder.setCost(expense.getCost());
        holder.setTime(expense.getTime());
        holder.setUsername(expense.getUsername());
        holder.setCategory(expense.getCategory());
        holder.setCurrency(expense.getCurrency());
    }

    @Override
    public int getItemCount() {
        return expensesList.size();
    }
}
