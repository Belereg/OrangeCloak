package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecommerce.Model.Cart;
import com.example.ecommerce.Prevalent.Prevalent;
import com.example.ecommerce.ViewHolder.CartViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private Button nextProcessbtn;
    private TextView txtTotalAmount, txtMsg1;
    private int totalPrice = 0;
    private Button remindMeLater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        createNotificationChannel();

        recyclerView = findViewById(R.id.cart_list);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        nextProcessbtn = (Button) findViewById(R.id.next_process_btn);
        txtTotalAmount = (TextView) findViewById(R.id.total_price);
        txtMsg1 = (TextView) findViewById(R.id.msg1);
        remindMeLater = (Button) findViewById(R.id.remind_process_btn);

        remindMeLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(CartActivity.this,"Reminder set", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(CartActivity.this, ReminderBroadcast.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(CartActivity.this,0,intent,0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                long timeAtButtonClick = System.currentTimeMillis();

                long tenSecondsInMilis = 1000*10;
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeAtButtonClick + tenSecondsInMilis, pendingIntent);
            }
        });

        nextProcessbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtTotalAmount.setText("Total price = " + String.valueOf(totalPrice) + "$");
                Intent intent = new Intent(CartActivity.this, ConfirmFinalOrderActivity.class);
                intent.putExtra("Total Price", String.valueOf(totalPrice));
                startActivity(intent);
            }
        });
    }

    public void ChangeFragment(View view){
        Fragment fragment;
        if (view == findViewById(R.id.with_vat)){
            Toast.makeText(CartActivity.this,"withVAT", Toast.LENGTH_SHORT).show();
            fragment = new VatPrice();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.price_fragment,fragment);
            ft.commit();
        }
        if (view == findViewById(R.id.without_vat)){
            Toast.makeText(CartActivity.this,"withoutVAT", Toast.LENGTH_SHORT).show();
            fragment = new WithoutVatPrice();
            FragmentManager fm = getFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.price_fragment,fragment);
            ft.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        CheckOrderState();

        txtTotalAmount.setText("Total price = " + String.valueOf(totalPrice) + "$");

        final DatabaseReference cartListRef = FirebaseDatabase.getInstance().getReference().child("Cart List");
        FirebaseRecyclerOptions<Cart> options =
                new FirebaseRecyclerOptions.Builder<Cart>()
                        .setQuery(cartListRef.child("User View")
                                .child(Prevalent.currentOnlineUser.getPhone()).child("Products"), Cart.class)
                        .build();

        //iau produsele line by line
        FirebaseRecyclerAdapter<Cart, CartViewHolder> adapter = new FirebaseRecyclerAdapter<Cart, CartViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, int i, @NonNull Cart cart) {

                cartViewHolder.txtProductQuantity.setText("Quantity = " + cart.getQuantity());
                cartViewHolder.txtProductName.setText("Name = " + cart.getPname());
                cartViewHolder.txtProductPrice.setText("Price = " + cart.getPrice() + "$");

                int productTotalPrice = ((Integer.parseInt(cart.getPrice()))) * Integer.parseInt(cart.getQuantity());
                totalPrice += productTotalPrice;
                cartViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CharSequence options[] = new CharSequence[]{
                                "Edit",
                                "Delete"
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                        builder.setTitle("Cart Options:");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //daca apasa pe Edit
                                if (which == 0) {
                                    Intent intent = new Intent(CartActivity.this, ProductDetailsActivity.class);
                                    intent.putExtra("pid", cart.getPid());
                                    startActivity(intent);
                                }
                                //daca apasa pe Delete
                                if (which == 1) {
                                    cartListRef.child("User View")
                                            .child(Prevalent.currentOnlineUser.getPhone())
                                            .child("Products")
                                            .child(cart.getPid())
                                            .removeValue()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(CartActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(CartActivity.this, HomeActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }
                                            });

//                                    cartListRef.child("Admin View")
//                                            .child(Prevalent.currentOnlineUser.getPhone())
//                                            .child("Products")
//                                            .child(cart.getPid())
//                                            .removeValue()
//                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                @Override
//                                                public void onComplete(@NonNull Task<Void> task) {
//                                                    if (task.isSuccessful()) {
//                                                        Toast.makeText(CartActivity.this, "Item removed", Toast.LENGTH_SHORT).show();
//                                                        Intent intent = new Intent(CartActivity.this, HomeActivity.class);
//                                                        startActivity(intent);
//                                                    }
//                                                }
//                                            });
                                }
                            }
                        });
                        builder.show();
                    }
                });
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items_layout, parent, false);
                CartViewHolder holder = new CartViewHolder(view);
                return holder;
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "OrangeCloakReminder";
            String descripton = "Best shopping app!";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("notifyUser", name, importance);
            channel.setDescription(descripton);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void CheckOrderState()
    {
        DatabaseReference ordersRef;
        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders").child(Prevalent.currentOnlineUser.getPhone());

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String shippingState = snapshot.child("state").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();

                    if (shippingState.equals("shipped")) {
                        txtTotalAmount.setText("Dear" + userName + "/n order is shipped successfully.");
                        recyclerView.setVisibility(View.GONE);

                        txtMsg1.setVisibility(View.VISIBLE);
                        txtMsg1.setText("Your order has been shipped successfully. Soon you will recieve your order at your doorstep.");
                        nextProcessbtn.setVisibility(View.GONE);
                        Toast.makeText(CartActivity.this, "you can purchase more products once you recieved your first order.", Toast.LENGTH_SHORT).show();

                    } else if (shippingState.equals("not shipped")) {
                        txtTotalAmount.setText("Shipping state = not shipped");
                        recyclerView.setVisibility(View.GONE);

                        txtMsg1.setVisibility(View.VISIBLE);
                        nextProcessbtn.setVisibility(View.GONE);
                        Toast.makeText(CartActivity.this, "you can purchase more products once you recieved your first order.", Toast.LENGTH_SHORT).show();

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}