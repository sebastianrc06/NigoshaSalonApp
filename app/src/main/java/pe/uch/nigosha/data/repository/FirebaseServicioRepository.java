package pe.uch.nigosha.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import pe.uch.nigosha.domain.models.Servicio;
import pe.uch.nigosha.domain.repository.ServicioRepository;

public class FirebaseServicioRepository implements ServicioRepository {

  private final DatabaseReference reference;

  public FirebaseServicioRepository(DatabaseReference reference) {
    this.reference = reference;
  }

  @Override
  public Subscription observe(Callback callback) {
    final boolean[] active = {true};

    ValueEventListener listener = new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (!active[0]) {
          return;
        }

        List<Servicio> servicios = new ArrayList<>();

        try {
          for (DataSnapshot child : snapshot.getChildren()) {
            Servicio servicio = child.getValue(Servicio.class);

            if (servicio == null) {
              continue;
            }

            servicio.id = child.getKey();

            if (Boolean.FALSE.equals(servicio.activo)) {
              continue;
            }

            if (servicio.nombre == null
                    || servicio.nombre.trim().isEmpty()
                    || !Double.isFinite(servicio.precio)
                    || servicio.precio < 0
                    || servicio.duracionMinutos < 0) {

              callback.onError(
                      "Hay un servicio con datos incompletos. "
                              + "Revisa nombre, precio y duración."
              );
              return;
            }

            servicio.nombre = servicio.nombre.trim();

            if (servicio.categoria == null
                    || servicio.categoria.trim().isEmpty()) {
              servicio.categoria = "Otros";
            } else {
              servicio.categoria = servicio.categoria.trim();
            }

            servicios.add(servicio);
          }

          servicios.sort(
                  (a, b) -> a.nombre.compareToIgnoreCase(b.nombre)
          );

          callback.onSuccess(servicios);

        } catch (RuntimeException exception) {
          Log.e(
                  "ServicioRepository",
                  "Datos de servicios incompatibles",
                  exception
          );

          callback.onError(
                  "No pudimos interpretar el catálogo. "
                          + "Revisa los datos de los servicios."
          );
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        if (!active[0]) {
          return;
        }

        Log.e(
                "ServicioRepository",
                "Error al consultar servicios",
                error.toException()
        );

        callback.onError(
                "No pudimos cargar los servicios. "
                        + "Comprueba tu conexión e inténtalo de nuevo."
        );
      }
    };

    reference.addValueEventListener(listener);

    return () -> {
      active[0] = false;
      reference.removeEventListener(listener);
    };
  }
}