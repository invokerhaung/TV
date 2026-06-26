package com.fongmi.android.tv.ui.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Config;
import com.fongmi.android.tv.databinding.DialogConfigSourceBinding;
import com.fongmi.android.tv.databinding.ItemConfigSourceBinding;
import com.fongmi.android.tv.impl.ConfigListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class ConfigSourceDialog extends BaseAlertDialog {

    private DialogConfigSourceBinding binding;
    private List<Config> configs;
    private Config currentConfig;

    public static ConfigSourceDialog create() {
        return new ConfigSourceDialog();
    }

    public void show(Fragment fragment) {
        show(fragment.getChildFragmentManager(), null);
    }

    @Override
    protected ViewBinding getBinding() {
        return binding = DialogConfigSourceBinding.inflate(getLayoutInflater());
    }

    @Override
    protected MaterialAlertDialogBuilder getBuilder() {
        return builder()
                .setTitle(R.string.dialog_source_title)
                .setView(getBinding().getRoot())
                .setNegativeButton(R.string.dialog_negative, null);
    }

    @Override
    protected void initView() {
        currentConfig = VodConfig.get().getConfig();
        configs = new ArrayList<>(Config.getAll(0));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(new ConfigAdapter());
    }

    @Override
    protected void initEvent() {
        binding.add.setOnClickListener(this::onAdd);
    }

    private void onAdd(View view) {
        dismiss();
        ConfigDialog.create().vod().show(requireParentFragment());
    }

    private void onSelect(Config config) {
        dismiss();
        ((ConfigListener) requireParentFragment()).setConfig(config);
    }

    private void onDelete(Config config, int position) {
        config.delete();
        configs.remove(position);
        binding.recyclerView.getAdapter().notifyItemRemoved(position);
        binding.recyclerView.getAdapter().notifyItemRangeChanged(position, configs.size());
    }

    private class ConfigAdapter extends RecyclerView.Adapter<ConfigAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(ItemConfigSourceBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Config config = configs.get(position);
            holder.binding.name.setText(config.getDesc());
            holder.binding.url.setText(config.getUrl());
            holder.binding.check.setVisibility(config.equals(currentConfig) ? View.VISIBLE : View.GONE);
            holder.binding.getRoot().setOnClickListener(v -> onSelect(config));
            holder.binding.getRoot().setOnLongClickListener(v -> {
                holder.binding.delete.setVisibility(View.VISIBLE);
                return true;
            });
            holder.binding.delete.setOnClickListener(v -> onDelete(config, holder.getAdapterPosition()));
        }

        @Override
        public int getItemCount() {
            return configs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemConfigSourceBinding binding;

            ViewHolder(ItemConfigSourceBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
