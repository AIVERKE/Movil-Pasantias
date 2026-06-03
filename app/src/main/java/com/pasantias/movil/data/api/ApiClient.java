package com.pasantias.movil.data.api;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.pasantias.movil.BuildConfig;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.ui.auth.LoginActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    private static ApiClient instance;
    private final PasantiasApi api;
    private Context appContext;

    private ApiClient(Context context) {
        this.appContext = context.getApplicationContext();
        OkHttpClient client = buildClient();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(PasantiasApi.class);
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
    }

    public static ApiClient get() {
        return instance;
    }

    public PasantiasApi api() {
        return api;
    }

    public static String mediaUrl(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("http")) return path;
        String base = BuildConfig.MEDIA_BASE_URL;
        if (!path.startsWith("/")) path = "/" + path;
        return base + path;
    }

    private OkHttpClient buildClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor authInterceptor = chain -> {
            Request.Builder builder = chain.request().newBuilder();
            String token = TokenManager.get().getToken();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
            return chain.proceed(builder.build());
        };

        Interceptor unauthorizedInterceptor = chain -> {
            Response response = chain.proceed(chain.request());
            if (response.code() == 401 && TokenManager.get().isLoggedIn()) {
                TokenManager.get().clear();
                Intent intent = new Intent(appContext, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                appContext.startActivity(intent);
            }
            return response;
        };

        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(authInterceptor)
                .addInterceptor(unauthorizedInterceptor)
                .addInterceptor(logging)
                .build();
    }

    public static <T> void enqueue(retrofit2.Call<T> call, ApiCallback<T> callback) {
        call.enqueue(new retrofit2.Callback<T>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<T> c, @NonNull retrofit2.Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<T> c, @NonNull Throwable t) {
                callback.onError(t.getMessage() != null ? t.getMessage() : "Error de red");
            }
        });
    }

    private static String parseError(retrofit2.Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (IOException ignored) {
        }
        return "Error " + response.code();
    }

    public interface ApiCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
