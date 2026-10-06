package pe.uch.nigosha.data.repository;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import pe.uch.nigosha.domain.repository.DisponibilidadRepository;

public class FirebaseDisponibilidadRepository implements DisponibilidadRepository {

  private static final TimeZone ZONE = TimeZone.getTimeZone("America/Lima");

  private final DatabaseReference reference = pe.uch.nigosha.data.firebase.FirebaseProvider.citas();

  @Override
  public Subscription observe(Callback callback) {
    final boolean[] active = {true};

    ValueEventListener listener =
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!active[0]) {
              return;
            }

            List<Intervalo> ocupados = new ArrayList<>();

            for (DataSnapshot cita : snapshot.getChildren()) {
              String estado = text(cita.child("estado").getValue());

              if ("CANCELADA".equalsIgnoreCase(estado)
                  || "RECHAZADA".equalsIgnoreCase(estado)
                  || "EXPIRADA".equalsIgnoreCase(estado)) {
                continue;
              }

              Long inicio = number(cita.child("inicioMillis").getValue());

              Long fin = number(cita.child("finMillis").getValue());

              if (inicio == null || fin == null) {
                inicio = parseDay(text(cita.child("fecha").getValue()));

                if (inicio == null) {
                  callback.onError(
                      "Hay una cita antigua con fecha inválida. "
                          + "Debemos corregirla para consultar "
                          + "la disponibilidad.");
                  return;
                }

                Calendar next = Calendar.getInstance(ZONE);
                next.setTimeInMillis(inicio);
                next.add(Calendar.DAY_OF_MONTH, 1);
                fin = next.getTimeInMillis();
              }

              if (fin <= inicio) {
                callback.onError(
                    "Hay una cita con duración inválida. " + "Debemos corregir sus datos.");
                return;
              }

              ocupados.add(new Intervalo(inicio, fin));
            }

            callback.onSuccess(ocupados);
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
            if (active[0]) {
              callback.onError(
                  "No pudimos consultar la ocupación. " + "Comprueba tu conexión y reintenta.");
            }
          }
        };

    reference.addValueEventListener(listener);

    return () -> {
      active[0] = false;
      reference.removeEventListener(listener);
    };
  }

  private Long number(Object value) {
    return value instanceof Number ? ((Number) value).longValue() : null;
  }

  private String text(Object value) {
    return value instanceof String ? (String) value : "";
  }

  private Long parseDay(String value) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);

    formatter.setTimeZone(ZONE);
    formatter.setLenient(false);

    try {
      java.util.Date date = formatter.parse(value);

      if (date == null || !formatter.format(date).equals(value)) {
        return null;
      }

      return date.getTime();

    } catch (ParseException exception) {
      return null;
    }
  }
}
