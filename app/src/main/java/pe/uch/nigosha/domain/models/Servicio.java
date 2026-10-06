package pe.uch.nigosha.domain.models;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Servicio implements Serializable {

  private static final long serialVersionUID = 1L;

  public String id;
  public String nombre;
  public String categoria;
  public double precio;
  public int duracionMinutos;
  public String descripcion;

  public String tipo;
  public Boolean activo;
  public boolean precioPorConfirmar;
  public boolean precioDesde;

  public Servicio() {
    // Constructor requerido por Firebase.
  }

  public Servicio(
          String id,
          String nombre,
          String categoria,
          double precio,
          int duracionMinutos
  ) {
    this.id = id;
    this.nombre = nombre;
    this.categoria = categoria;
    this.precio = precio;
    this.duracionMinutos = duracionMinutos;
  }

  @Exclude
  public boolean esPaquete() {
    return "PAQUETE".equals(tipo);
  }

  @Exclude
  public boolean sePuedeReservar() {
    return duracionMinutos > 0
            && !precioPorConfirmar
            && !precioDesde
            && !Boolean.FALSE.equals(activo);
  }
}