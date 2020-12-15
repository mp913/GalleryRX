package com.example.mleykin.GalleryRX;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Bitmap currentBitmap = null;
    private Button startButton;
    private Button stopButton;
    private ImageView imageView;
    ArrayList<String> imagePaths;
    private static final int GALLERY_PERMISSION = 0;
    private Subscription subscription = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        startButton.setOnClickListener(v -> start());
        stopButton.setOnClickListener(v -> stop());

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION);
        }
        else {
            initGallery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GALLERY_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initGallery();
                } else {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.galleryPermissionDenied),Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    public void initGallery(){
        String[] projection = new String[] {
                MediaStore.Images.Media.DATA,
        };

        Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = managedQuery(imagesUri,
                projection,
                "",
                null,
                ""
        );

        imagePaths = new ArrayList<String>();
        if (cur.moveToFirst()) {
            int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
            do {
                imagePaths.add(cur.getString(dataColumn));
            } while (cur.moveToNext());
        }
        cur.close();
    }

    private void start() {
        if (subscription != null) {
            subscription.unsubscribe();
        }

        Observable<String> observableImagePaths = Observable.from(imagePaths);
        subscription = observableImagePaths.subscribeOn(Schedulers.io()).doOnNext(s -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e("Thread exception", "Thread sleep exception");
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(imagePath -> {
                    try {
                        currentBitmap = BitmapFactory.decodeFile(imagePath);
                        imageView.setImageBitmap(currentBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    private void stop() {
        subscription.unsubscribe();
        imageView.setImageResource(android.R.color.transparent);
    }
}
