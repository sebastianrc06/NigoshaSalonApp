package pe.uch.nigosha;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import pe.uch.nigosha.feature.auth.splash.SplashActivity;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = new Intent(
            this,
            SplashActivity.class
    );

    startActivity(intent);

    finish();
  }
}