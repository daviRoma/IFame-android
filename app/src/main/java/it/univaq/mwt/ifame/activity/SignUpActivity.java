package it.univaq.mwt.ifame.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.dialog.LoadingSpinnerDialogFragment;
import it.univaq.mwt.ifame.utility.RestAPI;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = SignUpActivity.class.getName();

    private String email, password;

    private TextInputEditText inputName, inputSurname, inputEmail, inputUsername, inputPassword, inputRePassword;

    private LoadingSpinnerDialogFragment spinnerDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        spinnerDialogFragment = new LoadingSpinnerDialogFragment();

        inputEmail = findViewById(R.id.newUserEmail);
        inputPassword = findViewById(R.id.newUserPassword);
        inputName = findViewById(R.id.newUserName);
        inputSurname = findViewById(R.id.newUserSurname);
        inputUsername = findViewById(R.id.newUserUsername);
        inputRePassword = findViewById(R.id.newUserRePassword);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void doSignUp(View view) {

        email = inputEmail.getText().toString();
        password = inputPassword.getText().toString();

        boolean canSignUp = true;

        if (email.isEmpty()) {
            inputEmail.setError(getString(R.string.email_required));
            canSignUp = false;
        }
        if (password.isEmpty()) {
            inputPassword.setError(getString(R.string.password_required));
            canSignUp = false;
        }
        if (inputName.getText().toString().isEmpty()) {
            inputName.setError(getString(R.string.name_required));
            canSignUp = false;
        }
        if (inputSurname.getText().toString().isEmpty()) {
            inputSurname.setError(getString(R.string.surname_required));
            canSignUp = false;
        }
        if (inputUsername.getText().toString().isEmpty()) {
            inputUsername.setError(getString(R.string.username_required));
            canSignUp = false;
        }
        if (inputRePassword.getText().toString().isEmpty()) {
            inputRePassword.setError(getString(R.string.password_required));
            canSignUp = false;
        }
        if (!inputPassword.getText().toString().equals(inputRePassword.getText().toString())) {
            inputPassword.setError(getString(R.string.password_mismatch));
            inputRePassword.setError(getString(R.string.password_mismatch));
            canSignUp = false;
        }

        if (inputPassword.getText().toString().length() < 6) {
            inputPassword.setError(getString(R.string.password_too_short));
            canSignUp = false;
        }
        if (canSignUp) {
            spinnerDialogFragment.show(getSupportFragmentManager(), "signUpSpinner");
            performSignUp();
        }
    }

    private void performSignUp() {

        try {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", inputUsername.getText().toString());
            jsonBody.put("password", inputPassword.getText().toString());
            jsonBody.put("firstname", inputName.getText().toString());
            jsonBody.put("lastname", inputSurname.getText().toString());
            jsonBody.put("email", inputEmail.getText().toString());

            final String requestBody = jsonBody.toString();

            Log.i(TAG, "[performSignUp]::Request --> " + requestBody);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST, RestAPI.Account.REGISTER,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "[performSignUp]::Response --> " + response);

                            spinnerDialogFragment.dismiss();

                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            spinnerDialogFragment.dismiss();
                            Toast.makeText(SignUpActivity.this, getString(R.string.signup_failed), Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "[performSignUp]::Error Message --> " + error.toString());
                        }
                    })
                    {
                        @Override
                        public String getBodyContentType() {
                            return "application/json";
                        }
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            final Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            return headers;
                        }
                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            spinnerDialogFragment.dismiss();
                            try {
                                return requestBody == null ? null : requestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                                return null;
                            }
                        }

                    };

            // Add the request to the RequestQueue.
            queue.add(stringRequest);

        } catch (JSONException e) {
            spinnerDialogFragment.dismiss();
            e.printStackTrace();
        }
    }

    public void goLogin(View view) {

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
        finish();

    }
}