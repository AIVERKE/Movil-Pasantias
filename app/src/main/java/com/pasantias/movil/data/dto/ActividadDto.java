package com.pasantias.movil.data.dto;

import com.google.gson.annotations.SerializedName;

public class ActividadDto {
    public int id_actividad;
    public String titulo;
    public String descripcion;
    public String estado;

    @SerializedName("fecha_fin")
    public String fecha_limite;

    public String fecha_inicio;
    public int porcentaje_avance;
    public int id_pasantia;
    public String pasantia_titulo;
    public String estudiante_nombre;
    public int id_inscripcion;
    public java.util.List<InscripcionDto> asignados;
}
