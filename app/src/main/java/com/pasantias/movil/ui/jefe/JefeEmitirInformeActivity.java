package com.pasantias.movil.ui.jefe;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pasantias.movil.R;
import com.pasantias.movil.ui.common.DialogHelper;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.InformeJefeDto;
import com.pasantias.movil.data.dto.MessageResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JefeEmitirInformeActivity extends AppCompatActivity {
    public static final String EXTRA_INSC_ID = "inscripcion_id";
    public static final String EXTRA_INSCRIPCION_ID = "inscripcion_id"; // Para retrocompatibilidad

    private int inscripcionId;

    private ProgressBar progress;
    private TextView textError;
    private NestedScrollView contentScroll;

    private TextView textEstudiante;
    private TextView textPasantia;
    private TextView textBitacoras;
    private TextView textNotaSugerida;

    private TextInputLayout layoutConocimiento;
    private TextInputEditText inputConocimiento;
    private TextInputLayout layoutResponsabilidad;
    private TextInputEditText inputResponsabilidad;
    private TextInputLayout layoutTrabajoEquipo;
    private TextInputEditText inputTrabajoEquipo;
    private TextInputLayout layoutIniciativa;
    private TextInputEditText inputIniciativa;

    private TextInputLayout layoutApreciacion;
    private TextInputEditText inputApreciacion;
    private TextInputLayout layoutLogros;
    private TextInputEditText inputLogros;

    private MaterialCardView cardConfirmar;
    private CheckBox checkConfirmar;
    private MaterialButton btnEmitir;

    private MaterialCardView cardNotaFinal;
    private TextView textNotaFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jefe_emitir_informe);

        inscripcionId = getIntent().getIntExtra(EXTRA_INSC_ID, 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Emitir Informe Final");
        }

        progress = findViewById(R.id.progress);
        textError = findViewById(R.id.textError);
        contentScroll = findViewById(R.id.contentScroll);

        textEstudiante = findViewById(R.id.textEstudiante);
        textPasantia = findViewById(R.id.textPasantia);
        textBitacoras = findViewById(R.id.textBitacoras);
        textNotaSugerida = findViewById(R.id.textNotaSugerida);

        layoutConocimiento = findViewById(R.id.layoutConocimiento);
        inputConocimiento = findViewById(R.id.inputConocimiento);
        layoutResponsabilidad = findViewById(R.id.layoutResponsabilidad);
        inputResponsabilidad = findViewById(R.id.inputResponsabilidad);
        layoutTrabajoEquipo = findViewById(R.id.layoutTrabajoEquipo);
        inputTrabajoEquipo = findViewById(R.id.inputTrabajoEquipo);
        layoutIniciativa = findViewById(R.id.layoutIniciativa);
        inputIniciativa = findViewById(R.id.inputIniciativa);

        layoutApreciacion = findViewById(R.id.layoutApreciacion);
        inputApreciacion = findViewById(R.id.inputApreciacion);
        layoutLogros = findViewById(R.id.layoutLogros);
        inputLogros = findViewById(R.id.inputLogros);

        cardConfirmar = findViewById(R.id.cardConfirmar);
        checkConfirmar = findViewById(R.id.checkConfirmar);
        btnEmitir = findViewById(R.id.btnEmitir);

        cardNotaFinal = findViewById(R.id.cardNotaFinal);
        textNotaFinal = findViewById(R.id.textNotaFinal);

        btnEmitir.setOnClickListener(v -> emitirInforme());

        loadInformeDetalle();
    }

    private void loadInformeDetalle() {
        progress.setVisibility(View.VISIBLE);
        contentScroll.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);

        ApiClient.enqueue(ApiClient.get().api().getJefeInformes(), new ApiClient.ApiCallback<List<InformeJefeDto>>() {
            @Override
            public void onSuccess(List<InformeJefeDto> data) {
                progress.setVisibility(View.GONE);
                if (data == null) {
                    showError("No se encontraron informes.");
                    return;
                }

                InformeJefeDto informe = null;
                for (InformeJefeDto inf : data) {
                    if (inf.id == inscripcionId) {
                        informe = inf;
                        break;
                    }
                }

                if (informe == null) {
                    showError("No se encontró el informe de la inscripción solicitada.");
                    return;
                }

                displayInforme(informe);
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                showError(message);
            }
        });
    }

    private void displayInforme(InformeJefeDto inf) {
        textEstudiante.setText(inf.estudiante != null ? inf.estudiante : "Estudiante");
        textPasantia.setText(inf.pasantia != null ? inf.pasantia : "Pasantía");
        textBitacoras.setText(inf.bitacorasEvaluadas + " / " + inf.totalBitacoras);
        textNotaSugerida.setText(inf.notaSugerida + " / 100");

        boolean emitido = inf.estado != null && inf.estado.toLowerCase().contains("emitido");

        if (emitido) {
            // Modo Solo Lectura
            cardConfirmar.setVisibility(View.GONE);
            cardNotaFinal.setVisibility(View.VISIBLE);
            textNotaFinal.setText("Nota Final: " + (inf.notaFinal != null ? inf.notaFinal : 0) + " / 100");

            inputConocimiento.setEnabled(false);
            inputResponsabilidad.setEnabled(false);
            inputTrabajoEquipo.setEnabled(false);
            inputIniciativa.setEnabled(false);
            inputApreciacion.setEnabled(false);
            inputLogros.setEnabled(false);
            checkConfirmar.setEnabled(false);

            inputApreciacion.setText(inf.contenido);
            inputLogros.setText(inf.logrosAlcanzados);

            // Mapear criterios de evaluación
            if (inf.criterios != null) {
                for (InformeJefeDto.Criterio c : inf.criterios) {
                    String name = c.nombre != null ? c.nombre.toLowerCase() : "";
                    if (name.contains("conocimiento")) {
                        inputConocimiento.setText(String.valueOf(c.puntaje));
                    } else if (name.contains("responsabilidad")) {
                        inputResponsabilidad.setText(String.valueOf(c.puntaje));
                    } else if (name.contains("trabajo") || name.contains("relaciones")) {
                        inputTrabajoEquipo.setText(String.valueOf(c.puntaje));
                    } else if (name.contains("iniciativa") || name.contains("proactividad")) {
                        inputIniciativa.setText(String.valueOf(c.puntaje));
                    }
                }
            }
        } else {
            // Modo Edición / Emisión
            cardConfirmar.setVisibility(View.VISIBLE);
            cardNotaFinal.setVisibility(View.GONE);

            inputConocimiento.setEnabled(true);
            inputResponsabilidad.setEnabled(true);
            inputTrabajoEquipo.setEnabled(true);
            inputIniciativa.setEnabled(true);
            inputApreciacion.setEnabled(true);
            inputLogros.setEnabled(true);
            checkConfirmar.setEnabled(true);

            // Nota sugerida o valor por defecto (70)
            int valDefault = inf.notaSugerida > 0 ? inf.notaSugerida : 70;
            inputConocimiento.setText(String.valueOf(valDefault));
            inputResponsabilidad.setText(String.valueOf(valDefault));
            inputTrabajoEquipo.setText(String.valueOf(valDefault));
            inputIniciativa.setText(String.valueOf(valDefault));

            inputApreciacion.setText(inf.contenido != null ? inf.contenido : "");
            inputLogros.setText(inf.logrosAlcanzados != null ? inf.logrosAlcanzados : "");
        }

        contentScroll.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        textError.setText(message);
        textError.setVisibility(View.VISIBLE);
    }

    private void emitirInforme() {
        layoutConocimiento.setError(null);
        layoutResponsabilidad.setError(null);
        layoutTrabajoEquipo.setError(null);
        layoutIniciativa.setError(null);
        layoutApreciacion.setError(null);
        layoutLogros.setError(null);

        if (!checkConfirmar.isChecked()) {
            Toast.makeText(this, "Debe confirmar que la evaluación es definitiva.", Toast.LENGTH_SHORT).show();
            return;
        }

        String conocimientoStr = inputConocimiento.getText() != null ? inputConocimiento.getText().toString().trim() : "";
        String responsabilidadStr = inputResponsabilidad.getText() != null ? inputResponsabilidad.getText().toString().trim() : "";
        String trabajoEquipoStr = inputTrabajoEquipo.getText() != null ? inputTrabajoEquipo.getText().toString().trim() : "";
        String iniciativaStr = inputIniciativa.getText() != null ? inputIniciativa.getText().toString().trim() : "";

        String apreciacion = inputApreciacion.getText() != null ? inputApreciacion.getText().toString().trim() : "";
        String logros = inputLogros.getText() != null ? inputLogros.getText().toString().trim() : "";

        int conocimiento = validateNotaField(conocimientoStr, layoutConocimiento);
        int responsabilidad = validateNotaField(responsabilidadStr, layoutResponsabilidad);
        int trabajoEquipo = validateNotaField(trabajoEquipoStr, layoutTrabajoEquipo);
        int iniciativa = validateNotaField(iniciativaStr, layoutIniciativa);

        if (conocimiento == -1 || responsabilidad == -1 || trabajoEquipo == -1 || iniciativa == -1) {
            return;
        }

        if (TextUtils.isEmpty(apreciacion)) {
            layoutApreciacion.setError("La apreciación global es obligatoria");
            return;
        }

        if (TextUtils.isEmpty(logros)) {
            layoutLogros.setError("Los logros alcanzados son obligatorios");
            return;
        }

        btnEmitir.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        Map<String, Object> body = new HashMap<>();
        body.put("apreciacion", apreciacion);
        body.put("logros_alcanzados", logros);
        body.put("crit_conocimiento_tecnico", conocimiento);
        body.put("crit_responsabilidad", responsabilidad);
        body.put("crit_trabajo_equipo", trabajoEquipo);
        body.put("crit_iniciativa", iniciativa);

        ApiClient.enqueue(ApiClient.get().api().emitirInformeFinal(inscripcionId, body), new ApiClient.ApiCallback<MessageResponse>() {
            @Override
            public void onSuccess(MessageResponse data) {
                progress.setVisibility(View.GONE);
                btnEmitir.setEnabled(true);
                DialogHelper.showSuccess(JefeEmitirInformeActivity.this, "Informe emitido correctamente", () -> finish());
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                btnEmitir.setEnabled(true);
                Toast.makeText(JefeEmitirInformeActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int validateNotaField(String valStr, TextInputLayout layout) {
        if (TextUtils.isEmpty(valStr)) {
            layout.setError("Este campo es obligatorio");
            return -1;
        }
        try {
            int val = Integer.parseInt(valStr);
            if (val < 0 || val > 100) {
                layout.setError("El puntaje debe estar entre 0 y 100");
                return -1;
            }
            return val;
        } catch (NumberFormatException e) {
            layout.setError("Número inválido");
            return -1;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

