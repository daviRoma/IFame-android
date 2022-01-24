package it.univaq.mwt.ifame.dialog;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import it.univaq.mwt.ifame.R;
import it.univaq.mwt.ifame.utility.LocationHelper;
import it.univaq.mwt.ifame.utility.PermissionManager;
import it.univaq.mwt.ifame.utility.Preference;
import it.univaq.mwt.ifame.utility.PreferenceKey;

public class SelectCityDialog extends DialogFragment {

    private static final String TAG = SelectCityDialog.class.getSimpleName();
    private OnCitySelected onCitySelected;

    public SelectCityDialog(OnCitySelected onCitySelected) {
        this.onCitySelected = onCitySelected;
    }

    private LocationHelper helper;
    private List<Address> addresses;
    private Geocoder gcd;

    private String defaultCity;
    private double defaultLatitude, defaultLongitude;

    private ConstraintLayout constraintLayout;
    private TextInputEditText inputCity;
    private Button getPosition, setCity;

    private LoadingSpinnerDialogFragment loadingSpinnerDialogFragment;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Dialog dialog = new Dialog(requireActivity());
        dialog.setContentView(R.layout.dialog_select_city);

        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        loadingSpinnerDialogFragment = new LoadingSpinnerDialogFragment();

        defaultCity = Preference.loadString(getContext(), PreferenceKey.USER_CITY.toString(), "");
        defaultLatitude = Preference.loadDouble(getContext(), PreferenceKey.USER_LATITUDE.toString(), 0);
        defaultLongitude = Preference.loadDouble(getContext(), PreferenceKey.USER_LONGITUDE.toString(), 0);

        constraintLayout = dialog.findViewById(R.id.constraintLayoutSelectCity);
        inputCity = dialog.findViewById(R.id.inputCity);
        getPosition = dialog.findViewById(R.id.getPosition);
        setCity = dialog.findViewById(R.id.setCity);

        gcd = new Geocoder(getContext(), Locale.getDefault());

        if (defaultCity.isEmpty() || defaultLongitude == 0 || defaultLatitude == 0) {
            addresses = null;
        } else {
            try {
                addresses = gcd.getFromLocation(defaultLatitude, defaultLongitude, 1);
                inputCity.setText(defaultCity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (PermissionManager.isLocationPermissionGranted(requireContext())) {
                    getPositionByAPIFramework();
                } else {
                    PermissionManager.requestLocationPermission(getParentFragment());
                }

            }
        });

        setCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (addresses != null) {
                    if (addresses.get(0).getLocality().equals(inputCity.getText().toString())){

                        Preference.saveString(getContext(), PreferenceKey.USER_CITY.toString(), addresses.get(0).getLocality());
                        Preference.saveDouble(getContext(), PreferenceKey.USER_LATITUDE.toString(), addresses.get(0).getLatitude());
                        Preference.saveDouble(getContext(), PreferenceKey.USER_LONGITUDE.toString(), addresses.get(0).getLongitude());

                        onCitySelected.onSelection(addresses.get(0).getLocality());

                    }else {

                        try {
                            addresses = gcd.getFromLocationName(inputCity.getText().toString(), 1);
                            Preference.saveString(getContext(), PreferenceKey.USER_CITY.toString(), addresses.get(0).getLocality());
                            Preference.saveDouble(getContext(), PreferenceKey.USER_LATITUDE.toString(), addresses.get(0).getLatitude());
                            Preference.saveDouble(getContext(), PreferenceKey.USER_LONGITUDE.toString(), addresses.get(0).getLongitude());

                            onCitySelected.onSelection(addresses.get(0).getLocality());

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                else {

                    try {
                        addresses = gcd.getFromLocationName(inputCity.getText().toString(), 1);
                        Preference.saveString(getContext(), PreferenceKey.USER_CITY.toString(), addresses.get(0).getLocality());
                        Preference.saveDouble(getContext(), PreferenceKey.USER_LATITUDE.toString(), addresses.get(0).getLatitude());
                        Preference.saveDouble(getContext(), PreferenceKey.USER_LONGITUDE.toString(), addresses.get(0).getLongitude());

                        onCitySelected.onSelection(addresses.get(0).getLocality());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                dismiss();

            }
        });
        return dialog;
    }


    private void getPositionByAPIFramework() {

        loadingSpinnerDialogFragment.show(getChildFragmentManager(), "spinnerCity");

        helper = new LocationHelper();
        helper.start(getContext(), new LocationHelper.OnLocationChangedListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                setupMyLocation(position);
            }
        });
    }


    private void setupMyLocation(LatLng location) {

        if (location == null) return;
        try {
            addresses = gcd.getFromLocation(location.latitude, location.longitude, 1);
            inputCity.setText(addresses.get(0).getLocality());

            loadingSpinnerDialogFragment.dismiss();

        } catch (IOException e) {
            e.printStackTrace();
            loadingSpinnerDialogFragment.dismiss();

        }
    }

    public interface OnCitySelected{
        void onSelection(String cityName);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PermissionManager.REQUEST_LOCATION_PERMISSION_CODE) {
            if (PermissionManager.isLocationPermissionGranted(getContext())) {
                getPositionByAPIFramework();
            } else {
                Snackbar.make(constraintLayout, getString(R.string.permission_required_location), BaseTransientBottomBar.LENGTH_LONG)
                        .setAction(getString(R.string.ok), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (PermissionManager.isLocationPermissionGranted(requireContext())) {
                                    getPositionByAPIFramework();
                                } else {
                                    PermissionManager.requestLocationPermission(getParentFragment());
                                }
                            }
                        }).show();
            }
        }
    }

}
