package app.practice.canopy_native_android.views.dialogs;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ExperimentalPagingApi;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import app.practice.canopy_native_android.R;
import app.practice.canopy_native_android.databinding.DialogFilterBinding;
import app.practice.canopy_native_android.repositories.SurveyRepository.SurveyFilter;
import app.practice.canopy_native_android.viewmodels.SurveyViewModel;

public class FilterBottomSheetDialog extends BottomSheetDialogFragment {

    private DialogFilterBinding binding;
    private FilterListener filterListener;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public interface FilterListener {
        void onFilterApplied(SurveyFilter filter);
    }

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupDropdowns();
        setupDatePickers();
        setupButtons();
    }

    @OptIn(markerClass = ExperimentalPagingApi.class)
    private void setupDropdowns() {
        SurveyViewModel viewModel = new ViewModelProvider(requireActivity()).get(SurveyViewModel.class);

        viewModel.getSurveyTypes().observe(getViewLifecycleOwner(), types -> {
            if (types != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        types
                );
                binding.dropdownSurveyType.setAdapter(adapter);
            }
        });

        viewModel.getWeatherConditions().observe(getViewLifecycleOwner(), conditions -> {
            if (conditions != null) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        conditions
                );
                binding.dropdownWeather.setAdapter(adapter);
            }
        });
    }

    private void setupDatePickers() {
        binding.editDateFrom.setOnClickListener(v -> showDatePicker(date ->
                binding.editDateFrom.setText(date)));

        binding.editDateTo.setOnClickListener(v -> showDatePicker(date ->
                binding.editDateTo.setText(date)));
    }

    private void showDatePicker(DateSelectedCallback callback) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                requireContext(),
                (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);
                    callback.onDateSelected(dateFormat.format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private interface DateSelectedCallback {
        void onDateSelected(String date);
    }

    private void setupButtons() {
        binding.buttonApply.setOnClickListener(v -> {
            SurveyFilter filter = new SurveyFilter();

            String surveyType = binding.dropdownSurveyType.getText().toString().trim();
            if (!surveyType.isEmpty()) {
                filter.setSurveyType(surveyType);
            }

            String weather = binding.dropdownWeather.getText().toString().trim();
            if (!weather.isEmpty()) {
                filter.setWeatherCondition(weather);
            }

            String dateFrom = binding.editDateFrom.getText().toString().trim();
            if (!dateFrom.isEmpty()) {
                filter.setDateFrom(dateFrom);
            }

            String dateTo = binding.editDateTo.getText().toString().trim();
            if (!dateTo.isEmpty()) {
                filter.setDateTo(dateTo);
            }

            if (filterListener != null) {
                filterListener.onFilterApplied(filter);
            }
            dismiss();
        });

        binding.buttonReset.setOnClickListener(v -> {
            binding.dropdownSurveyType.setText("", false);
            binding.dropdownWeather.setText("", false);
            binding.editDateFrom.setText("");
            binding.editDateTo.setText("");

            if (filterListener != null) {
                filterListener.onFilterApplied(new SurveyFilter());
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
