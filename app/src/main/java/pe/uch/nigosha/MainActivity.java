package pe.uch.nigosha;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import pe.uch.nigosha.activities.HomeClienteActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Espera 1.5 segundos mostrando el Splash y redirige al Home del Cliente
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, HomeClienteActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }
}