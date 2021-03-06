package ch.manuelroth.gadgetothek_android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ch.manuelroth.gadgetothek_android.library.Callback;
import ch.manuelroth.gadgetothek_android.library.LibraryService;


public class RegisterActivity extends Activity {

    // UI references.
    private EditText name;
    private EditText email;
    private EditText studentNumber;
    private EditText firstPassword;
    private EditText secondPassword;
    private View registerFormView;
    private View registerProgressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the register form.
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        studentNumber = (EditText) findViewById(R.id.matrikelnr);
        firstPassword = (EditText) findViewById(R.id.first_password);
        firstPassword.setTypeface(Typeface.DEFAULT);
        secondPassword = (EditText) findViewById(R.id.second_password);
        secondPassword.setTypeface(Typeface.DEFAULT);
        secondPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                    attemptRegister();
                    handled = true;
                }
                return handled;
            }
        });

        // Listener for registerButton
        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        registerFormView = findViewById(R.id.register_form);
        registerProgressView = findViewById(R.id.register_progress);
    }

    public void attemptRegister() {
        // Reset errors.
        name.setError(null);
        email.setError(null);
        studentNumber.setError(null);
        firstPassword.setError(null);
        secondPassword.setError(null);

        // Store values at the time of the login attempt.
        String name = this.name.getText().toString();
        String email = this.email.getText().toString();
        String matrikelNr = studentNumber.getText().toString();
        String firstPassword = this.firstPassword.getText().toString();
        String secondPassword = this.secondPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a name
        if (name.isEmpty()) {
            this.name.setError(getString(R.string.error_noname));
            focusView = this.name;
            cancel = true;
        }

        // Check for matrikelNr
        if (matrikelNr.isEmpty()) {
            studentNumber.setError(getString(R.string.error_nomatriklnr));
            focusView = this.studentNumber;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            this.email.setError(getString(R.string.error_no_email));
            focusView = this.email;
            cancel = true;
        } else if (!isEmailValid(email)) {
            this.email.setError(getString(R.string.error_invalid_email));
            focusView = this.email;
            cancel = true;
        }

        // Check for a valid firstPassword, if the user entered one.
        if (TextUtils.isEmpty(firstPassword)) {
            this.firstPassword.setError(getString(R.string.error_no_password));
            focusView = this.firstPassword;
            cancel = true;
        } else if (!isPasswordValid(firstPassword)) {
            this.firstPassword.setError(getString(R.string.error_invalid_password));
            focusView = this.firstPassword;
            cancel = true;
        }

        // Check for a valid secondPassword, if the user entered one.
        if (TextUtils.isEmpty(secondPassword)) {
            this.secondPassword.setError(getString(R.string.error_no_password));
            focusView = this.secondPassword;
            cancel = true;
        } else if (!isPasswordValid(secondPassword)) {
            this.secondPassword.setError(getString(R.string.error_invalid_password));
            focusView = this.secondPassword;
            cancel = true;
        }


        // Check for equality of both passwords
        if (!firstPassword.equals(secondPassword)) {
            this.secondPassword.setError(getString(R.string.error_passwords_notequal));
            focusView = this.secondPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            LibraryService.register(email, firstPassword, name, matrikelNr, new Callback<Boolean>() {
                @Override
                public void notfiy(Boolean input) {
                    if (input) {
                        showProgress(false);
                        Context context = RegisterActivity.this.getApplicationContext();
                        CharSequence text = "Registration erfolgreich";
                        int duration = Toast.LENGTH_SHORT;
                        Toast.makeText(context, text, duration).show();
                        finish();
                    } else {
                        showProgress(false);
                        Context context = RegisterActivity.this.getApplicationContext();
                        CharSequence text = "Registration nicht erfolgreich";
                        int duration = Toast.LENGTH_SHORT;
                        Toast.makeText(context, text, duration).show();
                    }
                }
            });

        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            registerFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            registerProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            registerProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    registerProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            registerProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
