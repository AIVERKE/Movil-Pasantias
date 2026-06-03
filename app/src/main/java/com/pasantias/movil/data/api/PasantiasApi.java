package com.pasantias.movil.data.api;

import com.pasantias.movil.data.dto.ActividadDto;
import com.pasantias.movil.data.dto.BitacoraEstudianteRequest;
import com.pasantias.movil.data.dto.ComentarioDto;
import com.pasantias.movil.data.dto.ComentarioRequest;
import com.pasantias.movil.data.dto.CreateInscripcionRequest;
import com.pasantias.movil.data.dto.CreatePasantiaRequest;
import com.pasantias.movil.data.dto.EmpresaDto;
import com.pasantias.movil.data.dto.EstudianteActividadesResponse;
import com.pasantias.movil.data.dto.EstudianteDashboardDto;
import com.pasantias.movil.data.dto.GerenteDashboardDto;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.data.dto.JefeActividadDto;
import com.pasantias.movil.data.dto.JefeDashboardDto;
import com.pasantias.movil.data.dto.LoginRequest;
import com.pasantias.movil.data.dto.LoginResponse;
import com.pasantias.movil.data.dto.MessageResponse;
import com.pasantias.movil.data.dto.NotificacionConteoDto;
import com.pasantias.movil.data.dto.NotificacionDto;
import com.pasantias.movil.data.dto.PasantiaDto;
import com.pasantias.movil.data.dto.UpdateEstadoActividadRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PasantiasApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/logout")
    Call<MessageResponse> logout();

    @GET("auth/gerente/dashboard")
    Call<GerenteDashboardDto> gerenteDashboard();

    @GET("auth/gerente/empresa")
    Call<EmpresaDto> getEmpresaGerente();

    @PATCH("auth/gerente/empresa")
    Call<EmpresaDto> updateEmpresaGerente(@Body Map<String, Object> body);

    @Multipart
    @POST("auth/gerente/empresa/logo")
    Call<EmpresaDto> uploadLogoEmpresa(@Part MultipartBody.Part logo);

    @GET("pasantias/gerente")
    Call<List<PasantiaDto>> listPasantiasGerente();

    @POST("pasantias/gerente")
    Call<PasantiaDto> createPasantiaGerente(@Body CreatePasantiaRequest body);

    @PATCH("pasantias/{id}")
    Call<PasantiaDto> updatePasantia(@Path("id") int id, @Body CreatePasantiaRequest body);

    @DELETE("pasantias/{id}")
    Call<MessageResponse> deletePasantia(@Path("id") int id);

    @PATCH("pasantias/{id}/estado")
    Call<PasantiaDto> updateEstadoPasantia(@Path("id") int id, @Body Map<String, String> body);

    @GET("auth/jefe/dashboard")
    Call<JefeDashboardDto> jefeDashboard();

    @GET("auth/jefe/pasantias")
    Call<List<PasantiaDto>> listPasantiasJefe();

    @GET("auth/jefe/inscripciones")
    Call<List<InscripcionDto>> listInscripcionesJefe();

    @PATCH("auth/jefe/inscripciones/{id}/aprobar")
    Call<InscripcionDto> aprobarInscripcionJefe(@Path("id") int id);

    @PATCH("auth/jefe/inscripciones/{id}/rechazar")
    Call<InscripcionDto> rechazarInscripcionJefe(@Path("id") int id);

    @GET("auth/jefe/pasantes")
    Call<List<InscripcionDto>> listPasantesJefe();

    @GET("actividades/jefe/actividades")
    Call<List<JefeActividadDto>> listActividadesJefe();

    @GET("actividades/{id}/comentarios")
    Call<List<ComentarioDto>> listComentariosActividad(@Path("id") int id);

    @POST("actividades/{id}/comentarios")
    Call<ComentarioDto> crearComentarioActividad(@Path("id") int id, @Body ComentarioRequest body);

    @GET("pasantias")
    Call<List<PasantiaDto>> listPasantias(@Query("estado") String estado);

    @GET("pasantias/{id}")
    Call<PasantiaDto> getPasantia(@Path("id") int id);

    @POST("inscripciones")
    Call<InscripcionDto> crearInscripcion(@Body CreateInscripcionRequest body);

    @GET("actividades/estudiante/{id}")
    Call<EstudianteActividadesResponse> actividadesEstudiante(@Path("id") int estudianteId);

    @PATCH("actividades/estudiante/{id}/estado")
    Call<ActividadDto> actualizarEstadoActividadEstudiante(
            @Path("id") int actividadId,
            @Body UpdateEstadoActividadRequest body
    );

    @POST("bitacoras/mi")
    Call<Object> crearBitacoraEstudiante(@Body BitacoraEstudianteRequest body);

    @GET("dashboard/estudiante/{id}")
    Call<EstudianteDashboardDto> dashboardEstudiante(@Path("id") int id);

    @GET("notificaciones/mias")
    Call<List<NotificacionDto>> notificacionesMias();

    @GET("notificaciones/conteo")
    Call<NotificacionConteoDto> notificacionesConteo();

    @PATCH("notificaciones/{id}/leida")
    Call<NotificacionDto> marcarNotificacionLeida(@Path("id") int id);
}
