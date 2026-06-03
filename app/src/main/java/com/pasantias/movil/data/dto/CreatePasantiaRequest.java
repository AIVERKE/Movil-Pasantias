package com.pasantias.movil.data.dto;

import java.util.List;

public class CreatePasantiaRequest {
    public String titulo;
    public String descripcion;
    public String fecha_inicio;
    public String fecha_fin;
    public String area;
    public String modalidad;
    public String horario_laboral;
    public Integer cupos_totales;
    public List<String> requisitos;

    public CreatePasantiaRequest() {}

    public CreatePasantiaRequest(String titulo, String descripcion, String fechaInicio, String fechaFin) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha_inicio = fechaInicio;
        this.fecha_fin = fechaFin;
    }
}
