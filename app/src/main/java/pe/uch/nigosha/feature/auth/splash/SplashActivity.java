package pe.uch.nigosha.feature.auth.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import pe.uch.nigosha.R;
import pe.uch.nigosha.feature.auth.login.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_TIME = 1400;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable abrirLogin = () -> {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        Intent intent = new Intent(
                SplashActivity.this,
                LoginActivity.class
        );

        startActivity(intent);
        finish();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        handler.postDelayed(abrirLogin, SPLASH_TIME);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(abrirLogin);
        super.onDestroy();
    }
}