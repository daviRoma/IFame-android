package it.univaq.mwt.ifame.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.activity.LoginActivity;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.RestAPI;

public class EditPasswordDialogFragment extends BottomSheetDialogFragment {

    private static final String TAG = EditPasswordDialogFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_password_dialog_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextInputEditText inputOldPassword = view.findViewById(R.id.oldPassword);
        final TextInputEditText inputNewPassword = view.findViewById(R.id.newPassword);
        final TextInputEditText inputRetypePassword = view.findViewById(R.id.retypePassword);

        Button buttonSaveChanges = view.findViewById(R.id.buttonSaveChanges);

        buttonSaveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (inputNewPassword.getText().toString().equals(inputRetypePassword.getText().toString()) && !inputNewPassword.getText().toString().isEmpty()){
                    changePassword(inputOldPassword.getText().toString(), inputNewPassword.getText().toString());
                }else {
                    inputRetypePassword.setError(getString(R.string.password_mismatch));
                }
            }
        });
    }

    private void changePassword(String oldPassword, final String newPassword) {

        try {
            Log.i(TAG, "[changePassword]::Calling account/update");

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getContext());

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("password", newPassword);

            final String requestBody = jsonBody.toString();

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.PUT, RestAPI.Account.UPDATE + CurrentUser.user.id,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "[changePassword]::Response --> " + response);
                            Toast.makeText(getActivity(),
                                    getString(R.string.password_updated), Toast.LENGTH_SHORT)
                                    .show();
                            dismiss();

                            // Re-authenticate
                            Preference.saveString(getContext(), "token", null);
                            Intent intent = new Intent(getContext(), LoginActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "[updateUserEmail]::Response Message --> " + error.toString());
                            Toast.makeText(getActivity(),
                                    getString(R.string.password_mismatch), Toast.LENGTH_SHORT)
                                    .show();
                            dismiss();
                        }
                    }) {
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
            e.printStackTrace();
        }

    }

}
