package com.pasantias.movil.ui.gerente;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.ActividadDto;
import com.pasantias.movil.data.dto.MessageResponse;
import com.pasantias.movil.ui.common.DialogHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PasantiaActividadesActivity extends AppCompatActivity {

    public static final String EXTRA_PASANTIA_ID = "pasantia_id";
    public static final String EXTRA_PASANTIA_TITULO = "pasantia_titulo";
    public static final String EXTRA_PASANTIA_FECHA_INICIO = "pasantia_fecha_inicio";
    public static final String EXTRA_PASANTIA_FECHA_FIN = "pasantia_fecha_fin";

    private int pasantiaId = -1;
    private String pasantiaTitulo = "";
    private String pasantiaFechaInicioStr = "";
    private String pasantiaFechaFinStr = "";

    private TextView tvFormTitle;
    private TextInputEditText inputActDescripcion, inputActFechaInicio, inputActFechaFin;
    private TextInputLayout layoutActFechaInicio, layoutActFechaFin;
    private Button btnCancelarEdicion, btnGuardarActividad;

    private RecyclerView recyclerActividades;
    private LinearLayout layoutActividadesEmpty;

    private ActividadesAdapter adapter;
    private boolean isEditingActivity = false;
    private int editingActivityId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pasantia_actividades);

        // Parametros
        pasantiaId = getIntent().getIntExtra(EXTRA_PASANTIA_ID, -1);
        pasantiaTitulo = getIntent().getStringExtra(EXTRA_PASANTIA_TITULO);
        pasantiaFechaInicioStr = getIntent().getStringExtra(EXTRA_PASANTIA_FECHA_INICIO);
        pasantiaFechaFinStr = getIntent().getStringExtra(EXTRA_PASANTIA_FECHA_FIN);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Actividades: " + (pasantiaTitulo != null ? pasantiaTitulo : ""));
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Referencias Form
        tvFormTitle          = findViewById(R.id.tvFormTitle);
        inputActDescripcion  = findViewById(R.id.inputActDescripcion);
        inputActFechaInicio  = findViewById(R.id.inputActFechaInicio);
        inputActFechaFin     = findViewById(R.id.inputActFechaFin);
        layoutActFechaInicio = findViewById(R.id.layoutActFechaInicio);
        layoutActFechaFin    = findViewById(R.id.layoutActFechaFin);
        btnCancelarEdicion   = findViewById(R.id.btnCancelarEdicion);
        btnGuardarActividad  = findViewById(R.id.btnGuardarActividad);

        // Referencias List
        recyclerActividades   = findViewById(R.id.recyclerActividades);
        layoutActividadesEmpty = findViewById(R.id.layoutActividadesEmpty);

        // Setup Pickers
        View.OnClickListener pickerInicio = v -> showDatePicker(inputActFechaInicio);
        inputActFechaInicio.setOnClickListener(pickerInicio);
        layoutActFechaInicio.setEndIconOnClickListener(pickerInicio);

        View.OnClickListener pickerFin = v -> showDatePicker(inputActFechaFin);
        inputActFechaFin.setOnClickListener(pickerFin);
        layoutActFechaFin.setEndIconOnClickListener(pickerFin);

        // Setup default dates as today
        resetForm();

        // Setup Recycler
        recyclerActividades.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActividadesAdapter();
        adapter.setCallbacks(new ActividadesAdapter.Callbacks() {
            @Override
            public void onEdit(ActividadDto act) {
                cargarEdicion(act);
            }

            @Override
            public void onDelete(int id) {
                confirmarEliminar(id);
            }
        });
        recyclerActividades.setAdapter(adapter);

        // Botones Form
        btnGuardarActividad.setOnClickListener(v -> guardarActividad());
        btnCancelarEdicion.setOnClickListener(v -> resetForm());

        // Cargar
        loadActividades();
    }

    private void showDatePicker(TextInputEditText target) {
        Calendar cal = Calendar.getInstance();
        String actual = text(target);
        if (actual.length() == 10) {
            try {
                String[] parts = actual.split("-");
                cal.set(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]) - 1,
                        Integer.parseInt(parts[2]));
            } catch (Exception ignored) { }
        }
        new DatePickerDialog(this, (picker, year, month, day) -> {
            String fecha = String.format(Locale.ROOT, "%04d-%02d-%02d", year, month + 1, day);
            target.setText(fecha);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void resetForm() {
        isEditingActivity = false;
        editingActivityId = -1;
        tvFormTitle.setText("Nueva Actividad");
        inputActDescripcion.setText("");
        
        String hoy = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date());
        inputActFechaInicio.setText(hoy);
        inputActFechaFin.setText(hoy);
        
        btnCancelarEdicion.setVisibility(View.GONE);
        btnGuardarActividad.setText("Agregar Actividad");
    }

    private void cargarEdicion(ActividadDto act) {
        isEditingActivity = true;
        editingActivityId = act.id_actividad;
        tvFormTitle.setText("Editar Actividad");
        inputActDescripcion.setText(act.descripcion);
        
        if (act.fecha_inicio != null && act.fecha_inicio.length() >= 10) {
            inputActFechaInicio.setText(act.fecha_inicio.substring(0, 10));
        }
        if (act.fecha_limite != null && act.fecha_limite.length() >= 10) {
            inputActFechaFin.setText(act.fecha_limite.substring(0, 10));
        } else if (act.fecha_inicio != null && act.fecha_inicio.length() >= 10) {
            inputActFechaFin.setText(act.fecha_inicio.substring(0, 10));
        }
        
        btnCancelarEdicion.setVisibility(View.VISIBLE);
        btnGuardarActividad.setText("Guardar Cambios");
        inputActDescripcion.requestFocus();
    }

    private void loadActividades() {
        ApiClient.enqueue(ApiClient.get().api().getActividadesPasantia(pasantiaId), new ApiClient.ApiCallback<List<ActividadDto>>() {
            @Override
            public void onSuccess(List<ActividadDto> data) {
                if (data == null || data.isEmpty()) {
                    recyclerActividades.setVisibility(View.GONE);
                    layoutActividadesEmpty.setVisibility(View.VISIBLE);
                } else {
                    layoutActividadesEmpty.setVisibility(View.GONE);
                    recyclerActividades.setVisibility(View.VISIBLE);
                    adapter.setItems(data);
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(PasantiaActividadesActivity.this, "Error al cargar actividades: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarActividad() {
        String desc = text(inputActDescripcion);
        String fInicio = text(inputActFechaInicio);
        String fFin = text(inputActFechaFin);

        if (desc.isEmpty()) {
            inputActDescripcion.setError("Requerido");
            inputActDescripcion.requestFocus();
            return;
        }

        if (fFin.compareTo(fInicio) < 0) {
            Toast.makeText(this, "La fecha fin debe ser posterior a la de inicio", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de fechas contra la pasantía (como en la app web)
        if (pasantiaFechaInicioStr != null && !pasantiaFechaInicioStr.isEmpty() && pasantiaFechaInicioStr.length() >= 10) {
            String pInicioClean = pasantiaFechaInicioStr.substring(0, 10);
            if (fInicio.compareTo(pInicioClean) < 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Fecha fuera de rango")
                        .setMessage("La actividad no puede iniciar antes que la pasantía. Por favor, edita primero la información de la pasantía aumentando sus fechas.")
                        .setPositiveButton("Entendido", null)
                        .show();
                return;
            }
        }

        if (pasantiaFechaFinStr != null && !pasantiaFechaFinStr.isEmpty() && pasantiaFechaFinStr.length() >= 10) {
            String pFinClean = pasantiaFechaFinStr.substring(0, 10);
            if (fInicio.compareTo(pFinClean) > 0 || fFin.compareTo(pFinClean) > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Fecha fuera de rango")
                        .setMessage("La actividad no puede exceder la fecha de fin de la pasantía. Por favor, edita primero la información de la pasantía aumentando sus fechas.")
                        .setPositiveButton("Entendido", null)
                        .show();
                return;
            }
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("descripcion", desc);
        payload.put("fecha_inicio", fInicio);
        payload.put("fecha_fin", fFin);
        payload.put("id_pasantia", pasantiaId);

        if (isEditingActivity) {
            ApiClient.enqueue(ApiClient.get().api().updateActividad(editingActivityId, payload), new ApiClient.ApiCallback<ActividadDto>() {
                @Override
                public void onSuccess(ActividadDto data) {
                    DialogHelper.showSuccess(PasantiaActividadesActivity.this, "Actividad actualizada", () -> {
                        resetForm();
                        // Actualizar lista inmediatamente con el dato devuelto y luego refrescar
                        if (data != null) adapter.updateItem(data);
                        loadActividades();
                    });
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(PasantiaActividadesActivity.this, "Error al actualizar: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ApiClient.enqueue(ApiClient.get().api().createActividad(payload), new ApiClient.ApiCallback<ActividadDto>() {
                @Override
                public void onSuccess(ActividadDto data) {
                    DialogHelper.showSuccess(PasantiaActividadesActivity.this, "Actividad agregada con éxito", () -> {
                        resetForm();
                        // Añadir el item inmediatamente al adapter con el dato devuelto
                        if (data != null) {
                            adapter.addItem(data);
                            recyclerActividades.setVisibility(View.VISIBLE);
                            layoutActividadesEmpty.setVisibility(View.GONE);
                        }
                        // Refrescar desde servidor para asegurar consistencia
                        loadActividades();
                    });
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(PasantiaActividadesActivity.this, "Error al crear: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void confirmarEliminar(int id) {
        new AlertDialog.Builder(this)
                .setTitle("¿Eliminar actividad?")
                .setMessage("Esta acción quitará la actividad del cronograma permanentemente")
                .setPositiveButton("Sí, eliminar", (dialog, which) -> {
                    ApiClient.enqueue(ApiClient.get().api().deleteActividad(id), new ApiClient.ApiCallback<MessageResponse>() {
                        @Override
                        public void onSuccess(MessageResponse data) {
                            DialogHelper.showSuccess(PasantiaActividadesActivity.this, "Actividad eliminada", () -> {
                                loadActividades();
                            });
                        }

                        @Override
                        public void onError(String message) {
                            Toast.makeText(PasantiaActividadesActivity.this, "Error al eliminar: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
