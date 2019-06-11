package br.com.lddm.vigilantesurbanos.model.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Ver referência de https://opencagedata.com/api
 */
public interface LocationService {
    @GET("json")
    Call<LocationAPI> getLocation(@Query("key") String key, @Query("q") String latLong);
}
