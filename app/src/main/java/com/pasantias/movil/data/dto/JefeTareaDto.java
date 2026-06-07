package com.pasantias.movil.data.dto;

public class JefeTareaDto {
    public int id_tarea;
    public String titulo_actividad;
    public String descripcion_actividad;
    public String estado_semaforo;
    public InscripcionDto inscripcion;

    public String getTitulo() {
        if (titulo_actividad == null) return "Tarea";
        return titulo_actividad.length() > 60 ? titulo_actividad.substring(0, 57) + "…" : titulo_actividad;
    }
}
