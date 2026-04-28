package app.practice.canopy_native_android.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ExperimentalPagingApi;
import androidx.paging.LoadState;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import app.practice.canopy_native_android.R;
import app.practice.canopy_native_android.database.entities.SurveyEntity;
import app.practice.canopy_native_android.databinding.ActivityMainBinding;
import app.practice.canopy_native_android.repositories.SurveyRepository;
import app.practice.canopy_native_android.viewmodels.AuthViewModel;
import app.practice.canopy_native_android.viewmodels.SurveyViewModel;
import app.practice.canopy_native_android.views.adapters.SurveyAdapter;
import app.practice.canopy_native_android.views.adapters.SurveyLoadStateAdapter;
import app.practice.canopy_native_android.views.dialogs.FilterBottomSheetDialog;

@ExperimentalPagingApi
public class MainActivity extends AppCompatActivity implements SurveyAdapter.OnSurveyClickListener {

    private ActivityMainBinding binding;
    private SurveyViewModel surveyViewModel;
    private AuthViewModel authViewModel;
    private SurveyAdapter adapter;

    // Debounce handler for search
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DEBOUNCE_MS = 300;

    // Activity result launchers
    private ActivityResultLauncher<Intent> createSurveyLauncher;
    private ActivityResultLauncher<Intent> detailLauncher;

    public static final String EXTRA_SURVEY_ID = "survey_id";
    public static final String EXTRA_RESULT_ACTION = "result_action";
    public static final String ACTION_CREATED = "created";
    public static final String ACTION_UPDATED = "updated";
    public static final String ACTION_DELETED = "deleted";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewModels();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupChips();
        setupFab();
        setupSwipeRefresh();
        setupActivityLaunchers();
        observeViewModel();
    }

    private void setupViewModels() {
        surveyViewModel = new ViewModelProvider(this).get(SurveyViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Set current user ID for "My Surveys" filter
        String userId = authViewModel.getCurrentUserId();
        if (userId != null) {
            surveyViewModel.setCurrentUserId(userId);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    private void setupRecyclerView() {
        adapter = new SurveyAdapter(this);

        // Add load state footer for infinite scroll loading indicator
        binding.recyclerSurveys.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerSurveys.setAdapter(
                adapter.withLoadStateFooter(new SurveyLoadStateAdapter(adapter::retry))
        );

        // Observe load states for empty/loading/error states
        adapter.addLoadStateListener(loadStates -> {
            LoadState refresh = loadStates.getRefresh();
            LoadState append = loadStates.getAppend();

            // Show/hide empty state
            boolean isEmpty = refresh instanceof LoadState.NotLoading &&
                    adapter.getItemCount() == 0;
            binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerSurveys.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

            // Show/hide loading
            boolean isLoading = refresh instanceof LoadState.Loading;
            binding.progressBar.setVisibility(isLoading && adapter.getItemCount() == 0
                    ? View.VISIBLE : View.GONE);

            // Handle errors
            if (refresh instanceof LoadState.Error) {
                LoadState.Error error = (LoadState.Error) refresh;
                showError(error.getError().getLocalizedMessage());
            }

            // Stop swipe refresh
            if (!(refresh instanceof LoadState.Loading)) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }

            return null;
        });
    }

    private void setupSearch() {
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Debounce search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> surveyViewModel.setSearchQuery(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }
        });
    }

    private void setupChips() {
        binding.chipAllSurveys.setOnClickListener(v -> {
            binding.chipAllSurveys.setChecked(true);
            binding.chipMySurveys.setChecked(false);
            surveyViewModel.showAllSurveys();
        });

        binding.chipMySurveys.setOnClickListener(v -> {
            binding.chipAllSurveys.setChecked(false);
            binding.chipMySurveys.setChecked(true);
            surveyViewModel.showMySurveys();
        });

        binding.chipFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateSurveyActivity.class);
            createSurveyLauncher.launch(intent);
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary);
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            surveyViewModel.forceRefresh();
        });
    }

    private void setupActivityLaunchers() {
        createSurveyLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String action = result.getData().getStringExtra(EXTRA_RESULT_ACTION);
                        if (ACTION_CREATED.equals(action)) {
                            showSuccess("Survey created");
                            adapter.refresh();
                        }
                    }
                }
        );

        detailLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String action = result.getData().getStringExtra(EXTRA_RESULT_ACTION);
                        if (ACTION_UPDATED.equals(action)) {
                            showSuccess("Survey updated");
                            adapter.refresh();
                        } else if (ACTION_DELETED.equals(action)) {
                            showSuccess("Survey deleted");
                            adapter.refresh();
                        }
                    }
                }
        );
    }

    private void observeViewModel() {
        // Observe paged surveys
        surveyViewModel.getPagedSurveys().observe(this, pagingData -> {
            adapter.submitData(getLifecycle(), pagingData);
        });

        // Observe pending count for sync status bar
        surveyViewModel.getPendingCount().observe(this, count -> {
            if (count != null && count > 0) {
                binding.cardSyncStatus.setVisibility(View.VISIBLE);
                binding.textSyncStatus.setText(getString(R.string.pending_surveys, count));
            } else {
                binding.cardSyncStatus.setVisibility(View.GONE);
            }
        });

        // Observe sync result
        surveyViewModel.getSyncResult().observe(this, resource -> {
            if (resource == null) return;

            if (resource.isSuccess() && resource.getData() != null) {
                SurveyRepository.SyncResult result = resource.getData();
                if (result.isFullSuccess()) {
                    showSuccess(getString(R.string.sync_complete));
                } else {
                    showError(result.failed + " surveys failed to sync");
                }
                adapter.refresh();
            } else if (resource.isError()) {
                showError(resource.getMessage());
            }
        });

        // Observe refresh result (pull-to-refresh)
        surveyViewModel.getRefreshResult().observe(this, resource -> {
            if (resource == null) return;

            if (resource.isSuccess()) {
                adapter.refresh();
            } else if (resource.isError()) {
                showError(resource.getMessage());
            }
        });

        // Observe clear cache result
        surveyViewModel.getClearCacheResult().observe(this, resource -> {
            if (resource == null) return;

            if (resource.isSuccess()) {
                showSuccess(getString(R.string.cache_cleared));
                adapter.refresh();
            } else if (resource.isError()) {
                showError(resource.getMessage());
            }
        });

        // Setup sync button
        binding.buttonSync.setOnClickListener(v -> surveyViewModel.syncPendingSurveys());
    }

    private void showFilterDialog() {
        FilterBottomSheetDialog dialog = new FilterBottomSheetDialog();
        dialog.setFilterListener(filter -> {
            surveyViewModel.applyFilter(filter);
        });
        dialog.show(getSupportFragmentManager(), "filter");
    }

    @Override
    public void onSurveyClick(SurveyEntity survey) {
        Intent intent = new Intent(this, SurveyDetailActivity.class);
        intent.putExtra(EXTRA_SURVEY_ID, survey.getSurveyId());
        detailLauncher.launch(intent);
    }

    @Override
    public void onSurveyLongClick(SurveyEntity survey) {
        // Show options menu (edit, delete)
        new AlertDialog.Builder(this)
                .setTitle(survey.getSurveyType() != null ? survey.getSurveyType() : "Survey")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        // Edit
                        Intent intent = new Intent(this, CreateSurveyActivity.class);
                        intent.putExtra(EXTRA_SURVEY_ID, survey.getSurveyId());
                        createSurveyLauncher.launch(intent);
                    } else {
                        // Delete
                        confirmDelete(survey);
                    }
                })
                .show();
    }

    private void confirmDelete(SurveyEntity survey) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_survey)
                .setMessage(R.string.delete_survey_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    surveyViewModel.deleteSurvey(survey.getSurveyId());
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            surveyViewModel.syncPendingSurveys();
            return true;
        } else if (id == R.id.action_clear_cache) {
            confirmClearCache();
            return true;
        } else if (id == R.id.action_logout) {
            confirmLogout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton(R.string.logout, (dialog, which) -> {
                    authViewModel.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmClearCache() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_cache)
                .setMessage(R.string.clear_cache_confirm)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    surveyViewModel.clearCache();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showSuccess(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message != null ? message : "An error occurred",
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}