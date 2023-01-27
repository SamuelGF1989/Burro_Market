package com.example.burromarket20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.burromarket20.Prevalent.Prevalent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private CircleImageView profileImageView;
    private EditText fullnameEditText,userBoletaEditText,adressEditText;
    private TextView profileChangeTextBtn, closeTextBtn,saveTextButton;

    private Uri imageUri;
    private String myUrl= "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePictureRef;
    private String checker= "";







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");



        profileImageView = (CircleImageView) findViewById(R.id.settings_profile_image);
        fullnameEditText = (EditText) findViewById(R.id.settings_full_name);
        userBoletaEditText = (EditText) findViewById(R.id.settings_boleta_number);
        adressEditText = (EditText) findViewById(R.id.settings_address);
        profileChangeTextBtn = (TextView) findViewById(R.id.profile_image_change_btn);
        closeTextBtn = (TextView) findViewById(R.id.close_settings_btn);
        saveTextButton = (TextView) findViewById(R.id.update_account_settings_btn);

        userInfoDisplay(profileImageView,fullnameEditText,userBoletaEditText,adressEditText);

        closeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        saveTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checker.equals("clicked")){
                    userInfoSaved();
                }else{
                    updateOnlyUserInfo();
                }

            }
        });



        profileChangeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Si el usuario hace click verificamos que se presiona
                checker = "clicked";

            }
        });




    }

    private void updateOnlyUserInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        HashMap<String, Object> userMap = new HashMap<>();
        ///////////////REVISAR ESTA LINEA SI DA ERROR cambiar name a nombre o viceversa
        userMap.put("nombre",fullnameEditText.getText().toString());
        userMap.put("adress",adressEditText.getText().toString());
        userMap.put("boletaOrder",userBoletaEditText.getText().toString());
        ref.child(Prevalent.onlineUser.getBoleta()).updateChildren(userMap);

        startActivity(new Intent(SettingsActivity.this,MainActivity.class));
        Toast.makeText(SettingsActivity.this, "Informacion actualizada", Toast.LENGTH_SHORT).show();
        finish();

    }


    private void userInfoSaved() {
        if(TextUtils.isEmpty(fullnameEditText.getText().toString())){
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(adressEditText.getText().toString())){
            Toast.makeText(this, "La direccion de entrega es obligatoria", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(userBoletaEditText.getText().toString())){
            Toast.makeText(this, "La boleta es obligatoria", Toast.LENGTH_SHORT).show();

        }else if(checker.equals("clicked")){
            uploadImage();
        }
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Actualizar Perfil");
        progressDialog.setMessage("Por favor espera mientras tu perfil se actualiza");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if(imageUri != null){
            final StorageReference filRef = storageProfilePictureRef
                    .child(Prevalent.onlineUser.getBoleta()+".jpg");
            uploadTask = filRef.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }

                    return filRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                        HashMap<String, Object> userMap = new HashMap<>();
                        ///////////////REVISAR ESTA LINEA SI DA ERROR cambiar name a nombre o viceversa
                        userMap.put("nombre",fullnameEditText.getText().toString());
                        userMap.put("adress",adressEditText.getText().toString());
                        userMap.put("boletaOrder",userBoletaEditText.getText().toString());
                        userMap.put("image",myUrl);
                        ref.child(Prevalent.onlineUser.getBoleta()).updateChildren(userMap);


                        progressDialog.dismiss();
                        startActivity(new Intent(SettingsActivity.this,HomeActivity.class));
                        Toast.makeText(SettingsActivity.this, "Informacion actualizada", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    else{
                        progressDialog.dismiss();
                        Toast.makeText(SettingsActivity.this, "Erros", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        else{
            Toast.makeText(this, "Imagen no seleccionada", Toast.LENGTH_SHORT).show();
        }
    }

    private void userInfoDisplay(CircleImageView profileImageView, EditText fullnameEditText, EditText userBoletaEditText, EditText adressEditText) {
        DatabaseReference UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.onlineUser.getBoleta());
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Se verifica si el usuario existe
                if(snapshot.exists()){
                    if(snapshot.child("image").exists()){
                        String image = snapshot.child("image").getValue().toString();
                        String name = snapshot.child("nombre").getValue().toString();
                        String boleta = snapshot.child("boleta").getValue().toString();
                        String address = snapshot.child("address").getValue().toString();

                        Picasso.get().load(image).into(profileImageView);
                        fullnameEditText.setText(name);
                        userBoletaEditText.setText(boleta);
                        adressEditText.setText(address);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}