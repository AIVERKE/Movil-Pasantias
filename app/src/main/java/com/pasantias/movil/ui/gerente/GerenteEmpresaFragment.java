package com.pasantias.movil.ui.gerente;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
    private TextView tvPreviewNombre, tvPreviewRubro, tvPreviewTelefono, tvPreviewDireccion, tvLogoPlaceholder;
    private ImageView imgLogo;
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
        
        tvPreviewNombre = view.findViewById(R.id.tvPreviewNombre);
        tvPreviewRubro = view.findViewById(R.id.tvPreviewRubro);
        tvPreviewTelefono = view.findViewById(R.id.tvPreviewTelefono);
        tvPreviewDireccion = view.findViewById(R.id.tvPreviewDireccion);
        tvLogoPlaceholder = view.findViewById(R.id.tvLogoPlaceholder);
        imgLogo = view.findViewById(R.id.imgLogo);
        
        progress = view.findViewById(R.id.progress);
        Button btnGuardar = view.findViewById(R.id.btnGuardar);
        TextView btnSubirLogo = view.findViewById(R.id.btnSubirLogo);
        
        btnGuardar.setOnClickListener(v -> guardar());
        btnSubirLogo.setOnClickListener(v -> pickImage.launch("image/*"));
        
        cargar();
    }

    private void cargar() {
        progress.setVisibility(View.VISIBLE);
        ApiClient.enqueue(ApiClient.get().api().getEmpresaGerente(), new ApiClient.ApiCallback<EmpresaDto>() {
            @Override
            public void onSuccess(EmpresaDto e) {
                progress.setVisibility(View.GONE);
                if (e != null) {
                    bindEmpresa(e);
                }
            }

            @Override
            public void onError(String message) {
                progress.setVisibility(View.GONE);
                UiUtils.toast(requireActivity(), message);
            }
        });
    }

    private void bindEmpresa(EmpresaDto e) {
        inputNombre.setText(e.nombre);
        inputRubro.setText(e.rubro);
        inputDireccion.setText(e.direccion);
        inputTelefono.setText(e.telefono);
        inputEmail.setText(e.email_contacto);
        inputDescripcion.setText(e.descripcion);

        // Previsualización
        tvPreviewNombre.setText(e.nombre != null && !e.nombre.isEmpty() ? e.nombre : "Nombre de Empresa");
        tvPreviewRubro.setText(e.rubro != null && !e.rubro.isEmpty() ? e.rubro : "Rubro no especificado");
        tvPreviewTelefono.setText(e.telefono != null && !e.telefono.isEmpty() ? e.telefono : "Sin registrar");
        tvPreviewDireccion.setText(e.direccion != null && !e.direccion.isEmpty() ? e.direccion : "Sin registrar");

        // Cargar logo
        if (e.url_logo != null && !e.url_logo.isEmpty()) {
            String url = ApiClient.mediaUrl(e.url_logo);
            descargarCargarImagen(url, imgLogo);
        } else {
            imgLogo.setVisibility(View.GONE);
            tvLogoPlaceholder.setVisibility(View.VISIBLE);
            String inicial = "E";
            if (e.nombre != null && !e.nombre.isEmpty()) {
                inicial = e.nombre.substring(0, 1).toUpperCase();
            }
            tvLogoPlaceholder.setText(inicial);
        }
    }

    private void descargarCargarImagen(String url, ImageView imageView) {
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        imageView.setVisibility(View.GONE);
                        tvLogoPlaceholder.setVisibility(View.VISIBLE);
                    });
                }
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    byte[] bytes = response.body().bytes();
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap);
                                imageView.setVisibility(View.VISIBLE);
                                tvLogoPlaceholder.setVisibility(View.GONE);
                            } else {
                                imageView.setVisibility(View.GONE);
                                tvLogoPlaceholder.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            imageView.setVisibility(View.GONE);
                            tvLogoPlaceholder.setVisibility(View.VISIBLE);
                        });
                    }
                }
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
                if (data != null) {
                    bindEmpresa(data);
                }
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
                    if (response.isSuccessful() && response.body() != null) {
                        UiUtils.toast(requireActivity(), "Logo actualizado");
                        bindEmpresa(response.body());
                    } else {
                        UiUtils.toast(requireActivity(), "Error al subir logo");
                    }
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
