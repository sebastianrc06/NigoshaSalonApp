package pe.uch.nigosha.app;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public final class NigoshaApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    FirebaseApp.initializeApp(this);
  }
}
