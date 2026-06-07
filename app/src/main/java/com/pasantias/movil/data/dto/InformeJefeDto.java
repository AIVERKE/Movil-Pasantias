package com.pasantias.movil.data.dto;

import java.util.List;

public class InformeJefeDto {
    public int id; // ID de la inscripción
    public Integer informeId;
    public String iniciales;
    public String estudiante;
    public String pasantia;
    public int bitacorasEvaluadas;
    public int totalBitacoras;
    public int notaSugerida;
    public String estado; // "Emitido" o "Pendiente"
    public String logrosAlcanzados;
    public String contenido; // Apreciación global
    public Integer notaFinal;
    public List<Criterio> criterios;

    public static class Criterio {
        public String nombre;
        public int puntaje;
    }
}
