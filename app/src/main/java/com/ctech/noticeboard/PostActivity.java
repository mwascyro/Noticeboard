package com.ctech.noticeboard;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {

    private Button btncamera, btnbrowse, btnupload, btnDate;
    private EditText txtTitle, txtGenre, txtDate, txtDescription;
    private Uri ImagePathUri;
    private ProgressDialog progressDialog;
    private DatePickerDialog datePicker;
    private ImageView imgview;
    private File actualImage = null;
    private File compressedImage = null;
    private Bitmap imageBitmap;
    private int Image_Request_Code = 7;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private int year, month, day;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.my_toolbar);
        toolbar.setTitle("Post");
        setSupportActionBar(toolbar);

        storageReference = FirebaseStorage.getInstance().getReference().child("Uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Uploads").child("Notice");
        includes();
    }

    private void includes(){
        btnbrowse = (Button) findViewById(R.id.btn_image_browse);
        btncamera = (Button) findViewById(R.id.btn_image_camera);
        btnupload = (Button) findViewById(R.id.btn_post);
        btnDate = (Button) findViewById(R.id.btn_date_picker);
        txtTitle = (EditText) findViewById(R.id.edt_title);
        txtTitle.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
        txtGenre = (EditText) findViewById(R.id.edt_genre);
        txtDate = (EditText) findViewById(R.id.edt_date);
        txtDescription = (EditText) findViewById(R.id.edt_description);
        imgview = (ImageView) findViewById(R.id.img_view);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        txtDate.setText(day+ "/" + (month + 1) + "/" + year);

        btnDate.setOnClickListener(new ClickListener());


        btnbrowse.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");

                intent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(intent, "Select Image"), Image_Request_Code);
            }
        });

        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                postNotice();
            }
        });
    }

    private boolean checkConnectivity(){
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
    private void postNotice(){

        final String title_val = txtTitle.getText().toString().trim();
        final String genre_val = txtGenre.getText().toString().trim();
        final String date_val = txtDate.getText().toString().trim();
        final String description_val = txtDescription.getText().toString().trim();
        final Intent intent = new Intent(this, MainActivity.class);

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(genre_val) && !TextUtils.isEmpty(date_val) && !TextUtils.isEmpty(description_val) && ImagePathUri != null) {
            if (checkConnectivity()) {
                progressDialog.setTitle("Posting to Noticeboard...");
                progressDialog.setProgress(0);
                progressDialog.show();

                final StorageReference filepath = storageReference.child("Images").child(ImagePathUri.getLastPathSegment());
                Uri fileUri = Uri.fromFile(new File(String.valueOf(compressedImage)));
                filepath.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String url = uri.toString();

                                DatabaseReference newPost = databaseReference.push();

                                newPost.child("title").setValue(title_val);
                                newPost.child("genre").setValue(genre_val);
                                newPost.child("date").setValue(date_val);
                                newPost.child("description").setValue(description_val);
                                newPost.child("imageUrl").setValue(url);

                                Toast.makeText(getApplicationContext(), "Post Successful", Toast.LENGTH_SHORT).show();
                            }
                        });
                        //dismiss the progress dialog after successful upload
                        progressDialog.dismiss();
                        //return to the main activity by removing the post activity from the activity stack on post complete
                        finish();

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                        progressDialog.setProgress(currentProgress);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Snackbar.make(btnupload, "Failed...\nError message:\t"+e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            } else {
                Snackbar.make(btnupload, "Please check your connection...", Snackbar.LENGTH_LONG).show();
            }
        }else {
            Toast.makeText(this, "Please fill each field", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "Get PDF not implemented...", Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(this, "Please provide permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null){
            Toast.makeText(this, "PDF select not implemented...", Toast.LENGTH_LONG).show();
        }else if (requestCode == Image_Request_Code && data.getData() != null) {
            ImagePathUri = data.getData();
            try {
                actualImage = FileUtil.from(this, ImagePathUri);
                compressImage ();
            } catch (IOException e) {
                Toast.makeText(this, "Failed to read image file....", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            Picasso.get().load(ImagePathUri).centerInside().fit().into(imgview);
        }else {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
        }
    }

    private void compressImage () {
        try {
            compressedImage = new Compressor(this).setQuality(80).compressToFile(actualImage);
        } catch (IOException e) {
            Toast.makeText(this, "Image compression failed...", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private class ClickListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_date_picker:
                    datePicker = new DatePickerDialog(PostActivity.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                    txtDate.setText(i2+ "/" + (i1 + 1) + "/" + i);
                                }
                            }, year, month, day);
                    datePicker.show();
                    break;
            }
        }
    }
}
