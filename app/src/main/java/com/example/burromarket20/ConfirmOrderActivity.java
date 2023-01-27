package com.example.burromarket20;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.burromarket20.Prevalent.Prevalent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ConfirmOrderActivity extends AppCompatActivity {

    private EditText nameEditText, phoneEditText, AddressEditText, buildingEditText;
    private Button confirmOrderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_order);


        confirmOrderBtn = (Button) findViewById(R.id.confirm_final_order_btn);
        nameEditText = (EditText) findViewById(R.id.shipment_name);
        phoneEditText = (EditText) findViewById(R.id.shipment_name);
        AddressEditText = (EditText) findViewById(R.id.shipment_address);
        buildingEditText = (EditText) findViewById(R.id.shipment_building);

        confirmOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Check();
            }
        });
    }

    private void Check() {
        if(TextUtils.isEmpty(nameEditText.getText().toString())){
            Toast.makeText(this, "Por favor ingresa el nombre", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(phoneEditText.getText().toString())){
            Toast.makeText(this, "Por favor ingresa tu numero de telefono", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(AddressEditText.getText().toString())){
            Toast.makeText(this, "Por favor Ingresa el lugar de entrega", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(buildingEditText.getText().toString())){
            Toast.makeText(this, "Por favor ingresa el edificio para entrega", Toast.LENGTH_SHORT).show();

        }else{
            ConfirmOrder();
        }
    }

    private void ConfirmOrder() {

        final String saveCurrentDate,saveCurrentTime;
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        saveCurrentTime = currentDate.format(calForDate.getTime());

        final DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference()
                .child("Orders")
                .child(Prevalent.onlineUser.getBoleta());
        HashMap<String, Object> ordersMap = new HashMap<>();
        //12 24
        //Revisar si se necesita cambiar por name o nombre
        ordersMap.put("name",nameEditText.getText().toString());
        //Revisar si no es necesaria lla boleta
        ordersMap.put("phone",phoneEditText.getText().toString());
        ordersMap.put("address",AddressEditText.getText().toString());
        ordersMap.put("Building",buildingEditText.getText().toString());
        ordersMap.put("date",saveCurrentDate);
        ordersMap.put("time",saveCurrentTime);
        ordersMap.put("state","No enviado");

        ordersRef.updateChildren(ordersMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Vaciado del carrito
                if(task.isSuccessful()){
                    FirebaseDatabase.getInstance().getReference()
                            .child("Card List")
                            .child("User View")
                            .child(Prevalent.onlineUser.getBoleta())
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(ConfirmOrderActivity.this, "Tu orden ha sido recibida", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(ConfirmOrderActivity.this, HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();

                                    }
                                }
                            });
                }
            }
        });


    }
}