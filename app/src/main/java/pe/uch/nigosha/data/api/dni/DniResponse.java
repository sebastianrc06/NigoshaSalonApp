package pe.uch.nigosha.data.api.dni;

import com.google.gson.annotations.SerializedName;

public class DniResponse {

    @SerializedName("dni")
    private String dni;

    @SerializedName("nombres")
    private String nombres;

    @SerializedName("apellidoPaterno")
    private String apellidoPaterno;

    @SerializedName("apellidoMaterno")
    private String apellidoMaterno;

    public String getDni() {
        return dni;
    }

    public String getNombres() {
        return nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public String getApellidosCompletos() {

        String paterno =
                apellidoPaterno == null ? "" : apellidoPaterno.trim();

        String materno =
                apellidoMaterno == null ? "" : apellidoMaterno.trim();

        return (paterno + " " + materno).trim();
    }
}