package pe.uch.nigosha.domain.repository;

import java.util.List;

public interface DisponibilidadRepository {

  final class Intervalo {
    public final long inicio;
    public final long fin;

    public Intervalo(long inicio, long fin) {
      this.inicio = inicio;
      this.fin = fin;
    }
  }

  interface Callback {
    void onSuccess(List<Intervalo> ocupados);

    void onError(String message);
  }

  interface Subscription {
    void cancel();
  }

  Subscription observe(Callback callback);
}
