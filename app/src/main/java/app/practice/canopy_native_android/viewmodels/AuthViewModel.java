package app.practice.canopy_native_android.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.List;

import app.practice.canopy_native_android.database.entities.UserEntity;
import app.practice.canopy_native_android.repositories.AuthRepository;
import app.practice.canopy_native_android.utils.Resource;

/// ViewModel for authentication screens (Login, Register, Profile)
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    // current user (from local)
    private final MediatorLiveData<UserEntity> currentUser = new MediatorLiveData<>();

    // auth operation results
    private final MutableLiveData<Resource<UserEntity>> loginResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<UserEntity>> registerResult = new MutableLiveData<>();

    // ui state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    // Track observers for cleanup
    private final List<Runnable> observerCleanups = new ArrayList<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);

        // load cached user if logged in
        if (authRepository.isLoggedIn()) {
            currentUser.addSource(authRepository.getCachedUser(), currentUser::setValue);
        }
    }

    // -- auth operations

    public void login(String email, String password) {
        isLoading.setValue(true);
        loginResult.setValue(Resource.loading());

        LiveData<Resource<UserEntity>> source = authRepository.login(email, password);
        observeUntilComplete(source, resource -> {
            loginResult.setValue(resource);

            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);

                if (resource.isSuccess() && resource.getData() != null) {
                    currentUser.setValue(resource.getData());
                }
            }
        });
    }

    public void register(String email, String password, String fullName,
                         String organization, String phoneNumber) {
        isLoading.setValue(true);
        registerResult.setValue(Resource.loading());

        LiveData<Resource<UserEntity>> source = authRepository.register(
                email, password, fullName, organization, phoneNumber
        );
        observeUntilComplete(source, resource -> {
            registerResult.setValue(resource);

            if (resource.getStatus() != Resource.Status.LOADING) {
                isLoading.setValue(false);

                if (resource.isSuccess() && resource.getData() != null) {
                    currentUser.setValue(resource.getData());
                }
            }
        });
    }

    public void logout() {
        authRepository.logout(false);  // keep local survey data
        currentUser.setValue(null);
        loginResult.setValue(null);
        registerResult.setValue(null);
    }

    public void logoutAndClearData() {
        authRepository.logout(true);  // clear all local data
        currentUser.setValue(null);
        loginResult.setValue(null);
        registerResult.setValue(null);
    }

    // -- getter

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    public String getCurrentUserId() {
        return authRepository.getCurrentUserId();
    }

    public LiveData<UserEntity> getCurrentUser() {
        return currentUser;
    }

    public LiveData<Resource<UserEntity>> getLoginResult() {
        return loginResult;
    }

    public LiveData<Resource<UserEntity>> getRegisterResult() {
        return registerResult;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // -- validation

    public boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public boolean isValidFullName(String fullName) {
        return fullName != null && fullName.trim().length() >= 2;
    }

    // clear any error states (call when navigating away)
    public void clearResults() {
        loginResult.setValue(null);
        registerResult.setValue(null);
    }

    /**
     * Observe a LiveData source, forwarding values to the callback,
     * and auto-removing the observer after a terminal (non-LOADING) state.
     */
    private <T> void observeUntilComplete(LiveData<Resource<T>> source,
                                           Observer<Resource<T>> callback) {
        Observer<Resource<T>>[] holder = new Observer[1];
        holder[0] = resource -> {
            callback.onChanged(resource);
            if (resource.getStatus() != Resource.Status.LOADING) {
                source.removeObserver(holder[0]);
            }
        };
        source.observeForever(holder[0]);
        observerCleanups.add(() -> source.removeObserver(holder[0]));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        for (Runnable cleanup : observerCleanups) {
            cleanup.run();
        }
        observerCleanups.clear();
    }
}
