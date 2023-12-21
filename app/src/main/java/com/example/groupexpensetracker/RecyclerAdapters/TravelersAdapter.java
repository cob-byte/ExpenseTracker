package com.example.groupexpensetracker.RecyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupexpensetracker.Entities.FindTraveler;
import com.example.groupexpensetracker.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class TravelersAdapter extends RecyclerView.Adapter<TravelersAdapter.TravelersViewHolder>{

    private ArrayList<FindTraveler> travelerList;

    private OnItemClickListener mListener;

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }


    public static class TravelersViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public TravelersViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
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
        public void setProfileImage(String profileImage) {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.allTravelerProfileImage);
            Picasso.with(mView.getContext()).load(profileImage).placeholder(R.drawable.ic_launcher_background).into(myImage);
        }
        public void setFullname(String fullname) {
            TextView myName = (TextView) mView.findViewById(R.id.allTravelerProfileFullName);
            myName.setText(fullname);
        }
        public void setCountry(String country) {
            TextView myCountry = (TextView) mView.findViewById(R.id.allTravelerProfileCountry);
            myCountry.setText(country);
        }
    }

    public TravelersAdapter(ArrayList<FindTraveler> travelerList){
        this.travelerList = travelerList;
    }

    @NonNull
    @Override
    public TravelersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_travelers_dispaly_layout, parent, false);
        TravelersViewHolder travelersViewHolder = new TravelersViewHolder(view, mListener);
        return travelersViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TravelersViewHolder holder, int position) {
        FindTraveler currentTraveler = travelerList.get(position);

        holder.setProfileImage(currentTraveler.getProfileImage());
        holder.setFullname(currentTraveler.getFullname());
        holder.setCountry(currentTraveler.getCountry());
    }

    @Override
    public int getItemCount() {
        return travelerList.size();
    }

}
