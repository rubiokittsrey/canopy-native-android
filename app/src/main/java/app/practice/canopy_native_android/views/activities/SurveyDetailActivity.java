package app.practice.canopy_native_android.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ExperimentalPagingApi;

import com.google.android.material.snackbar.Snackbar;

import app.practice.canopy_native_android.R;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.database.entities.TopographicEntity;
import app.practice.canopy_native_android.database.entities.VegetationEntity;
import app.practice.canopy_native_android.databinding.ActivitySurveyDetailBinding;
import app.practice.canopy_native_android.databinding.ItemDetailRowBinding;
import app.practice.canopy_native_android.utils.Constants;
import app.practice.canopy_native_android.viewmodels.AuthViewModel;
import app.practice.canopy_native_android.viewmodels.SurveyViewModel;

@ExperimentalPagingApi
public class SurveyDetailActivity extends AppCompatActivity {

    private ActivitySurveyDetailBinding binding;
    private SurveyViewModel surveyViewModel;
    private AuthViewModel authViewModel;

    private String surveyId;
    private SurveyEntity currentSurvey;
    private boolean isOwner = false;

    private ActivityResultLauncher<Intent> editLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySurveyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        surveyId = getIntent().getStringExtra(MainActivity.EXTRA_SURVEY_ID);
        if (surveyId == null) {
            finish();
            return;
        }

        setupViewModels();
        setupToolbar();
        setupActivityLaunchers();
        observeViewModel();
    }

    private void setupViewModels() {
        surveyViewModel = new ViewModelProvider(this).get(SurveyViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Select survey for observation
        surveyViewModel.selectSurvey(surveyId);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupActivityLaunchers() {
        editLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Refresh data
                        surveyViewModel.refreshSelectedSurvey();

                        Intent data = new Intent();
                        data.putExtra(MainActivity.EXTRA_RESULT_ACTION, MainActivity.ACTION_UPDATED);
                        setResult(RESULT_OK, data);
                    }
                }
        );
    }

    private void observeViewModel() {
        // Observe survey data
        surveyViewModel.getSelectedSurvey().observe(this, survey -> {
            if (survey != null) {
                currentSurvey = survey;
                displaySurvey(survey);
                checkOwnership(survey);
            }
        });

        // Observe section data
        surveyViewModel.getSelectedTopographic().observe(this, this::displayTopographic);
        surveyViewModel.getSelectedVegetation().observe(this, this::displayVegetation);
        surveyViewModel.getSelectedSoil().observe(this, soil -> {
            binding.cardSoil.setVisibility(soil != null ? View.VISIBLE : View.GONE);
            // Add soil details...
        });
        surveyViewModel.getSelectedWater().observe(this, water -> {
            binding.cardWater.setVisibility(water != null ? View.VISIBLE : View.GONE);
        });
        surveyViewModel.getSelectedBiodiversity().observe(this, bio -> {
            binding.cardBiodiversity.setVisibility(bio != null ? View.VISIBLE : View.GONE);
        });
        surveyViewModel.getSelectedHazard().observe(this, hazard -> {
            binding.cardHazard.setVisibility(hazard != null ? View.VISIBLE : View.GONE);
        });
        surveyViewModel.getSelectedInfrastructure().observe(this, infra -> {
            binding.cardInfrastructure.setVisibility(infra != null ? View.VISIBLE : View.GONE);
        });

        // Observe loading
        surveyViewModel.getIsLoading().observe(this, isLoading -> {
            binding.layoutLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Observe delete result
        surveyViewModel.getDeleteResult().observe(this, resource -> {
            if (resource != null && resource.isSuccess()) {
                Intent data = new Intent();
                data.putExtra(MainActivity.EXTRA_RESULT_ACTION, MainActivity.ACTION_DELETED);
                setResult(RESULT_OK, data);
                finish();
            } else if (resource != null && resource.isError()) {
                showError(resource.getMessage());
            }
        });
    }

    private void checkOwnership(SurveyEntity survey) {
        String currentUserId = authViewModel.getCurrentUserId();
        isOwner = currentUserId != null && currentUserId.equals(survey.getObserverId());
        invalidateOptionsMenu();
    }

    private void displaySurvey(SurveyEntity survey) {
        // Update toolbar title
        binding.toolbar.setTitle(survey.getSurveyType() != null ?
                survey.getSurveyType() : getString(R.string.survey_detail));

        // Sync status
        updateSyncStatus(survey.getSyncStatus());

        // Metadata
        setRowValue(binding.rowSurveyType, "Survey Type",
                survey.getSurveyType() != null ? survey.getSurveyType() : "N/A");
        setRowValue(binding.rowObserver, "Observer", survey.getObserverName());
        setRowValue(binding.rowOrganization, "Organization",
                survey.getOrganization() != null ? survey.getOrganization() : "N/A");
        setRowValue(binding.rowDate, "Date", survey.getSurveyDate());
        setRowValue(binding.rowTime, "Time", survey.getSurveyTime());
        setRowValue(binding.rowWeather, "Weather", survey.getWeatherCondition());
        setRowValue(binding.rowMethod, "Method", survey.getSurveyMethod());

        // Location
        String location = "N/A";
        if (survey.getGpsLatitude() != null && survey.getGpsLongitude() != null) {
            location = String.format("%.6f, %.6f", survey.getGpsLatitude(), survey.getGpsLongitude());
        }
        setRowValue(binding.rowLocation, "Location", location);

        // Notes
        setRowValue(binding.rowNotes, "Notes",
                survey.getNotes() != null && !survey.getNotes().isEmpty() ?
                        survey.getNotes() : "No notes");
    }

    private void updateSyncStatus(String status) {
        binding.cardSyncStatus.setVisibility(View.VISIBLE);

        int iconRes;
        int colorRes;
        String statusText;

        switch (status) {
            case Constants.SYNC_SYNCED:
                iconRes = R.drawable.ic_cloud_done;
                colorRes = R.color.sync_synced;
                statusText = "Synced";
                binding.cardSyncStatus.setVisibility(View.GONE);
                break;
            case Constants.SYNC_FAILED:
                iconRes = R.drawable.ic_cloud_off;
                colorRes = R.color.sync_failed;
                statusText = "Sync failed";
                break;
            case Constants.SYNC_PENDING:
            default:
                iconRes = R.drawable.ic_cloud_upload;
                colorRes = R.color.sync_pending;
                statusText = "Pending sync";
                break;
        }

        binding.imageSyncStatus.setImageResource(iconRes);
        binding.imageSyncStatus.setColorFilter(getColor(colorRes));
        binding.textSyncStatus.setText(statusText);
        binding.textSyncStatus.setTextColor(getColor(colorRes));
    }

    private void setRowValue(ItemDetailRowBinding rowBinding, String label, String value) {
        rowBinding.textLabel.setText(label);
        rowBinding.textValue.setText(value);
    }

    private void displayTopographic(TopographicEntity data) {
        if (data == null) {
            binding.cardTopographic.setVisibility(View.GONE);
            return;
        }

        binding.cardTopographic.setVisibility(View.VISIBLE);
        LinearLayout layout = binding.layoutTopographic;

        // Clear previous dynamic views (keep title and divider)
        int childCount = layout.getChildCount();
        if (childCount > 2) {
            layout.removeViews(2, childCount - 2);
        }

        // Add data rows
        addDetailRow(layout, "Elevation", data.getElevationM() != null ?
                data.getElevationM() + " m" : "N/A");
        addDetailRow(layout, "Slope Gradient", data.getSlopeGradientDeg() != null ?
                data.getSlopeGradientDeg() + "°" : "N/A");
        addDetailRow(layout, "Slope Aspect", data.getSlopeAspect());
        addDetailRow(layout, "Landform Type", data.getLandformType());
        addDetailRow(layout, "Drainage Pattern", data.getDrainagePattern());
        addDetailRow(layout, "Land Use", data.getLandUse());
        addDetailRow(layout, "Land Cover", data.getLandCoverDescription());
    }

    private void displayVegetation(VegetationEntity data) {
        if (data == null) {
            binding.cardVegetation.setVisibility(View.GONE);
            return;
        }

        binding.cardVegetation.setVisibility(View.VISIBLE);
        LinearLayout layout = binding.layoutVegetation;

        int childCount = layout.getChildCount();
        if (childCount > 2) {
            layout.removeViews(2, childCount - 2);
        }

        addDetailRow(layout, "Vegetation Type", data.getVegetationType());
        addDetailRow(layout, "Canopy Cover", data.getCanopyCoverPct() != null ?
                data.getCanopyCoverPct() + "%" : "N/A");
        addDetailRow(layout, "Canopy Height", data.getCanopyHeightM() != null ?
                data.getCanopyHeightM() + " m" : "N/A");
        addDetailRow(layout, "Dominant Species", data.getDominantSpecies());
        addDetailRow(layout, "Invasive Species", data.getInvasiveSpeciesPresent() != null &&
                data.getInvasiveSpeciesPresent() ? "Yes" : "No");
        addDetailRow(layout, "NDVI Observation", data.getNdviObservation());
        addDetailRow(layout, "Disturbance", data.getVegetationDisturbance());
    }

    private void addDetailRow(LinearLayout parent, String label, String value) {
        ItemDetailRowBinding rowBinding = ItemDetailRowBinding.inflate(
                getLayoutInflater(), parent, false);
        rowBinding.textLabel.setText(label);
        rowBinding.textValue.setText(value != null ? value : "N/A");
        parent.addView(rowBinding.getRoot());
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_survey_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        // Only show edit/delete if user is owner
        menu.findItem(R.id.action_edit).setVisible(isOwner);
        menu.findItem(R.id.action_delete).setVisible(isOwner);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, CreateSurveyActivity.class);
            intent.putExtra(MainActivity.EXTRA_SURVEY_ID, surveyId);
            editLauncher.launch(intent);
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_survey)
                .setMessage(R.string.delete_survey_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    surveyViewModel.deleteSurvey(surveyId);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message != null ? message : "An error occurred",
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        surveyViewModel.clearSelection();
        binding = null;
    }
}