package pe.uch.nigosha.app;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;

public final class NigoshaApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    // Nigosha utiliza una identidad visual clara fija.
    // No dependerá del modo claro/oscuro configurado en el teléfono.
    AppCompatDelegate.setDefaultNightMode(
            AppCompatDelegate.MODE_NIGHT_NO
    );

    FirebaseApp.initializeApp(this);
  }
}