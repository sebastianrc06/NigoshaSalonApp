package pe.uch.nigosha.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import pe.uch.nigosha.core.session.ClienteSession;
import pe.uch.nigosha.core.util.InputValidator;
import pe.uch.nigosha.data.firebase.FirebaseProvider;
import pe.uch.nigosha.domain.models.Cita;
import pe.uch.nigosha.domain.repository.MisCitasRepository;

public class FirebaseMisCitasRepository implements MisCitasRepository {

  private static final String CLIENTE_ID = ClienteSession.clienteId();

  private final DatabaseReference reference = FirebaseProvider.citas();

  public Subscription observe(Callback callback) {
    Query query = reference.orderByChild("clienteId").equalTo(CLIENTE_ID);

    final boolean[] active = {true};

    ValueEventListener listener =
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!active[0]) {
              return;
            }

            List<Cita> citas = new ArrayList<>();

            try {
              for (DataSnapshot child : snapshot.getChildren()) {
                Cita cita = leerCita(child);
                if (cita != null) {
                  citas.add(cita);
                }
              }

              callback.onSuccess(citas);

            } catch (RuntimeException exception) {
              callback.onError("No pudimos interpretar los datos de tus citas.");
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
            if (active[0]) {
              callback.onError(
                  "No pudimos cargar tus citas. " + "Comprueba tu conexión y reintenta.");
            }
          }
        };

    query.addValueEventListener(listener);

    return () -> {
      active[0] = false;
      query.removeEventListener(listener);
    };
  }

  public void cancelar(String id, CancelCallback callback) {
    if (!InputValidator.idValido(id)) {
      callback.onError("El identificador de la cita no es válido.");
      return;
    }

    reference
        .child(id)
        .runTransaction(
            new Transaction.Handler() {

              private String rejection;

              @NonNull
              @Override
              public Transaction.Result doTransaction(@NonNull MutableData current) {
                rejection = null;

                // Firebase puede iniciar la transacción sin datos locales.
                // Al proponer un resultado sin cambios, el servidor
                // comprobará la versión y proporcionará los datos actuales.
                if (current.getValue() == null) {
                  return Transaction.success(current);
                }

                String clienteId = text(current.child("clienteId").getValue());

                if (!CLIENTE_ID.equals(clienteId)) {
                  rejection = "La cita no pertenece a esta sesión.";
                  return Transaction.abort();
                }

                String estado = text(current.child("estado").getValue());

                if ("CANCELADA".equalsIgnoreCase(estado)) {
                  return Transaction.success(current);
                }

                Object startValue = current.child("inicioMillis").getValue();

                Object paidValue = current.child("adelantoPagado").getValue();

                String estadoPago = text(current.child("estadoPago").getValue());

                if (!(startValue instanceof Number)
                    || ((Number) startValue).longValue() <= System.currentTimeMillis()
                    || !"PENDIENTE".equalsIgnoreCase(estado)
                    || !"PENDIENTE".equalsIgnoreCase(estadoPago)
                    || !(paidValue instanceof Number)
                    || ((Number) paidValue).doubleValue() != 0.0) {

                  rejection =
                      "La cita cambió o ya no permite cancelación " + "desde la aplicación.";
                  return Transaction.abort();
                }

                current.child("estado").setValue("CANCELADA");
                current.child("canceladaEn").setValue(ServerValue.TIMESTAMP);

                return Transaction.success(current);
              }

              @Override
              public void onComplete(
                  @Nullable DatabaseError error,
                  boolean committed,
                  @Nullable DataSnapshot snapshot) {
                if (error != null) {
                  callback.onError(
                      "No pudimos confirmar la cancelación. "
                          + "Revisa la conexión y el estado de la cita.");
                } else if (!committed) {
                  callback.onError(rejection == null ? "No se pudo cancelar la cita." : rejection);
                } else if (snapshot == null || !snapshot.exists()) {
                  callback.onError("La cita ya no existe.");
                } else if (!"CANCELADA"
                    .equalsIgnoreCase(snapshot.child("estado").getValue(String.class))) {
                  callback.onError(
                      "No pudimos confirmar el estado cancelado. Revisa la cita y reintenta.");
                } else {
                  callback.onSuccess();
                }
              }
            },
            false);
  }

  @Override
  public Subscription observeCita(String id, CitaCallback callback) {
    if (!InputValidator.idValido(id)) {
      callback.onError("No se recibió un código de cita válido.");
      return () -> {};
    }
    DatabaseReference item = reference.child(id);
    final boolean[] active = {true};
    ValueEventListener listener =
        new ValueEventListener() {
          @Override
          public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (!active[0]) return;
            if (!snapshot.exists()) {
              callback.onError("Esta cita ya no está disponible.");
              return;
            }
            try {
              Cita cita = leerCita(snapshot);
              if (cita == null || !CLIENTE_ID.equals(cita.clienteId)) {
                callback.onError("La cita no pertenece a esta sesión.");
                return;
              }
              callback.onSuccess(cita);
            } catch (RuntimeException exception) {
              callback.onError("Los datos de esta cita requieren revisión.");
            }
          }

          @Override
          public void onCancelled(@NonNull DatabaseError error) {
            if (active[0])
              callback.onError("No pudimos consultar la cita. Revisa tu conexión y reintenta.");
          }
        };
    item.addValueEventListener(listener);
    return () -> {
      active[0] = false;
      item.removeEventListener(listener);
    };
  }

  private Cita leerCita(DataSnapshot child) {
    Cita cita = child.getValue(Cita.class);
    if (cita == null) return null;
    cita.id = child.getKey();
    if (cita.servicioNombre == null)
      cita.servicioNombre = child.child("servicio").getValue(String.class);
    if (cita.clienteNombre == null)
      cita.clienteNombre = child.child("nombreCliente").getValue(String.class);
    if (!child.hasChild("precio")) {
      Object old = child.child("precioTotal").getValue();
      if (old instanceof Number) cita.precio = ((Number) old).doubleValue();
    }
    return cita;
  }

  private String text(Object value) {
    return value instanceof String ? (String) value : "";
  }
}
