package com.example.burromarket20;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private String category_name,Description,Price,Pname,savecurrentDate,saveCurrentTime;
    private Button AddNewProductButton;
    private ImageView InputProductImage;
    private EditText InputProductName, InputProductDescription, InputProductPrice;
    private static final int GalleryPick=1;
    private Uri ImageUri;
    private String productRandomKey,downloadImageUrl;
    private StorageReference ProductImagesRef;
    private DatabaseReference ProductsRef;
    private ProgressDialog loadingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        category_name = getIntent().getExtras().get("categoria").toString();
        ProductImagesRef = FirebaseStorage.getInstance().getReference().child("Product Images");
        ProductsRef = FirebaseDatabase.getInstance().getReference().child("Products");
        Toast.makeText(this, category_name, Toast.LENGTH_SHORT).show();

        AddNewProductButton = (Button) findViewById(R.id.add_new_product);
        InputProductImage = (ImageView) findViewById(R.id.select_product_image);
        InputProductName = (EditText) findViewById(R.id.product_name);
        InputProductDescription = (EditText) findViewById(R.id.product_description);
        InputProductPrice = (EditText) findViewById(R.id.product_price);
        loadingbar = new ProgressDialog(this);


        InputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        AddNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();
            }
        });


        //Toast.makeText(this, "Admin, bienvenido", Toast.LENGTH_SHORT).show();
    }

    private void OpenGallery() {

        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,GalleryPick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null){
            ImageUri = data.getData();
            InputProductImage.setImageURI(ImageUri);
        }
    }
    private void ValidateProductData(){
        Description = InputProductDescription.getText().toString();
        Price = InputProductPrice.getText().toString();
        Pname = InputProductName.getText().toString();
        if(ImageUri == null){
            Toast.makeText(this, "Una foto del producto es necesaria", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Ingrese una descripcion del producto", Toast.LENGTH_SHORT).show();
        }else if(TextUtils.isEmpty(Price)){
            Toast.makeText(this, "Ingrese el precio del producto", Toast.LENGTH_SHORT).show();

        }else if(TextUtils.isEmpty(Pname)){
            Toast.makeText(this, "Ingrese el nombre del producto", Toast.LENGTH_SHORT).show();
        }else{
            StoreProductInformation();
        }
    }

    private void StoreProductInformation() {

        loadingbar.setTitle("A??adir producto");
        loadingbar.setMessage("Por favor espera, estamos a??adiendo el producto");
        loadingbar.setCanceledOnTouchOutside(false);
        loadingbar.show();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentdate = new SimpleDateFormat("MMM dd,yyyy");
        savecurrentDate = currentdate.format(calendar.getTime());

        SimpleDateFormat currentime = new SimpleDateFormat("HH:mm:ss a");
        savecurrentDate = currentime.format(calendar.getTime());

        //GENERACION DE LA LLAVE DE PRODUCTO
        productRandomKey = savecurrentDate + saveCurrentTime;

        StorageReference filePath = ProductImagesRef.child(ImageUri.getLastPathSegment()+productRandomKey+".jpg");
        final UploadTask uploadTask = filePath.putFile(ImageUri);
         uploadTask.addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 String message = e.toString();
                 Toast.makeText(AdminAddNewProductActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                 loadingbar.dismiss();

             }
         }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
             @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 Toast.makeText(AdminAddNewProductActivity.this, "Imagen cargada exitosamente", Toast.LENGTH_SHORT).show();

                 Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                     @Override
                     public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                         if(!task.isSuccessful()){
                             throw task.getException();

                         }
                         downloadImageUrl = filePath.getDownloadUrl().toString();
                         return filePath.getDownloadUrl();

                     }
                 }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                     @Override
                     public void onComplete(@NonNull Task<Uri> task) {
                         if(task.isSuccessful()){
                             downloadImageUrl = task.getResult().toString();
                             Toast.makeText(AdminAddNewProductActivity.this, "Imagen guardada a la base de datos exitosamente", Toast.LENGTH_SHORT).show();

                             SaveProductInfoToDatabase();
                         }

                     }
                 });
             }
         });




    }

    private void SaveProductInfoToDatabase() {
        HashMap<String,Object> productMap = new HashMap<>();
        productMap.put("pid",productRandomKey);
        productMap.put("date",savecurrentDate);
        productMap.put("time",saveCurrentTime);
        productMap.put("description",Description);
        productMap.put("image",downloadImageUrl);
        productMap.put("category",category_name);
        productMap.put("price",Price);
        productMap.put("pname",Pname);

        ProductsRef.child(productRandomKey).updateChildren(productMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(AdminAddNewProductActivity.this,AdminAddNewProductActivity.class);
                            startActivity(intent);

                            loadingbar.dismiss();
                            Toast.makeText(AdminAddNewProductActivity.this, "Producto a??adido exitosamente...", Toast.LENGTH_SHORT).show();
                        }else{
                            loadingbar.dismiss();
                            String message = task.getException().toString();
                            Toast.makeText(AdminAddNewProductActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}