package br.com.lddm.vigilantesurbanos.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.lddm.vigilantesurbanos.R;
import br.com.lddm.vigilantesurbanos.home.HomeActivity;
import br.com.lddm.vigilantesurbanos.model.User;

public class RegisterActivity extends AppCompatActivity {

    EditText mEmailView;
    EditText mConfirmPasswordView;
    EditText mPasswordView;

    FirebaseAuth mAuth;

    private View mProgressView;
    private View mLoginFormView;
    private CheckBox cbIsOap;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mProgressView  = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);
        cbIsOap = findViewById(R.id.isOap);

        mProgressView.requestFocus();

    }

    public void onBackPressed(View view) {
        this.onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, LoginActivity.class));
    }

    public void attemptRegister(View view) {

        mEmailView = findViewById(R.id.email);
        mPasswordView = findViewById(R.id.password);
        mConfirmPasswordView = findViewById(R.id.confirm_password);
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mConfirmPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String confirmPassword = mConfirmPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        if (!TextUtils.isEmpty(confirmPassword) && !confirmPassword.equals(password)) {
            mConfirmPasswordView.setError(getString(R.string.error_confirm_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        showProgress(false);
                        if(task.isSuccessful()) {
                            if (cbIsOap.isChecked()) {
                                FirebaseUser user = Objects.requireNonNull(task.getResult()).getUser();

                                db.collection("user")
                                        .document(Objects.requireNonNull(user.getEmail()))
                                        .set(new User(false, user.getUid()));
                            }
                            startActivity(new Intent(getBaseContext(), HomeActivity.class));
                        }
                    });
        }
    }
    private boolean isEmailValid(String email) {
        Matcher matcher = Pattern
                .compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE)
                .matcher(email);

        return matcher.find();

    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
