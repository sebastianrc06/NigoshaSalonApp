package pe.uch.nigosha.domain.repository;

import java.util.List;
import pe.uch.nigosha.domain.models.Cita;

public interface MisCitasRepository {
  interface Subscription {
    void cancel();
  }

  interface Callback {
    void onSuccess(List<Cita> citas);

    void onError(String message);
  }

  interface CitaCallback {
    void onSuccess(Cita cita);

    void onError(String message);
  }

  interface CancelCallback {
    void onSuccess();

    void onError(String message);
  }

  Subscription observe(Callback callback);

  Subscription observeCita(String id, CitaCallback callback);

  void cancelar(String id, CancelCallback callback);
}
