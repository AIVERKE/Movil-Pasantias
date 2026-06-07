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
import com.pasantias.movil.data.dto.JefeDto;
import com.pasantias.movil.data.dto.LoginRequest;
import com.pasantias.movil.data.dto.LoginResponse;
import com.pasantias.movil.data.dto.MessageResponse;
import com.pasantias.movil.data.dto.NotificacionConteoDto;
import com.pasantias.movil.data.dto.NotificacionDto;
import com.pasantias.movil.data.dto.PasantiaDto;
import com.pasantias.movil.data.dto.ProfileResponse;
import com.pasantias.movil.data.dto.UpdateEstadoActividadRequest;
import com.pasantias.movil.data.dto.BitacoraJefeDto;
import com.pasantias.movil.data.dto.InformeJefeDto;
import com.pasantias.movil.data.dto.InscripcionDetalleJefeDto;
import com.pasantias.movil.data.dto.EstudianteCatalogoDto;
import com.pasantias.movil.data.dto.JefeTareaDto;

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
import retrofit2.http.HTTP;

public interface PasantiasApi {

    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/logout")
    Call<MessageResponse> logout();

    @GET("auth/profile")
    Call<ProfileResponse> getProfile();

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

    @GET("actividades/pasantia-actividad/{id}/comentarios")
    Call<List<ComentarioDto>> listComentariosActividadPasantia(@Path("id") int id);

    @POST("actividades/pasantia-actividad/{id}/comentarios")
    Call<ComentarioDto> crearComentarioActividadPasantia(@Path("id") int id, @Body ComentarioRequest body);

    @GET("pasantias")
    Call<List<PasantiaDto>> listPasantias(@Query("estado") String estado);

    @GET("pasantias/{id}")
    Call<PasantiaDto> getPasantia(@Path("id") int id);

    @GET("inscripciones/estudiante/{id}")
    Call<List<InscripcionDto>> getInscripcionesEstudiante(@Path("id") int estudianteId);

    @POST("inscripciones")
    Call<InscripcionDto> crearInscripcion(@Body CreateInscripcionRequest body);

    @PATCH("inscripciones/{id}/cancelar")
    Call<InscripcionDto> cancelarInscripcion(@Path("id") int id);

    @PATCH("inscripciones/{id}/aceptar-invitacion")
    Call<InscripcionDto> aceptarInvitacion(@Path("id") int id);

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

    @GET("pasantias/jefes/by-empresa/{id}")
    Call<List<JefeDto>> getJefesByEmpresa(@Path("id") int id);

    @PATCH("pasantias/{id}/jefe")
    Call<PasantiaDto> assignJefe(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("pasantias/{pasantiaId}/jefe")
    Call<MessageResponse> removeJefe(@Path("pasantiaId") int pasantiaId, @Query("jefe_id") int jefeId);

    @GET("actividades/pasantia/{pasantiaId}")
    Call<List<ActividadDto>> getActividadesPasantia(@Path("pasantiaId") int pasantiaId);

    @POST("actividades")
    Call<ActividadDto> createActividad(@Body Map<String, Object> body);

    @PATCH("actividades/{id}")
    Call<ActividadDto> updateActividad(@Path("id") int id, @Body Map<String, Object> body);

    @DELETE("actividades/{id}")
    Call<MessageResponse> deleteActividad(@Path("id") int id);

    @POST("auth/jefe/invitaciones")
    Call<InscripcionDto> invitarEstudiante(@Body Map<String, Object> body);

    @GET("estudiantes/catalogo")
    Call<List<EstudianteCatalogoDto>> getEstudiantesCatalogo();

    @GET("auth/jefe/inscripciones/{id}")
    Call<InscripcionDetalleJefeDto> getInscripcionDetalleJefe(@Path("id") int id);

    @GET("auth/jefe/bitacoras")
    Call<List<BitacoraJefeDto>> getJefeBitacoras(
            @Query("inscripcionId") Integer inscripcionId,
            @Query("actividadId") Integer actividadId
    );

    @POST("auth/jefe/bitacoras/{id}/calificar")
    Call<MessageResponse> calificarBitacora(@Path("id") int id, @Body Map<String, Object> body);

    @GET("auth/jefe/informes")
    Call<List<InformeJefeDto>> getJefeInformes();

    @POST("auth/jefe/informes/{id}/emitir")
    Call<MessageResponse> emitirInformeFinal(@Path("id") int id, @Body Map<String, Object> body);

    @POST("auth/jefe/pasantes/{id}/baja")
    Call<MessageResponse> darDeBajaPasante(@Path("id") int id, @Body Map<String, Object> body);

    @PATCH("auth/jefe/pasantes/{id}/estado")
    Call<MessageResponse> cambiarEstadoPasante(@Path("id") int id, @Body Map<String, Object> body);

    @GET("actividades/jefe")
    Call<List<JefeTareaDto>> listTareasJefe();

    @POST("actividades/jefe")
    Call<JefeActividadDto> crearTareaJefe(@Body Map<String, Object> body);

    @PATCH("actividades/jefe/actividades/{id}/estado")
    Call<ActividadDto> cambiarEstadoActividadJefe(@Path("id") int id, @Body Map<String, Object> body);

    @POST("actividades/jefe/actividades/{id}/asignar")
    Call<ActividadDto> asignarPasanteActividad(@Path("id") int id, @Body Map<String, Object> body);

    @HTTP(method = "DELETE", path = "actividades/jefe/actividades/{id}/desasignar", hasBody = true)
    Call<ActividadDto> desasignarPasanteActividad(@Path("id") int id, @Body Map<String, Object> body);
}

