package pe.uch.nigosha.models;

import java.io.Serializable;

public class Servicio implements Serializable {
    public String id;
    public String nombre;
    public String categoria;
    public double precio;
    public int duracionMinutos;

    public Servicio() { }

    public Servicio(String id, String nombre, String categoria, double precio, int duracionMinutos) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.duracionMinutos = duracionMinutos;
    }
}