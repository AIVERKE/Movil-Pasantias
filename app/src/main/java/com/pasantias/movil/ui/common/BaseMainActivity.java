package com.pasantias.movil.ui.common;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pasantias.movil.R;
import com.pasantias.movil.data.api.ApiClient;
import com.pasantias.movil.data.local.TokenManager;
import com.pasantias.movil.ui.auth.LoginActivity;

public abstract class BaseMainActivity extends AppCompatActivity {

    protected MaterialToolbar toolbar;
    protected BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!TokenManager.get().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main_shell);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.inflateMenu(getBottomMenuRes());
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f = createFragment(item.getItemId());
            if (f != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, f)
                        .commit();
                toolbar.setTitle(item.getTitle());
                return true;
            }
            return false;
        });
        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.nav_dashboard);
        }
    }

    @MenuRes
    protected abstract int getBottomMenuRes();

    protected abstract Fragment createFragment(int itemId);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            ApiClient.enqueue(ApiClient.get().api().logout(), new ApiClient.ApiCallback<com.pasantias.movil.data.dto.MessageResponse>() {
                @Override
                public void onSuccess(com.pasantias.movil.data.dto.MessageResponse data) {
                    doLogout();
                }

                @Override
                public void onError(String message) {
                    doLogout();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void doLogout() {
        TokenManager.get().clear();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    public void selectBottomNavItem(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }
}
