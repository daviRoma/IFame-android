package it.univaq.mwt.ifame.fragment.myevents;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.activity.NewEventActivity;
import it.univaq.mwt.ifame.adapter.EventAdapter;
import it.univaq.mwt.ifame.model.Event;
import it.univaq.mwt.ifame.model.Participant;
import it.univaq.mwt.ifame.model.Restaurant;
import it.univaq.mwt.ifame.model.relation.EventRelation;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.PreferenceKey;
import it.univaq.mwt.ifame.utility.RestAPI;

public class MyEventsFragment extends Fragment {
    private static final String TAG = MyEventsFragment.class.getSimpleName();

    private FloatingActionButton buttonNewEvent;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<EventRelation> data = new ArrayList<>();
    private RecyclerView eventsRecycler;
    private EventAdapter eventAdapter;
    private TextView noEvents;

    private boolean startActivity = true;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = view.findViewById(R.id.swipeMyEventsContainer);
        buttonNewEvent = view.findViewById(R.id.buttonNewEvent);
        noEvents = view.findViewById(R.id.noEventsMyEvents);

        buttonNewEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), NewEventActivity.class);
                startActivity(intent);
            }
        });

        eventsRecycler = view.findViewById(R.id.eventsContainer);
        eventsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        eventAdapter = new EventAdapter(data, getContext());
        eventsRecycler.setAdapter(eventAdapter);

        if (eventAdapter != null) {
            Log.i(TAG, "[onViewCreated]::LoadMyJoinedEvents");
            getMyEvents();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "[Refresh]");
                getMyEvents();

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (!startActivity) {
            Log.i(TAG, "[onResume]::LoadMyJoinedEvents");
            getMyEvents();
        }

    }

    private void getMyEvents() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<EventRelation> events = new ArrayList<>();

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getContext());
                String requestUrl = RestAPI.Ifame.GET_EVENTS_JOINED + Preference.loadString(getContext(), "username", null);

                Log.i(TAG, "[getMyEvents]::Calling ifame/getMyJoinedEvents");

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET,  requestUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.i(TAG, "[getMyEvents]::Response --> " + response);
                                if (response != null) {
                                    try {
                                        JSONArray jsonResponse = new JSONArray(response);
                                        for (Integer i = 0; i < jsonResponse.length(); i++) {
                                            JSONObject obj = jsonResponse.getJSONObject(i);
                                            Event evt = new Event();
                                            evt.setId(String.valueOf(obj.getInt("id")));
                                            evt.setIdAuthor(String.valueOf(obj.getInt("ownerId")));
                                            evt.setIdRestaurant(String.valueOf(obj.getJSONObject("restaurant").getInt("id")));
                                            evt.setDay(obj.getString("eventDate"));
                                            evt.setHour(obj.getJSONObject("eventTime").getString("hour") + ":" + (obj.getJSONObject("eventTime").getInt("minute") == 0 ? "00" : obj.getJSONObject("eventTime").getInt("minute")));

                                            evt.setTitle(obj.getString("title"));
                                            evt.setMessage(obj.getString("description"));
                                            evt.setMaxParticipants(Long.valueOf(obj.getInt("participantNumber")));

                                            Restaurant restaurant = new Restaurant();
                                            restaurant.setId(evt.getIdRestaurant());
                                            restaurant.setName(obj.getJSONObject("restaurant").getString("name"));

                                            EventRelation evtRel = new EventRelation();
                                            evtRel.event = evt;
                                            evtRel.restaurant = restaurant;

                                            // Set participants
                                            evtRel.participants = new ArrayList<Participant>();
                                            JSONArray jsonParticipants = obj.getJSONArray("participants");
                                            for (Integer j = 0; j < jsonParticipants.length(); j++) {
                                                Participant participant = new Participant(jsonParticipants.getJSONObject(j).getString("username"));
                                                evtRel.participants.add(participant);
                                            }

                                            events.add(evtRel);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    data.clear();
                                    data.addAll(events);

                                    if(startActivity) startActivity = false;
                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            eventAdapter.notifyDataSetChanged();
                                            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()){
                                                swipeRefreshLayout.setRefreshing(false);
                                            }
                                            if (data.size() == 0)noEvents.setVisibility(View.VISIBLE);
                                            else noEvents.setVisibility(View.GONE);
                                        }
                                    });

                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "[getMyEvents]::Error --> " + error.toString());
                    }
                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        // Get JWT
                        String token = Preference.loadString(getActivity().getApplicationContext(), "token", null);

                        final Map<String, String> headers = new HashMap<>();
                        headers.put("Content-Type", "application/json");
                        headers.put("Authorization", "Bearer " + token);
                        return headers;
                    }

                };

                // Add the request to the RequestQueue.
                queue.add(stringRequest);

            }
        }).start();
    }
}