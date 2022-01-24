package it.univaq.mwt.ifame.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.dialog.LoadingSpinnerDialogFragment;
import it.univaq.mwt.ifame.model.User;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.RestAPI;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();
    private static final String UNAUTHORIZED = "Unauthorized";

    private User user;
    private LoadingSpinnerDialogFragment spinnerDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        spinnerDialogFragment = new LoadingSpinnerDialogFragment();

        if (Preference.loadString(getApplicationContext(), "token", null) != null && Preference.loadString(getApplicationContext(), "username", null) != null) {
            reEnter();
        } else Log.d(TAG, "onCreate: No user logged in");
    }

    public void doLogin(View view) {

        TextInputEditText inputUsername = findViewById(R.id.loginEmail);
        TextInputEditText inputPassword = findViewById(R.id.loginPassword);

        String username = inputUsername.getText().toString();
        String password = inputPassword.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {

            if (username.isEmpty()) inputUsername.setError(getString(R.string.username_required));
            if (password.isEmpty()) inputPassword.setError(getString(R.string.password_required));

        } else {
            spinnerDialogFragment.show(getSupportFragmentManager(), "loginSpinner");

            try {
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(this);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("username", username);
                jsonBody.put("password", password);
                final String requestBody = jsonBody.toString();

                Log.i(TAG, "[doLogin]::Request --> " + requestBody);

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.POST, RestAPI.Auth.LOGIN,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "[doLogin]::Response --> " + response);

                            if (response != null) {

                                try {
                                    JSONObject jsonResponse = new JSONObject(response);

                                    if (jsonResponse.getString("token") != null) {
                                        // Save JWT into shared preferences
                                        Preference.saveString(getApplicationContext(), "token", jsonResponse.getString("token"));

                                        prepareDataAndGoHome(username);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                            }
                            spinnerDialogFragment.dismiss();

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            spinnerDialogFragment.dismiss();
                            Log.e(TAG, "[doLogin]::Error Message --> " + error.toString());
                        }
                    })
                    {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
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
    }

    public void goSignUp(View view) {
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void prepareDataAndGoHome(String username) {
        Log.i(TAG, "[prepareDataAndGoHome]::" + username);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        spinnerDialogFragment.show(getSupportFragmentManager(), "loginSpinner");

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET,  RestAPI.Account.GET + username,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    // Display the first 500 characters of the response string.
                    if (response != null) {
                        Log.i(TAG, "[prepareDataAndGoHome]::Response --> " + response);
                        runOnUiThread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonResponse = new JSONObject(response);
                                    user = new User();
                                    user.username = jsonResponse.getString("username");
                                    user.id = String.valueOf(jsonResponse.getInt("id"));
                                    user.email = jsonResponse.getString("email");
                                    user.name = jsonResponse.getString("firstname");
                                    user.surname = jsonResponse.getString("lastname");
                                    user.avatar = jsonResponse.getString("picture");

                                    JSONArray jArray = jsonResponse.getJSONArray("preferences");
                                    if (jArray != null) {
                                        user.preferences = new ArrayList<>();

                                        for (int i = 0; i < jArray.length();i ++) {
                                            user.preferences.add(jArray.getString(i));
                                        }
                                    }

                                    CurrentUser.user = user;
                                    Preference.saveString(getApplicationContext(), "username", user.username);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                spinnerDialogFragment.dismiss();

                                Intent intent = new Intent(getApplicationContext(), BottomBarControllerActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    spinnerDialogFragment.dismiss();
                    Log.e(TAG, "[prepareDataAndGoHome]::Error Message --> " + error.toString());
                }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void reEnter() {
        Log.i(TAG, "[tokenValidation]");

        spinnerDialogFragment.show(getSupportFragmentManager(), "loginSpinner");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("token", Preference.loadString(getApplicationContext(), "token", null));
            jsonBody.put("username", Preference.loadString(getApplicationContext(), "username", null));
        } catch (JSONException e) {
            spinnerDialogFragment.dismiss();
            e.printStackTrace();
        }

        final String requestBody = jsonBody.toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, RestAPI.Auth.VALIDATE_TOKEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "[tokenValidation]::Response --> " + response);
                        spinnerDialogFragment.dismiss();

                        try {
                            JSONObject result = new JSONObject(response);

                            if (!result.getString("message").equals(UNAUTHORIZED)) {
                                Preference.saveString(getApplicationContext(), "token", result.getString("token"));
                                prepareDataAndGoHome(Preference.loadString(getApplicationContext(), "username", null));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        spinnerDialogFragment.dismiss();
                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "[tokenValidation]::Error Message --> " + error.toString());
                    }
                })
                {
                @Override
                public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    final Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
                @Override
                public byte[] getBody() throws AuthFailureError {
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
    }
}