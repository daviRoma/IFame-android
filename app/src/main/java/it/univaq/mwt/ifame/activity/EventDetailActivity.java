package it.univaq.mwt.ifame.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.dialog.LoadingSpinnerDialogFragment;
import it.univaq.mwt.ifame.model.Event;
import it.univaq.mwt.ifame.model.Participant;
import it.univaq.mwt.ifame.model.Restaurant;
import it.univaq.mwt.ifame.model.relation.EventRelation;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.ImageManager;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.RestAPI;
import it.univaq.mwt.ifame.utility.Utils;

public class EventDetailActivity extends AppCompatActivity {
    private static final String TAG = EventDetailActivity.class.getSimpleName();

    private EventRelation eventRelation;

    private TextView participants, title, restaurantName, maxParticipants, message, day, hour, address, authorMessage;
    private ImageView image;
    private CardView cardViewMessage;
    private Button deleteEvent, actionButton;

    private LoadingSpinnerDialogFragment spinnerDialogFragment;

    private final View.OnClickListener joinOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            final Participant newParticipant = new Participant();
            newParticipant.setUsername(CurrentUser.user.username);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    storeParticipation(CurrentUser.user.username);
                }
            }).start();

            Map<String, Object> event = new HashMap<>();
            ArrayList<String> currentParticipants = new ArrayList<>();
            for (Participant participant : eventRelation.participants) {
                currentParticipants.add(participant.getUsername());
            }
            currentParticipants.add(CurrentUser.user.username);
            event.put("currentParticipants", currentParticipants);

            eventRelation.participants.add(new Participant(CurrentUser.user.username));
            setJoined();
        }
    };

    private final View.OnClickListener leaveOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    removeParticipation(CurrentUser.user.username);
                }
            }).start();

            Participant participantToRemove = null;

            Map<String, Object> event = new HashMap<>();
            ArrayList<String> currentParts = new ArrayList<>();

            for (Participant participant : eventRelation.participants) {
                if (!participant.getUsername().equals(CurrentUser.user.username))
                    currentParts.add(participant.getUsername());
                else participantToRemove = participant;
            }

            if (participantToRemove != null) {
                eventRelation.participants.remove(participantToRemove);
            }

            event.put("currentParticipants", currentParts);

            setJoined();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        spinnerDialogFragment = new LoadingSpinnerDialogFragment();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventRelation = (EventRelation) getIntent().getSerializableExtra("event");

        getEventDetail();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void deleteEvent(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                deleteEvent();
            }
        }).start();
        finish();
    }

    public void setJoined() {

        actionButton.setEnabled(eventRelation.participants.size() <= eventRelation.event.getMaxParticipants());

        boolean joined = false;
        for (Participant participant : eventRelation.participants) {
            if (participant.getUsername().equals(CurrentUser.user.username)) joined = true;
        }

        if (joined) {
            actionButton.setText(R.string.leave);
            actionButton.setOnClickListener(leaveOnClickListener);
        } else {
            actionButton.setEnabled(eventRelation.participants.size() < eventRelation.event.getMaxParticipants());
            actionButton.setText(R.string.join);
            actionButton.setOnClickListener(joinOnClickListener);
        }

        participants.setText(String.valueOf(eventRelation.participants.size()));
    }

    private void setUpDetails() {

        cardViewMessage = findViewById(R.id.cardViewMessage);
        participants = findViewById(R.id.detailParticipants);
        deleteEvent = findViewById(R.id.deleteButton);
        actionButton = findViewById(R.id.buttonActionEvent);
        title = findViewById(R.id.detailTitle);
        restaurantName = findViewById(R.id.detailRestaurantName);
        maxParticipants = findViewById(R.id.detailMaxParticipants);
        message = findViewById(R.id.detailMessage);
        authorMessage = findViewById(R.id.authorMessage);
        day = findViewById(R.id.detailDay);
        hour = findViewById(R.id.detailHour);
        address = findViewById(R.id.detailRestaurantAddress);
        image = findViewById(R.id.detailImage);

        title.setText(eventRelation.event.getTitle());
        restaurantName.setText(eventRelation.restaurant.getName());
        maxParticipants.setText(String.valueOf(eventRelation.event.getMaxParticipants()));
        day.setText(eventRelation.event.getDay());
        hour.setText(eventRelation.event.getHour());
        address.setText(eventRelation.restaurant.getAddress() + ", " + eventRelation.restaurant.getCity());
        participants.setText(String.valueOf(eventRelation.participants.size()));

        if (eventRelation.event.getMessage() != null && !eventRelation.event.getMessage().equals("")) {
            message.setText(eventRelation.event.getMessage());
        } else {
            cardViewMessage.setVisibility(View.GONE);
            authorMessage.setVisibility(View.GONE);
        }
        if (eventRelation.event.getImage() != null && !eventRelation.event.getImage().isEmpty()) {
            Log.i(TAG, "[image]::"+eventRelation.event.getImage());
            image.setImageBitmap(ImageManager.getInstance(getApplicationContext()).loadImage(eventRelation.event.getImage()));
        }

        if (!eventRelation.event.getIdAuthor().equals(CurrentUser.user.id)) {
            deleteEvent.setVisibility(View.GONE);
        }

        setJoined();
    }

    private void getEventDetail() {
        Log.i(TAG, "[getEventDetail]::Calling ifame/event/{eventId}/{restaurantId}");
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String requestUrl = RestAPI.Ifame.GET_EVENT + eventRelation.event.getId() + "/" + eventRelation.restaurant.getId();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET,  requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.i(TAG, "[getEventDetail]::Response --> " + response);
                        if (response != null) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);

                                Event evt = new Event();
                                evt.setId(String.valueOf(jsonResponse.getInt("id")));
                                evt.setIdAuthor(String.valueOf(jsonResponse.getInt("ownerId")));
                                evt.setIdRestaurant(String.valueOf(jsonResponse.getJSONObject("restaurant").getInt("id")));
                                evt.setDay(Utils.getDate(jsonResponse.getString("eventDate"), "dd-MM-yyyy"));
                                evt.setHour(Utils.hourFormatter(jsonResponse.getJSONObject("eventTime").getString("hour") + ":" + jsonResponse.getJSONObject("eventTime").getString("minute"), false));
                                evt.setTitle(jsonResponse.getString("title"));
                                evt.setMessage(jsonResponse.getString("description"));
                                evt.setMaxParticipants(jsonResponse.getLong("participantNumber"));
                                evt.setImage(jsonResponse.getString("image"));

                                Restaurant restaurant = new Restaurant();
                                restaurant.setId(evt.getIdRestaurant());
                                restaurant.setName(jsonResponse.getJSONObject("restaurant").getString("name"));
                                restaurant.setAddress(jsonResponse.getJSONObject("restaurant").getString("street"));
                                restaurant.setState(jsonResponse.getJSONObject("restaurant").getString("state"));
                                restaurant.setLatitude(jsonResponse.getJSONObject("restaurant").getDouble("latitude"));
                                restaurant.setLongitude((jsonResponse.getJSONObject("restaurant").getDouble("longitude")));
                                restaurant.setCity(jsonResponse.getJSONObject("restaurant").getString("city"));

                                JSONArray participants = jsonResponse.getJSONArray("participants");
                                List<Participant> eventParticipants = new ArrayList<>();

                                for (int i = 0; i < participants.length(); i++) {
                                    Participant participant = new Participant(participants.getJSONObject(i).getString("username"));
                                    eventParticipants.add(participant);
                                }

                                EventRelation evtRel = new EventRelation();
                                evtRel.event = evt;
                                evtRel.restaurant = restaurant;
                                evtRel.participants = eventParticipants;

                                eventRelation = evtRel;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //spinnerDialogFragment.dismiss();
                            setUpDetails();
                        }

                    }

                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "[getEventDetail]::Error Message --> " + error.toString());
                        spinnerDialogFragment.dismiss();
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
                        headers.put("Authorization", "Bearer " + Preference.loadString(getApplicationContext(), "token", null));
                        return headers;
                    }
                };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void storeParticipation(String username) {
        Log.i(TAG, "[storeParticipation]::Calling ifame/participations/event/join");

        try {
            // Instantiate the RequestQueue.
            RequestQueue queue = Volley.newRequestQueue(this);

            List<String> participants = new ArrayList<>();
            participants.add(Preference.loadString(getApplicationContext(), "username", null));

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("eventId", eventRelation.event.getId());
            jsonBody.put("participants", new JSONArray(participants));

            final String requestBody = jsonBody.toString();

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.POST,  RestAPI.Ifame.JOIN,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i(TAG, "[storeParticipation]::Response --> " + response);
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "[storeParticipation]::Error Message --> " + error.toString());
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

    private void removeParticipation(String username) {

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, RestAPI.Ifame.PARTICIPATION_REMOVE + eventRelation.event.getId() + "/" + username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG,"[removeParticipation]::Response --> " + response);
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG,"[removeParticipation]::Error Message --> " + error.toString());
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
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private void deleteEvent() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.DELETE, RestAPI.Ifame.EVENT_DELETE + eventRelation.event.getId(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i(TAG, "[deleteEvent]::Response --> " + response);
                        eventRelation = null;
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "[deleteEvent]::Error Message --> " + error.toString());
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
        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}