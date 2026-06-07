package com.pasantias.movil.ui.estudiante;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.ActividadDto;
import com.pasantias.movil.data.dto.BitacoraEstudianteRequest;
import com.pasantias.movil.data.dto.UpdateEstadoActividadRequest;
import com.pasantias.movil.ui.common.DialogHelper;

public class ActividadDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "id_actividad";
    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_ESTADO = "estado";
    public static final String EXTRA_DESCRIPCION = "descripcion";
    public static final String EXTRA_FECHA = "fecha";
    public static final String EXTRA_TIPO = "tipo";
    public static final String EXTRA_NOTA = "nota";

    private int actividadId;
    private String estadoActual;
    private String tipoActividad; // "actividad" | "tarea"

    // Views
    private TextView textTitulo, textEstado, textFecha, textDescripcion, textNota;
    private LinearLayout sectionNota, sectionStepper, sectionBitacora;
    private TextView stepCirculo1, stepCirculo2, stepCirculo3, stepCirculo4;
    private MaterialButton btnIniciar, btnFinalizar, btnGuardarBitacora, btnComentarios;
    private TextInputEditText inputBitacora, inputPorcentaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad_detalle);

        actividadId = getIntent().getIntExtra(EXTRA_ID, -1);
        tipoActividad = getIntent().getStringExtra(EXTRA_TIPO);
        estadoActual = getIntent().getStringExtra(EXTRA_ESTADO);
        if (tipoActividad == null) tipoActividad = "actividad";

        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Bind
        textTitulo = findViewById(R.id.textTitulo);
        textEstado = findViewById(R.id.textEstado);
        textFecha = findViewById(R.id.textFecha);
        textDescripcion = findViewById(R.id.textDescripcion);
        textNota = findViewById(R.id.textNota);
        sectionNota = findViewById(R.id.sectionNota);
        sectionStepper = findViewById(R.id.sectionStepper);
        sectionBitacora = findViewById(R.id.sectionBitacora);
        stepCirculo1 = findViewById(R.id.stepCirculo1);
        stepCirculo2 = findViewById(R.id.stepCirculo2);
        stepCirculo3 = findViewById(R.id.stepCirculo3);
        stepCirculo4 = findViewById(R.id.stepCirculo4);
        btnIniciar = findViewById(R.id.btnIniciar);
        btnFinalizar = findViewById(R.id.btnFinalizar);
        btnGuardarBitacora = findViewById(R.id.btnGuardarBitacora);
        btnComentarios = findViewById(R.id.btnComentarios);
        inputBitacora = findViewById(R.id.inputBitacora);
        inputPorcentaje = findViewById(R.id.inputPorcentaje);

        btnComentarios.setOnClickListener(v -> {
            android.content.Intent i = new android.content.Intent(this, com.pasantias.movil.ui.jefe.ActividadComentariosActivity.class);
            i.putExtra(com.pasantias.movil.ui.jefe.ActividadComentariosActivity.EXTRA_ACTIVIDAD_ID, actividadId);
            i.putExtra(com.pasantias.movil.ui.jefe.ActividadComentariosActivity.EXTRA_TITULO, textTitulo.getText().toString());
            i.putExtra(com.pasantias.movil.ui.jefe.ActividadComentariosActivity.EXTRA_TIPO, tipoActividad);
            startActivity(i);
        });

        poblarDatos();
        configurarUI();
    }

    private void poblarDatos() {
        String titulo = getIntent().getStringExtra(EXTRA_TITULO);
        String descripcion = getIntent().getStringExtra(EXTRA_DESCRIPCION);
        String fecha = getIntent().getStringExtra(EXTRA_FECHA);
        String nota = getIntent().getStringExtra(EXTRA_NOTA);

        setTitle(titulo);
        textTitulo.setText(titulo != null ? titulo : "Actividad");
        textDescripcion.setText(descripcion != null ? descripcion : "Sin descripción.");
        textFecha.setText(fecha != null ? "Fecha límite: " + fecha : "");

        // Estado chip
        textEstado.setText(formatEstado(estadoActual));
        colorearChipEstado(estadoActual);

        // Nota (solo si viene y es una tarea)
        if (nota != null && !nota.isEmpty()) {
            textNota.setText(nota + "/100");
            sectionNota.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configura la UI según si es 'tarea' o 'actividad'.
     * - Tarea: solo muestra info + nota si tiene. Sin formulario de bitácora ni botones de estado.
     * - Actividad: muestra stepper + botones de cambio de estado + formulario de bitácora (si no está cerrada/finalizada).
     */
    private void configurarUI() {
        if ("tarea".equals(tipoActividad)) {
            // Las tareas no tienen stepper ni bitácora
            sectionStepper.setVisibility(View.GONE);
            sectionBitacora.setVisibility(View.GONE);
            return;
        }

        // Es una actividad del cronograma de pasantía
        sectionStepper.setVisibility(View.VISIBLE);
        actualizarStepper(estadoActual);

        boolean puedeEditar = !"cerrada".equals(estadoActual) && !"finalizada".equals(estadoActual);

        if ("pendiente".equals(estadoActual)) {
            btnIniciar.setVisibility(View.VISIBLE);
            btnFinalizar.setVisibility(View.GONE);
            btnIniciar.setOnClickListener(v -> cambiarEstado("en_desarrollo"));
        } else if ("en_desarrollo".equals(estadoActual)) {
            btnIniciar.setVisibility(View.GONE);
            btnFinalizar.setVisibility(View.VISIBLE);
            btnFinalizar.setOnClickListener(v -> cambiarEstado("finalizada"));
        } else {
            btnIniciar.setVisibility(View.GONE);
            btnFinalizar.setVisibility(View.GONE);
        }

        // Bitácora solo si la actividad está activa (no cerrada ni finalizada)
        sectionBitacora.setVisibility(puedeEditar ? View.VISIBLE : View.GONE);

        btnGuardarBitacora.setOnClickListener(v -> guardarBitacora());
    }

    private void cambiarEstado(String nuevoEstado) {
        btnIniciar.setEnabled(false);
        btnFinalizar.setEnabled(false);
        btnIniciar.setText("Procesando...");
        btnFinalizar.setText("Procesando...");

        ApiClient.enqueue(
                ApiClient.get().api().actualizarEstadoActividadEstudiante(
                        actividadId, new UpdateEstadoActividadRequest(nuevoEstado)),
                new ApiClient.ApiCallback<ActividadDto>() {
                    @Override
                    public void onSuccess(ActividadDto data) {
                        estadoActual = nuevoEstado;
                        textEstado.setText(formatEstado(nuevoEstado));
                        colorearChipEstado(nuevoEstado);
                        actualizarStepper(nuevoEstado);
                        configurarBotonesEstadoTrasActualizar(nuevoEstado);
                        String msg = "en_desarrollo".equals(nuevoEstado)
                                ? "Actividad iniciada" : "Actividad marcada como finalizada";
                        DialogHelper.showSuccess(ActividadDetalleActivity.this, msg, () -> {});
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ActividadDetalleActivity.this, message, Toast.LENGTH_LONG).show();
                        btnIniciar.setEnabled(true);
                        btnFinalizar.setEnabled(true);
                        btnIniciar.setText("Iniciar actividad");
                        btnFinalizar.setText("Marcar como finalizada");
                    }
                });
    }

    private void configurarBotonesEstadoTrasActualizar(String nuevoEstado) {
        if ("en_desarrollo".equals(nuevoEstado)) {
            btnIniciar.setVisibility(View.GONE);
            btnFinalizar.setVisibility(View.VISIBLE);
            btnFinalizar.setEnabled(true);
            btnFinalizar.setText("Marcar como finalizada");
            btnFinalizar.setOnClickListener(v -> cambiarEstado("finalizada"));
            sectionBitacora.setVisibility(View.VISIBLE);
        } else if ("finalizada".equals(nuevoEstado)) {
            btnIniciar.setVisibility(View.GONE);
            btnFinalizar.setVisibility(View.GONE);
            sectionBitacora.setVisibility(View.GONE);
        }
    }

    private void guardarBitacora() {
        String contenido = inputBitacora.getText() != null
                ? inputBitacora.getText().toString().trim() : "";
        int pct = 0;
        try {
            String pctStr = inputPorcentaje.getText() != null
                    ? inputPorcentaje.getText().toString() : "0";
            pct = Integer.parseInt(pctStr.isEmpty() ? "0" : pctStr);
        } catch (NumberFormatException ignored) {}

        if (contenido.length() < 10) {
            Toast.makeText(this, "Escribí al menos 10 caracteres describiendo el avance.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnGuardarBitacora.setEnabled(false);
        btnGuardarBitacora.setText("Guardando...");

        ApiClient.enqueue(
                ApiClient.get().api().crearBitacoraEstudiante(
                        new BitacoraEstudianteRequest(actividadId, contenido, pct)),
                new ApiClient.ApiCallback<Object>() {
                    @Override
                    public void onSuccess(Object data) {
                        DialogHelper.showSuccess(ActividadDetalleActivity.this, "Avance registrado correctamente", () -> {
                            inputBitacora.setText("");
                            inputPorcentaje.setText("");
                            btnGuardarBitacora.setEnabled(true);
                            btnGuardarBitacora.setText("Guardar avance");
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(ActividadDetalleActivity.this, message, Toast.LENGTH_LONG).show();
                        btnGuardarBitacora.setEnabled(true);
                        btnGuardarBitacora.setText("Guardar avance");
                    }
                });
    }

    // ── Stepper visual ────────────────────────────────────────────────────────

    private void actualizarStepper(String estado) {
        int[] indices = {0, 1, 2, 3};
        String[] pasos = {"pendiente", "en_desarrollo", "finalizada", "cerrada"};
        TextView[] circles = {stepCirculo1, stepCirculo2, stepCirculo3, stepCirculo4};
        String[] labels = {"Pendiente", "En desarrollo", "Finalizada", "Cerrada"};

        int indiceActual = 0;
        for (int i = 0; i < pasos.length; i++) {
            if (pasos[i].equals(estado)) { indiceActual = i; break; }
        }

        for (int i = 0; i < circles.length; i++) {
            if (i < indiceActual) {
                // Completado
                circles[i].setText("✓");
                circles[i].setBackgroundResource(R.drawable.bg_badge_estado);
                circles[i].setTextColor(Color.WHITE);
            } else if (i == indiceActual) {
                // Actual
                circles[i].setBackgroundResource(R.drawable.bg_stat_card);
                circles[i].setTextColor(getColor(R.color.primary));
                circles[i].setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                // Pendiente
                circles[i].setBackgroundResource(R.drawable.bg_stat_card);
                circles[i].setTextColor(Color.parseColor("#9AA0A6"));
            }
        }
    }

    // ── Formateo visual ───────────────────────────────────────────────────────

    private String formatEstado(String estado) {
        if (estado == null) return "Pendiente";
        switch (estado) {
            case "pendiente": return "Pendiente";
            case "en_desarrollo": return "En desarrollo";
            case "finalizada": return "Finalizada";
            case "cerrada": return "Cerrada";
            case "Completada": return "Completada";
            case "En_curso": return "En curso";
            case "No_completada": return "No completada";
            default: return estado.replace("_", " ");
        }
    }

    private void colorearChipEstado(String estado) {
        if (estado == null) return;
        int bg, fg;
        switch (estado) {
            case "cerrada":
            case "Completada":
                bg = Color.parseColor("#D1FAE5"); fg = Color.parseColor("#065F46"); break;
            case "en_desarrollo":
            case "En_curso":
                bg = Color.parseColor("#FEF3C7"); fg = Color.parseColor("#92400E"); break;
            case "finalizada":
                bg = Color.parseColor("#EDE9FE"); fg = Color.parseColor("#5B21B6"); break;
            case "No_completada":
                bg = Color.parseColor("#FEE2E2"); fg = Color.parseColor("#991B1B"); break;
            default:
                bg = Color.parseColor("#F3F4F6"); fg = Color.parseColor("#374151"); break;
        }
        textEstado.setBackgroundColor(bg);
        textEstado.setTextColor(fg);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
