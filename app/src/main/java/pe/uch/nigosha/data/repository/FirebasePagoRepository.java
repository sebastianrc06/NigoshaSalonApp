package pe.uch.nigosha.data.repository;

import pe.uch.nigosha.domain.models.Cita;
import pe.uch.nigosha.domain.models.ResumenPago;
import pe.uch.nigosha.domain.repository.*;

public final class FirebasePagoRepository implements PagoRepository {
  private final MisCitasRepository citas;

  public FirebasePagoRepository(MisCitasRepository citas) {
    this.citas = citas;
  }

  @Override
  public MisCitasRepository.Subscription observe(String id, Callback callback) {
    return citas.observeCita(
        id,
        new MisCitasRepository.CitaCallback() {
          @Override
          public void onSuccess(Cita cita) {
            callback.onSuccess(new ResumenPago(cita));
          }

          @Override
          public void onError(String message) {
            callback.onError(message);
          }
        });
  }
}
