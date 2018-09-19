package com.tk.apkdemo.mapsswipeapp;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tk.apkdemo.mapsswipeapp.swipeanim.SlideUp;
import com.tk.apkdemo.mapsswipeapp.swipeanim.SlideUpBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MainActivity extends Activity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private View sliderView;
    private SlideUp slideUp;
    private MapFragment mMap;
    private TextView tv_Distance,tv_Time;
    private LinearLayout ll_BottomSheet;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mGoogleMap;
    private RelativeLayout rootView;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private boolean isMarkerRotating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sliderView = findViewById(R.id.slideView);
        rootView = findViewById(R.id.rootView);
        ll_BottomSheet = findViewById(R.id.ll_BottomSheet);
        tv_Distance = findViewById(R.id.tv_Distance);
        tv_Time = findViewById(R.id.tv_Time);
        mMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.map));
        mMap.getMapAsync(this);

        slideUp = new SlideUpBuilder(sliderView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
//                        dim.setAlpha(1 - (percent / 100));
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if(visibility == View.VISIBLE)
                            ll_BottomSheet.setVisibility(View.GONE);
                        else {
                            ll_BottomSheet.setVisibility(View.GONE);
                            sliderView.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withLoggingEnabled(true)
                .withGesturesEnabled(true)
                .withStartState(SlideUp.State.HIDDEN)
                .withSlideFromOtherView(findViewById(R.id.rootView))
                .build();

    }



    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        buildGoogleApiClient();
        googleMap.setMyLocationEnabled(false);

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                final GMapV2Direction md = new GMapV2Direction();
                final LatLng srcLatlang = new LatLng(17.4671922,78.381008);
                final LatLng destLatlang = new LatLng(17.4256867,78.4180647);
                final Document doc = md.getDocument(srcLatlang, destLatlang,
                        GMapV2Direction.MODE_DRIVING);

                ArrayList<LatLng> directionPoint = md.getDirection(doc);
                final PolylineOptions rectLine = new PolylineOptions().width(8).color(
                        Color.BLUE);

                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        tv_Distance.setText("Distance : "+md.getDistanceText(doc));
                        tv_Time.setText("ETA : "+md.getDurationText(doc));
                        MarkerOptions markerOptions = new MarkerOptions();

                        // Setting the position for the marker
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destLatlang, 16.0f));
                        Polyline polylin = googleMap.addPolyline(rectLine);
                        markerOptions.position(srcLatlang);
                        googleMap.addMarker(markerOptions);

                    }
                });
            }
        }).start();*/
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16.0f));


        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void rotateMarker(final Marker marker, final float toRotation) {
        if(!isMarkerRotating) {
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final float startRotation = marker.getRotation();
            final long duration = 1000;

            final Interpolator interpolator = new LinearInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    isMarkerRotating = true;

                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = interpolator.getInterpolation((float) elapsed / duration);

                    float rot = t * toRotation + (1 - t) * startRotation;

                    marker.setRotation(-rot > 180 ? rot / 2 : rot);
                    if (t < 1.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    } else {
                        isMarkerRotating = false;
                    }
                }
            });
        }
    }
}
