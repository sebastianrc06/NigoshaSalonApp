package pe.uch.nigosha.domain.repository;

import pe.uch.nigosha.domain.models.Servicio;

public interface CitaRepository {

  interface Callback {
    void onSuccess(String citaId);

    void onError(String message);
  }

  void reservar(
      String requestId,
      Servicio servicio,
      String nombre,
      String telefono,
      long inicio,
      Callback callback);
}
