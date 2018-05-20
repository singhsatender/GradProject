package com.stressfreeroads.gradproject;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.GeocodeRequest;
import com.here.android.mpa.search.Location;
import com.here.android.mpa.search.ResultListener;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MapHomePage extends AppCompatActivity implements PositioningManager.OnPositionChangedListener {

    // map fragment embedded in this activity
    private MapFragment mapFragment = null;

    // map embedded in the map fragment
    private Map map = null;


    private GeocompleteAdapter mGeoAutoCompleteAdapter;

    private CustomAutoCompleteTextView mGeoAutocomplete;

    private PositioningManager positionManager;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private ImageButton m_GetLocationButton;
    private ImageButton m_settingsBtn;
    private SettingsPanel m_settingsPanel;
    private LinearLayout m_settingsLayout;
    private static final Integer THRESHOLD = 2;

    private GeoCoordinate currentLocation= null;

    private MapMarker m_positionIndicatorFixed = null;

    // Position Listener
    PositioningManager.OnPositionChangedListener positionListener = new PositioningManager.OnPositionChangedListener() {

        @Override
        public void onPositionUpdated(PositioningManager.LocationMethod method, GeoPosition position,
                                      boolean isMapMatched) {
            if (position != null) {
              mGeoAutoCompleteAdapter.setPosition(position);
            }
        }

        @Override
        public void onPositionFixChanged(PositioningManager.LocationMethod method, PositioningManager.LocationStatus status) {

        }
    };

    private PositioningManager mPositionManager;
    private MapMarker mMarker;
    protected ResultListener<List<Location>> m_listener = new ResultListener<List<Location>>() {
        @Override
        public void onCompleted(List<Location> data, ErrorCode error) {
            if (error == ErrorCode.NONE) {
                if (data != null && data.size() > 0) {
                    addMarker(data.get(0).getCoordinate());
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_map_home_page);


        //Check required permissions
        requestPermissions();

        //Initialze Map
        initializeMap();

        //Set Search Action Bar
        setSearchBar();

        //current Location Button
        initGetLocationButton();

        //settings panel
        initSettingsPanel();


    }

    public void initializeMap()
    {
        // Search for the map fragment to finish setup by calling init().
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapfragment);

        mapFragment.init(new OnEngineInitListener() {

            @Override
            public void onEngineInitializationCompleted(Error error) {
                if (error == Error.NONE) {
                    map = mapFragment.getMap();
                    map.setProjectionMode(Map.Projection.GLOBE);
                    map.getPositionIndicator().setVisible(true);
                    positionManager = PositioningManager.getInstance();
                    positionManager.addListener(new WeakReference<>(positionListener));
                    positionManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
                    currentLocation = PositioningManager.getInstance().getPosition().getCoordinate();
                    map.setCenter(positionManager.getPosition().getCoordinate(),Map.Animation.BOW);
                    System.out.println("map.setCentre= "+map.getCenter());
                    // Set the zoom level to the average between min and max
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);

                }else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });

    }

    public void setSearchBar()
    {
        // UI customization
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity)this).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setIcon(android.R.color.transparent);
        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflator.inflate(R.layout.action_bar, null);

        android.support.v7.app.ActionBar.LayoutParams layoutParams = new android.support.v7.app.ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.WRAP_CONTENT);
        actionBar.setCustomView(v, layoutParams);

        mGeoAutocomplete = (CustomAutoCompleteTextView) v.findViewById(R.id.geo_autocomplete);
        mGeoAutocomplete.setThreshold(THRESHOLD);
        mGeoAutocomplete.setLoadingIndicator((android.widget.ProgressBar) v
                .findViewById(R.id.pb_loading_indicator));

        mGeoAutoCompleteAdapter = new GeocompleteAdapter(this,currentLocation);
        mGeoAutocomplete.setAdapter(mGeoAutoCompleteAdapter);

        mGeoAutocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String result = (String) adapterView.getItemAtPosition(position);
                mGeoAutocomplete.setText(result);
                GeocodeRequest req = new GeocodeRequest(result);
                req.setSearchArea(map.getBoundingBox());
                req.execute(m_listener);
            }
        });

        mGeoAutocomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!"".equals(mGeoAutocomplete.getText().toString())) {
                    GeocodeRequest req = new GeocodeRequest(mGeoAutocomplete.getText().toString());
                    req.setSearchArea(map.getBoundingBox());
                    req.execute(m_listener);
                }
            }
        });
    }

    /**
     * Only when the app's target SDK is 23 or higher, it requests each dangerous permissions it
     * needs when the app is running.
     */
    private void requestPermissions() {

        final List<String> requiredSDKPermissions = new ArrayList<String>();
        requiredSDKPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        requiredSDKPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        requiredSDKPermissions.add(Manifest.permission.INTERNET);
        requiredSDKPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
        requiredSDKPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);

        ActivityCompat.requestPermissions(this,
                requiredSDKPermissions.toArray(new String[requiredSDKPermissions.size()]),
                REQUEST_CODE_ASK_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS: {
                for (int index = 0; index < permissions.length; index++) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {

                        /**
                         * If the user turned down the permission request in the past and chose the
                         * Don't ask again option in the permission request system dialog.
                         */
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                                permissions[index])) {
                            Toast.makeText(this,
                                    "Required permission " + permissions[index] + " not granted. "
                                            + "Please go to settings and turn on for sample app",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Required permission " + permissions[index] + " not granted",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
                // all permissions were granted
                //initialize();
                break;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

                break;
        }
    }

    /**
     * Add marker on map.
     *
     * @param geoCoordinate GeoCoordinate for marker to be added.
     */
    private void addMarker(GeoCoordinate geoCoordinate) {
        if (mMarker == null) {
            Image image = new Image();
            try {
                image.setImageResource(R.drawable.pin);
            } catch (final IOException e) {
                e.printStackTrace();
            }
            mMarker = new MapMarker(geoCoordinate, image);
            mMarker.setAnchorPoint(new PointF(image.getWidth() / 2, image.getHeight()));
            map.addMapObject(mMarker);
        } else {
            mMarker.setCoordinate(geoCoordinate);
        }
        map.setCenter(geoCoordinate, Map.Animation.BOW);
    }


    private void initGetLocationButton(){
        m_GetLocationButton =(ImageButton)findViewById(R.id.getLocationButton);
        m_GetLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null && m_positionIndicatorFixed != null) {
                    map.removeMapObject(m_positionIndicatorFixed);
                    m_positionIndicatorFixed = null;
                    //currentLocation = PositioningManager.getInstance().getPosition().getCoordinate();
                    map.setCenter(PositioningManager.getInstance().getPosition().getCoordinate(),Map.Animation.BOW);


                } else {
                    //currentLocation = PositioningManager.getInstance().getPosition().getCoordinate();
                    //map.setCenter(PositioningManager.getInstance().getPosition().getCoordinate(),Map.Animation.BOW);
                    currentLocation();
                } }
        });

    }


    private void currentLocation(){

        PositioningManager.getInstance().start(PositioningManager.LocationMethod.GPS_NETWORK);
        NavigationManager.getInstance().setMap(map);
        map.setCenter(PositioningManager.getInstance().getPosition().getCoordinate(),Map.Animation.BOW);

        Image icon = new Image();
        m_positionIndicatorFixed = new MapMarker();
        m_positionIndicatorFixed.setVisible(false);

        m_positionIndicatorFixed.setCoordinate(map.getCenter());
        map.addMapObject(m_positionIndicatorFixed);
       //TODO:what is this?  m_mapObjectList.add(m_positionIndicatorFixed);
        mapFragment.getPositionIndicator().setVisible(false);

        // create a map marker to show current position
        try {

            icon.setImageResource(R.drawable.gps_position);
            m_positionIndicatorFixed.setIcon(icon);
            map.setZoomLevel(16);
            m_positionIndicatorFixed.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void initSettingsPanel() {
        m_settingsBtn = (ImageButton)findViewById(R.id.settingButton);

        /* click settings panel button to open or close setting panel. */
        m_settingsBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                m_settingsLayout = (LinearLayout) findViewById(R.id.settingsPanelLayout);
                if (m_settingsLayout.getVisibility() == View.GONE) {
                    m_settingsLayout.setVisibility(View.VISIBLE);
                    if (m_settingsPanel == null) {
                        m_settingsPanel = new SettingsPanel(MapHomePage.this, map);
                    }
                } else {
                    m_settingsLayout.setVisibility(View.GONE);
                }
            }
        });
    }


    @Override
    public void onPositionUpdated(PositioningManager.LocationMethod locationMethod, GeoPosition geoPosition, boolean b) {
        final GeoCoordinate coordinate = geoPosition.getCoordinate();
        map.setCenter(coordinate, Map.Animation.BOW);

    }

    @Override
    public void onPositionFixChanged(PositioningManager.LocationMethod locationMethod, PositioningManager.LocationStatus locationStatus) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (positionManager != null && !positionManager.isActive()) {
            positionManager
                    .addListener(new WeakReference<>(positionListener));
            positionManager.start(PositioningManager.LocationMethod.GPS_NETWORK);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (positionManager != null) {
            positionManager.removeListener(positionListener);
            positionManager.stop();
        }
    }
}
