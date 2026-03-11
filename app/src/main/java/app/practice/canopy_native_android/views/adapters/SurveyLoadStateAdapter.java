package app.practice.canopy_native_android.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.LoadState;
import androidx.paging.LoadStateAdapter;
import androidx.recyclerview.widget.RecyclerView;

import app.practice.canopy_native_android.databinding.ItemLoadStateBinding;

// adapter for showing loading/error states at the end of the list
public class SurveyLoadStateAdapter extends LoadStateAdapter<SurveyLoadStateAdapter.LoadStateViewHolder> {

    private final Runnable retryCallback;

    public SurveyLoadStateAdapter(Runnable retryCallback) {
        this.retryCallback = retryCallback;
    }

    @NonNull
    @Override
    public LoadStateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @NonNull LoadState loadState) {
        ItemLoadStateBinding binding = ItemLoadStateBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new LoadStateViewHolder(binding, retryCallback);
    }

    @Override
    public void onBindViewHolder(@NonNull LoadStateViewHolder holder, @NonNull LoadState loadState) {
        holder.bind(loadState);
    }

    static class LoadStateViewHolder extends RecyclerView.ViewHolder {

        private final ItemLoadStateBinding binding;
        private final Runnable retryCallback;

        LoadStateViewHolder(ItemLoadStateBinding binding, Runnable retryCallback) {
            super(binding.getRoot());
            this.binding = binding;
            this.retryCallback = retryCallback;

            binding.buttonRetry.setOnClickListener(v -> {
                if (retryCallback != null) {
                    retryCallback.run();
                }
            });
        }

        void bind(LoadState loadState) {
            if (loadState instanceof LoadState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.textError.setVisibility(View.GONE);
                binding.buttonRetry.setVisibility(View.GONE);
            } else if (loadState instanceof LoadState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.textError.setVisibility(View.VISIBLE);
                binding.buttonRetry.setVisibility(View.VISIBLE);

                LoadState.Error errorState = (LoadState.Error) loadState;
                String errorMessage = errorState.getError().getLocalizedMessage();
                binding.textError.setText(errorMessage != null ? errorMessage : "An error occurred");
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.textError.setVisibility(View.GONE);
                binding.buttonRetry.setVisibility(View.GONE);
            }
        }
    }
}