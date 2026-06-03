package com.pasantias.movil.util;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

public final class UiUtils {

    private UiUtils() {}

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void snack(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show();
    }

    public static void setLoading(
            ProgressBar progress,
            @Nullable View content,
            @Nullable TextView error,
            boolean loading,
            @Nullable String errorMsg
    ) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (content != null) {
            content.setVisibility(loading || errorMsg != null ? View.GONE : View.VISIBLE);
        }
        if (error != null) {
            if (errorMsg != null) {
                error.setText(errorMsg);
                error.setVisibility(View.VISIBLE);
            } else {
                error.setVisibility(View.GONE);
            }
        }
    }
}
