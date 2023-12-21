package com.example.groupexpensetracker.RecyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupexpensetracker.Entities.Notification;
import com.example.groupexpensetracker.R;

import java.util.ArrayList;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private ArrayList<Notification> notificationsList;

    private NotificationsAdapter.OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(NotificationsAdapter.OnItemClickListener listener){
        mListener = listener;
    }

    public NotificationsAdapter(ArrayList<Notification> notificationsList) {
        this.notificationsList = notificationsList;
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public NotificationViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
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
        public void setDate(String date) {
            TextView myDate = (TextView) mView.findViewById(R.id.notificationItemDate);
            myDate.setText(date);
        }
        public void setTime(String time) {
            TextView myTime = (TextView) mView.findViewById(R.id.notificationItemTime);
            myTime.setText(time);
        }
        public void setUsername(String username) {
            TextView myUsername = (TextView) mView.findViewById(R.id.notificationItemName);
            myUsername.setText(username);
        }
        public void setAndReturnTypeText(String type){
            TextView myTypeText = (TextView) mView.findViewById(R.id.notificationItemTypeText);
            if(type.equals("traveler_request")){
                myTypeText.setText("is sending you a traveler request!");
            }
            if(type.equals("trip_request")){
                myTypeText.setText("is inviting you to join him on a trip!");
            }
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item_layout, parent, false);
        NotificationsAdapter.NotificationViewHolder notificationViewHolder = new NotificationsAdapter.NotificationViewHolder(view, mListener);
        return notificationViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationsList.get(position);

        holder.setDate(notification.getDate());
        holder.setTime(notification.getTime());
        holder.setUsername(notification.getUserName());
        holder.setAndReturnTypeText(notification.getType());
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }
}
