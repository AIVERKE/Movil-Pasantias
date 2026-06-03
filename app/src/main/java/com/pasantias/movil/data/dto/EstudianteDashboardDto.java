package com.pasantias.movil.data.dto;

public class EstudianteDashboardDto {
    public boolean pasantia_activa;
    public boolean tiene_postulaciones_pendientes;
    public String titulo_pasantia;
    public String empresa_nombre;
    public String estado_pasantia;
    public TareasCompletadas tareas_completadas;
    public Integer evaluacion_promedio;

    public static class TareasCompletadas {
        public int completadas;
        public int totales;
    }
}
