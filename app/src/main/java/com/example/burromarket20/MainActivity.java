package com.example.burromarket20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.burromarket20.Model.Users;
import com.example.burromarket20.Prevalent.Prevalent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {
    private Button joinNowButton,loginButton;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        joinNowButton = (Button) findViewById(R.id.main_join_now_btn);
        loginButton = (Button) findViewById(R.id.main_login_btn);
        loadingbar = new ProgressDialog(this);

        Paper.init(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        joinNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);

            }
        });
        //Recibimos la informacion de usuario para remember me
        String UserBoletaKey = Paper.book().read(Prevalent.UserBoleta);
        String UserPasswordKey = Paper.book().read(Prevalent.UserPasswordKey);
        //Revisamos si tiene activa la opcion
        if(UserBoletaKey != "" && UserPasswordKey != ""){
            if(!TextUtils.isEmpty(UserBoletaKey) && !TextUtils.isEmpty(UserPasswordKey)){
                AllowAcces(UserBoletaKey,UserPasswordKey);

                loadingbar.setTitle("Sesion ya iniciada");
                loadingbar.setMessage("Por favor espera....");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();
            }


        }
    }

    private void AllowAcces(final String boleta, final String password) {

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Verificacion del numero de boleta guardado en la base de datos
                if(snapshot.child("Users").child(boleta).exists()){

                    Users usersData = snapshot.child("Users").child(boleta).getValue(Users.class);

                    if(usersData.getBoleta().equals(boleta)){
                        if(usersData.getPassword().equals(password)){

                            Toast.makeText(MainActivity.this, "Sesion iniciada exitosamente", Toast.LENGTH_SHORT).show();
                            loadingbar.dismiss();
                            Intent intent = new Intent(MainActivity.this,HomeActivity.class);
                            Prevalent.onlineUser = usersData;
                            startActivity(intent);

                        }
                        else{
                            loadingbar.dismiss();
                            Toast.makeText(MainActivity.this, "Password incorrecto", Toast.LENGTH_SHORT).show();
                        }
                    }



                }else{
                    Toast.makeText(MainActivity.this, "No existe una cuenta con este numero de boleta:  "+boleta+"", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "Crea una cuenta :)", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}