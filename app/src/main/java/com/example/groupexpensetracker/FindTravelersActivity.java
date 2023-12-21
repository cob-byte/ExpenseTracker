package com.example.groupexpensetracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.groupexpensetracker.Entities.FindTraveler;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindTravelersActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TextView searchText;
    private ImageButton searchBtn;

    private RecyclerView recyclerViewList;

    private DatabaseReference allUsersRef;

    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_travelers);

        allUsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        //Toolbar setup
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbarFindTravelers);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Search for travel buddies");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewList = (RecyclerView) findViewById(R.id.findTravelerRecyclerview);
        recyclerViewList.setHasFixedSize(true);
        recyclerViewList.setLayoutManager(new LinearLayoutManager(FindTravelersActivity.this));

        searchText = (TextView) findViewById(R.id.findTravelerSearchTextBar);
        searchBtn = (ImageButton) findViewById(R.id.findTravelerSearchButton);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search_text = searchText.getText().toString();
                searchTravelers(search_text);
            }
        });
        searchTravelers("");
    }

    private void searchTravelers(String search_text) {

        Query searchTravelersQuery = allUsersRef.orderByChild("fullname")
                .startAt(search_text).endAt(search_text + "\uf8ff");

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<FindTraveler, FindTravelersViewHolder>(
                FindTraveler.class,
                R.layout.all_travelers_dispaly_layout,
                FindTravelersViewHolder.class,
                searchTravelersQuery
        ) {
            @Override
            protected void populateViewHolder(FindTravelersViewHolder holder, FindTraveler model, final int i) {
                holder.setProfileImage(getApplicationContext(), model.getProfileImage());
                holder.setFullname(model.getFullname());
                holder.setCountry(model.getCountry());

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(i).getKey();

                        Intent intent = new Intent(FindTravelersActivity.this, PersonProfileActivity.class);
                        intent.putExtra("visit_user_id", visit_user_id);
                        startActivity(intent);
                    }
                });
            }
        };
        recyclerViewList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindTravelersViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public FindTravelersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setProfileImage(Context ctx, String profileImage) {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.allTravelerProfileImage);
            Picasso.with(ctx).load(profileImage).placeholder(R.drawable.ic_launcher_background).into(myImage);
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

}
