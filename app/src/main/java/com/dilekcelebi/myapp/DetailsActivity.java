package com.dilekcelebi.myapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.dilekcelebi.myapp.databinding.ActivityDetailsBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;


public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;

    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        registerLauncher();

        database = this.openOrCreateDatabase("Films", MODE_PRIVATE, null);


        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.matches("new")){
            // new film
            binding.filmNameText.setText("");
            binding.typeOfFilmText.setText("");
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);

            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage);
            binding.selectImageView.setImageBitmap(selectImage);
        }

        else{
            int filmId = intent.getIntExtra("filmId", 1);
            binding.button.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM films WHERE id = ? ", new String[] {String.valueOf(filmId)}); // null olan şey ? yerine gelsin diye yazdık


                int filmNameIx = cursor.getColumnIndex("filmName");
                int typeOfFilmIx = cursor.getColumnIndex("typeOfFilm");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.filmNameText.setText(cursor.getString(filmNameIx));
                    binding.typeOfFilmText.setText(cursor.getString(typeOfFilmIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    // image

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.length);
                    binding.selectImageView.setImageBitmap(bitmap);
                }

                cursor.close();


            }catch (Exception e){
                e.printStackTrace();
            }
        }

        // launcher varsa oncreate'de register ederiz
    }


    public void save(View view) {

        String filmName = binding.filmNameText.getText().toString();
        String typeOfFilm = binding.typeOfFilmText.getText().toString();
        String year = binding.yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        // SQLite kayıt yapıcaz

        try {

            database.execSQL("CREATE TABLE IF NOT EXISTS films (id INTEGER PRIMARY KEY, filmName VARCHAR, typeOfFilm VARCHAR, year CARCHAR, image BLOB) ");


            String sqlString = ("INSERT INTO films (filmName,typeOfFilm, year, image) VALUES(?, ?, ?, ?)");

            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, filmName);
            sqLiteStatement.bindString(2, typeOfFilm);
            sqLiteStatement.bindString(3, year);
            sqLiteStatement.bindBlob(4, byteArray);
            sqLiteStatement.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }



        Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // yeni aktivite sadece açık kalsın, diğerleri kapansın!!!

        startActivity(intent);
    }

    public Bitmap makeSmallerImage(Bitmap image, int maxSize) { // küçülteceğimiz görseli parametre olarak aldık

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            // lanscape image

            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            // portrait image

            height = maxSize;
            width = (int) (height * bitmapRatio);

        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


    public void select(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_MEDIA_IMAGES)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("give permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // request permission
                            permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();

                } else {  // request permission directly
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                }
            } else { // gallery

                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);

            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
    }

    private void registerLauncher() {

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult o) {
                if (o.getResultCode() == RESULT_OK) {
                    Intent intentFromO = o.getData();
                    if (intentFromO != null) {
                        Uri imageData = intentFromO.getData();  // görselin nerede kayıtlı olduğunu verir
                        // binding.selectImageView.setImageURI(imageData);

                        try {

                            if (android.os.Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(DetailsActivity.this.getContentResolver(), imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.selectImageView.setImageBitmap(selectedImage);
                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(DetailsActivity.this.getContentResolver(), imageData);
                                binding.selectImageView.setImageBitmap(selectedImage);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean o) {
                if (o) {
                    // permission granted
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                } else {
                    // permission denied
                    Toast.makeText(DetailsActivity.this, "Permission needed!!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}