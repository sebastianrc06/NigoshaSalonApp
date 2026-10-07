package pe.uch.nigosha.data.api.dni;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface DniApiService {

    @GET("api/v1/dni/{dni}")
    Call<DniResponse> consultarDni(
            @Header("Authorization") String authorization,
            @Path("dni") String dni
    );
}