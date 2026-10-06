package pe.uch.nigosha.data.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public final class FirebaseProvider {
  private static final String DATABASE_URL = "https://nigoshasalonapp-default-rtdb.firebaseio.com";

  private FirebaseProvider() {}

  public static DatabaseReference servicios() {
    return root().child("servicios");
  }

  public static DatabaseReference citas() {
    return root().child("citas");
  }

  public static DatabaseReference salon() {
    return root().child("salon");
  }

  public static DatabaseReference conexion() {
    return root().child(".info/connected");
  }

  private static DatabaseReference root() {
    return FirebaseDatabase.getInstance(DATABASE_URL).getReference();
  }
}
