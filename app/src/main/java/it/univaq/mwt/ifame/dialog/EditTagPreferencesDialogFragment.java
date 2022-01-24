package it.univaq.mwt.ifame.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.adapter.PreferenceTagAdapter;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.RestAPI;

public class EditTagPreferencesDialogFragment extends BottomSheetDialogFragment {

    private static final String TAG = EditTagPreferencesDialogFragment.class.getSimpleName();

    private OnDismissPreferencesEditor onDismissPreferencesEditor;
    private ArrayList<String> categories = new ArrayList<>();

    private PreferenceTagAdapter tagsAdapter;

    private List<String> userCategory = new ArrayList<>();


    public EditTagPreferencesDialogFragment(OnDismissPreferencesEditor onDismissPreferencesEditor) {
        this.onDismissPreferencesEditor = onDismissPreferencesEditor;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_tag_preferences_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonSaveTag = view.findViewById(R.id.buttonSaveTag);

        RecyclerView tagsContainer = view.findViewById(R.id.tagsContainer);
        tagsContainer.setLayoutManager(new LinearLayoutManager(getContext()));

        tagsAdapter = new PreferenceTagAdapter(categories, userCategory);
        tagsContainer.setAdapter(tagsAdapter);

        buttonSaveTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        getString(R.string.preferences_updated), Toast.LENGTH_SHORT)
                        .show();

                updatePreferences(tagsAdapter.getSelected());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        readFromAPI();

    }

    private void readFromAPI() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                categories.clear();

                Log.i(TAG, "[readFromAPI]::Calling ifame/foodcategories/all");

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getContext());

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET,  RestAPI.Ifame.GET_FOODCATEGORIES,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.i(TAG, "[readFromAPI]::Response --> " + response);

                                if (response != null) {
                                    try {
                                        JSONArray jsonResponse = new JSONArray(response);

                                        for (int i = 0; i < jsonResponse.length(); i++) {
                                            categories.add(jsonResponse.getString(i).toLowerCase(Locale.ROOT));
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    userCategory = CurrentUser.user.preferences;

                                    tagsAdapter.setupAdapter(categories, userCategory);

                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tagsAdapter.notifyDataSetChanged();
                                        }
                                    });

                                }

                            }

                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "[readFromAPI]::Error Message --> " + error.toString());
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
                                headers.put("Authorization", "Bearer " + Preference.loadString(getContext(), "token", null));
                                return headers;
                            }
                        };
                // Add the request to the RequestQueue.
                queue.add(stringRequest);
            }
        }).start();
    }

    private void updatePreferences(ArrayList<String> selectedCategories) {

        Map<String, Object> updateUser = new HashMap<>();
        List<String> selected = new ArrayList<>();
        for (String category : selectedCategories) {
            selected.add(category);
        }
        updateUser.put("preferences", selected);

        try {
            Log.i(TAG, "[updatePreferences]::Calling account/update");

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getContext());

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("preferences", new JSONArray(selected));

            final String requestBody = jsonBody.toString();
            Log.i(TAG, "[updatePreferences]::requestBody ---> " + requestBody);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.PUT, RestAPI.Account.UPDATE + CurrentUser.user.id,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "[updatePreferences]::Response OK");
                            CurrentUser.user.preferences = selected;
                            dismiss();
                            onDismissPreferencesEditor.onDismiss();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "[updatePreferences]::Response Message --> " + error.toString());
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

    public interface OnDismissPreferencesEditor {

        void onDismiss();

    }

}