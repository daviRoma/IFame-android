package it.univaq.mwt.ifame.activity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.adapter.RestaurantAdapter;
import it.univaq.mwt.ifame.dialog.ChooseRestaurantDialogFragment;
import it.univaq.mwt.ifame.dialog.DatePickerDialogFragment;
import it.univaq.mwt.ifame.dialog.LoadingSpinnerDialogFragment;
import it.univaq.mwt.ifame.dialog.TimePickerDialogFragment;
import it.univaq.mwt.ifame.model.Event;
import it.univaq.mwt.ifame.model.Participant;
import it.univaq.mwt.ifame.model.Restaurant;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.ImageManager;
import it.univaq.mwt.ifame.utility.PermissionManager;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.RestAPI;
import it.univaq.mwt.ifame.utility.Utils;

public class NewEventActivity extends AppCompatActivity {

    private static final String TAG = NewEventActivity.class.getSimpleName();

    static final int REQUEST_IMAGE_GET = 1;

    private EditText title, participants, message;
    private SwitchMaterial includeMe;
    private LinearLayout timePicker, datePicker, buttonPickerRestaurant;
    private TextView dayView, hourView, restaurantView;
    private CardView imageCard;
    private ImageView eventImage;
    private ConstraintLayout constraintLayout;

    private String date = null;
    private String hour = null;
    private String restaurantId = null;
    private String restaurantName = null;

    private Restaurant selectedRestaurant;

    private Participant participant;
    private Map<String, Object> event;

    private Uri imageUri;

    private LoadingSpinnerDialogFragment loadingSpinnerDialogFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        loadingSpinnerDialogFragment = new LoadingSpinnerDialogFragment();

        constraintLayout = findViewById(R.id.constraintLayoutNewEvent);
        title = findViewById(R.id.inputTitle);
        participants = findViewById(R.id.inputParticipants);
        message = findViewById(R.id.inputMessage);
        includeMe = findViewById(R.id.switchIncludeMe);
        buttonPickerRestaurant = findViewById(R.id.buttonPickerRestaurant);
        timePicker = findViewById(R.id.buttonPickerHour);
        datePicker = findViewById(R.id.buttonPickerDate);
        dayView = findViewById(R.id.dayTextView);
        hourView = findViewById(R.id.hourTextView);
        restaurantView = findViewById(R.id.textViewRestaurant);
        imageCard = findViewById(R.id.imagePreviewCard);
        eventImage = findViewById(R.id.eventImageView);

        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                TimePickerDialogFragment newFragment = new TimePickerDialogFragment(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourInADay, int minute) {
                        hour = Utils.hourFormatter(String.valueOf(hourInADay + ":" + minute), false);
                        hourView.setText(hour);
                    }
                });
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialogFragment newFragment = new DatePickerDialogFragment(new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        date = day + "/" + (month+1) + "/" + year;
                        dayView.setText(date);
                    }
                });
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });


        buttonPickerRestaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChooseRestaurantDialogFragment restaurantDialogFragment = new ChooseRestaurantDialogFragment(new RestaurantAdapter.OnRestaurantClickListener() {
                    @Override
                    public void onClick(Restaurant restaurant) {
                        selectedRestaurant = restaurant;
                        restaurantId = restaurant.getId();
                        restaurantName = restaurant.getName();
                        restaurantView.setText(restaurant.getName());
                    }
                });
                restaurantDialogFragment.show(getSupportFragmentManager(), "chooseRestaurant");
            }
        });

    }

    public void saveEvent(View view) {
        Log.i(TAG, "[saveEvent]");
        boolean canSave = true;

        if (title.getText().toString().isEmpty()) {
            title.setError(getString(R.string.email_required));
            canSave = false;
        }

        if (participants.getText().toString().equals("")) {
            participants.setError(getString(R.string.participants_number_required));
            canSave = false;
        }

        if (date == null) {
            dayView.setError(getString(R.string.day_required));
            canSave = false;
        }

        if (hour == null) {
            hourView.setError(getString(R.string.hour_required));
            canSave = false;
        }

        if (restaurantId == null) {
            restaurantView.setError(getString(R.string.restaurant_required));
            canSave = false;
        }

        if (canSave) {
            loadingSpinnerDialogFragment.show(getSupportFragmentManager(), "newEventSpinner");
            createEventAPI();
        }
    }

    private void createEventAPI() {

        Event eventToStore = new Event();
        eventToStore.setTitle(title.getText().toString());
        eventToStore.setDay(Utils.getDateJsonFormat(date));
        eventToStore.setHour(Utils.hourFormatter(hour, true));
        eventToStore.setIdRestaurant(restaurantId);

        if (!message.getText().toString().isEmpty()) {
            eventToStore.setMessage(message.getText().toString());
        }
        eventToStore.setIdAuthor(CurrentUser.user.id);
        eventToStore.setMaxParticipants(Long.valueOf(participants.getText().toString()));

        event = new HashMap<>();
        event.put("title", eventToStore.getTitle());
        event.put("participants", eventToStore.getMaxParticipants());
        if (eventToStore.getMessage() != null) {
            event.put("message", eventToStore.getMessage());
        }
        event.put("author", eventToStore.getIdAuthor());
        event.put("day", eventToStore.getDay());
        event.put("restaurant", eventToStore.getIdRestaurant());
        event.put("hour", eventToStore.getHour());

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        final String requestBody = buildJsonRequest(eventToStore);

        Log.i(TAG, "[createEventAPI]::Request Body --> " + requestBody);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, RestAPI.Ifame.EVENT_CREATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "[createEventAPI] --> " + response);

                        event.put("id", response);
                        loadingSpinnerDialogFragment.dismiss();
                        finish();

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingSpinnerDialogFragment.dismiss();
                        Log.e(TAG, "[createEventAPI]::Error Message" + error.toString());
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
                        headers.put("Authorization", "Bearer " + Preference.loadString(getApplicationContext(), "token", null));
                        return headers;
                    }
                    @Override
                    public byte[] getBody() throws AuthFailureError {
                        loadingSpinnerDialogFragment.dismiss();
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

    private String buildJsonRequest(Event eventToStore) {
        JSONObject jsonBody = new JSONObject();

        try {
            JSONObject restaurant = new JSONObject();
            restaurant.put("id", eventToStore.getIdRestaurant());
            restaurant.put("name", restaurantName);

            jsonBody.put("title", eventToStore.getTitle());
            jsonBody.put("description",eventToStore.getMessage());
            jsonBody.put("eventDate", eventToStore.getDay()+ "T" + eventToStore.getHour()+ ":00");
            jsonBody.put("latitude", selectedRestaurant.getLatitude());
            jsonBody.put("longitude", selectedRestaurant.getLongitude());
            jsonBody.put("city", selectedRestaurant.getCity());
            jsonBody.put("restaurant", restaurant);
            jsonBody.put("ownerId", eventToStore.getIdAuthor());
            jsonBody.put("participantNumber", eventToStore.getMaxParticipants());
            jsonBody.put("image", convertImage());

            List<String> foodCategories = new ArrayList<>();
            for (String category : selectedRestaurant.getCategories()) {
                foodCategories.add(category.toUpperCase(Locale.ROOT));
            }
            jsonBody.put("foodCategories", new JSONArray(foodCategories));

            // Set self participation
            if (includeMe.isChecked()) {
                String username = Preference.loadString(getApplicationContext(), "username", null);
                participant = new Participant(username);
                JSONArray participants = new JSONArray();
                JSONObject me = new JSONObject();
                me.put("username", username);
                participants.put(me);
                jsonBody.put("participants", participants);
            }

        } catch (JSONException e) {
            Log.e(TAG, "[buildJsonRequest]::Error Message --> " + e.getMessage());
            e.printStackTrace();
        }

        return jsonBody.toString();
    }

    public void selectImage(View view) {

        if (PermissionManager.isReadStoragePermissionGranted(getApplicationContext())) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_GET);
        } else {
            PermissionManager.requestReadStoragePermission(this);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GET && resultCode == RESULT_OK) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), imageUri);
                imageCard.setVisibility(View.VISIBLE);
                eventImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissionManager.REQUEST_READ_STORAGE_PERMISSION_CODE) {
            if (PermissionManager.isReadStoragePermissionGranted(getApplicationContext())) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE_GET);
            } else {
                Snackbar.make(constraintLayout, getString(R.string.permission_required_storage), BaseTransientBottomBar.LENGTH_LONG)
                        .setAction(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (PermissionManager.isReadStoragePermissionGranted(getApplicationContext())) {
                                    selectImage(view.getRootView());
                                } else {
                                    PermissionManager.requestReadStoragePermission(NewEventActivity.this);
                                }
                            }
                        }).show();
            }
        }
    }

    private String convertImage() {
        if (eventImage != null) {
            BitmapDrawable drawable = (BitmapDrawable) eventImage.getDrawable();
            Bitmap bitmap = eventImage.getDrawingCache();
            if (bitmap != null) return ImageManager.getInstance(this).toBase64(bitmap);
        }
        return null;
    }

}