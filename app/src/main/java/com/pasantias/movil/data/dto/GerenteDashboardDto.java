package com.pasantias.movil.data.dto;

import java.util.List;

public class GerenteDashboardDto {
    public int pasantesActivos;
    public int pasantiasPublicadas;
    public int equipo;
    public List<PasantiaResumenDto> pasantiasRecientes;

    public static class PasantiaResumenDto {
        public int id;
        public String titulo;
        public String jefe;
        public int pasantes;
        public int postulantes;
        public String estado;
    }
}
