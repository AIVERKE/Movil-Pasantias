package com.pasantias.movil.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.pasantias.movil.R;

public abstract class ListRefreshFragment extends Fragment {

    protected SwipeRefreshLayout swipeRefresh;
    protected RecyclerView recycler;
    protected ProgressBar progress;
    protected TextView textError;
    protected TextView textEmpty;
    protected CardRowAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_refresh, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        recycler = view.findViewById(R.id.recycler);
        progress = view.findViewById(R.id.progress);
        textError = view.findViewById(R.id.textError);
        textEmpty = view.findViewById(R.id.textEmpty);
        adapter = new CardRowAdapter();
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(adapter);
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    protected abstract void loadData();

    protected void showLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (loading) {
            recycler.setVisibility(View.GONE);
            textError.setVisibility(View.GONE);
            textEmpty.setVisibility(View.GONE);
        }
    }

    protected void showContent(boolean hasItems) {
        swipeRefresh.setRefreshing(false);
        progress.setVisibility(View.GONE);
        textError.setVisibility(View.GONE);
        if (hasItems) {
            recycler.setVisibility(View.VISIBLE);
            textEmpty.setVisibility(View.GONE);
        } else {
            recycler.setVisibility(View.GONE);
            textEmpty.setVisibility(View.VISIBLE);
        }
    }

    protected void showError(String msg) {
        swipeRefresh.setRefreshing(false);
        progress.setVisibility(View.GONE);
        recycler.setVisibility(View.GONE);
        textEmpty.setVisibility(View.GONE);
        textError.setText(msg);
        textError.setVisibility(View.VISIBLE);
    }
}
