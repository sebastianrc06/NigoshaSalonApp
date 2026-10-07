package pe.uch.nigosha.data.api.dni;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class DniApiClient {

    private static final String BASE_URL =
            "https://dniruc.apisperu.com/";

    private static DniApiService service;

    private DniApiClient() {
    }

    public static synchronized DniApiService getService() {

        if (service == null) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(
                            GsonConverterFactory.create()
                    )
                    .build();

            service = retrofit.create(DniApiService.class);
        }

        return service;
    }
}