// app/src/main/java/app/practice/native_java_mvvm/views/activities/LoginActivity.java

package app.practice.canopy_native_android.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ExperimentalPagingApi;

import app.practice.canopy_native_android.databinding.ActivityLoginBinding;
import app.practice.canopy_native_android.utils.Resource;
import app.practice.canopy_native_android.viewmodels.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupViews();
        observeViewModel();
    }

    private void setupViews() {
        // Login button click
        binding.buttonLogin.setOnClickListener(v -> attemptLogin());

        // Enter key on password field
        binding.editPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin();
                return true;
            }
            return false;
        });

        // Register link
        binding.buttonRegisterLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void observeViewModel() {
        // Observe login result
        viewModel.getLoginResult().observe(this, resource -> {
            if (resource == null) return;

            switch (resource.getStatus()) {
                case LOADING:
                    showLoading(true);
                    hideError();
                    break;

                case SUCCESS:
                    showLoading(false);
                    navigateToMain();
                    break;

                case ERROR:
                    showLoading(false);
                    showError(resource.getMessage());
                    break;
            }
        });
    }

    private void attemptLogin() {
        String email = binding.editEmail.getText().toString().trim();
        String password = binding.editPassword.getText().toString();

        // Validate
        if (!viewModel.isValidEmail(email)) {
            binding.layoutEmail.setError(getString(app.practice.canopy_native_android.R.string.error_email_invalid));
            return;
        }
        binding.layoutEmail.setError(null);

        if (!viewModel.isValidPassword(password)) {
            binding.layoutPassword.setError(getString(app.practice.canopy_native_android.R.string.error_password_short));
            return;
        }
        binding.layoutPassword.setError(null);

        // Attempt login
        viewModel.login(email, password);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.buttonLogin.setEnabled(!show);
        binding.editEmail.setEnabled(!show);
        binding.editPassword.setEnabled(!show);
    }

    private void showError(String message) {
        binding.textError.setText(message);
        binding.textError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.textError.setVisibility(View.GONE);
    }

    @OptIn(markerClass = ExperimentalPagingApi.class)
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}