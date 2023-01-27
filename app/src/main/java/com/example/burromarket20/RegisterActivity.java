package com.example.burromarket20;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private Button CreateAccountButton;
    private EditText InputName, InputBoletaNumber, InputPassword;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        CreateAccountButton = (Button) findViewById(R.id.register_btn);
        InputName = (EditText) findViewById(R.id.register_username_input);
        InputPassword = (EditText) findViewById(R.id.register_password_input);
        InputBoletaNumber = (EditText) findViewById(R.id.register_boleta_number_input);
        loadingbar = new ProgressDialog(this);


        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAccount();
            }
        });

    }

    private void CreateAccount() {
        String name = InputName.getText().toString();
        String boleta = InputBoletaNumber.getText().toString();
        String password = InputPassword.getText().toString();
        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Ingresa tu nombre", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(boleta)){
            Toast.makeText(this, "Ingresa tu numero de boleta", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingbar.setTitle("Crear cuenta");
            loadingbar.setMessage("Por favor espera, estamos verificando las credenciales");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();

            ValidateboletaNumber(name,boleta,password);
        }
    }

    private void ValidateboletaNumber(String name, String boleta, String password) {
        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();
        //DatabaseReference userNameRef = RootRef.child("Users").child(boleta);

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!(snapshot.child("Users").child(boleta).exists())){

                    HashMap<String, Object> userdataMap = new HashMap<>();
                    userdataMap.put("boleta",boleta);
                    userdataMap.put("password",password);
                    userdataMap.put("nombre",name);
                    RootRef.child("Users").child(boleta).updateChildren(userdataMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "Tu cuenta ha sido creada", Toast.LENGTH_SHORT).show();
                                        loadingbar.dismiss();

                                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                    }
                                    else{
                                        loadingbar.dismiss();
                                        Toast.makeText(RegisterActivity.this, "Error en la red, Intenta nuevamente", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                }else{
                    Toast.makeText(RegisterActivity.this, "La boleta "+boleta+ " ya está registrada.", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                    Toast.makeText(RegisterActivity.this, "Intenta con otro numero de boleta", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}