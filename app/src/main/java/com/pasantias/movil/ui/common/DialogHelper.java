package com.pasantias.movil.ui.common;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

public class DialogHelper {

    public interface OnConfirmListener {
        void onConfirm();
    }

    public static void showSuccess(Context context, String mensaje) {
        showSuccess(context, "Éxito", mensaje, null);
    }

    public static void showSuccess(Context context, String mensaje, OnConfirmListener listener) {
        showSuccess(context, "Éxito", mensaje, listener);
    }

    public static void showSuccess(Context context, String titulo, String mensaje, OnConfirmListener listener) {
        if (context == null) return;
        new AlertDialog.Builder(context)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    if (listener != null) {
                        listener.onConfirm();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
