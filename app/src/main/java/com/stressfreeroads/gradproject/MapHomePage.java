package com.stressfreeroads.gradproject;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.here.android.mpa.common.GeoBoundingBox;
import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.GeoPosition;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.PositioningManager;
import com.here.android.mpa.common.RoadElement;
import com.here.android.mpa.guidance.NavigationManager;
import com.here.android.mpa.guidance.TrafficNotification;
import com.here.android.mpa.guidance.TrafficNotificationInfo;
import com.here.android.mpa.guidance.TrafficWarner;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapRoute;
import com.here.android.mpa.mapping.MapTrafficLayer;
import com.here.android.mpa.routing.CoreRouter;
import com.here.android.mpa.routing.Maneuver;
import com.here.android.mpa.routing.Route;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.routing.RouteTta;
import com.here.android.mpa.routing.RouteWaypoint;
import com.here.android.mpa.routing.Router;
import com.here.android.mpa.routing.RoutingError;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.GeocodeRequest;
import com.here.android.mpa.search.Location;
import com.here.android.mpa.search.ResultListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Manages all the main activities of HERE map.
 */
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
    private Button m_startNavigation;
    private TextView m_nextManeuver;
    private SettingsPanel m_settingsPanel;
    private LinearLayout m_settingsLayout;
    private static final Integer THRESHOLD = 2;
    private Route m_mapRoute;
    private NavigationManager m_navigationManager;
    private GeoBoundingBox m_geoBoundingBox;
    private boolean m_foregroundServiceStarted;
    private GeoCoordinate destination = null;
    private Boolean isNavigationPossible = false;
    private TextView dist_time_text;
    private Date current;
    private Date dateval;
    private long dateDiffBetman;
    private long datestored;
    private long timeleft;
    private long hours;
    private long min;
    private RouteTta rrta;
    private static String opt1 = "Very strong dislike/ avoid at all costs";
    private static String opt2 = "Dislike/ sometimes avoid";
    private static String opt3 = "Don’t care";
    private static String opt4 = "Like/ sometimes prefer";
    private static String opt5 = "Very strong like/ always prefer";



    private MapRoute mapRoute = null;
    private GeoCoordinate currentLocation = null;
    private MapMarker m_positionIndicatorFixed = null;

    //file management-start
    BufferedWriter writer;
    Long x;
    //file management- end

    /**
     * Position Listener
     */
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

    /**
     * Checks the result of Address searched and call route creating methods.
     */
    protected ResultListener<List<Location>> m_listener = new ResultListener<List<Location>>() {
        @Override
        public void onCompleted(List<Location> data, ErrorCode error) {
            if (error == ErrorCode.NONE) {
                if (data != null && data.size() > 0) {
                    addMarker(data.get(0).getCoordinate());
                    //create route
                    if (map != null && m_mapRoute != null) {
                        //map.removeMapObject(m_mapRoute);
                        m_mapRoute = null;
                    } else {
                    /*
                     * The route calculation requires local map data.Unless there is pre-downloaded
                     * map data on device by utilizing MapLoader APIs, it's not recommended to
                     * trigger the route calculation immediately after the MapEngine is
                     * initialized.The INSUFFICIENT_MAP_DATA error code may be returned by
                     * CoreRouter in this case.
                     *
                     */
                        initNaviControlButton(data.get(0).getCoordinate());
                        System.out.println("Start navigation");
                        hideKeyboard(MapHomePage.this);
                    }

                }
            }
        }
    };

    /**
     * Initiate the basic components used in Map Home Page.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_map_home_page);

        //Check required permissions
        requestPermissions();

        //Initialize Map
        initializeMap();

        //Set Search Action Bar
        setSearchBar();

        //current Location Button
        initGetLocationButton();

        //settings panel
        initSettingsPanel();

        //Initiate file to save trip details
        fileManager();

        m_startNavigation = (Button) findViewById(R.id.startNavigation);
        m_startNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_startNavigation.getText().equals("Start Navigation")) {
                    if (isNavigationPossible) {
                        isNavigationPossible = false;
                        startNavigation();
                        m_startNavigation.setText("Stop Navigation");
                        m_startNavigation.setBackgroundColor(Color.RED);
                    } else {
                        Toast.makeText(MapHomePage.this,
                                "Error:route not ready yet.",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    stopNavigation();
                }
            }
        });
    }

    /**
     * Clear mandatory fields and Stop Navigation.
     */
    private void stopNavigation() {
        m_navigationManager.stop();
        stopForegroundService();
        map.removeMapObject(mMarker);
        mMarker = null;
        mGeoAutocomplete.setText("");
        m_startNavigation.setVisibility(View.INVISIBLE);
        m_startNavigation.setText("Start Navigation");
        m_startNavigation.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        isNavigationPossible = false;
        map.removeMapObject(mapRoute);
        mapRoute = null;
        m_mapRoute = null;
        try {
            writer.write("Trip ended" + "\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dist_time_text.setVisibility(View.GONE);
        m_nextManeuver.setVisibility(View.GONE);
        map.setCenter(PositioningManager.getInstance().getPosition().getCoordinate(), Map.Animation.BOW);
    }

    /**
     * Initialize Map and its important components.
     */
    public void initializeMap() {

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
                    //TODO:initial position doesnt work correctly.
                    // currentLocation = PositioningManager.getInstance().getPosition().getCoordinate();
                    map.setCenter(positionManager.getPosition().getCoordinate(), Map.Animation.BOW);
                    // Set the zoom level to the average between min and max
                    map.setTrafficInfoVisible(true);
                    map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.FLOW, true);
                    map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.ONROUTE, true);
                    map.getMapTrafficLayer().setEnabled(MapTrafficLayer.RenderLayer.INCIDENT, true);

                    /**
                     * Get the NavigationManager instance.It is responsible for providing voice
                     * and visual instructions while driving and walking
                     */
                    m_navigationManager = NavigationManager.getInstance();
                } else {
                    System.out.println("ERROR: Cannot initialize Map Fragment");
                }
            }
        });
    }

    /**
     * Manages address Searching.
     */
    public void setSearchBar() {
        // UI customization
        android.support.v7.app.ActionBar actionBar = ((AppCompatActivity) this).getSupportActionBar();
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

        mGeoAutoCompleteAdapter = new GeocompleteAdapter(this, currentLocation);
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

    /**
     * Request Permission status.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
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

    /**
     * Location button functionality.
     */
    private void initGetLocationButton() {
        if (map != null && m_positionIndicatorFixed != null) {
            map.setCenter(positionManager.getPosition().getCoordinate(), Map.Animation.BOW);
        }
        m_GetLocationButton = (ImageButton) findViewById(R.id.getLocationButton);
        m_GetLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map != null && m_positionIndicatorFixed != null) {
                    map.removeMapObject(m_positionIndicatorFixed);
                    m_positionIndicatorFixed = null;
                    map.setCenter(PositioningManager.getInstance().getPosition().getCoordinate(), Map.Animation.BOW);
                } else {
                    currentLocation();
                }
            }
        });
    }

    /**
     * Fetch Current location and Add marker.S
     */
    private void currentLocation() {

        PositioningManager.getInstance().start(PositioningManager.LocationMethod.GPS_NETWORK);
        NavigationManager.getInstance().setMap(map);
        map.setCenter(PositioningManager.getInstance().getPosition().getCoordinate(), Map.Animation.BOW);
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

    /**
     * Manages Setting panel initialization.
     */
    private void initSettingsPanel() {
        m_settingsBtn = (ImageButton) findViewById(R.id.settingButton);

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

    /**
     * Creates a route from current location to selected destination.
     *
     * @param finalPosition
     */
    private void createRoute(GeoCoordinate finalPosition) {

        String [] answers = ProfileManager.getAnswers();
        /* Initialize a CoreRouter */
        CoreRouter coreRouter = new CoreRouter();

        /* Initialize a RoutePlan */
        RoutePlan routePlan = new RoutePlan();

        /*
         * Initialize a RouteOption.
         */
        RouteOptions routeOptions = new RouteOptions();

        /* Disable highway in this route. 1 */
        if(answers[3].equals(opt1) ||answers[3].equals(opt2)) {
            routeOptions.setHighwaysAllowed(false);
        } else {
            routeOptions.setHighwaysAllowed(true);
        }

        /* Calculate the fastest route available. 4 */
        if(answers[1].equals(opt1) || answers[6].equals(opt2)) {
            routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        } else if(answers[1].equals(opt4) || answers[6].equals(opt5)){
            routeOptions.setRouteType(RouteOptions.Type.SHORTEST);
        }
        else {
            routeOptions.setRouteType(RouteOptions.Type.BALANCED);
        }

        /* Calculate 1 route. */
        routeOptions.setRouteCount(1);
        /* Finally set the route option */
        routePlan.setRouteOptions(routeOptions);

        /* Define waypoints for the route */
        /* START: Current Location */
        RouteWaypoint startPoint = new RouteWaypoint(new GeoCoordinate(PositioningManager.getInstance().getPosition().getCoordinate().getLatitude(), PositioningManager.getInstance().getPosition().getCoordinate().getLongitude()));
       // RouteWaypoint startPoint = new RouteWaypoint(new GeoCoordinate(45.4154314,-75.67147890000001));
        /* END: Langley BC */
        RouteWaypoint destination = new RouteWaypoint(new GeoCoordinate(finalPosition.getLatitude(), finalPosition.getLongitude()));

        /* Add both waypoints to the route plan */
        routePlan.addWaypoint(startPoint);
        routePlan.addWaypoint(destination);

        /* Trigger the route calculation,results will be called back via the listener */
        coreRouter.calculateRoute(routePlan,
                new Router.Listener<List<RouteResult>, RoutingError>() {
                    @Override
                    public void onProgress(int i) {
                        /* The calculation progress can be retrieved in this callback. */
                    }

                    @Override
                    public void onCalculateRouteFinished(List<RouteResult> routeResults,
                                                         RoutingError routingError) {
                        /* Calculation is done.Let's handle the result */
                        if (routingError == RoutingError.NONE) {
                            if (routeResults.get(0).getRoute() != null) {
                                /* Create a MapRoute so that it can be placed on the map */
                                m_mapRoute = routeResults.get(0).getRoute();

                                /* Create a MapRoute so that it can be placed on the map */
                                mapRoute = new MapRoute(routeResults.get(0).getRoute());

                                /* Show the maneuver number on top of the route */
                                mapRoute.setManeuverNumberVisible(true);

                                /* Add the MapRoute to the map */
                                map.addMapObject(mapRoute);

                                /*
                                 * We may also want to make sure the map view is orientated properly
                                 * so the entire route can be easily seen.
                                 */
                                GeoBoundingBox m_geoBoundingBox = routeResults.get(0).getRoute()
                                        .getBoundingBox();
                                map.zoomTo(m_geoBoundingBox, Map.Animation.NONE,
                                        Map.MOVE_PRESERVE_ORIENTATION);

                                isNavigationPossible = true;

                                Route route = mapRoute.getRoute();

                                //show distance in meters
                                float dist = route.getLength();
                                //for Tta(Time to arrival
                                RouteTta rrta = route.getTta(Route.TrafficPenaltyMode.OPTIMAL, Route.WHOLE_ROUTE);
                                int getTimesec = rrta.getDuration();

                                List<GeoCoordinate> allCordinates = route.getRouteGeometry();
                                AddTextBox(allCordinates.get(allCordinates.size() / 2), dist, getTimesec);

                                m_startNavigation.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(MapHomePage.this,
                                        "Error:route results returned is not valid",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MapHomePage.this,
                                    "Error:route calculation returned error code: " + routingError,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /**
     * Stops navigation if route is null.
     *
     * @param coordinate
     */
    private void initNaviControlButton(final GeoCoordinate coordinate) {

        if (m_mapRoute == null) {
            createRoute(coordinate);
        } else {
            m_navigationManager.stop();

            /**
             * Restore the map orientation to show entire route on screen
             */
            map.zoomTo(m_geoBoundingBox, Map.Animation.NONE, 0f);
            m_mapRoute = null;
        }
    }

    /**
     * Start trip Navigation,
     */
    private void startNavigation() {

        try {
            writer.write("Trip Started" + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* Display the position indicator on map */
        map.getPositionIndicator().setVisible(true);
        /* Configure Navigation manager to launch navigation on current map */
        m_navigationManager.setMap(map);

        // set guidance view to position with road ahead, tilt and zoomlevel was setup before manually
        // choose other update modes for different position and zoom behavior
        NavigationManager.getInstance().setMapUpdateMode(NavigationManager.MapUpdateMode.POSITION_ANIMATION);

        m_navigationManager.startNavigation(m_mapRoute);
        //m_navigationManager.simulate(m_mapRoute,60);
        map.setTilt(60);
        startForegroundService();
        /*
         * Set the map update mode to ROADVIEW.This will enable the automatic map movement based on
         * the current location.If user gestures are expected during the navigation, it's
         * recommended to set the map update mode to NONE first. Other supported update mode can be
         * found in HERE Android SDK API doc
         */
        m_navigationManager.setMapUpdateMode(NavigationManager.MapUpdateMode.ROADVIEW);

        /*
         * NavigationManager contains a number of listeners which we can use to monitor the
         * navigation status and getting relevant instructions.
         */
        addNavigationListeners();
    }

    /*
     * Android 8.0 (API level 26) limits how frequently background apps can retrieve the user's
     * current location. Apps can receive location updates only a few times each hour.
     */
    private void startForegroundService() {
        if (!m_foregroundServiceStarted) {
            m_foregroundServiceStarted = true;
            Intent startIntent = new Intent(this, ForegroundService.class);
            startIntent.setAction(ForegroundService.START_ACTION);
            this.getApplicationContext().startService(startIntent);
        }
    }

    /**
     * Stop Foreground Services.
     */
    private void stopForegroundService() {
        if (m_foregroundServiceStarted) {
            m_foregroundServiceStarted = false;
            Intent stopIntent = new Intent(this, ForegroundService.class);
            stopIntent.setAction(ForegroundService.STOP_ACTION);
            this.getApplicationContext().startService(stopIntent);
        }
    }

    /**
     * Adds all required navigation Listeners to collect data fo the trip.
     */
    private void addNavigationListeners() {

        /**
         * Register a NewInstructionEventListener to monitor the status change on
         * NavigationManager
         */
        m_navigationManager.addNewInstructionEventListener(
                new WeakReference<NavigationManager.NewInstructionEventListener>(instructionHandler));

        /*
         * Register a NavigationManagerEventListener to monitor the status change on
         * NavigationManager
         */
        m_navigationManager.addNavigationManagerEventListener(
                new WeakReference<NavigationManager.NavigationManagerEventListener>(
                        m_navigationManagerEventListener));

        /* Register a PositionListener to monitor the position updates */
        m_navigationManager.addPositionListener(
                new WeakReference<NavigationManager.PositionListener>(m_positionListener));

        /*Register new Maneuver Listener to get the current information about the maneuver*/
        m_navigationManager.addManeuverEventListener(
                new WeakReference<NavigationManager.ManeuverEventListener>(m_maneuverListener));

        /*set up route recalculation in navigation*/
        m_navigationManager.addRerouteListener(
                new WeakReference<NavigationManager.RerouteListener>(m_reRouteListener));


    }

    /**
     * Calculates time and distance remaining on position change.
     */
    private NavigationManager.PositionListener m_positionListener = new NavigationManager.PositionListener() {
        @Override
        public void onPositionUpdated(GeoPosition geoPosition) {
            /* Current position information can be retrieved in this callback */
            if (m_navigationManager.getRunningState().equals(NavigationManager.NavigationState.RUNNING)) {
                Maneuver maneuver = NavigationManager.getInstance().getNextManeuver();
                if (maneuver != null) {
                    float distanceleft = mapRoute.getRoute().getLength() - maneuver.getDistanceFromStart() + maneuver.getDistanceFromPreviousManeuver();
                    distanceleft = Math.round(distanceleft / 10);
                    distanceleft /= 100;

                    String timeRemaining = "";
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(m_navigationManager.getEta(true, Route.TrafficPenaltyMode.OPTIMAL).getTime() - new Date().getTime());
                    long hourRemaining = minutes / 60;
                    if (hourRemaining == 0)
                        timeRemaining = minutes + " mins ";
                    else if (minutes == 0)
                        timeRemaining = timeleft + " secs";
                    else
                        timeRemaining = hours + " hrs " + minutes + " mins";
                    dist_time_text.setText("dist-Left:" + distanceleft + " km\nEstimated Time:" + timeRemaining);

                    if (dist_time_text.getVisibility() == View.GONE)
                        dist_time_text.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    /**
     * Displays next maneuver ti user.
     */
    private NavigationManager.NewInstructionEventListener instructionHandler = new NavigationManager.NewInstructionEventListener() {
        @Override
        public void onNewInstructionEvent() {
            Maneuver maneuver = m_navigationManager.getNextManeuver();
            if (maneuver != null) {
                if (maneuver.getAction() != Maneuver.Action.NO_ACTION) {
                    //notify the user about next maneuver
                    if(maneuver.getTurn().toString().equals("UNDEFINED"))
                    {
                        m_nextManeuver.setText("Next : NA");
                    } else {
                        m_nextManeuver.setText("Next : "+maneuver.getTurn());
                    }

                    m_nextManeuver.setVisibility(View.VISIBLE);
                }
                super.onNewInstructionEvent();
            }
        }
    };

    /**
     * Collects data for each Maneuver.
     */
    private NavigationManager.ManeuverEventListener m_maneuverListener = new NavigationManager.ManeuverEventListener() {
        @Override
        public void onManeuverEvent() {
            RoadElement roadElement = PositioningManager.getInstance().getRoadElement();
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            int Speed = (int)(roadElement.getSpeedLimit () * 3.6);
            Speed = (Speed == 0? 10: Speed);

            String data = "RoadName: " + roadElement.getRoadName() + ", GeoCordinates: "+roadElement.getGeometry() +
                    ", NumberOfLanes: " + (roadElement.getNumberOfLanes() == 0 ? 1 : roadElement.getNumberOfLanes())
                    + ", Speed Limit(Km/hr): " + Speed + ", Timestamp: " + timestamp;
            try {
                writer.write(data + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*Retrieve current traffic condition*/
            TrafficWarner trafficWarner = m_navigationManager.getTrafficWarner();
            trafficWarner.init();
            trafficWarner.addListener(new WeakReference<TrafficWarner.Listener>(m_trafficListener));
        }
    };

    /**
     * Collects data when traffic is not normal.
     */
    private TrafficWarner.Listener m_trafficListener = new TrafficWarner.Listener() {

        @Override
        public void onTraffic(TrafficNotification trafficNotification) {
            TrafficNotificationInfo trafficNotificationInfo = trafficNotification.getInfoList().get(0);
            try {
                writer.write("Traffic severity: " + trafficNotificationInfo.getSeverity() +
                        ", Traffic severity value: " + trafficNotificationInfo.getSeverity().getValue()
                        + ", Affected Length: " + trafficNotificationInfo.getAffectedLength() + "\n");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    /**
     * Monitors current state of the Navigation.
     */
    private NavigationManager.NavigationManagerEventListener m_navigationManagerEventListener = new NavigationManager.NavigationManagerEventListener() {

        @Override
        public void onRunningStateChanged() {

        }

        @Override
        public void onRouteUpdated(final Route updatedRoute) {
            // This does not happen on re-route
            Toast.makeText(getApplicationContext(), "Your route was udated!", Toast.LENGTH_LONG)
                    .show();
        }

        @Override
        public void onEnded(NavigationManager.NavigationMode navigationMode) {
            Toast.makeText(MapHomePage.this, navigationMode + " was ended", Toast.LENGTH_SHORT).show();
            stopForegroundService();
            try {
                writer.write("Trip ended" + "\n");
                writer.close();
                stopNavigation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Manages re-routing.
     */
    private NavigationManager.RerouteListener m_reRouteListener = new NavigationManager.RerouteListener() {
        @Override
        public void onRerouteBegin() {
            super.onRerouteBegin();
            Toast.makeText(getApplicationContext(), "reroute begin", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * Displays time and distance left.
     *
     * @param geo
     * @param dist
     * @param getTimesec
     */
    private void AddTextBox(GeoCoordinate geo, float dist, int getTimesec) {

        dist_time_text = (TextView) findViewById(R.id.distance_time);

        m_nextManeuver = (TextView) findViewById(R.id.nextManeuver);

        //calculate distance
        float distance = Math.round(dist / 10);
        distance /= 100;

        //calculate time
        int hours = getTimesec / 3600;
        int min = (getTimesec - hours * 3600) / 60;
        String Time = "";

        if (hours == 0)
            Time = min + " mins";
        else if (min == 0)
            Time = getTimesec + " secs";
        else
            Time = hours + " hrs " + min + " mins";

        dist_time_text.setText("dist: " + distance + " km\n" + "Time:" + Time);
        dist_time_text.setVisibility(View.VISIBLE);

    }

    /**
     * hide keyboard after search is complete
     *
     * @param activity
     */
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public void onDestroy() {
        /* Stop the navigation when app is destroyed */
        super.onDestroy();
        if (m_navigationManager != null) {
            stopForegroundService();
            m_navigationManager.stop();
        }
        try {
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*File Management
    * TODO:Refactor it to new file*/
    protected void fileManager() {
        try {
            // Creates a file in the primary external storage space of the current application.
            // If the file does not exists, it is created.
            File tripDetailFile = new File(this.getExternalFilesDir(null), "TripData.txt");
            if (!tripDetailFile.exists())
                tripDetailFile.createNewFile();

            // Adds a line to the file
            writer = new BufferedWriter(new FileWriter(tripDetailFile, true /*append*/));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
