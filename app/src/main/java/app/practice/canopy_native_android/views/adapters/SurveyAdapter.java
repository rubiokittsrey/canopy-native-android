package app.practice.canopy_native_android.views.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import app.practice.canopy_native_android.R;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.databinding.ItemSurveyBinding;
import app.practice.canopy_native_android.utils.Constants;

public class SurveyAdapter extends PagingDataAdapter<SurveyEntity, SurveyAdapter.SurveyViewHolder> {

    private final OnSurveyClickListener clickListener;

    public interface OnSurveyClickListener {
        void onSurveyClick(SurveyEntity survey);
        void onSurveyLongClick(SurveyEntity survey);
    }

    public SurveyAdapter(OnSurveyClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    private static final DiffUtil.ItemCallback<SurveyEntity> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<SurveyEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull SurveyEntity oldItem, @NonNull SurveyEntity newItem) {
                return oldItem.getSurveyId().equals(newItem.getSurveyId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull SurveyEntity oldItem, @NonNull SurveyEntity newItem) {
                // compare all relevant fields (null-safe)
                return oldItem.getSurveyId().equals(newItem.getSurveyId()) &&
                        java.util.Objects.equals(oldItem.getUpdatedAt(), newItem.getUpdatedAt()) &&
                        java.util.Objects.equals(oldItem.getSyncStatus(), newItem.getSyncStatus());
            }
        };

    @NonNull
    @Override
    public SurveyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSurveyBinding binding = ItemSurveyBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new SurveyViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(@NonNull SurveyViewHolder holder, int position) {
        SurveyEntity survey = getItem(position);
        if (survey != null) {
            holder.bind(survey, clickListener);
        }
    }

    static class SurveyViewHolder extends RecyclerView.ViewHolder {

        private final ItemSurveyBinding binding;

        SurveyViewHolder(ItemSurveyBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SurveyEntity survey, OnSurveyClickListener listener) {
            String surveyType = survey.getSurveyType();
            binding.textSurveyType.setText(
                    surveyType != null && !surveyType.isEmpty() ? surveyType : "Survey"
            );

            binding.textObserver.setText(survey.getObserverName());

            String org = survey.getOrganization();
            if (org != null && !org.isEmpty()) {
                binding.textOrganization.setText(org);
                binding.textOrganization.setVisibility(View.VISIBLE);
            } else {
                binding.textOrganization.setVisibility(View.GONE);
            }

            String dateTime = survey.getSurveyDate() + " • " + formatTime(survey.getSurveyTime());
            binding.textDateTime.setText(dateTime);

            Double lat = survey.getGpsLatitude();
            Double lng = survey.getGpsLongitude();
            if (lat != null && lng != null) {
                String location = String.format(Locale.getDefault(),"%.4f, %.4f", lat, lng);
                binding.textLocation.setText(location);
                binding.textLocation.setVisibility(View.VISIBLE);
            } else {
                binding.textLocation.setVisibility(View.GONE);
            }

            binding.textWeather.setText(survey.getWeatherCondition());

            updateSyncStatus(survey.getSyncStatus());

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSurveyClick(survey);
                }
            });

            binding.getRoot().setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onSurveyLongClick(survey);
                    return true;
                }
                return false;
            });
        }

        private void updateSyncStatus(String status) {
            int colorRes;
            int iconRes;

            switch (status) {
                case Constants.SYNC_SYNCED:
                    colorRes = R.color.sync_synced;
                    iconRes = R.drawable.ic_cloud_done;
                    break;
                case Constants.SYNC_FAILED:
                    colorRes = R.color.sync_failed;
                    iconRes = R.drawable.ic_cloud_off;
                    break;
                case Constants.SYNC_PENDING:
                default:
                    colorRes = R.color.sync_pending;
                    iconRes = R.drawable.ic_cloud_upload;
                    break;
            }

            binding.imageSyncStatus.setImageResource(iconRes);
            binding.imageSyncStatus.setColorFilter(
                    binding.getRoot().getContext().getColor(colorRes)
            );
        }

        private String formatTime(String time) {
            // convert "HH:mm:ss" to "HH:mm"
            if (time != null && time.length() >= 5) {
                return time.substring(0, 5);
            }
            return time != null ? time : "";
        }
    }
}