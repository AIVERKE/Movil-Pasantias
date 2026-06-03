package com.pasantias.movil.data.dto;

public class EstudianteActividadItemDto {
    public String tipo;
    public Integer id_actividad;
    public Integer id_tarea;
    public String titulo_actividad;
    public String descripcion_actividad;
    public String fecha_inicio;
    public String fecha_fin;
    public String fecha_asignacion;
    public String estado_actividad;
    public String estado_semaforo;
    public Integer porcentaje_actual;

    public int getIdForDetalle() {
        return id_actividad != null ? id_actividad : (id_tarea != null ? id_tarea : 0);
    }

    public String getTitulo() {
        return titulo_actividad != null ? titulo_actividad : "Actividad";
    }

    public String getEstado() {
        return estado_actividad != null ? estado_actividad : estado_semaforo;
    }

    public String getFechaLimite() {
        return fecha_fin != null ? fecha_fin : fecha_asignacion;
    }
}
