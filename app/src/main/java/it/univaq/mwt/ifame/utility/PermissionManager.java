package it.univaq.mwt.ifame.utility;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class PermissionManager {

    private static final String PERMISSION_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String PERMISSION_READ_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;

    public static final int REQUEST_LOCATION_PERMISSION_CODE = 100;
    public static final int REQUEST_READ_STORAGE_PERMISSION_CODE = 200;

    public static boolean isLocationPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, PERMISSION_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Fragment fragment){
        fragment.requestPermissions(new String[]{ PERMISSION_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
    }

    public static void requestLocationPermission(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{ PERMISSION_LOCATION}, REQUEST_LOCATION_PERMISSION_CODE);
    }

    public static boolean isReadStoragePermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, PERMISSION_READ_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestReadStoragePermission(Fragment fragment){
        fragment.requestPermissions(new String[]{ PERMISSION_READ_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_CODE);
    }

    public static void requestReadStoragePermission(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{ PERMISSION_READ_STORAGE}, REQUEST_READ_STORAGE_PERMISSION_CODE);
    }
}
