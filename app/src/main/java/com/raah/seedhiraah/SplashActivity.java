package com.raah.seedhiraah;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(R.color.background));
        setContentView(R.layout.activity_splash);
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isConnectedToInternet()){
                        startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                    }
                    else {
                        Toast.makeText(SplashActivity.this, "Please make sure to connect with internet!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    finish();
                }
            }, 2000);
    }
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null
                && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}