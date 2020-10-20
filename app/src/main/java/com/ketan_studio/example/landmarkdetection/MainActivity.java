package com.ketan_studio.example.landmarkdetection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button btn;
    ImageView imgView;
    Bitmap resizedBitmap;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        btn = (Button)findViewById(R.id.take);
        imgView = (ImageView)findViewById(R.id.img);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with(MainActivity.this)
                        .galleryOnly()
                        .start();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri r = data.getData();

            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(),r);



                int width = imageBitmap.getWidth();
                int height = imageBitmap.getHeight();
                float scaleWidth = ((float) 480) / width;
                float scaleHeight = ((float) 360) / height;
                // CREATE A MATRIX FOR THE MANIPULATION...
                Matrix matrix = new Matrix();
                // RESIZE THE BIT MAP....
                matrix.postScale(scaleWidth, scaleHeight);

                // "RECREATE" THE NEW BITMAP
                resizedBitmap = Bitmap.createBitmap(
                        imageBitmap, 0, 0, width, height, matrix, false);
                imageBitmap.recycle();
                imgView.setImageBitmap(resizedBitmap);
//                MainWorking(resizedBitmap);

                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(resizedBitmap);
                FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance().getVisionCloudLandmarkDetector();
                Task<List<FirebaseVisionCloudLandmark>> result = detector.detectInImage(image)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                                // Task completed successfully
                                // ...
                                Toast.makeText(MainActivity.this, "Task completed successfully", Toast.LENGTH_SHORT).show();
                                for (FirebaseVisionCloudLandmark landmark: firebaseVisionCloudLandmarks) {

                                    Rect bounds = landmark.getBoundingBox();
                                    String landmarkName = landmark.getLandmark();
                                    String entityId = landmark.getEntityId();
                                    float confidence = landmark.getConfidence();

                                    // Multiple locations are possible, e.g., the location of the depicted
                                    // landmark and the location the picture was taken.
                                    for (FirebaseVisionLatLng loc: landmark.getLocations()) {
                                        double latitude = loc.getLatitude();
                                        double longitude = loc.getLongitude();
                                        Toast.makeText(MainActivity.this, "long :"+longitude, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Exeception"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }

    }

    private void MainWorking(Bitmap imageBitmap) {

    }
}