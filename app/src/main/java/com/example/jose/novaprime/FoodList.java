package com.example.jose.novaprime;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import com.example.jose.novaprime.Common.Common;
import com.example.jose.novaprime.Interface.ItemClickListener;
import com.example.jose.novaprime.Model.Food;
import com.example.jose.novaprime.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapterFood;
    FirebaseDatabase database;
    DatabaseReference foodList;
    String categoryId = "";

    //Search Funcionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //Init fire base
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");
        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if(getIntent() != null)
            categoryId = getIntent().getStringExtra("categoryId");

        if(!categoryId.isEmpty()  && categoryId != null){

            if(Common.isConnectedToInternet(getBaseContext())) {
                loadListFoods(categoryId);
            } else {
                Toast.makeText(this, "Please check ypur internet conection", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        materialSearchBar = findViewById(R.id.searchBar);
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggest = new ArrayList<>();
                for(String search:suggestList){
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggestList);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean b) {
                if(!b)
                    recyclerView.setAdapter(adapterFood);
            }

            @Override
            public void onSearchConfirmed(CharSequence charSequence) {
                startSearch(charSequence);
            }

            @Override
            public void onButtonClicked(int i) {

            }
        });

    }

    private void startSearch(CharSequence charSequence){
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class, R.layout.food_item, FoodViewHolder.class,
                foodList.orderByChild("name").equalTo(charSequence.toString())) {
            @Override
            protected void populateViewHolder(FoodViewHolder foodViewHolder, Food food, int i) {
                foodViewHolder.product_name.setText(food.getName());

                Picasso.with(getBaseContext()).load(food.getImage()).into(foodViewHolder.product_image);

                final Food local = food;

                foodViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent productDetail = new Intent(FoodList.this, FoodDetail.class);
                        productDetail.putExtra("foodId",adapterFood.getRef(position).getKey());
                        startActivity(productDetail);
                    }
                });
            }
        };
        recyclerView.setAdapter(searchAdapter);
    }

    private void loadSuggest() {

        foodList.orderByChild("menuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren()){
                    Food item = postSnapshot.getValue(Food.class);
                    suggestList.add(item.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void loadListFoods(String categoryId) {
        adapterFood = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(Food.class, R.layout.food_item, FoodViewHolder.class,
                foodList.orderByChild("menuId").equalTo(categoryId)) {
            @Override
            protected void populateViewHolder(FoodViewHolder productViewHolder, Food food, int i) {
                productViewHolder.product_name.setText(food.getName());

                Picasso.with(getBaseContext()).load(food.getImage()).into(productViewHolder.product_image);

                final Food local = food;

                productViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                       Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                       foodDetail.putExtra("foodId",adapterFood.getRef(position).getKey());
                       startActivity(foodDetail);
                    }
                });
            }
        };
        recyclerView.setAdapter(adapterFood);
    }
}
