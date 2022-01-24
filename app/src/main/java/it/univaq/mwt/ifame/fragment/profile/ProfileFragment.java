package it.univaq.mwt.ifame.fragment.profile;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.activity.LoginActivity;
import it.univaq.mwt.ifame.dialog.EditPasswordDialogFragment;
import it.univaq.mwt.ifame.dialog.EditTagPreferencesDialogFragment;
import it.univaq.mwt.ifame.dialog.LoadingSpinnerDialogFragment;
import it.univaq.mwt.ifame.dialog.SelectCityDialog;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.ImageManager;
import it.univaq.mwt.ifame.utility.PermissionManager;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.PreferenceKey;
import it.univaq.mwt.ifame.utility.RestAPI;

import static android.app.Activity.RESULT_OK;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    static final int REQUEST_IMAGE_GET = 1;

    private static final String TAG = ProfileFragment.class.getName();

    private CoordinatorLayout coordinatorLayout;
    private ConstraintLayout constraintLayout;
    private TextInputLayout usernameInput, emailInput;
    private FloatingActionButton buttonEditProfile, buttonChangeImage;
    private Button buttonChangePassword, buttonEditPreferenceTags, buttonLogOut;
    private ImageView profileImage;
    private TextInputEditText editUsername, editEmail;
    private FlexboxLayout flexBoxLayoutPreferences;
    private LoadingSpinnerDialogFragment imageUpdateSpinner;
    private TextView name, surname;

    private Bitmap bitmap;
    private Boolean avatarChanged = false;
    private String locale = Locale.getDefault().getCountry();

    private boolean editingMode = false;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = view.findViewById(R.id.textName);
        surname = view.findViewById(R.id.textSurname);
        profileImage = view.findViewById(R.id.avatarImage);

        if (CurrentUser.user.avatar != null && !CurrentUser.user.avatar.isEmpty()) {
            profileImage.setImageBitmap(ImageManager.getInstance(getContext()).loadImage(CurrentUser.user.avatar));
        }

        final TextView cityView = view.findViewById(R.id.cityViewName);

        name.setText(CurrentUser.user.name);
        surname.setText(CurrentUser.user.surname);

        coordinatorLayout = view.findViewById(R.id.coordinatorLayoutProfile);
        buttonEditProfile = view.findViewById(R.id.buttonEditProfile);
        buttonChangePassword = view.findViewById(R.id.buttonChangePassword);
        buttonEditPreferenceTags = view.findViewById(R.id.buttonEditPreferences);
        buttonLogOut = view.findViewById(R.id.buttonLogOut);
        buttonChangeImage = view.findViewById(R.id.changeImageFab);
        buttonChangeImage.setVisibility(View.GONE);

        if (Preference.loadString(getContext(), PreferenceKey.USER_CITY.toString(), "").equals(""))
            cityView.setText("Select your city");
        else
            cityView.setText(Preference.loadString(getContext(), PreferenceKey.USER_CITY.toString(), ""));

        flexBoxLayoutPreferences = view.findViewById(R.id.flexBoxPreferences);

        constraintLayout = view.findViewById(R.id.constraintLayout);

        TransitionManager.beginDelayedTransition(constraintLayout);

        usernameInput = view.findViewById(R.id.inputLayoutUsername);
        emailInput = view.findViewById(R.id.inputLayoutEmail);

        editEmail = view.findViewById(R.id.inputEmailLayout);
        editUsername = view.findViewById(R.id.inputUsernameLayout);

        editEmail.setText(CurrentUser.user.email);
        editUsername.setText(CurrentUser.user.username);

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (editingMode) {

                    updateUserInfo();

                    usernameInput.setEnabled(false);
                    emailInput.setEnabled(false);
                    buttonEditProfile.setImageResource(R.drawable.ic_edit);
                    buttonEditProfile.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                    buttonChangePassword.setVisibility(View.GONE);
                    buttonChangeImage.setVisibility(View.GONE);

                } else {

                    usernameInput.setEnabled(true);
                    emailInput.setEnabled(true);
                    buttonEditProfile.setImageResource(R.drawable.ic_save);
                    buttonEditProfile.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.saveButton)));
                    buttonChangePassword.setVisibility(View.VISIBLE);
                    buttonChangeImage.setVisibility(View.VISIBLE);

                }
                editingMode = !editingMode;

            }
        });

        buttonChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditPasswordDialogFragment bottomSheet = new EditPasswordDialogFragment();
                bottomSheet.show(getChildFragmentManager(),
                        "Password Changed");

            }
        });

        buttonEditPreferenceTags.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EditTagPreferencesDialogFragment bottomSheet = new EditTagPreferencesDialogFragment(new EditTagPreferencesDialogFragment.OnDismissPreferencesEditor() {
                    @Override
                    public void onDismiss() {
                        readPreferencesFromUser();
                    }
                });
                bottomSheet.show(getChildFragmentManager(),
                        "Preference Edited");

            }
        });

        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                Preference.saveString(getContext(), "token", null);
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        buttonChangeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionManager.isReadStoragePermissionGranted(requireContext())) {
                    getImage();
                } else {
                    PermissionManager.requestReadStoragePermission(ProfileFragment.this);
                }
            }
        });

        Button changeCity = view.findViewById(R.id.changePosition);
        changeCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectCityDialog dialog = new SelectCityDialog(new SelectCityDialog.OnCitySelected() {
                    @Override
                    public void onSelection(String cityName) {
                        cityView.setText(cityName);
                    }
                });

                dialog.show(getChildFragmentManager(), "selectCityDialog");
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        readPreferencesFromUser();

    }

    private void updateUserInfo() {

        if (!editUsername.getText().toString().equals(CurrentUser.user.username)) {
            updateUserAPI(editUsername.getText().toString());
        } else {
            updateUserAPI(null);
        }

    }


    private void readPreferencesFromUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                populatePreferences();
            }
        }).start();
    }

    private void populatePreferences() {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                flexBoxLayoutPreferences.removeAllViews();
            }
        });

        for (String category : CurrentUser.user.preferences) {

            final Chip chip = new Chip(getContext());

            chip.setText(category);
            chip.setTextColor(getResources().getColor(R.color.chipColor));

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    flexBoxLayoutPreferences.addView(chip);
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {

            imageUpdateSpinner = new LoadingSpinnerDialogFragment();
            imageUpdateSpinner.show(getParentFragmentManager(), "imageSpinner");

            Uri fullPhotoUri = data.getData();

            uploadImage(fullPhotoUri);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "onRequestPermissionsResult: PERMISSION RESULT");

        if (requestCode == PermissionManager.REQUEST_READ_STORAGE_PERMISSION_CODE) {
            if (PermissionManager.isReadStoragePermissionGranted(getContext())) {
                getImage();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: PERMISSION RESULT ELSE");

                Snackbar.make(coordinatorLayout, getString(R.string.permission_required_storage), BaseTransientBottomBar.LENGTH_LONG)
                        .setAction(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (PermissionManager.isReadStoragePermissionGranted(requireContext())) {
                                    getImage();
                                } else {
                                    PermissionManager.requestReadStoragePermission(ProfileFragment.this);
                                }
                            }
                        }).show();
            }
        }
    }

    private void updateUserAPI(String username) {

        try {
            Log.i(TAG, "[updateUserAPI]::Calling account/update");

            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(getContext());

            JSONObject jsonBody = new JSONObject();

            // username changed
            if (username != null) jsonBody.put("username", username);

            // email changed
            if (!editEmail.getText().toString().equals(CurrentUser.user.email)) {
                jsonBody.put("email", editEmail.getText().toString());
            }
            // avatar changed
            if (avatarChanged) jsonBody.put("picture", ImageManager.getInstance(getContext()).toBase64(bitmap));

            final String requestBody = jsonBody.toString();

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.PUT, RestAPI.Account.UPDATE + CurrentUser.user.id,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "[updateUserAPI]::Response --> " + response);
                            if (!editEmail.getText().toString().equals(CurrentUser.user.email)) CurrentUser.user.email = editEmail.getText().toString();
                            if (username != null) {
                                CurrentUser.user.username = username;
                                reAuthentication();
                            }
                            if (avatarChanged) CurrentUser.user.avatar = ImageManager.getInstance(getContext()).toBase64(bitmap);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "[updateUserAPI]::Response Message --> " + error.toString());
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
                    headers.put("Authorization", "Bearer " + Preference.loadString(getActivity().getApplicationContext(), "token", null));
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

    private void uploadImage(final Uri fileUri) {

        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), fileUri);
            avatarChanged = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_GET);
    }

    private void reAuthentication() {
        Log.i(TAG, "[reAuthentication]");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("token", Preference.loadString(getContext(), "token", null));
            jsonBody.put("username", CurrentUser.user.username);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String requestBody = jsonBody.toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, RestAPI.Auth.VALIDATE_TOKEN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "[reAuthentication]::Response --> " + response);

                        try {
                            JSONObject result = new JSONObject(response);
                            Preference.saveString(getContext(), "token", result.getString("token"));
                            Preference.saveString(getContext(), "username", CurrentUser.user.username);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "[reAuthentication]::Error Message --> " + error.toString());
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