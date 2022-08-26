package com.example.android.workingwithfirebasestorage;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.android.workingwithfirestore.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST_CODE = 10001;
    private static final int PICK_FILE_REQUEST_CODE =1212 ;
    private StorageReference mRef;
    private final int REQUEST_PERMISSION_WRITE = 1001;
    private boolean permissionsGranted = false;
    ImageView imageView;
    private ProgressBar uploadProgress;
    private TextView progressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView= findViewById(R.id.imageView);
        progressText = findViewById(R.id.progressTxt);
        uploadProgress = findViewById(R.id.progressBar);
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        mRef = firebaseStorage.getReference("docs/");

    }

    public void uploadClicked(View view) {

        if (!permissionsGranted ) {
            checkPermission();
        }
        if (!checkPermission()) {
            return;
        }

        Intent intent = new Intent();
       // intent.setType("image/*");
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
     //   startActivityForResult(Intent.createChooser(intent, "Select an Image"), PICK_IMAGE_REQUEST_CODE);
        startActivityForResult(Intent.createChooser(intent, "Select an file"), PICK_FILE_REQUEST_CODE);

    }

    private void writeImageusingByteArray() {
        Bitmap bitmap = readImage();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        UploadTask uploadTask = mRef.child("images/view.jpg")
                .putBytes(baos.toByteArray());
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "image Uploaded !", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeImageUsingInputStream() {
        File file = new File(getFilesDir(), "azeem.jpg");
        FileInputStream fileInputStream = null;

        try {
            fileInputStream = new FileInputStream(file);
            UploadTask uploadTask = mRef.child("images2/azeem2.jpg").putStream(fileInputStream);
            FileInputStream finalFileInputStream = fileInputStream;
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    if (finalFileInputStream != null) {
                        try {
                            finalFileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
  //      if (requestCode == PICK_IMAGE_REQUEST_CODE && data != null) {
        if (requestCode == PICK_FILE_REQUEST_CODE && data != null) {
            Uri imageUri = data.getData();

          //  UploadTask uploadTask = mRef.child("External_Images/" + imageUri.getLastPathSegment()).putFile(imageUri);

           // Write Image Using Put File method
            UploadTask uploadTask = mRef.child("External_Images/pdfs").putFile(imageUri);
            uploadProgress.setVisibility(View.VISIBLE);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    long progress = (100 * snapshot.getBytesTransferred())/ snapshot.getTotalByteCount();
                    uploadProgress.setProgress((int) progress);
                    progressText.setText(progress+" %");
                }
            });
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressText.append("File Uploaded");
                }
            });
        }
    }

    //from Assets
    private Bitmap readImage() {
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("sddf.jpg");
            BitmapDrawable bitmapDrawable = (BitmapDrawable) Drawable.createFromStream(inputStream, null);
            return bitmapDrawable.getBitmap();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /*private void writeImage(Context context) {

        File file = new File(context.getFilesDir(), "azeem.jpg");
        FileOutputStream outputStream = null;
        Bitmap bitmap = readImage();
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
*/

    /* check if external storage is available for read and write*/
    public boolean isExternalStorageWriteAble() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state));
    }

    /* check if external storage is available to at least read */
    public boolean isExternalStorageReadAble() {
        String state = Environment.getExternalStorageState();
        return (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state));
    }

    /* Initiate request for permission */
    private boolean checkPermission() {
        if (!isExternalStorageWriteAble() || !isExternalStorageReadAble()) {
            Toast.makeText(this, "This App is only works on devices with usable external storage", Toast.LENGTH_SHORT).show();
            return false;
        }
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_WRITE);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_WRITE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionsGranted = true;
                    Toast.makeText(this, "External storage permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "You must grant permission!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    public void downloadClicked(View view) {
        if (!permissionsGranted ) {
            checkPermission();
        }
        if (!checkPermission()) {
            return;
        }

        DownloadImageUsingUriAndDownloadManager();


        //    downloadUsingGetfile();


        //  downloadFileUsingGetBytes();
    }

    private void DownloadImageUsingUriAndDownloadManager() {
        mRef.child("External_Images/image:19").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                downloadFile(uri);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFile(Uri uri) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"myImage.jpg");

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request  request = new DownloadManager.Request(uri)
                .setTitle("File Downloading...")
                .setDescription("This is file downloading Demo")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationUri(Uri.fromFile(file));
        downloadManager.enqueue(request);
    }

    private void downloadUsingGetFile() {
        File file = new File(Environment.getExternalStorageDirectory(),"my2.jpg");
      //  File file =  File.createTempFile("file","jpg");
        mRef.child("External_Images/image:7444").getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                imageView.setImageURI(Uri.fromFile(file));
                Toast.makeText(MainActivity.this, "Download", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "download Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFileUsingGetBytes() {
        File file = new File(Environment.getExternalStorageDirectory(),"file1.pdf");

        long ONE_MEGABYTE = (1024*1024*2);
        mRef.child("External_Images/pdfs").getBytes(ONE_MEGABYTE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {

                    @Override
                    public void onSuccess(byte[] bytes) {

                        Bitmap b = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        ImageView image = (ImageView) findViewById(R.id.imageView);
                        image.setImageBitmap(b);
                        try {
                            FileOutputStream outputStream = new FileOutputStream(file);
                            outputStream.write(bytes);
                            outputStream.close();
                            Toast.makeText(MainActivity.this, file.getPath().toString(), Toast.LENGTH_SHORT).show();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Download Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}