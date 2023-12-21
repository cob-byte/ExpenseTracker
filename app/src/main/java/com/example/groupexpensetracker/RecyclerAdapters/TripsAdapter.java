package com.example.groupexpensetracker.RecyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupexpensetracker.Entities.HistoryTrip;
import com.example.groupexpensetracker.R;

import java.util.ArrayList;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.TripsViewHolder> {

    private ArrayList<HistoryTrip> tripsList;

    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

    public static class TripsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public TripsViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            mView = itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
        public void setName(String name){
            TextView myName = (TextView) mView.findViewById(R.id.tripHistoryItemName);
            myName.setText(name);
        }
        public void setCountry(String country){
            TextView myCountry = (TextView) mView.findViewById(R.id.tripHistoryItemCountry);
            myCountry.setText(country);
        }
        public void setStartDate(String startDate){
            TextView myStartDate = (TextView) mView.findViewById(R.id.tripHistoryItemStartDate);
            myStartDate.setText(startDate);
        }
        public void setEndDate(String endDate){
            TextView myEndDate = (TextView) mView.findViewById(R.id.tripHistoryItemEndDate);
            myEndDate.setText(endDate);
        }
    }

    public TripsAdapter(ArrayList<HistoryTrip> tripsList) {
        this.tripsList = tripsList;
    }

    @NonNull
    @Override
    public TripsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_trip_item_layout,parent, false);
        TripsViewHolder tripsViewHolder = new TripsViewHolder(view, mListener);
        return tripsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TripsViewHolder holder, int position) {
        HistoryTrip historyTrip = tripsList.get(position);

        holder.setName(historyTrip.getName());
        holder.setCountry(historyTrip.getCountry());
        holder.setStartDate(historyTrip.getStart_date());
        holder.setEndDate(historyTrip.getEnd_date());
    }

    @Override
    public int getItemCount() {
        return tripsList.size();
    }



}
