package com.ctech.noticeboard.ui.upload;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ctech.noticeboard.FileUtil;
import com.ctech.noticeboard.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class UploadFragment extends Fragment {

    private UploadViewModel uploadViewModel;
    private Button selectFile, upload, btnbrowse, btnupload;
    private TextView notification;
    private Uri fileUri, ImagePathUri;
    private ProgressDialog progressDialog;
    private EditText txtdata, txtTitle, txtGenre, txtDate, txtDescription;
    private ImageView imgview;
    public File imageFile;

    public File actualImage = null;
    public File compressedImage = null;

    private ProgressBar mProgressBar;
    private Bitmap imageBitmap;
    private byte[] mUploadBytes;

    private int Image_Request_Code = 7;

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;//used for uploading files
    private FirebaseDatabase database;//used to store URLs of uploaded files..

    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        uploadViewModel =
                ViewModelProviders.of(this).get(UploadViewModel.class);
        root = inflater.inflate(R.layout.fragment_upload, container, false);
        final TextView textView = root.findViewById(R.id.text_upload);
        uploadViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference().child("Uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Uploads").child("Notice");

        storage = FirebaseStorage.getInstance();//return an object of Firebase Storage
        database =FirebaseDatabase.getInstance();//return an object of FireBase Database

        includes();
        //includesFileUpload();


        return root;
    }

    private void includes(){
        btnbrowse = (Button) root.findViewById(R.id.btn_browse);
        btnupload = (Button) root.findViewById(R.id.btn_upload);
        txtTitle = (EditText) root.findViewById(R.id.edit_title);
        txtGenre = (EditText) root.findViewById(R.id.edit_genre);
        txtDate = (EditText) root.findViewById(R.id.edit_date);
        txtDescription = (EditText) root.findViewById(R.id.edit_description);
        imgview = (ImageView) root.findViewById(R.id.image_view);
        mProgressBar = (ProgressBar) root.findViewById(R.id.progressBar);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

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

    private void postNotice(){

        final String title_val = txtTitle.getText().toString().trim();
        final String genre_val = txtGenre.getText().toString().trim();
        final String date_val = txtDate.getText().toString().trim();
        final String description_val = txtDescription.getText().toString().trim();

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(genre_val) && !TextUtils.isEmpty(date_val) && !TextUtils.isEmpty(description_val) && ImagePathUri != null) {

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

                            Toast.makeText(getContext(), "Post Successful", Toast.LENGTH_SHORT).show();
                        }
                    });
                    progressDialog.dismiss();

                    //HomeFragment homeFragment = new HomeFragment();
                    //FragmentManager manager = getFragmentManager();
                    //manager.beginTransaction().replace(R.id.nav_upload, homeFragment, homeFragment.getTag()).commit();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressDialog.setProgress(currentProgress);
                }
            });


        }else {
            Toast.makeText(getContext(), "Please fill each field", Toast.LENGTH_LONG).show();
        }

    }

    private void uploadImage(){
        StorageReference imageRef = storage.getReference("Uploads").child("Images");
        final DatabaseReference imageDatabaseRef = database.getReference("Uploads").child("Images");

        if (ImagePathUri != null){
            progressDialog.setTitle("Image is Uploading ...");
            progressDialog.setProgress(0);
            progressDialog.show();

            StorageReference storageReference2 = imageRef.child(System.currentTimeMillis()+"."+GetFileExtension(ImagePathUri));
            storageReference2.putFile(ImagePathUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String TempImageName = txtdata.getText().toString().trim();

                    progressDialog.dismiss();

                    Toast.makeText(getContext(), "Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

                    @SuppressWarnings("VisibleForTests") UploadInfo imageUploadInfo = new UploadInfo(TempImageName, taskSnapshot.getUploadSessionUri().toString());
                    String ImageUploadId = imageDatabaseRef.push().getKey();
                    databaseReference.child(ImageUploadId).setValue(imageUploadInfo);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), "Image not successfully uploaded : \n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    int currentProgress = (int) (100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progressDialog.setProgress(currentProgress);
                }
            });
        }else {
            Toast.makeText(getContext(), "Please Select Image or Add Image Name", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 9 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            selectPdf();
        }
        else {
            Toast.makeText(getContext(), "Please provide permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectPdf() {

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 86);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 86 && resultCode == RESULT_OK && data != null){
            fileUri = data.getData();
            notification.setText("File selected : "+data.getData().getLastPathSegment());
        }else if (requestCode == Image_Request_Code && data.getData() != null) {
            ImagePathUri = data.getData();
            try {
                actualImage = FileUtil.from(getContext(), ImagePathUri);
                compressImage ();
            } catch (IOException e) {
                Toast.makeText(getContext(), "Failed to read image file....", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            Picasso.get().load(ImagePathUri).centerInside().fit().into(imgview);
            /*
            try {
                Bitmap bmp = null;
                bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), ImagePathUri);
                imgview.setImageBitmap(bmp);
            }catch (IOException e){
                e.printStackTrace();
            }

             */
        }else {
            Toast.makeText(getContext(), "Please select a file", Toast.LENGTH_SHORT).show();
        }
    }

    private void compressImage () {
        try {
            compressedImage = new Compressor(getActivity()).setQuality(80).compressToFile(actualImage);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Image compression failed...", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private String GetFileExtension (Uri uri){
        ContentResolver contentResolver = getActivity().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadNewPhoto (Bitmap bitmap){
        Log.d(TAG, "uploadNewPhoto: uploading a new image bitmap to storage");
        BackgroundImageResize resize = new BackgroundImageResize(bitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    private void uploadNewPhoto (Uri imagePath){
        Log.d(TAG, "uploadNewPhoto: uploading a new image uri to storage.");
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imagePath);
    }

    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {
        Bitmap mBitmap;

        public BackgroundImageResize(Bitmap bitmap){
            if (bitmap != null){
                this.mBitmap = bitmap;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getActivity(), "Compressing image...", Toast.LENGTH_SHORT).show();
            showProgressBar();
        }

        @Override
        protected byte[] doInBackground(Uri... uris) {
            Log.d(TAG, "doInBackground : started.");
            if (mBitmap == null){
                Log.d(TAG, "doInBackground : in if statement.");
                try{
                    Log.d(TAG, "doInBackground : in try statement.");
                    mBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uris[0]);
                    Log.d(TAG, "doInBackground : try statement completed.");
                }catch (IOException e) {
                    Log.d(TAG, "doInBackground : in catch statement.");
                    Log.e(TAG, "doInBackground : IOExcemption : " + e.getMessage());
                }
            }
            byte[] bytes = null;
            if (mBitmap == null){
                Log.d(TAG, "no bitmap to compress");
            }else {
                Log.d(TAG, "Image size before compressing : " + (mBitmap.getByteCount() / 1000000));
                bytes = getBytesFromBitmap(mBitmap, 100);
                Log.d(TAG, "Image size After compressing : " + (bytes.length / 1000000));
                }
            return bytes;
        }

        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            mUploadBytes = bytes;
            hideProgressBar();
            //execute the upload task
            //postNotice();
        }
    }


    public static byte[] getBytesFromBitmap (Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void showProgressBar () {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar () {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

}
