package com.lt.fcmsample;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.graphics.drawable.PictureDrawable;
import com.caverock.androidsvg.SVG;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "FCMExample";
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        // Get the FCM token asynchronously
        getTokenAsync();
        loadImages();

        // Replace {54321} with the actual user ID you want to check
        String userId = "54321";
        String urlString = "http://172.20.16.10:5000/SecureApp/check-user/" + userId;
        Log.d(TAG, urlString);

        // Execute the network request
        new CheckUserTask().execute(urlString);
    }

    private void getTokenAsync() {
        // Submit the task to the executor service
        executorService.submit(() -> {
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token
                            String token = task.getResult();

                            // Log the token
                            Log.d(TAG, "FCM Token: " + token);

                            // Optionally, you can send the token to your server here
                        }
                    });
        });
    }

    private void loadImages() {
        ImageView imageView1 = findViewById(R.id.imageView1);
        ImageView imageView2 = findViewById(R.id.imageView2);
        ImageView imageView3 = findViewById(R.id.imageView3);
        ImageView imageView4 = findViewById(R.id.imageView4);

        // URLs to load
        String url1 = "https://static.qa.m2exchange.com/img/doge.svg";
        String url2 = "https://static.qa.m2exchange.com/img/matic.svg";
        String url3 = "https://static.qa.m2exchange.com/img/Coin=usdt.svg";
        String url4 = "https://static.qa.m2exchange.com/img/shib.svg";

        // Load SVG images using loadSvgImage method
        loadSvgImage(url1, imageView1);
        loadSvgImage(url2, imageView2);
        loadSvgImage(url3, imageView3);
        loadSvgImage(url4, imageView4);

//        // Load images using Glide which is nbot necessory after loadSvgImage
//        Glide.with(this)
//                .load(url1)
//                .into(imageView1);
//
//        Glide.with(this)
//                .load(url2)
//                .into(imageView2);
//
//        Glide.with(this)
//                .load(url3)
//                .into(imageView3);
//
//        Glide.with(this)
//                .load(url4)
//                .into(imageView4);

        Log.d(TAG, "Images: " + imageView1 + imageView2 + imageView3 + imageView4);
    }

    private void loadSvgImage(String url, ImageView imageView) {
        new Thread(() -> {
            try {
                InputStream inputStream = new URL(url).openStream();
                SVG svg = SVG.getFromInputStream(inputStream);
                PictureDrawable drawable = new PictureDrawable(svg.renderToPicture());
                runOnUiThread(() -> imageView.setImageDrawable(drawable));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static class CheckUserTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String urlString = params[0];
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    return response.toString();
                } else {
                    return "Error: " + responseCode;
                }
            } catch (Exception e) {
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            // Log the result
            Log.d(TAG, result);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor service when the activity is destroyed
        executorService.shutdown();
    }
}
