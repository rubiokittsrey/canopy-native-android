package app.practice.canopy_native_android.views.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ExperimentalPagingApi;

import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import app.practice.canopy_native_android.R;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.database.entities.TopographicEntity;
import app.practice.canopy_native_android.databinding.ActivityCreateSurveyBinding;
import app.practice.canopy_native_android.models.enums.SurveyMethod;
import app.practice.canopy_native_android.models.enums.WeatherCondition;
import app.practice.canopy_native_android.utils.Constants;
import app.practice.canopy_native_android.viewmodels.AuthViewModel;
import app.practice.canopy_native_android.viewmodels.SurveyViewModel;

@ExperimentalPagingApi
public class CreateSurveyActivity extends AppCompatActivity {

    private ActivityCreateSurveyBinding binding;
    private SurveyViewModel surveyViewModel;
    private AuthViewModel authViewModel;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

    private String surveyId; // Null for create, non-null for edit
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateSurveyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        surveyId = getIntent().getStringExtra(MainActivity.EXTRA_SURVEY_ID);
        isEditMode = surveyId != null;

        setupViewModels();
        setupToolbar();
        setupDropdowns();
        setupDateTimePickers();
        setupSectionToggles();
        setupSaveButton();
        observeViewModel();

        if (isEditMode) {
            loadExistingSurvey();
        } else {
            setDefaultValues();
        }
    }

    private void setupViewModels() {
        surveyViewModel = new ViewModelProvider(this).get(SurveyViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    private void setupToolbar() {
        binding.toolbar.setTitle(isEditMode ? R.string.edit_survey : R.string.create_survey);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        // Weather conditions
        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                WeatherCondition.getDisplayValues()
        );
        binding.dropdownWeather.setAdapter(weatherAdapter);

        // Survey methods
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                SurveyMethod.getDisplayValues()
        );
        binding.dropdownMethod.setAdapter(methodAdapter);

        // Land use options
        String[] landUseOptions = {"Agriculture", "Forest", "Built-up", "Bare soil", "Wetland", "Water body"};
        ArrayAdapter<String> landUseAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                landUseOptions
        );
        binding.dropdownLandUse.setAdapter(landUseAdapter);
    }

    private void setupDateTimePickers() {
        binding.editSurveyDate.setOnClickListener(v -> showDatePicker());
        binding.editSurveyTime.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, day);
                    binding.editSurveyDate.setText(dateFormat.format(selected.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    String time = String.format(Locale.US, "%02d:%02d:00", hourOfDay, minute);
                    binding.editSurveyTime.setText(time);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
        ).show();
    }

    private void setupSectionToggles() {
        binding.checkTopographic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.cardTopographic.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Similar for other sections...
    }

    private void setupSaveButton() {
        binding.buttonSave.setOnClickListener(v -> saveSurvey());
    }

    private void observeViewModel() {
        surveyViewModel.getCreateResult().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    Intent data = new Intent();
                    data.putExtra(MainActivity.EXTRA_RESULT_ACTION, MainActivity.ACTION_CREATED);
                    data.putExtra(MainActivity.EXTRA_SURVEY_ID, resource.getData());
                    setResult(RESULT_OK, data);
                    finish();
                    break;

                case ERROR:
                    showLoading(false);
                    showError(resource.getMessage());
                    break;
            }
        });

        surveyViewModel.getUpdateResult().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    showLoading(true);
                    break;

                case SUCCESS:
                    showLoading(false);
                    Intent data = new Intent();
                    data.putExtra(MainActivity.EXTRA_RESULT_ACTION, MainActivity.ACTION_UPDATED);
                    setResult(RESULT_OK, data);
                    finish();
                    break;

                case ERROR:
                    showLoading(false);
                    showError(resource.getMessage());
                    break;
            }
        });
    }

    private void setDefaultValues() {
        Calendar now = Calendar.getInstance();
        binding.editSurveyDate.setText(dateFormat.format(now.getTime()));
        binding.editSurveyTime.setText(timeFormat.format(now.getTime()));
    }

    private void loadExistingSurvey() {
        surveyViewModel.selectSurvey(surveyId);

        surveyViewModel.getSelectedSurvey().observe(this, survey -> {
            if (survey != null) {
                binding.editSurveyType.setText(survey.getSurveyType());
                binding.editSurveyDate.setText(survey.getSurveyDate());
                binding.editSurveyTime.setText(survey.getSurveyTime());
                binding.dropdownWeather.setText(survey.getWeatherCondition(), false);
                binding.dropdownMethod.setText(survey.getSurveyMethod(), false);

                if (survey.getGpsLatitude() != null) {
                    binding.editLatitude.setText(String.valueOf(survey.getGpsLatitude()));
                }
                if (survey.getGpsLongitude() != null) {
                    binding.editLongitude.setText(String.valueOf(survey.getGpsLongitude()));
                }

                binding.editNotes.setText(survey.getNotes());
            }
        });

        // Load topographic data if exists
        surveyViewModel.getSelectedTopographic().observe(this, topo -> {
            if (topo != null) {
                binding.checkTopographic.setChecked(true);
                binding.cardTopographic.setVisibility(View.VISIBLE);

                if (topo.getElevationM() != null) {
                    binding.editElevation.setText(String.valueOf(topo.getElevationM()));
                }
                if (topo.getSlopeGradientDeg() != null) {
                    binding.editSlopeGradient.setText(String.valueOf(topo.getSlopeGradientDeg()));
                }
                binding.dropdownLandUse.setText(topo.getLandUse(), false);
                binding.editLandCover.setText(topo.getLandCoverDescription());
            }
        });
    }

    private void saveSurvey() {
        // Validate required fields
        String surveyDate = binding.editSurveyDate.getText().toString();
        String surveyTime = binding.editSurveyTime.getText().toString();
        String weatherCondition = binding.dropdownWeather.getText().toString();
        String surveyMethod = binding.dropdownMethod.getText().toString();

        if (surveyDate.isEmpty()) {
            showError("Survey date is required");
            return;
        }
        if (surveyTime.isEmpty()) {
            showError("Survey time is required");
            return;
        }
        if (weatherCondition.isEmpty()) {
            showError("Weather condition is required");
            return;
        }
        if (surveyMethod.isEmpty()) {
            showError("Survey method is required");
            return;
        }

        // Check at least one section is selected
        boolean hasSection = binding.checkTopographic.isChecked() ||
                binding.checkVegetation.isChecked() ||
                binding.checkSoil.isChecked() ||
                binding.checkWater.isChecked() ||
                binding.checkBiodiversity.isChecked() ||
                binding.checkHazard.isChecked() ||
                binding.checkInfrastructure.isChecked();

        if (!hasSection) {
            showError("At least one data section must be selected");
            return;
        }

        // Get user info
        String observerId = authViewModel.getCurrentUserId();
        String observerName = "Unknown";
        String organization = null;

        if (authViewModel.getCurrentUser().getValue() != null) {
            observerName = authViewModel.getCurrentUser().getValue().getFullName();
            organization = authViewModel.getCurrentUser().getValue().getOrganization();
        }

        // Build survey entity
        String id = isEditMode ? surveyId : UUID.randomUUID().toString();
        String now = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                .format(Calendar.getInstance().getTime());

        SurveyEntity survey = new SurveyEntity(
                id,
                observerId,
                observerName,
                surveyDate,
                surveyTime,
                weatherCondition,
                surveyMethod,
                Constants.SYNC_PENDING,
                now,
                now
        );

        survey.setSurveyType(binding.editSurveyType.getText().toString().trim());
        survey.setOrganization(organization);
        survey.setNotes(binding.editNotes.getText().toString().trim());

        // Parse coordinates
        String latStr = binding.editLatitude.getText().toString().trim();
        String lngStr = binding.editLongitude.getText().toString().trim();
        if (!latStr.isEmpty()) {
            try {
                survey.setGpsLatitude(Double.parseDouble(latStr));
            } catch (NumberFormatException ignored) {}
        }
        if (!lngStr.isEmpty()) {
            try {
                survey.setGpsLongitude(Double.parseDouble(lngStr));
            } catch (NumberFormatException ignored) {}
        }

        // Build section entities
        TopographicEntity topographic = null;
        if (binding.checkTopographic.isChecked()) {
            topographic = new TopographicEntity(id);

            String elevation = binding.editElevation.getText().toString().trim();
            if (!elevation.isEmpty()) {
                try {
                    topographic.setElevationM(Double.parseDouble(elevation));
                } catch (NumberFormatException ignored) {}
            }

            String slope = binding.editSlopeGradient.getText().toString().trim();
            if (!slope.isEmpty()) {
                try {
                    topographic.setSlopeGradientDeg(Double.parseDouble(slope));
                } catch (NumberFormatException ignored) {}
            }

            topographic.setLandUse(binding.dropdownLandUse.getText().toString());
            topographic.setLandCoverDescription(binding.editLandCover.getText().toString().trim());
        }

        // For brevity, other sections would be built similarly...

        // Save
        if (isEditMode) {
            surveyViewModel.updateSurvey(survey, topographic, null, null, null, null, null, null);
        } else {
            surveyViewModel.createSurvey(survey, topographic, null, null, null, null, null, null);
        }
    }

    private void showLoading(boolean show) {
        binding.layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.buttonSave.setEnabled(!show);
    }

    private void showError(String message) {
        binding.textError.setText(message);
        binding.textError.setVisibility(View.VISIBLE);
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        surveyViewModel.clearResults();
        binding = null;
    }
}