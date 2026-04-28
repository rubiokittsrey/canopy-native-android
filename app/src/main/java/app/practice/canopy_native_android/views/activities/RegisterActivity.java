// app/src/main/java/app/practice/native_java_mvvm/views/activities/RegisterActivity.java

package app.practice.canopy_native_android.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import app.practice.canopy_native_android.R;
import app.practice.canopy_native_android.databinding.ActivityRegisterBinding;
import app.practice.canopy_native_android.viewmodels.AuthViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupViews();
        observeViewModel();
    }

    private void setupViews() {
        // Back button
        binding.buttonBack.setOnClickListener(v -> finish());

        // Register button
        binding.buttonRegister.setOnClickListener(v -> attemptRegister());

        // Login link
        binding.buttonLoginLink.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        viewModel.getRegisterResult().observe(this, resource -> {
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

    private void attemptRegister() {
        String fullName = binding.editFullName.getText().toString().trim();
        String email = binding.editEmail.getText().toString().trim();
        String organization = binding.editOrganization.getText().toString().trim();
        String phone = binding.editPhone.getText().toString().trim();
        String password = binding.editPassword.getText().toString();
        String confirmPassword = binding.editConfirmPassword.getText().toString();

        // Validate
        boolean valid = true;

        if (!viewModel.isValidFullName(fullName)) {
            binding.layoutFullName.setError(getString(R.string.error_name_required));
            valid = false;
        } else {
            binding.layoutFullName.setError(null);
        }

        if (!viewModel.isValidEmail(email)) {
            binding.layoutEmail.setError(getString(R.string.error_email_invalid));
            valid = false;
        } else {
            binding.layoutEmail.setError(null);
        }

        if (!viewModel.isValidPassword(password)) {
            binding.layoutPassword.setError(getString(R.string.error_password_short));
            valid = false;
        } else {
            binding.layoutPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            binding.layoutConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valid = false;
        } else {
            binding.layoutConfirmPassword.setError(null);
        }

        if (!valid) return;

        // Attempt registration
        viewModel.register(email, password, fullName,
                organization.isEmpty() ? null : organization,
                phone.isEmpty() ? null : phone);
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.buttonRegister.setEnabled(!show);
    }

    private void showError(String message) {
        binding.textError.setText(message);
        binding.textError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        binding.textError.setVisibility(View.GONE);
    }

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