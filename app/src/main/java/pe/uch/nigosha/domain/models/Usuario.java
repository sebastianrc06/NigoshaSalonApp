package pe.uch.nigosha.domain.models;

public class Usuario {

  public String uid;
  public String dni;

  public String nombres;
  public String apellidoPaterno;
  public String apellidoMaterno;

  public String correo;
  public String telefono;

  public String rol;

  public boolean activo;
  public long fechaRegistro;


  public Usuario() {
    // Requerido por Firebase
  }


  public Usuario(
          String uid,
          String dni,
          String nombres,
          String apellidoPaterno,
          String apellidoMaterno,
          String correo,
          String telefono,
          String rol,
          boolean activo,
          long fechaRegistro
  ) {

    this.uid = uid;
    this.dni = dni;

    this.nombres = nombres;

    this.apellidoPaterno =
            apellidoPaterno;

    this.apellidoMaterno =
            apellidoMaterno;

    this.correo = correo;

    this.telefono = telefono;

    this.rol = rol;

    this.activo = activo;

    this.fechaRegistro =
            fechaRegistro;
  }


  public String getNombreCompleto() {

    StringBuilder nombreCompleto =
            new StringBuilder();


    if (nombres != null
            && !nombres.trim().isEmpty()) {

      nombreCompleto.append(
              nombres.trim()
      );
    }


    if (apellidoPaterno != null
            && !apellidoPaterno
            .trim()
            .isEmpty()) {

      if (nombreCompleto.length() > 0) {

        nombreCompleto.append(" ");
      }

      nombreCompleto.append(
              apellidoPaterno.trim()
      );
    }


    if (apellidoMaterno != null
            && !apellidoMaterno
            .trim()
            .isEmpty()) {

      if (nombreCompleto.length() > 0) {

        nombreCompleto.append(" ");
      }

      nombreCompleto.append(
              apellidoMaterno.trim()
      );
    }


    return nombreCompleto.toString();
  }
}