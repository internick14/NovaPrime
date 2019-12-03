package com.example.jose.novaprime;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.jose.novaprime.Common.Common;
import com.example.jose.novaprime.Database.Database;
import com.example.jose.novaprime.Model.Food;
import com.example.jose.novaprime.Model.Order;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class FoodDetail extends AppCompatActivity {

    TextView food_name, food_price, food_description;
    ImageView product_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    ElegantNumberButton numberButton;
    String foodId = "";
    FirebaseDatabase database;
    DatabaseReference foods;
    Food current_food;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");

        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        food_description = findViewById(R.id.food_description);
        food_name = findViewById(R.id.food_name);
        food_price = findViewById(R.id.food_price);
        product_image = findViewById(R.id.img_food);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);


        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new Database(getBaseContext()).addToCart(new Order(foodId,
                        current_food.getName(),
                        numberButton.getNumber(),
                        current_food.getPrice(),
                        current_food.getDiscount()));
                Toast.makeText(FoodDetail.this, "Added to cart", Toast.LENGTH_SHORT).show();
            }
        });

        if(getIntent() !=  null)
            foodId = getIntent().getStringExtra("foodId");
        if(!foodId.isEmpty()){
            if(Common.isConnectedToInternet(getBaseContext())) {
                getDetailFoods(foodId);
            } else {
                Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
                return;
            }
        }

    }

    private void getDetailFoods(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                current_food = dataSnapshot.getValue(Food.class);

                Picasso.with(getBaseContext()).load(current_food.getImage()).into(product_image);

                collapsingToolbarLayout.setTitle(current_food.getName());
                food_price.setText(current_food.getPrice());
                food_name.setText(current_food.getName());
                food_description.setText(current_food.getDescription());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
