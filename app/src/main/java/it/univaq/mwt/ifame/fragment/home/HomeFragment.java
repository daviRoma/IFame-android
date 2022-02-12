package it.univaq.mwt.ifame.fragment.home;

import static java.lang.String.join;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.adapter.ForYouEventAdapter;
import it.univaq.mwt.ifame.dialog.LoadingSpinnerDialogFragment;
import it.univaq.mwt.ifame.model.Event;
import it.univaq.mwt.ifame.model.Participant;
import it.univaq.mwt.ifame.model.Restaurant;
import it.univaq.mwt.ifame.model.relation.EventRelation;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.LocationHelper;
import it.univaq.mwt.ifame.utility.PermissionManager;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.PreferenceKey;
import it.univaq.mwt.ifame.utility.RestAPI;
import it.univaq.mwt.ifame.utility.Utils;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();

    private final ArrayList<EventRelation> eventsForYou = new ArrayList<>();

    private LocationHelper locationHelper;

    private ForYouEventAdapter forYouEventAdapter;
    private RecyclerView forYouRecyclerView;
    private TextView noEvents;
    private List<Address> addresses;
    private Geocoder gcd;
    private LatLng userPosition;

    private SwipeRefreshLayout swipeRefreshLayout;
    private LoadingSpinnerDialogFragment spinnerDialogFragment;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        spinnerDialogFragment = new LoadingSpinnerDialogFragment();
        gcd = new Geocoder(getContext(), Locale.getDefault());

        if (PermissionManager.isLocationPermissionGranted(requireContext())) {
            getPositionByAPIFramework();
        } else {
            PermissionManager.requestLocationPermission(getParentFragment());
            getPositionByAPIFramework();
        }
        spinnerDialogFragment.show(getActivity().getSupportFragmentManager(), "homeLoading");

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = view.findViewById(R.id.swipeHomeContainer);
        forYouRecyclerView = view.findViewById(R.id.forYouViewPager);
        noEvents = view.findViewById(R.id.noEventsHome);

        forYouRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        forYouEventAdapter = new ForYouEventAdapter(eventsForYou, getContext());
        forYouRecyclerView.setAdapter(forYouEventAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                readEventsFromAPI();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        if (userPosition != null) {
            readEventsFromAPI();
        }
    }

    private void readEventsFromAPI() {
        Log.i(TAG, "[readEventsFromAPI]::User Position --> " + String.valueOf(userPosition));

        new Thread(new Runnable() {
            final List<EventRelation> eventsFY = new ArrayList<EventRelation>();
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getContext());
                String requestUrl = RestAPI.Ifame.GET_EVENTS + "?latitude=" + userPosition.latitude + "&longitude="+userPosition.longitude + "&foodCategories=" + join(",", CurrentUser.user.preferences);

                Log.i(TAG, "[readEventsFromAPI]::Calling ifame/events/all");

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET,  requestUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.i(TAG, "[readEventsFromAPI]::Response --> " + response);

                                if (response != null) {
                                    try {
                                        JSONArray jsonResponse = new JSONArray(response);
                                        for (Integer i = 0; i < jsonResponse.length(); i++) {
                                            JSONObject obj = jsonResponse.getJSONObject(i);
                                            Event evt = new Event();
                                            evt.setId(String.valueOf(obj.getInt("id")));
                                            evt.setIdAuthor(String.valueOf(obj.getInt("ownerId")));
                                            evt.setIdRestaurant(String.valueOf(obj.getJSONObject("restaurant").getInt("id")));

                                            evt.setHour(Utils.hourFormatter(obj.getJSONObject("eventTime").getString("hour") + ":" + obj.getJSONObject("eventTime").getString("minute"), false));

                                            evt.setDay(Utils.getDate( obj.getString("eventDate"), "dd-MM-yyyy"));
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

                                            eventsFY.add(evtRel);
                                        }

                                        spinnerDialogFragment.dismiss();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    eventsForYou.clear();
                                    eventsForYou.addAll(eventsFY);

                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            forYouEventAdapter.notifyDataSetChanged();
                                            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                                            if (eventsForYou.size() == 0) noEvents.setVisibility(View.VISIBLE);
                                            else noEvents.setVisibility(View.GONE);
                                        }
                                    });
                                }

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "[readEventsFromAPI]::Error Message --> " + error.toString());
                            }
                })
                {
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

    private void getPositionByAPIFramework() {
        locationHelper = new LocationHelper();

        locationHelper.start(getContext(), new LocationHelper.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                setupMyLocation(position);
            }
        });
    }

    private void setupMyLocation(LatLng position) {
        if (position == null) return;
        userPosition = position;
        readEventsFromAPI();
        //try {
            //addresses = gcd.getFromLocation(position.latitude, position.longitude, 1);
            Preference.saveString(getActivity().getApplicationContext(), PreferenceKey.USER_CITY.toString(), "L'Aquila");
            Preference.saveDouble(getActivity().getApplicationContext(), PreferenceKey.USER_LATITUDE.toString(), position.latitude);
            Preference.saveDouble(getActivity().getApplicationContext(), PreferenceKey.USER_LONGITUDE.toString(), position.longitude);


        /*} catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}