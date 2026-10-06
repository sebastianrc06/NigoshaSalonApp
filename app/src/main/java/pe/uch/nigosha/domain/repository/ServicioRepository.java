package pe.uch.nigosha.domain.repository;

import java.util.List;
import pe.uch.nigosha.domain.models.Servicio;

public interface ServicioRepository {

  interface Callback {
    void onSuccess(List<Servicio> servicios);

    void onError(String message);
  }

  interface Subscription {
    void cancel();
  }

  Subscription observe(Callback callback);
}
