package com.workstation.pocketmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NetworkFactory {
    String BASE_URL = "http://www.omdbapi.com";

    @GET("?")
    Call<SearchResultModel> search(@Query("s") String query, @Query("type") String type, @Query("page") int page);

    @GET("/")
    Call<MovieModel> getMovie(@Query("i") String imdbId);

    class Factory {
        public static NetworkFactory service;

        public static NetworkFactory getInstance() {
            if (service == null) {

                OkHttpClient.Builder httpClient =
                        new OkHttpClient.Builder();
                httpClient.addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        HttpUrl originalHttpUrl = original.url();

                        HttpUrl url = originalHttpUrl.newBuilder()
                                .addQueryParameter("apikey", BuildConfig.API_KEY)
                                .build();

                        // Request customization: add request headers
                        Request.Builder requestBuilder = original.newBuilder()
                                .url(url);

                        Request request = requestBuilder.build();
                        return chain.proceed(request);
                    }
                });

                Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL)
                        .client(httpClient.build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                service = retrofit.create(NetworkFactory.class);
                return service;
            } else {
                return service;
            }
        }
    }
}
