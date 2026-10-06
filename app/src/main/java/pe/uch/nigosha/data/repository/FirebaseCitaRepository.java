package pe.uch.nigosha.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import pe.uch.nigosha.core.session.ClienteSession;
import pe.uch.nigosha.core.util.InputValidator;
import pe.uch.nigosha.core.util.MoneyFormatter;
import pe.uch.nigosha.data.firebase.FirebaseProvider;
import pe.uch.nigosha.domain.models.ConfiguracionReservas;
import pe.uch.nigosha.domain.models.Servicio;
import pe.uch.nigosha.domain.repository.CitaRepository;

public class FirebaseCitaRepository implements CitaRepository {

  private static final TimeZone ZONE =
          TimeZone.getTimeZone("America/Lima");

  private final DatabaseReference citasRef =
          FirebaseProvider.citas();

  @Override
  public void reservar(
          String requestId,
          Servicio servicio,
          String nombre,
          String telefono,
          long inicio,
          Callback callback
  ) {
    if (!InputValidator.idValido(requestId)) {
      callback.onError("Código de solicitud inválido.");
      return;
    }

    if (servicio == null
            || !servicio.sePuedeReservar()
            || !InputValidator.idValido(servicio.id)
            || servicio.nombre == null
            || servicio.duracionMinutos <= 0
            || !Double.isFinite(servicio.precio)
            || servicio.precio < 0) {
      callback.onError("El servicio no tiene datos válidos.");
      return;
    }

    if (!InputValidator.nombreValido(nombre)
            || !InputValidator.celularValido(telefono)) {
      callback.onError("Revisa tu nombre y celular.");
      return;
    }

    long fin =
            inicio + servicio.duracionMinutos * 60L * 1000L;

    double requerido =
            MoneyFormatter.adelanto(servicio.precio);

    Map<String, Object> nueva = new HashMap<>();

    nueva.put("id", requestId);
    nueva.put("clienteId", ClienteSession.clienteId());
    nueva.put("clienteNombre", nombre.trim());
    nueva.put("servicioNombre", servicio.nombre);
    nueva.put("precio", servicio.precio);
    nueva.put("adelanto", requerido);
    nueva.put("fecha", format(inicio, "dd/MM/yyyy"));
    nueva.put("hora", format(inicio, "HH:mm"));
    nueva.put("estado", "PENDIENTE");

    nueva.put("servicioId", servicio.id);
    nueva.put("telefono", telefono);
    nueva.put("inicioMillis", inicio);
    nueva.put("finMillis", fin);
    nueva.put("duracionMinutos", servicio.duracionMinutos);
    nueva.put("zonaHoraria", "America/Lima");

    nueva.put("estadoPago", "PENDIENTE");
    nueva.put("adelantoRequerido", requerido);
    nueva.put("adelantoPagado", 0);
    nueva.put("saldoPendiente", servicio.precio);
    nueva.put("creadaEn", ServerValue.TIMESTAMP);

    FirebaseProvider.conexion()
            .addListenerForSingleValueEvent(
                    new ValueEventListener() {
                      @Override
                      public void onDataChange(
                              @NonNull DataSnapshot conexion
                      ) {
                        if (!Boolean.TRUE.equals(
                                conexion.getValue(Boolean.class)
                        )) {
                          callback.onError(
                                  "Necesitas conexión para solicitar "
                                          + "una cita. Conéctate "
                                          + "y vuelve a intentarlo."
                          );
                          return;
                        }

                        citasRef.runTransaction(
                                new Transaction.Handler() {

                                  private String rejection;

                                  @NonNull
                                  @Override
                                  public Transaction.Result doTransaction(
                                          @NonNull MutableData current
                                  ) {
                                    rejection = null;

                                    MutableData existente =
                                            current.child(requestId);

                                    if (existente.getValue() != null) {
                                      Long savedStart = number(
                                              existente.child(
                                                      "inicioMillis"
                                              ).getValue()
                                      );

                                      String savedService = text(
                                              existente.child(
                                                      "servicioId"
                                              ).getValue()
                                      );

                                      boolean sameRequest =
                                              savedStart != null
                                                      && savedStart == inicio
                                                      && servicio.id.equals(
                                                      savedService
                                              )
                                                      && ClienteSession
                                                      .clienteId()
                                                      .equals(
                                                              text(
                                                                      existente.child(
                                                                              "clienteId"
                                                                      ).getValue()
                                                              )
                                                      )
                                                      && nombre.trim().equals(
                                                      text(
                                                              existente.child(
                                                                      "clienteNombre"
                                                              ).getValue()
                                                      )
                                              )
                                                      && telefono.equals(
                                                      text(
                                                              existente.child(
                                                                      "telefono"
                                                              ).getValue()
                                                      )
                                              );

                                      if (sameRequest) {
                                        return Transaction.success(
                                                current
                                        );
                                      }

                                      rejection =
                                              "La solicitud ya existe "
                                                      + "con otros datos.";

                                      return Transaction.abort();
                                    }

                                    if (!validarJornada(inicio, fin)) {
                                      rejection =
                                              "Ese horario ya no es válido. "
                                                      + "Elige otro día u hora.";

                                      return Transaction.abort();
                                    }

                                    for (MutableData cita
                                            : current.getChildren()) {

                                      String estado = text(
                                              cita.child("estado")
                                                      .getValue()
                                      );

                                      if ("CANCELADA".equalsIgnoreCase(estado)
                                              || "RECHAZADA".equalsIgnoreCase(estado)
                                              || "EXPIRADA".equalsIgnoreCase(estado)) {
                                        continue;
                                      }

                                      Long otroInicio = number(
                                              cita.child("inicioMillis")
                                                      .getValue()
                                      );

                                      Long otroFin = number(
                                              cita.child("finMillis")
                                                      .getValue()
                                      );

                                      if (otroInicio == null
                                              || otroFin == null) {

                                        Long day = parseDay(
                                                text(
                                                        cita.child("fecha")
                                                                .getValue()
                                                )
                                        );

                                        if (day == null) {
                                          rejection =
                                                  "Hay una cita antigua "
                                                          + "con fecha inválida. "
                                                          + "Debemos corregirla "
                                                          + "antes de reservar.";

                                          return Transaction.abort();
                                        }

                                        Calendar next =
                                                Calendar.getInstance(ZONE);

                                        next.setTimeInMillis(day);
                                        next.add(
                                                Calendar.DAY_OF_MONTH,
                                                1
                                        );

                                        otroInicio = day;
                                        otroFin = next.getTimeInMillis();
                                      }

                                      if (otroFin <= otroInicio) {
                                        rejection =
                                                "Hay una cita con duración "
                                                        + "inválida. Debemos "
                                                        + "corregir sus datos.";

                                        return Transaction.abort();
                                      }

                                      if (ConfiguracionReservas.seCruzan(
                                              inicio,
                                              fin,
                                              otroInicio,
                                              otroFin
                                      )) {
                                        rejection =
                                                "El horario se cruza "
                                                        + "con otra cita. "
                                                        + "Selecciona otro.";

                                        return Transaction.abort();
                                      }
                                    }

                                    current.child(requestId)
                                            .setValue(nueva);

                                    return Transaction.success(current);
                                  }

                                  @Override
                                  public void onComplete(
                                          @Nullable DatabaseError error,
                                          boolean committed,
                                          @Nullable DataSnapshot snapshot
                                  ) {
                                    if (error != null) {
                                      callback.onError(
                                              "No pudimos confirmar "
                                                      + "el registro. Comprueba "
                                                      + "tu conexión y reintenta "
                                                      + "con los mismos datos."
                                      );

                                    } else if (!committed) {
                                      callback.onError(
                                              rejection == null
                                                      ? "No pudimos registrar "
                                                      + "ese horario."
                                                      : rejection
                                      );

                                    } else {
                                      callback.onSuccess(requestId);
                                    }
                                  }
                                },
                                false
                        );
                      }

                      @Override
                      public void onCancelled(
                              @NonNull DatabaseError error
                      ) {
                        callback.onError(
                                "No pudimos comprobar la conexión. "
                                        + "Vuelve a intentarlo."
                        );
                      }
                    }
            );
  }

  private boolean validarJornada(long inicio, long fin) {
    return ConfiguracionReservas.jornadaValida(
            inicio,
            fin,
            System.currentTimeMillis()
    );
  }

  private Long number(Object value) {
    return value instanceof Number
            ? ((Number) value).longValue()
            : null;
  }

  private String text(Object value) {
    return value instanceof String
            ? (String) value
            : "";
  }

  private Long parseDay(String value) {
    SimpleDateFormat formatter = new SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.ROOT
    );

    formatter.setTimeZone(ZONE);
    formatter.setLenient(false);

    try {
      java.util.Date date = formatter.parse(value);

      if (date == null
              || !formatter.format(date).equals(value)) {
        return null;
      }

      return date.getTime();

    } catch (ParseException exception) {
      return null;
    }
  }

  private String format(long value, String pattern) {
    SimpleDateFormat formatter = new SimpleDateFormat(
            pattern,
            Locale.forLanguageTag("es-PE")
    );

    formatter.setTimeZone(ZONE);
    return formatter.format(value);
  }
}