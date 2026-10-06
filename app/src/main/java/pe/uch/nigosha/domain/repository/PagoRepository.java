package pe.uch.nigosha.domain.repository;

import pe.uch.nigosha.domain.models.ResumenPago;

/** Consulta del registro económico. No inicia cobros ni modifica su estado. */
public interface PagoRepository {
  interface Callback {
    void onSuccess(ResumenPago pago);

    void onError(String message);
  }

  MisCitasRepository.Subscription observe(String citaId, Callback callback);
}
