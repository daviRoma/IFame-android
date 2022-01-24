package it.univaq.mwt.ifame.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

public class LocationHelper {

    public interface OnLocationChangedListener {

        void onLocationChanged(Location location);
    }

    private OnLocationChangedListener callback = null;

    private final LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (callback != null) callback.onLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @SuppressLint("MissingPermission")
    public void start(Context context, OnLocationChangedListener callback) {

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager != null && PermissionManager.isLocationPermissionGranted(context)) {

            this.callback = callback;

            manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, Looper.myLooper());
            manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, Looper.myLooper());

        }
    }

    public void stop(Context context) {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            manager.removeUpdates(listener);
        }
    }
}
