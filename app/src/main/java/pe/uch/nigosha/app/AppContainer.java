package pe.uch.nigosha.app;

import pe.uch.nigosha.data.firebase.FirebaseProvider;
import pe.uch.nigosha.data.repository.*;
import pe.uch.nigosha.domain.repository.*;

/** Composition root: pantallas y ViewModel consumen contratos del dominio. */
public final class AppContainer {
  private static ServicioRepository servicios;
  private static MisCitasRepository citas;
  private static CitaRepository reservas;
  private static DisponibilidadRepository disponibilidad;
  private static SalonRepository salon;
  private static PagoRepository pagos;

  private AppContainer() {}

  public static synchronized ServicioRepository servicios() {
    if (servicios == null) servicios = new FirebaseServicioRepository(FirebaseProvider.servicios());
    return servicios;
  }

  public static synchronized MisCitasRepository citas() {
    if (citas == null) citas = new FirebaseMisCitasRepository();
    return citas;
  }

  public static synchronized CitaRepository reservas() {
    if (reservas == null) reservas = new FirebaseCitaRepository();
    return reservas;
  }

  public static synchronized DisponibilidadRepository disponibilidad() {
    if (disponibilidad == null) disponibilidad = new FirebaseDisponibilidadRepository();
    return disponibilidad;
  }

  public static synchronized SalonRepository salon() {
    if (salon == null) salon = new FirebaseSalonRepository();
    return salon;
  }

  public static synchronized PagoRepository pagos() {
    if (pagos == null) pagos = new FirebasePagoRepository(citas());
    return pagos;
  }
}
