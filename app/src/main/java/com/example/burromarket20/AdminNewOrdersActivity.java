package com.example.burromarket20;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.burromarket20.Model.AdminOrders;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminNewOrdersActivity extends AppCompatActivity {

    private RecyclerView ordersList;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_orders);

        ordersRef = FirebaseDatabase.getInstance().getReference().child("Orders");

        ordersList = findViewById(R.id.orders_list);
        ordersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {

        super.onStart();

        FirebaseRecyclerOptions<AdminOrders> options =
                new FirebaseRecyclerOptions.Builder<AdminOrders>()
                        .setQuery(ordersRef,AdminOrders.class)
                        .build();
        FirebaseRecyclerAdapter<AdminOrders,AdminOrdersViewHolder> adapter =
                new FirebaseRecyclerAdapter<AdminOrders, AdminOrdersViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull AdminOrdersViewHolder holder, final int position, @NonNull AdminOrders model) {
                        holder.userName.setText("Nombre "+model.getName());
                        holder.userPhonenNumber.setText("Boleta "+model.getPhone());
                        holder.userDateTime.setText("Fecha del pedido "+model.getDate()+" "+model.getTime());
                        holder.userShippingAddress.setText("Lugar de entrega "+model.getAddress()+","+model.getBuilding());

                        holder.ShowOrdersButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String uID = getRef(holder.getAdapterPosition()).getKey();
                                Intent intent = new Intent(AdminNewOrdersActivity.this, AdminUserProductsActivity.class);
                                intent.putExtra("uid",uID);
                                startActivity(intent);

                            }
                        });

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{
                                        "Si",
                                        "No"
                                };
                                AlertDialog.Builder builder = new AlertDialog.Builder(AdminNewOrdersActivity.this);
                                builder.setTitle("??Pedido ya entregado?");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        if(i==0){

                                            String uID = getRef(holder.getAdapterPosition()).getKey();
                                            RemoveOrder(uID);

                                        }else{
                                            finish();
                                        }
                                    }
                                });
                                builder.show();

                            }
                        });


                    }

                    @NonNull
                    @Override
                    public AdminOrdersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.orders_layout, parent, false);
                        return new AdminOrdersViewHolder(view);
                    }
                };
        ordersList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class AdminOrdersViewHolder extends RecyclerView.ViewHolder{
        public TextView userName,userPhonenNumber,userDateTime,userShippingAddress;
        public Button ShowOrdersButton;
        public AdminOrdersViewHolder(@NonNull View itemView) {

            super(itemView);


            userName = itemView.findViewById(R.id.order_user_name);
            userPhonenNumber = itemView.findViewById(R.id.order_boleta_number);
            userDateTime = itemView.findViewById(R.id.order_date_time);
            userShippingAddress = itemView.findViewById(R.id.order_Address);
            ShowOrdersButton = itemView.findViewById(R.id.show_all_products);

        }
    }

    private void RemoveOrder(String uID) {

        ordersRef.child(uID).removeValue();

    }
}