package com.pasantias.movil.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.gson.Gson;
import com.pasantias.movil.data.dto.UserDto;

public final class TokenManager {

    private static final String PREFS = "pasantias_secure_prefs";
    private static final String KEY_TOKEN = "access_token";
    private static final String KEY_USER = "auth_user";
    private static final String KEY_LAST_NOTIF_COUNT = "last_notif_count";

    private static TokenManager instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private TokenManager(Context context) {
        prefs = createPrefs(context);
    }

    private static SharedPreferences createPrefs(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        }
    }

    public static void init(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
    }

    public static TokenManager get() {
        return instance;
    }

    public void saveSession(String token, UserDto user) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER, gson.toJson(user))
                .apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    @Nullable
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    @Nullable
    public UserDto getUser() {
        String json = prefs.getString(KEY_USER, null);
        if (json == null) return null;
        return gson.fromJson(json, UserDto.class);
    }

    public boolean isLoggedIn() {
        return getToken() != null && getUser() != null;
    }

    public int getLastNotificationCount() {
        return prefs.getInt(KEY_LAST_NOTIF_COUNT, 0);
    }

    public void setLastNotificationCount(int count) {
        prefs.edit().putInt(KEY_LAST_NOTIF_COUNT, count).apply();
    }
}
