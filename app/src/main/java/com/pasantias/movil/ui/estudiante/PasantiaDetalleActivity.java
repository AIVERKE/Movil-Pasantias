package com.pasantias.movil.ui.estudiante;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.ActividadDto;
import com.pasantias.movil.data.dto.ComentarioDto;
import com.pasantias.movil.data.dto.CreateInscripcionRequest;
import com.pasantias.movil.data.dto.InscripcionDto;
import com.pasantias.movil.data.dto.PasantiaDto;
import com.pasantias.movil.data.dto.ProfileResponse;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.ui.common.DialogHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PasantiaDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_pasantia";

    private int pasantiaId;
    private PasantiaDto pasantiaActual;
    private List<InscripcionDto> inscripciones = new ArrayList<>();
    private int semestreEstudiante = 0;

    // Views
    private ProgressBar progress;
    private View scrollView;
    private TextView textTitulo, textEmpresa, textModalidad, textHorario, textCupos;
    private TextView textDescripcion, textBloqueo;
    private LinearLayout containerRequisitos, containerActividades, containerComentarios;
    private LinearLayout bannerBloqueo;
    private MaterialButton btnPostular, btnRechazar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasantia_detalle);
        pasantiaId = getIntent().getIntExtra(EXTRA_ID, -1);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Bind views
        progress = findViewById(R.id.progress);
        scrollView = findViewById(R.id.scrollView);
        textTitulo = findViewById(R.id.textTitulo);
        textEmpresa = findViewById(R.id.textEmpresa);
        textModalidad = findViewById(R.id.textModalidad);
        textHorario = findViewById(R.id.textHorario);
        textCupos = findViewById(R.id.textCupos);
        textDescripcion = findViewById(R.id.textDescripcion);
        textBloqueo = findViewById(R.id.textBloqueo);
        containerRequisitos = findViewById(R.id.containerRequisitos);
        containerActividades = findViewById(R.id.containerActividades);
        containerComentarios = findViewById(R.id.containerComentarios);
        bannerBloqueo = findViewById(R.id.bannerBloqueo);
        btnPostular = findViewById(R.id.btnPostular);
        btnRechazar = findViewById(R.id.btnRechazar);

        cargarDatos();
    }

    /**
     * Carga en paralelo: pasantía, inscripciones del estudiante y perfil.
     * Cuando los 3 completan, renderiza la UI.
     */
    private void cargarDatos() {
        progress.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);

        int estudianteId = TokenManager.get().getUser().id;
        AtomicInteger pendientes = new AtomicInteger(3);
        AtomicReference<String> errorGlobal = new AtomicReference<>(null);

        Runnable checkYRenderizar = () -> {
            if (pendientes.decrementAndGet() == 0) {
                if (errorGlobal.get() != null) {
                    runOnUiThread(() -> {
                        progress.setVisibility(View.GONE);
                        Toast.makeText(this, errorGlobal.get(), Toast.LENGTH_LONG).show();
                    });
                } else {
                    runOnUiThread(this::renderizarUI);
                }
            }
        };

        // 1. Detalle pasantía
        ApiClient.enqueue(ApiClient.get().api().getPasantia(pasantiaId),
                new ApiClient.ApiCallback<PasantiaDto>() {
                    @Override
                    public void onSuccess(PasantiaDto data) {
                        pasantiaActual = data;
                        checkYRenderizar.run();
                    }

                    @Override
                    public void onError(String message) {
                        errorGlobal.set(message);
                        checkYRenderizar.run();
                    }
                });

        // 2. Inscripciones del estudiante
        ApiClient.enqueue(ApiClient.get().api().getInscripcionesEstudiante(estudianteId),
                new ApiClient.ApiCallback<List<InscripcionDto>>() {
                    @Override
                    public void onSuccess(List<InscripcionDto> data) {
                        inscripciones = data != null ? data : new ArrayList<>();
                        checkYRenderizar.run();
                    }

                    @Override
                    public void onError(String message) {
                        // No bloqueamos si falla, seguimos con lista vacía
                        inscripciones = new ArrayList<>();
                        checkYRenderizar.run();
                    }
                });

        // 3. Perfil (para obtener semestre)
        ApiClient.enqueue(ApiClient.get().api().getProfile(),
                new ApiClient.ApiCallback<ProfileResponse>() {
                    @Override
                    public void onSuccess(ProfileResponse data) {
                        if (data != null && data.estudiante != null) {
                            semestreEstudiante = data.estudiante.semestre;
                        }
                        checkYRenderizar.run();
                    }

                    @Override
                    public void onError(String message) {
                        // Usamos nivel del token si el perfil falla
                        semestreEstudiante = TokenManager.get().getUser().nivel;
                        checkYRenderizar.run();
                    }
                });
    }

    private void renderizarUI() {
        progress.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        if (pasantiaActual == null) return;

        // Datos básicos
        textTitulo.setText(pasantiaActual.titulo);
        textEmpresa.setText(
                pasantiaActual.empresa != null ? pasantiaActual.empresa.nombre
                        : pasantiaActual.empresa_nombre != null ? pasantiaActual.empresa_nombre : ""
        );

        // Tarjetas de resumen
        textModalidad.setText(pasantiaActual.modalidad != null ? pasantiaActual.modalidad : "Presencial");
        textHorario.setText(pasantiaActual.horario_laboral != null ? pasantiaActual.horario_laboral : "Horario de oficina");
        int ocupados = pasantiaActual.cupos_ocupados != null ? pasantiaActual.cupos_ocupados : 0;
        int totales = pasantiaActual.cupos_totales != null ? pasantiaActual.cupos_totales : 0;
        textCupos.setText(ocupados + "/" + totales);

        // Descripción
        textDescripcion.setText(pasantiaActual.descripcion != null ? pasantiaActual.descripcion : "—");

        // Requisitos
        renderizarRequisitos();

        // Actividades
        renderizarActividades();

        // Comentarios de ex pasantes
        renderizarComentarios();

        // Lógica del botón CTA
        configurarCTA();
    }

    private void renderizarRequisitos() {
        containerRequisitos.removeAllViews();
        if (pasantiaActual.requisitos == null || pasantiaActual.requisitos.isEmpty()) {
            agregarTextoItalico(containerRequisitos, "No se especificaron requisitos especiales.");
            return;
        }
        for (String req : pasantiaActual.requisitos) {
            TextView tv = new TextView(this);
            tv.setText("• " + req);
            tv.setTextColor(Color.parseColor("#4A5568"));
            tv.setTextSize(14);
            tv.setPadding(0, 6, 0, 0);
            containerRequisitos.addView(tv);
        }
    }

    private void renderizarActividades() {
        containerActividades.removeAllViews();
        if (pasantiaActual.actividades == null || pasantiaActual.actividades.isEmpty()) {
            agregarTextoItalico(containerActividades, "La empresa aún no publicó actividades para esta convocatoria.");
            return;
        }
        for (ActividadDto act : pasantiaActual.actividades) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(px(12), px(10), px(12), px(10));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, px(6), 0, 0);
            card.setLayoutParams(params);
            card.setBackgroundResource(R.drawable.bg_card);

            TextView tvDesc = new TextView(this);
            tvDesc.setText("✓  " + (act.descripcion != null ? act.descripcion : "Actividad"));
            tvDesc.setTextColor(Color.parseColor("#1A2233"));
            tvDesc.setTextSize(13);
            tvDesc.setTextAppearance(android.R.style.TextAppearance_Small);
            card.addView(tvDesc);

            if (act.fecha_inicio != null) {
                TextView tvFecha = new TextView(this);
                String rango = formatearFechaCorta(act.fecha_inicio);
                if (act.fecha_limite != null) rango += " – " + formatearFechaCorta(act.fecha_limite);
                if (act.estado != null) rango += "  ·  " + act.estado.replace("_", " ");
                tvFecha.setText(rango);
                tvFecha.setTextColor(Color.parseColor("#9AA0A6"));
                tvFecha.setTextSize(11);
                tvFecha.setPadding(0, px(4), 0, 0);
                card.addView(tvFecha);
            }
            containerActividades.addView(card);
        }
    }

    private void renderizarComentarios() {
        containerComentarios.removeAllViews();
        if (pasantiaActual.comentarios == null || pasantiaActual.comentarios.isEmpty()) {
            agregarTextoItalico(containerComentarios, "No hay comentarios de ex pasantes para esta empresa aún.");
            return;
        }
        for (ComentarioDto c : pasantiaActual.comentarios) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(px(14), px(12), px(14), px(12));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, px(8), 0, 0);
            card.setLayoutParams(params);
            card.setBackgroundResource(R.drawable.bg_card);

            // Encabezado: nombre + estrellas
            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);

            TextView tvAutor = new TextView(this);
            tvAutor.setText(c.autor != null ? c.autor : "Ex Pasante");
            tvAutor.setTextColor(Color.parseColor("#1A2233"));
            tvAutor.setTextSize(13);
            tvAutor.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams authorParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            tvAutor.setLayoutParams(authorParams);
            header.addView(tvAutor);

            // Estrellas
            if (c.estrellas != null) {
                TextView tvStars = new TextView(this);
                StringBuilder stars = new StringBuilder();
                for (int i = 1; i <= 5; i++) stars.append(i <= c.estrellas ? "★" : "☆");
                tvStars.setText(stars.toString());
                tvStars.setTextColor(Color.parseColor("#FBBF24"));
                tvStars.setTextSize(14);
                header.addView(tvStars);
            }
            card.addView(header);

            // Texto del comentario
            TextView tvTexto = new TextView(this);
            tvTexto.setText(c.texto != null ? c.texto : "");
            tvTexto.setTextColor(Color.parseColor("#4A5568"));
            tvTexto.setTextSize(13);
            tvTexto.setPadding(0, px(6), 0, 0);
            tvTexto.setLineSpacing(0, 1.4f);
            card.addView(tvTexto);

            containerComentarios.addView(card);
        }
    }

    /**
     * Lógica condicional del botón CTA — espejo exacto de la web.
     */
    private void configurarCTA() {
        // Inscripción de esta pasantía específica
        InscripcionDto inscActual = null;
        for (InscripcionDto ins : inscripciones) {
            if (ins.id_pasantia == pasantiaId) {
                inscActual = ins;
                break;
            }
        }

        // Inscripción bloqueante (cualquier pasantía activa)
        InscripcionDto bloqueante = null;
        for (InscripcionDto ins : inscripciones) {
            if (esInscripcionActiva(ins)) {
                bloqueante = ins;
                break;
            }
        }

        final InscripcionDto inscFinal = inscActual;
        final InscripcionDto bloqueanteFinal = bloqueante;

        btnRechazar.setVisibility(View.GONE);
        bannerBloqueo.setVisibility(View.GONE);

        // CASO: tiene inscripción en esta pasantía
        if (inscActual != null) {
            switch (inscActual.estado) {
                case "invitado":
                    // Mostrar dos botones
                    btnPostular.setText("Aceptar invitación");
                    btnPostular.setEnabled(true);
                    btnPostular.setBackgroundColor(getColor(R.color.primary));
                    btnRechazar.setVisibility(View.VISIBLE);

                    btnPostular.setOnClickListener(v ->
                            confirmarAccion("¿Aceptar invitación?",
                                    "Quedarás inscrito oficialmente en esta pasantía.",
                                    () -> aceptarInvitacion(inscFinal.getId())));

                    btnRechazar.setOnClickListener(v ->
                            confirmarAccion("¿Rechazar invitación?",
                                    "Rechazarás la invitación de la empresa para esta vacante.",
                                    () -> cancelarPostulacion(inscFinal.getId())));
                    break;

                case "pendiente":
                    btnPostular.setText("Cancelar Postulación");
                    btnPostular.setEnabled(true);
                    btnPostular.setBackgroundColor(Color.parseColor("#FEE2E2"));
                    btnPostular.setTextColor(Color.parseColor("#DC2626"));
                    btnPostular.setOnClickListener(v ->
                            confirmarAccion("¿Cancelar postulación?",
                                    "Esta acción retirará tu candidatura para esta pasantía.",
                                    () -> cancelarPostulacion(inscFinal.getId())));
                    break;

                case "aprobada":
                case "completada":
                    btnPostular.setText("Inscripción aceptada");
                    btnPostular.setEnabled(false);
                    btnPostular.setBackgroundColor(Color.parseColor("#D1FAE5"));
                    btnPostular.setTextColor(Color.parseColor("#065F46"));
                    break;

                default:
                    btnPostular.setText("Postulación " + inscActual.estado);
                    btnPostular.setEnabled(false);
                    btnPostular.setBackgroundColor(Color.parseColor("#F3F4F6"));
                    btnPostular.setTextColor(Color.parseColor("#6B7280"));
                    break;
            }
            return;
        }

        // CASO: tiene inscripción bloqueante en OTRA pasantía
        if (bloqueante != null) {
            boolean esAprobada = "aprobada".equals(bloqueante.estado);
            btnPostular.setText(esAprobada ? "Pasantía activa en curso" : "Ya tienes una postulación activa");
            btnPostular.setEnabled(false);
            btnPostular.setBackgroundColor(Color.parseColor("#F3F4F6"));
            btnPostular.setTextColor(Color.parseColor("#6B7280"));

            bannerBloqueo.setVisibility(View.VISIBLE);
            textBloqueo.setText(esAprobada
                    ? "Estás en una pasantía activa. Debes finalizarla antes de postular a otra."
                    : "Ya tenés una postulación en revisión. Solo podés postular a una pasantía a la vez.");
            return;
        }

        // CASO: semestre insuficiente
        if (semestreEstudiante < 8) {
            btnPostular.setText("Semestre Insuficiente");
            btnPostular.setEnabled(false);
            btnPostular.setBackgroundColor(Color.parseColor("#E5E7EB"));
            btnPostular.setTextColor(Color.parseColor("#9CA3AF"));
            return;
        }

        // CASO: cupos agotados
        int ocupados = pasantiaActual.cupos_ocupados != null ? pasantiaActual.cupos_ocupados : 0;
        int totales = pasantiaActual.cupos_totales != null ? pasantiaActual.cupos_totales : 0;
        if (totales > 0 && ocupados >= totales) {
            btnPostular.setText("Cupos Agotados");
            btnPostular.setEnabled(false);
            btnPostular.setBackgroundColor(Color.parseColor("#E5E7EB"));
            btnPostular.setTextColor(Color.parseColor("#9CA3AF"));
            return;
        }

        // CASO: disponible para postular
        btnPostular.setText("Postularme");
        btnPostular.setEnabled(true);
        btnPostular.setBackgroundColor(getColor(R.color.primary));
        btnPostular.setTextColor(Color.WHITE);
        btnPostular.setOnClickListener(v ->
                confirmarAccion("¿Confirmar postulación?",
                        "Tu perfil será enviado a la empresa para esta vacante.",
                        this::postularse));
    }

    private boolean esInscripcionActiva(InscripcionDto ins) {
        if (ins == null) return false;
        String estado = ins.estado;
        if ("rechazada".equals(estado) || "rechazada_estudiante".equals(estado) || "completada".equals(estado)) {
            return false;
        }
        // aprobada no bloquea si es de esta misma pasantía (se maneja arriba), pero si es de otra sí bloquea
        return true;
    }

    private void postularse() {
        int estudianteId = TokenManager.get().getUser().id;
        btnPostular.setEnabled(false);
        btnPostular.setText("Enviando...");
        ApiClient.enqueue(
                ApiClient.get().api().crearInscripcion(new CreateInscripcionRequest(estudianteId, pasantiaId)),
                new ApiClient.ApiCallback<InscripcionDto>() {
                    @Override
                    public void onSuccess(InscripcionDto data) {
                        DialogHelper.showSuccess(PasantiaDetalleActivity.this, "¡Postulación enviada exitosamente!", () -> finish());
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PasantiaDetalleActivity.this, message, Toast.LENGTH_LONG).show();
                        btnPostular.setEnabled(true);
                        btnPostular.setText("Postularme");
                    }
                });
    }

    private void cancelarPostulacion(int inscripcionId) {
        btnPostular.setEnabled(false);
        btnRechazar.setEnabled(false);
        ApiClient.enqueue(
                ApiClient.get().api().cancelarInscripcion(inscripcionId),
                new ApiClient.ApiCallback<InscripcionDto>() {
                    @Override
                    public void onSuccess(InscripcionDto data) {
                        DialogHelper.showSuccess(PasantiaDetalleActivity.this, "Postulación cancelada.", () -> finish());
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PasantiaDetalleActivity.this, message, Toast.LENGTH_LONG).show();
                        btnPostular.setEnabled(true);
                        btnRechazar.setEnabled(true);
                    }
                });
    }

    private void aceptarInvitacion(int inscripcionId) {
        btnPostular.setEnabled(false);
        btnRechazar.setEnabled(false);
        ApiClient.enqueue(
                ApiClient.get().api().aceptarInvitacion(inscripcionId),
                new ApiClient.ApiCallback<InscripcionDto>() {
                    @Override
                    public void onSuccess(InscripcionDto data) {
                        DialogHelper.showSuccess(PasantiaDetalleActivity.this, "Invitación aceptada. ¡Bienvenido a la pasantía!", () -> finish());
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(PasantiaDetalleActivity.this, message, Toast.LENGTH_LONG).show();
                        btnPostular.setEnabled(true);
                        btnRechazar.setEnabled(true);
                    }
                });
    }

    private void confirmarAccion(String titulo, String mensaje, Runnable accion) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Confirmar", (d, w) -> accion.run())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // ── Utilidades ──────────────────────────────────────────────────────────────

    private void agregarTextoItalico(LinearLayout container, String texto) {
        TextView tv = new TextView(this);
        tv.setText(texto);
        tv.setTextColor(Color.parseColor("#9AA0A6"));
        tv.setTextSize(13);
        tv.setTypeface(null, android.graphics.Typeface.ITALIC);
        tv.setPadding(0, px(4), 0, 0);
        container.addView(tv);
    }

    private String formatearFechaCorta(String iso) {
        if (iso == null || iso.isEmpty()) return "—";
        try {
            // ISO: "2025-03-01T00:00:00.000Z" → "01/03/2025"
            String[] parts = iso.split("T")[0].split("-");
            if (parts.length == 3) return parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception ignored) {}
        return iso;
    }

    private int px(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
