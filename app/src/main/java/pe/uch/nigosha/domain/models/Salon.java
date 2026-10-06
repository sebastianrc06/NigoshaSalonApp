package pe.uch.nigosha.domain.models;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Salon {
  public String nombre = "Nigosha Salón & Spa";
  public String direccion = "Av. San Felipe 272";
  public String distrito = "Comas 15313 · Lima, Perú";
  public String telefono = "51990938752";
  public String whatsapp = "51990938752";
  public String enlaceMaps = "https://maps.app.goo.gl/2iy6Tjn5PEWWRJFv7";
  public double latitud = -11.9062679;
  public double longitud = -77.0354599;
  public String horario = "Por confirmar";

  public Salon() {}

  public String direccionCompleta() {
    return direccion + ", " + distrito;
  }
}
