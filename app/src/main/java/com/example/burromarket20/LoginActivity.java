package com.example.burromarket20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {

    private EditText InputBoletaNumber, InputPassword;
    private Button LoginButton;
    private ProgressDialog loadingbar;
    private String parentDbName = "Users";
    private TextView AdminLink, NotAdminlink;

    private CheckBox chkboxRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        LoginButton = (Button) findViewById(R.id.login_btn);
        InputPassword = (EditText) findViewById(R.id.login_password_input);
        InputBoletaNumber = (EditText) findViewById(R.id.login_boleta_number_input);
        AdminLink = (TextView) findViewById(R.id.admin_panel_link);
        NotAdminlink = (TextView) findViewById(R.id.not_admin_panel_link);
        loadingbar = new ProgressDialog(this);

        chkboxRememberMe = (CheckBox) findViewById(R.id.remember_me_chkb);
        Paper.init(this);




        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LoginUser();

            }
        });
        AdminLink.setOnClickListener(new View.OnClickListener() {
            //Visibilidad para el login de usuarios que son administradores
            @Override
            public void onClick(View v) {
                LoginButton.setText("Admin Login");
                AdminLink.setVisibility(View.INVISIBLE);
                NotAdminlink.setVisibility(View.VISIBLE);
                parentDbName = "Admins";

            }
        });

        NotAdminlink.setOnClickListener(new View.OnClickListener() {
            //Visibilidad para el login de usuarios que son usuarios
            @Override
            public void onClick(View v) {
                LoginButton.setText("Login");
                AdminLink.setVisibility(View.VISIBLE);
                NotAdminlink.setVisibility(View.INVISIBLE);
                parentDbName = "Users";
            }
        });
    }

    private void LoginUser() {

        String boleta = InputBoletaNumber.getText().toString();
        String password = InputPassword.getText().toString();

        if(TextUtils.isEmpty(boleta)){
            Toast.makeText(this, "Ingresa tu numero de boleta", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Ingresa tu contraseña", Toast.LENGTH_SHORT).show();
        }
        else{
            loadingbar.setTitle("Iniciar sesion en la cuenta");
            loadingbar.setMessage("Por favor espera, estamos verificando las credenciales");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();

            AllowAccessToAccount(boleta,password);
        }

    }

    private void AllowAccessToAccount(String boleta, String password) {
        //Se guardan los valores de sesion si se elige palomear el checkbox
        if(chkboxRememberMe.isChecked()){
            //Enviamos el valor de la boleta y contraseña a la memoria
            Paper.book().write(Prevalent.UserBoleta,boleta);
            Paper.book().write(Prevalent.UserPasswordKey,password);
        }

        final DatabaseReference RootRef;
        RootRef = FirebaseDatabase.getInstance().getReference();

        RootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Verificacion del numero de boleta guardado en la base de datos
                if(snapshot.child(parentDbName).child(boleta).exists()){

                    Users usersData = snapshot.child(parentDbName).child(boleta).getValue(Users.class);

                    if(usersData.getBoleta().equals(boleta)){
                        if(usersData.getPassword().equals(password)){

                            if(parentDbName.equals("Admins")){
                                Toast.makeText(LoginActivity.this, "Administrador, sesion iniciada exitosamente", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                                Intent intent = new Intent(LoginActivity.this,AdminCategoryActivity.class);
                                startActivity(intent);
                            }
                            else if(parentDbName.equals("Users")){
                                Toast.makeText(LoginActivity.this, "Sesion iniciada exitosamente", Toast.LENGTH_SHORT).show();
                                loadingbar.dismiss();
                                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                Prevalent.onlineUser = usersData;
                                startActivity(intent);

                            }

                        }
                        else{
                            loadingbar.dismiss();
                            Toast.makeText(LoginActivity.this, "Password incorrecto", Toast.LENGTH_SHORT).show();
                        }
                    }



                }else{
                    Toast.makeText(LoginActivity.this, "No existe una cuenta con este numero de boleta:  "+boleta+"", Toast.LENGTH_SHORT).show();
                    Toast.makeText(LoginActivity.this, "Crea una cuenta :)", Toast.LENGTH_SHORT).show();
                    loadingbar.dismiss();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }
}