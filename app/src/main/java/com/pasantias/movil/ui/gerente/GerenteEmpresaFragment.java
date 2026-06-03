package com.pasantias.movil.ui.gerente;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.dto.EmpresaDto;
import com.pasantias.movil.util.UiUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GerenteEmpresaFragment extends Fragment {

    private TextInputEditText inputNombre, inputRubro, inputDireccion, inputTelefono, inputEmail, inputDescripcion;
    private ProgressBar progress;
    private ActivityResultLauncher<String> pickImage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickImage = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) uploadLogo(uri);
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_empresa_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        inputNombre = view.findViewById(R.id.inputNombre);
        inputRubro = view.findViewById(R.id.inputRubro);
        inputDireccion = view.findViewById(R.id.inputDireccion);
        inputTelefono = view.findViewById(R.id.inputTelefono);
        inputEmail = view.findViewById(R.id.inputEmail);
        inputDescripcion = view.findViewById(R.id.inputDescripcion);
        progress = view.findViewById(R.id.progress);
        MaterialButton btnGuardar = view.findViewById(R.id.btnGuardar);
        MaterialButton btnLogo = view.findViewById(R.id.btnLogo);
        btnGuardar.setOnClickListener(v -> guardar());
        btnLogo.setOnClickListener(v -> pickImage.launch("image/*"));
        cargar();
    }

    private void cargar() {
        progress.setVisibility(View.VISIBLE);
        ApiClient.enqueue(ApiClient.get().api().getEmpresaGerente(), new ApiClient.ApiCallback<EmpresaDto>() {
            @Override
            public void onSuccess(EmpresaDto e) {
                progress.setVisibility(View.GONE);
                inputNombre.setText(e.nombre);
                inputRubro.setText(e.rubro);
                inputDireccion.setText(e.direccion);
                inputTelefono.setText(e.telefono);
                inputEmail.setText(e.email_contacto);
                inputDescripcion.setText(e.descripcion);
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                UiUtils.toast(requireActivity(), message);
            }
        });
    }

    private void guardar() {
        Map<String, Object> body = new HashMap<>();
        body.put("nombre", text(inputNombre));
        body.put("rubro", text(inputRubro));
        body.put("direccion", text(inputDireccion));
        body.put("telefono", text(inputTelefono));
        body.put("email_contacto", text(inputEmail));
        body.put("descripcion", text(inputDescripcion));
        progress.setVisibility(View.VISIBLE);
        ApiClient.enqueue(ApiClient.get().api().updateEmpresaGerente(body), new ApiClient.ApiCallback<EmpresaDto>() {
            @Override
            public void onSuccess(EmpresaDto data) {
                progress.setVisibility(View.GONE);
                UiUtils.toast(requireActivity(), "Empresa actualizada");
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                UiUtils.toast(requireActivity(), message);
            }
        });
    }

    private void uploadLogo(Uri uri) {
        try {
            File file = new File(requireContext().getCacheDir(), "logo_upload.jpg");
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            FileOutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[4096];
            int n;
            while (in != null && (n = in.read(buf)) > 0) out.write(buf, 0, n);
            if (in != null) in.close();
            out.close();
            RequestBody req = RequestBody.create(file, MediaType.parse("image/*"));
            MultipartBody.Part part = MultipartBody.Part.createFormData("logo", file.getName(), req);
            progress.setVisibility(View.VISIBLE);
            ApiClient.get().api().uploadLogoEmpresa(part).enqueue(new Callback<EmpresaDto>() {
                @Override
                public void onResponse(@NonNull Call<EmpresaDto> call, @NonNull Response<EmpresaDto> response) {
                    progress.setVisibility(View.GONE);
                    UiUtils.toast(requireActivity(), response.isSuccessful() ? "Logo actualizado" : "Error al subir logo");
                }

                @Override
                public void onFailure(@NonNull Call<EmpresaDto> call, @NonNull Throwable t) {
                    progress.setVisibility(View.GONE);
                    UiUtils.toast(requireActivity(), t.getMessage());
                }
            });
        } catch (Exception e) {
            UiUtils.toast(requireActivity(), e.getMessage());
        }
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
