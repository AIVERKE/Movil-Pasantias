package com.pasantias.movil.data.dto;

import com.google.gson.annotations.SerializedName;

public class NotificacionDto {
    public int id_notificacion;
    public String titulo;
    public String mensaje;
    public String tipo;
    public boolean leido;
    @SerializedName("leida")
    public boolean leida;
    public String fecha_creacion;
    public String created_at;

    public boolean isLeida() {
        return leido || leida;
    }
}
