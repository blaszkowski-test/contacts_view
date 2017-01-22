package com.example.piotr.contactsview;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener
{
    private GoogleMap mMap;
    private boolean mapReady = false;
    private LocationManager locationManager;
    private String provider;

    public MapsFragment()
    {
        // Required empty public constructor
    }

    public static MapsFragment newInstance()
    {
        return new MapsFragment();
    }

    private String checkLocationProvider()
    {
        if(locationManager
            .isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            return LocationManager.GPS_PROVIDER;
        }
        else
        {
            return LocationManager.NETWORK_PROVIDER;
        }
    }


    @SuppressWarnings("MissingPermission")
    private void requestLocationUpdate()
    {
        provider = checkLocationProvider();
        locationManager.requestLocationUpdates(provider, 400, 1, this);
    }

    @SuppressWarnings("MissingPermission")
    private void removeLocationUpdate()
    {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {

        View view = inflater.inflate(R.layout.activity_maps, container, false);
        SupportMapFragment mapFragment = new SupportMapFragment();
        Helper.LoadFragment(getFragmentManager(), R.id.map, mapFragment, false);
        mapFragment.getMapAsync(this);
        return view;
    }



    @Override
    public void onResume()
    {
        super.onResume();
        requestLocationUpdate();
    }

    @SuppressWarnings("MissingPermission")
    @Override
    public void onPause()
    {
        super.onPause();
        removeLocationUpdate();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressWarnings("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mapReady = true;

        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null)
        {
            onLocationChanged(location);
        }
        else
        {
            LatLng city = new LatLng(50.251489, 19.014624);
            mMap.addMarker(new MarkerOptions().position(city).title("kaj to je :)"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(city));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    @Override
    public void onLocationChanged(Location location)
    {
        if(mapReady == true && mMap != null)
        {
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            LatLng city = new LatLng(lat, lng);

            mMap.addMarker(new MarkerOptions().position(city).title("kaj to je :)"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(city));
            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Toast.makeText(getActivity(), "status changed " + provider + " " + String.valueOf(status),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        Toast.makeText(getActivity(), "Enabled new provider " + provider,
                Toast.LENGTH_SHORT).show();

        removeLocationUpdate();
        requestLocationUpdate();
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        Toast.makeText(getActivity(), "Disabled provider " + provider,
                Toast.LENGTH_SHORT).show();

        removeLocationUpdate();
        requestLocationUpdate();
    }
}
