package pe.uch.nigosha.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.*;
import pe.uch.nigosha.data.firebase.FirebaseProvider;
import pe.uch.nigosha.domain.models.Salon;
import pe.uch.nigosha.domain.repository.SalonRepository;

public final class FirebaseSalonRepository implements SalonRepository {
  @Override
  public Subscription observe(Callback callback) {
    DatabaseReference ref = FirebaseProvider.salon();
    final boolean[] active = {true};
    // Datos proporcionados por la dueña; /salon puede sobrescribirlos sin escribir desde el
    // cliente.
    callback.onSuccess(new Salon());
    ValueEventListener listener =
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!active[0]) return;
            try {
              Salon salon = snapshot.exists() ? snapshot.getValue(Salon.class) : new Salon();
              if (salon == null
                  || salon.nombre == null
                  || salon.direccion == null
                  || salon.distrito == null
                  || salon.telefono == null
                  || !salon.telefono.matches("[0-9]{8,15}")
                  || salon.whatsapp == null
                  || !salon.whatsapp.matches("[0-9]{8,15}")
                  || !Double.isFinite(salon.latitud)
                  || Math.abs(salon.latitud) > 90
                  || !Double.isFinite(salon.longitud)
                  || Math.abs(salon.longitud) > 180
                  || salon.enlaceMaps == null
                  || !(salon.enlaceMaps.startsWith("https://maps.app.goo.gl/")
                      || salon.enlaceMaps.startsWith("https://www.google.com/maps/"))) {
                callback.onError(
                    "La información del salón requiere revisión. Mostramos los datos de"
                        + " referencia.");
                return;
              }
              callback.onSuccess(salon);
            } catch (RuntimeException e) {
              callback.onError("No pudimos actualizar la información del salón.");
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
            if (active[0])
              callback.onError(
                  "Mostramos los datos de referencia. No pudimos consultar cambios del salón.");
          }
        };
    ref.addValueEventListener(listener);
    return () -> {
      active[0] = false;
      ref.removeEventListener(listener);
    };
  }
}
