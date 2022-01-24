package it.univaq.mwt.ifame.fragment.events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.activity.BottomBarControllerActivity;
import it.univaq.mwt.ifame.activity.EventDetailActivity;
import it.univaq.mwt.ifame.adapter.EventAdapter;
import it.univaq.mwt.ifame.dialog.LoadingSpinnerDialogFragment;
import it.univaq.mwt.ifame.fragment.home.HomeFragment;
import it.univaq.mwt.ifame.model.Event;
import it.univaq.mwt.ifame.model.Participant;
import it.univaq.mwt.ifame.model.Restaurant;
import it.univaq.mwt.ifame.model.User;
import it.univaq.mwt.ifame.model.relation.EventRelation;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.LocationHelper;
import it.univaq.mwt.ifame.utility.PermissionManager;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.PreferenceKey;
import it.univaq.mwt.ifame.utility.RestAPI;

public class EventsFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = EventsFragment.class.getSimpleName();

    private EventAdapter eventAdapter;
    private ArrayList<EventRelation> data = new ArrayList<>();

    private BottomSheetBehavior mapBottomSheetBehavior;
    private LoadingSpinnerDialogFragment loadingSpinnerDialogFragment;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View mapBottomSheet;
    private CoordinatorLayout coordinatorLayout;
    private TextView noEvents;

    private LocationHelper helper;
    private Marker marker;
    private LatLng position;
    private ArrayList<Marker> eventMarkers = new ArrayList<>();
    private GoogleMap gMap;

    private boolean firstTime = true;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (PermissionManager.isLocationPermissionGranted(requireContext())) {
            getPositionByAPIFramework();
        } else {
            PermissionManager.requestLocationPermission(getParentFragment());
        }
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        coordinatorLayout = view.findViewById(R.id.coordinatorLayoutEvents);
        swipeRefreshLayout = view.findViewById(R.id.swipeEventContainer);
        mapBottomSheet = view.findViewById(R.id.bottomSheetMapEvent);
        noEvents = view.findViewById(R.id.noEventsEvents);

        loadingSpinnerDialogFragment = new LoadingSpinnerDialogFragment();

        if (firstTime) {
            loadingSpinnerDialogFragment.show(getParentFragmentManager(), "loadingSpinner");
        }
        RecyclerView recyclerView = view.findViewById(R.id.eventRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        eventAdapter = new EventAdapter(data, getContext());
        recyclerView.setAdapter(eventAdapter);

        mapBottomSheetBehavior = BottomSheetBehavior.from(mapBottomSheet);
        mapBottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        //SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapContainer);
        //mapFragment.getMapAsync(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "[Refresh]");
                getAllEvents();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "[onResume]");
        if (!firstTime && position != null) getAllEvents();

    }

    @Override
    public void onPause() {
        super.onPause();
        if (helper != null) helper.stop(getContext());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /*
        setMyLocationMarker();

        gMap = googleMap;

        gMap.getUiSettings().setAllGesturesEnabled(false);
        gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag() != null) {
                    Intent intent = new Intent(getContext(), EventDetailActivity.class);
                    intent.putExtra("event", (EventRelation) marker.getTag());
                    startActivity(intent);
                }
            }
        });

        firstTime = false;

        setupMyLocation(position);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });*/

    }

    private void setupMarkers() {

        if (gMap != null) {

            requireActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (Marker m : eventMarkers) m.remove();
                    eventMarkers.clear();
                }
            });

            for (final EventRelation relation : data) {
                LatLng position = new LatLng(relation.restaurant.getLatitude(), relation.restaurant.getLongitude());

                final MarkerOptions option = new MarkerOptions();
                option.title(relation.event.getTitle());
                option.position(position);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Marker marker = gMap.addMarker(option);
                        marker.setTag(relation);
                        eventMarkers.add(marker);
                    }
                });
            }
        }

        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(false);
    }

    private void setMyLocationMarker() {

        if (PermissionManager.isLocationPermissionGranted(getContext())) {
            getPositionByAPIFramework();
        } else {
            PermissionManager.requestLocationPermission(this);
        }
    }

    private void getAllEvents() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<EventRelation> events = new ArrayList<>();

                Log.i(TAG, "[getAllEvents]::Calling ifame/getEvents");

                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getContext());
                String requestUrl = RestAPI.Ifame.GET_EVENTS + "?latitude=" + position.latitude + "&longitude=" + position.longitude;

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET,  requestUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.i(TAG, "[getAllEvents]::Response --> " + response);

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

                                    firstTime = false;
                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            eventAdapter.notifyDataSetChanged();
                                            if (loadingSpinnerDialogFragment != null)
                                                loadingSpinnerDialogFragment.dismiss();
                                            if (data.size() == 0) noEvents.setVisibility(View.VISIBLE);
                                            else noEvents.setVisibility(View.GONE);
                                        }
                                    });

                                    //setupMarkers();

                                }

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "[getAllEvents]::Error Message --> " + error.toString());
                    }
                })
                {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissionManager.REQUEST_LOCATION_PERMISSION_CODE) {
            if (PermissionManager.isLocationPermissionGranted(getContext())) {
                getPositionByAPIFramework();
            } else {
                Snackbar.make(coordinatorLayout, getString(R.string.permission_required_location), BaseTransientBottomBar.LENGTH_LONG)
                        .setAction(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setMyLocationMarker();
                            }
                        }).show();
            }
        }
    }

    private void getPositionByAPIFramework() {

        helper = new LocationHelper();
        helper.start(getContext(), new LocationHelper.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                position = new LatLng(location.getLatitude(), location.getLongitude());
                setupMyLocation(position);
            }
        });
    }

    private void setupMyLocation(LatLng location) {

        if (location == null) return;

        if (data.isEmpty()) getAllEvents();

        if (gMap != null) {

            if (marker == null) {

                MarkerOptions options = new MarkerOptions();
                options.title(Preference.loadString(getContext(), "username", null));
                options.position(position);
                options.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable(getContext(), R.drawable.ic_person_marker)));
                marker = gMap.addMarker(options);

            } else {
                marker.setPosition(position);
            }

            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 11));

        }
    }

    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}