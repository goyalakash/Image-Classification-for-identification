package com.example.lenovo.project;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisInDomainResult;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;

public class MainActivity extends AppCompatActivity{
    public VisionServiceClient visionServiceClient = new VisionServiceRestClient("edb61b0ded494854834e64a5af7c6e4d");
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    Bitmap photo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.imageView = (ImageView)this.findViewById(R.id.imageView1);
        Button photoButton = (Button) this.findViewById(R.id.button1);
        photoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);

            }
        });
        Button resultbtn = (Button) this.findViewById(R.id.button);
        resultbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageView.setDrawingCacheEnabled(true);
                imageView.buildDrawingCache();
                Bitmap bmap = imageView.getDrawingCache();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());


                final AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
                    ProgressDialog mdialog = new ProgressDialog(MainActivity.this);
                    @Override
                    protected String doInBackground(InputStream... params) {
                        try {
                            publishProgress("recognizing...");
                            String[] features = {"Description"};
                            String[] details = {};
                            AnalysisResult result = visionServiceClient.analyzeImage(params[0],features,details);
                            String strResult = new Gson().toJson(result);
                            return  strResult;
                        } catch (Exception e) {
                            return null;
                        }

                    }
                    @Override
                    protected void onPreExecute(){
                        mdialog.show();
                    }

                    @Override
                    protected void onPostExecute(String s){
                        mdialog.dismiss();
                        AnalysisResult result = new Gson().fromJson(s,AnalysisResult.class);
                        TextView textView = (TextView) findViewById(R.id.responseView);
                        StringBuilder stringBuilder = new StringBuilder();
                        for(Caption caption:result.description.captions){
                            stringBuilder.append(caption.text);
                                                    }
                        textView.setText(stringBuilder);
                    }

                    @Override
                    protected void onProgressUpdate(String...values){
                        mdialog.setMessage(values[0]);
                    }
                };
                visionTask.execute(inputStream);
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);



        /*    AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    // All your networking logic
                    // should be here
                }
            });

            }*/
    }
/*
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }*//*
    private void apirequest() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // All your networking logic
                // should be here
            }
        });
    }*/
}
}