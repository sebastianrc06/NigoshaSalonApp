package pe.uch.nigosha.models;

public class Usuario {
    public String uid;
    public String nombre;
    public String correo;
    public String telefono;
    public String rol; // "CLIENTE", "ADMINISTRADORA", "PERSONAL"

    public Usuario() { }

    public Usuario(String uid, String nombre, String correo, String telefono, String rol) {
        this.uid = uid;
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.rol = rol;
    }
}