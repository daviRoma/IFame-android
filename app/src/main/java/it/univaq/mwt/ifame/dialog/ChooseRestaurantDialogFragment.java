package it.univaq.mwt.ifame.dialog;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.activity.NewEventActivity;
import it.univaq.mwt.ifame.adapter.RestaurantAdapter;
import it.univaq.mwt.ifame.model.Restaurant;
import it.univaq.mwt.ifame.utility.CurrentUser;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.PreferenceKey;
import it.univaq.mwt.ifame.utility.RestAPI;

public class ChooseRestaurantDialogFragment extends DialogFragment {

    private static final String TAG = NewEventActivity.class.getSimpleName();

    private RestaurantAdapter.OnRestaurantClickListener clickListener;
    private List<Restaurant> restaurants = new ArrayList<>();
    private RestaurantAdapter restaurantAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    public ChooseRestaurantDialogFragment(RestaurantAdapter.OnRestaurantClickListener onRestaurantClickListener) {
        this.clickListener = onRestaurantClickListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = new Dialog(requireActivity());
        dialog.setCancelable(true);

        dialog.setContentView(R.layout.dialog_choose_restaurant);

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        swipeRefreshLayout = dialog.findViewById(R.id.swipeRestaurantContainer);

        final RecyclerView restaurantRecyclerView = dialog.findViewById(R.id.restaurantRecyclerView);
        restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        restaurantAdapter = new RestaurantAdapter(this, restaurants, getContext());

        restaurantAdapter.setClickListener(clickListener);
        restaurantRecyclerView.setAdapter(restaurantAdapter);

        readRestaurantsFromAPI();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                readRestaurantsFromAPI();

            }
        });

        return dialog;

    }

    private void readRestaurantsFromAPI() {

        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                restaurants.clear();

                Log.i(TAG, "[readRestaurantsFromAPI]::Calling ifame/restaurants/getRestaurants");
                // Instantiate the RequestQueue.
                RequestQueue queue = Volley.newRequestQueue(getContext());
                String requestUrl = RestAPI.Ifame.GET_RESTAURANTS +
                        "?latitude=" +
                        Preference.loadDouble(getContext(), PreferenceKey.USER_LATITUDE.toString(), 0) +
                        "&longitude=" + Preference.loadDouble(getContext(), PreferenceKey.USER_LONGITUDE.toString(), 0) +
                        "&categories=" + String.join(",", CurrentUser.user.preferences);

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET,  requestUrl,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Display the first 500 characters of the response string.
                                Log.i(TAG, "[readRestaurantsFromAPI]::Response --> " + response);

                                if (response != null) {
                                    try {
                                        JSONArray jsonResponse = new JSONArray(response);

                                        for (int i = 0; i < jsonResponse.length(); i++) {
                                            JSONObject obj = jsonResponse.getJSONObject(i);
                                            Restaurant restaurant = new Restaurant();
                                            restaurant.setCity(obj.getString("city"));
                                            restaurant.setLatitude(obj.getDouble("latitude"));
                                            restaurant.setLongitude(obj.getDouble("longitude"));
                                            restaurant.setState(obj.getString("state"));
                                            restaurant.setAddress(obj.getString("street"));
                                            restaurant.setName(obj.getString("name"));
                                            restaurant.setId(String.valueOf(obj.getInt("id")));
                                            restaurant.setCategories(new ArrayList<>());

                                            JSONArray categories = obj.getJSONArray("foodCategories");
                                            for (int j = 0; j < categories.length(); j++) {
                                                restaurant.getCategories().add(categories.getString(j).toLowerCase(Locale.ROOT));
                                            }
                                            restaurants.add(restaurant);
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    requireActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            restaurantAdapter.notifyDataSetChanged();
                                            if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing())
                                                swipeRefreshLayout.setRefreshing(false);
                                        }
                                    });
                                }

                            }

                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "[readRestaurantsFromAPI]::Error Message --> " + error.toString());
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

}
