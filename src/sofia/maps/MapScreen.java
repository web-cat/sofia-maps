package sofia.maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.w3c.dom.Document;

import sofia.app.Screen;
import sofia.app.internal.PersistenceManager;
import sofia.app.internal.ScreenMixin;
import sofia.gps.LocationTracker;
import sofia.gps.MapDirection;
import sofia.internal.events.EventDispatcher;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

// -------------------------------------------------------------------------
/**
 * Redesigned in March of 2014 to support Google Maps API V2
 * 
 * @author Cameron Wyatt
 * @author Tyler Lenig
 * @version 2014.05.07
 * 
 *         Due to implementation details in the Android library,
 *         {@code MapScreen} cannot actually extend the {@link Screen} class. It
 *         does, however, support all the same helper methods that
 *         {@code Screen} supports.
 * 
 * @author Tony Allevato
 * @version 2011.12.16
 */
public class MapScreen extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {

	// ~ Instance/static variables .............................................
	private ScreenMixin mixin;

	// Keep track of the mapping between Markers and MapItems for MapItem
	// removal
	private HashMap<Marker, MapItem> mapItems = null;
	// Keep track of the mapping between Routes and Polylines for Route removal
	private HashMap<Route, Polyline> routes = null;
	private GoogleMap myMap = null;

	// EventDispatchers that are overriden by user to define custom behavior
	private static EventDispatcher mapItemWasClicked = new EventDispatcher(
			"mapItemWasClicked");
	private static EventDispatcher mapItemDetailWasClicked = new EventDispatcher(
			"mapItemDetailWasClicked");
	private static EventDispatcher mapItemWasDragged = new EventDispatcher(
			"mapItemWasDragged");
	private static EventDispatcher mapItemWasDraggedEnd = new EventDispatcher(
			"mapItemWasDraggedEnd");
	private static EventDispatcher mapItemWasDraggedStart = new EventDispatcher(
			"mapItemWasDraggedStart");
	private static EventDispatcher locationDidChange = new EventDispatcher(
			"locationDidChange");
	private static EventDispatcher mapWasLongClicked = new EventDispatcher(
			"mapWasLongClicked");

	// Constants used to specify mode of travel when getting LatLng points for
	// route
	public static final String MODE_WALKING = "walking";
	public static final String MODE_DRIVING = "driving";

	// What mode is selected for MapDirection
	private static String mode = "";

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

	private LocationClient mLocationClient;

	// ~ Constructors ..........................................................

	// ----------------------------------------------------------
	/**
	 * Initializes a new {@code MapScreen} object.
	 */
	public MapScreen() {
		mixin = new ScreenMixin(this);
	}

	// ~ Methods ...............................................................
	// ----------------------------------------------------------
	/**
	 * Called before {@link #initialize()} during the screen creation process.
	 * Most users typically will not need to override this method; it is
	 * intended for Sofia's own subclasses of {@link Screen} so that users can
	 * override {@link #initialize()} without being required to call the
	 * superclass implementation.
	 */
	protected void beforeInitialize() {
		// Do nothing.
	}

	// ----------------------------------------------------------
	/**
	 * Called once the screen has been created and made visible.
	 */
	protected void afterInitialize() {
		// Do nothing.
	}

	// ----------------------------------------------------------
	/**
	 * This method is called after an attempted was made to inflate the screen's
	 * layout. Most users will not need to call or override this method; it is
	 * provided for Sofia's own subclasses of {@code Screen} to support custom
	 * behavior depending on whether a user layout was provided or not.
	 * 
	 * @return true if a layout was found and inflated, otherwise false
	 */
	protected void afterLayoutInflated(boolean inflated) {
		// Do nothing.
	}

	// ----------------------------------------------------------
	/**
	 * Called when the activity is created.
	 * 
	 * @param savedInstanceState
	 *            instance data previously saved by this activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mapItems = new HashMap<Marker, MapItem>();
		routes = new HashMap<Route, Polyline>();

		// Create the Google Maps API V2 LocationClient
		mLocationClient = new LocationClient(this, this, this);

		mixin.restoreInstanceState(savedInstanceState);

		// Grab the input arguments, if there were any.
		final Object[] args = mixin.getScreenArguments(getIntent());

		beforeInitialize();
		afterLayoutInflated(mixin.tryToInflateLayout());
		mixin.invokeInitialize(args);

		getWindow().getDecorView().post(new Runnable() {
			public void run() {
				afterInitialize();
			}
		});
	}

	/**
	 * Called when a Marker is clicked on </br></br> Dispatches to
	 * onMapItemClicked
	 * 
	 * @param marker
	 *            the Marker that was clicked on
	 * @return
	 */
	protected boolean handleClick(Marker marker) {
		MapItem foundItem = mapItems.get(marker);
		return mapItemWasClicked.dispatch(this, foundItem);
	}

	/**
	 * Called when a Marker's InfoWindow is clicked on </br></br> Dispatches to
	 * onMapItemDetailClicked
	 * 
	 * @param marker
	 *            the Marker that had its InfoWindow clicked on
	 */
	protected void handleDetailClick(Marker marker) {
		MapItem foundItem = mapItems.get(marker);
		mapItemDetailWasClicked.dispatch(this, foundItem);
	}

	/**
	 * Called when a Marker is dragged </br></br> Dispatches to onMapItemDragged
	 * 
	 * @param marker
	 *            the Marker that was dragged
	 */
	protected void handleDrag(Marker marker) {
		MapItem foundItem = mapItems.get(marker);
		mapItemWasDragged.dispatch(this, foundItem);
	}

	/**
	 * Called when a Marker is no longer being dragged </br></br> Dispatches to
	 * onMapItemDraggedEnd
	 * 
	 * @param marker
	 *            the Marker that is no longer being dragged
	 */
	protected void handleDragEnd(Marker marker) {
		MapItem foundItem = mapItems.get(marker);
		mapItemWasDraggedEnd.dispatch(this, foundItem);
	}

	/**
	 * Called when a Marker is beginning to be dragged </br></br> Dispatches to
	 * onMapItemDraggedStart
	 * 
	 * @param marker
	 *            the Marker that is beginning being dragged
	 */
	protected void handleDragStart(Marker marker) {
		MapItem foundItem = mapItems.get(marker);
		mapItemWasDraggedStart.dispatch(this, foundItem);
	}

	/**
	 * Called when the Map is long-pressed </br></br> Dispatches to
	 * onMapLongClick
	 * 
	 * @param point
	 *            the LatLng point that corresponds to the spot on the map that
	 *            was long-pressed
	 */
	protected void handleLongClick(LatLng point) {
		mapWasLongClicked.dispatch(this, point);
	}

	/**
	 * Add a MapItem to the map
	 * 
	 * @param mapItem
	 *            the mapItem to be added
	 * @param showDetails
	 *            whether the details window should be shown immediately
	 * @return whether the marker was successfully added
	 */
	protected boolean addMapItem(MapItem mapItem, boolean showDetails) {
		if (mapItem != null && myMap != null) {
			Marker marker = myMap.addMarker(mapItem.getMapItem());
			mapItems.put(marker, mapItem);
			if (showDetails) {
				marker.showInfoWindow();
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Add a MapItem to the map with the ability to zoom to that MapItem
	 * 
	 * @param mapItem
	 *            the MapItem to be added
	 * @param showDetails
	 *            whether the details window should be shown immediately
	 * @param zoomToPoint
	 *            whether the map should be centered on this MapItem immediately
	 * @param zoomLevel
	 *            immediately the desired zoom level, in the range of 2.0 to
	 *            21.0. Values below this range are set to 2.0, and values above
	 *            it are set to 21.0. Increase the value to zoom in. Not all
	 *            areas have tiles at the largest zoom levels.
	 * @return whether the marker was successfully added
	 */
	protected boolean addMapItem(MapItem mapItem, boolean showDetails,
			boolean zoomToPoint, float zoomLevel) {
		if (mapItem != null && myMap != null) {
			Marker marker = myMap.addMarker(mapItem.getMapItem());
			mapItems.put(marker, mapItem);
			if (showDetails) {
				marker.showInfoWindow();
			}
			myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
					mapItem.getPosition(), zoomLevel));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Takes a variable number of MapItem objects and removes them from the map
	 * 
	 * @return
	 */
	protected boolean removeMarker(MapItem... myItems) {
		try {
			for (MapItem myItem : myItems) {
				if (mapItems.containsValue(myItem)) {
					for (Entry<Marker, MapItem> e : mapItems.entrySet()) {
						if (e.getValue().equals(myItem)) {
							Marker key = e.getKey();
							key.remove();
							mapItems.remove(key);
						}
					}
				}
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Remove all of the MapItem objects that are currently on the map
	 * 
	 * @return
	 */
	protected boolean removeAllMarkers() {
		try {
			for (Entry<Marker, MapItem> e : mapItems.entrySet()) {
				e.getKey().remove();
			}
			mapItems = new HashMap<Marker, MapItem>();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Adds a Route to the map as a straight line between the source and
	 * destination of the Route
	 * 
	 * @param route
	 */
	protected void drawRoute(Route route) {
		Polyline polyLine = myMap.addPolyline(route.getMyRoute());
		routes.put(route, polyLine);
	}

	/**
	 * Adds a Route to the map as an overlay on the roads and walkways that the
	 * Route will follow from the source to the destination of the Route
	 * 
	 * @param route
	 * @param modeOfTransport
	 *            Either {@link MODE_DRIVING} or {@link MODE_WALKING}
	 * @return
	 * @throws Exception
	 *             if modeOfTransport is neither {@link MODE_DRIVING} nor
	 *             {@link MODE_WALKING}
	 */
	protected void drawRoute(Route route, String modeOfTransport)
			throws Exception {
		ArrayList<LatLng> directionPoint = null;
		if (MODE_WALKING.equals(modeOfTransport)) {
			mode = MODE_WALKING;
		} else if (MODE_DRIVING.equals(modeOfTransport)) {
			mode = MODE_DRIVING;
		} else {
			throw new Exception(
					"Must use either MapScreen.MODE_WALKING or MapScreen.MODE_DRIVING");
		}
		directionPoint = new GetDirectionsTask().execute(
				route.getSource().getPosition(),
				route.getDestination().getPosition()).get();
		PolylineOptions rectLine = new PolylineOptions()
				.width(route.getWidth()).color(route.getColor().toRawColor());

		for (int i = 0; i < directionPoint.size(); i++) {
			rectLine.add(directionPoint.get(i));
		}
		Polyline polyLine = myMap.addPolyline(rectLine);
		routes.put(route, polyLine);
	}

	/**
	 * Takes a variable number of Route objects and removes them from the map
	 * 
	 * @param route
	 */
	protected boolean removeRoute(Route... routes) {
		try {
			for (Route route : routes) {
				this.routes.get(route).remove();
				this.routes.remove(route);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Remove all of the Route objects that are currently on the map
	 * 
	 * @return
	 */
	protected boolean removeAllRoutes() {
		try {
			for (Entry<Route, Polyline> e : routes.entrySet()) {
				e.getValue().remove();
				this.routes.remove(e.getKey());
			}
			this.routes = new HashMap<Route, Polyline>();
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Computes the distance between two LatLngs
	 * 
	 * @param source
	 * @param destination
	 * @return the distance as a float
	 */
	protected float distanceBetween(LatLng source, LatLng destination) {
		float[] results = new float[1];
		Location.distanceBetween(source.latitude, source.longitude,
				destination.latitude, destination.longitude, results);
		return results[0];
	}

	/**
	 * Computes the distance between two MapItems
	 * 
	 * @param source
	 * @param destination
	 * @return the distance as a float
	 */
	protected float distanceBetween(MapItem source, MapItem destination) {
		float[] results = new float[1];
		Location.distanceBetween(source.getPosition().latitude,
				source.getPosition().longitude,
				destination.getPosition().latitude,
				destination.getPosition().longitude, results);
		return results[0];
	}

	/**
	 * Return a Location object representing the last location that
	 * mLocationClient has recorded
	 * 
	 * @return
	 */
	protected Location getLastLocation() {
		if (mLocationClient != null && mLocationClient.isConnected()) {
			return mLocationClient.getLastLocation();
		} else {
			// Unable to get the last location
			return null;
		}
	}

	/**
	 * Pass in a locationTracker in order to receive location updates </br></br>
	 * When the location changes, locationDidChange(Location location) will be
	 * called
	 * 
	 * @param locationTracker
	 */
	protected void getLocationUpdates(LocationTracker locationTracker) {
		if (locationTracker != null && mLocationClient != null
				&& servicesConnected() && mLocationClient.isConnected()) {
			mLocationClient.requestLocationUpdates(
					locationTracker.getLocationRequest(), this);
		}
	}

	/**
	 * Stop receiving location updates
	 */
	protected void stopLocationUpdates() {
		mLocationClient.disconnect();
	}

	/**
	 * This method MUST be called in order to use the available EventDispatchers
	 * 
	 * @param i
	 *            The R.id.<name> of the MapFragment in the layout XML file
	 * @return The GoogleMap object represented by the MapFragment
	 */
	protected GoogleMap getMap(int i) {
		myMap = ((MapFragment) getFragmentManager().findFragmentById(i))
				.getMap();
		myMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker marker) {
				return handleClick(marker);
			}
		});
		myMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
			@Override
			public void onInfoWindowClick(Marker marker) {
				handleDetailClick(marker);
			}
		});
		myMap.setOnMarkerDragListener(new OnMarkerDragListener() {
			@Override
			public void onMarkerDrag(Marker marker) {
				handleDrag(marker);

			}

			@Override
			public void onMarkerDragEnd(Marker marker) {
				handleDragEnd(marker);

			}

			@Override
			public void onMarkerDragStart(Marker marker) {
				handleDragStart(marker);
			}
		});
		myMap.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(LatLng point) {
				handleLongClick(point);
			}
		});
		return myMap;
	}

	/**
	 * Return the MapFragment
	 * 
	 * @param i
	 *            R.id.<name> of the MapFragment
	 * @return
	 */
	protected MapFragment getMapFragment(int i) {
		return (MapFragment) getFragmentManager().findFragmentById(i);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	// ----------------------------------------------------------
	@Override
	protected void onStop() {
		PersistenceManager.getInstance().savePersistentContext(this);
		mLocationClient.disconnect();
		super.onStop();
	}

	// ----------------------------------------------------------
	@Override
	protected void onResume() {
		super.onResume();
		mixin.runResumeInjections();
	}

	// ----------------------------------------------------------
	@Override
	protected void onPause() {
		mixin.runPauseInjections();
		super.onPause();
	}

	// ----------------------------------------------------------
	@Override
	protected void onDestroy() {
		mixin.runDestroyInjections();
		super.onDestroy();
	}

	// ----------------------------------------------------------
	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		mixin.saveInstanceState(bundle);
		super.onSaveInstanceState(bundle);
	}

	// ----------------------------------------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return mixin.onCreateOptionsMenu(menu)
				|| super.onCreateOptionsMenu(menu);
	}

	// ----------------------------------------------------------
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO: onMenuItemSelected in ScreenMixin takes an int argument
		// featureID but never uses it
		return mixin.onMenuItemSelected(0, item)
				|| super.onOptionsItemSelected(item);
	}

	// ----------------------------------------------------------
	/**
	 * Not intended to be called by users; this method is public as an
	 * implementation detail.
	 */
	public ScreenMixin getScreenMixin() {
		return mixin;
	}

	// ----------------------------------------------------------
	/**
	 * Prints an informational message to the system log, tagged with the
	 * "User Log" tag so that it can be easily identified in the LogCat view.
	 * 
	 * @param message
	 *            the message to log
	 */
	public void log(String message) {
		Log.i("User Log", message);
	}

	// ----------------------------------------------------------
//	/**
//	 * Displays an alert dialog and waits for the user to dismiss it.
//	 * 
//	 * @param title
//	 *            the title to display in the dialog
//	 * @param message
//	 *            the message to display in the dialog
//	 */
//	public void showAlertDialog(String title, String message) {
//		mixin.showAlertDialog(title, message);
//	}

	// ----------------------------------------------------------
//	/**
//	 * Displays a confirmation dialog and waits for the user to select an
//	 * option.
//	 * 
//	 * @param title
//	 *            the title to display in the dialog
//	 * @param message
//	 *            the message to display in the dialog
//	 * @return true if the user clicked the "Yes" option; false if the user
//	 *         clicked the "No" option or cancelled the dialog (for example, by
//	 *         pressing the Back button)
//	 */
//	public boolean showConfirmationDialog(final String title,
//			final String message) {
//		/*
//		 * TODO: mixin.showConfirmationDialog(title, message) throws error
//		 * java.lang.IllegalStateException: Sofia modal runnables are not
//		 * supported on this version of the Android API because the
//		 * MessageQueue.mQuitAllowed field could not be found or there was a
//		 * problem changing it.
//		 */
//		mixin.showConfirmationDialog(title, message)
//		final boolean clickedOk;
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(title);
//		builder.setMessage(message);
//		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				clickedOk = true;
//			}
//			
//		});
//		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				clickedOk = false;
//			}
//			
//		});
//		builder.create().show();
//	}

	// ----------------------------------------------------------
	/**
	 * Starts the activity with the specified intent. This method will not
	 * return until the new activity is dismissed by the user.
	 * 
	 * @param intent
	 *            an {@code Intent} that describes the activity to start
	 * @param returnMethod
	 *            the name of the method to call when the activity returns
	 */
	public void presentActivity(Intent intent, String returnMethod) {
		mixin.presentActivity(intent, returnMethod);
	}

	// ----------------------------------------------------------
	/**
	 * Starts the activity represented by the specified {@code Screen} subclass
	 * and slides it into view. This method will not return until the new screen
	 * is dismissed by the user.
	 * 
	 * @param screenClass
	 *            the subclass of {@code Screen} that will be displayed
	 * @param args
	 *            the arguments to be sent to the screen's {@code initialize}
	 *            method
	 */
	public void presentScreen(Class<? extends Screen> screenClass,
			Object... args) {
		mixin.presentScreen(screenClass, Void.class, args);
	}

	// ----------------------------------------------------------
	/**
	 * Call this method when the current screen is finished and should be
	 * closed. The specified value will be passed back to the previous screen
	 * and returned from the {@link #presentScreen(Class, Object...)} call that
	 * originally presented this screen.
	 * 
	 * @param result
	 *            the value to pass back to the previous screen
	 */
	public void finish(Object result) {
		mixin.finish(result);
	}

	// ----------------------------------------------------------
	/**
	 * Called when a sub-activity returns yielding a result. Subclasses that
	 * override this method <b>must</b> call the superclass implementation in
	 * order to make sure that built-in methods like
	 * {@link #selectImageFromGallery()} work correctly.
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CONNECTION_FAILURE_RESOLUTION_REQUEST:
			break;
		default:
			mixin.handleOnActivityResult(requestCode, resultCode, data);

			super.onActivityResult(requestCode, resultCode, data);

		}
	}

	/**
	 * Called to make sure that the GooglePlayServices are connected
	 * 
	 * @return
	 */
	private boolean servicesConnected() {
		// Check that Google Play services is available
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		// If Google Play services is available
		if (ConnectionResult.SUCCESS == resultCode) {
			// In debug mode, log the status
			log("Google Play services is available.");
			// Continue
			return true;
			// Google Play services was not available for some reason
		} else {
			log("Connection Failed - Google Play services is unavailable");
			return false;
		}
	}

	@Override
	/**
	 * If a connection to GooglePlayServices fails, try to resolve the error
	 */
	public void onConnectionFailed(ConnectionResult result) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (result.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				result.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				log(e.getMessage());
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			log("Connection Failed - Connection to Google Play services has failed");
		}
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		log("Connected to Google Play services");
	}

	@Override
	public void onDisconnected() {
		log("Disconnected from Google Play services");
	}

	@Override
	/**
	 * Called by the LocationListener when the location changes 
	 */
	public void onLocationChanged(Location location) {
		log("Location changed");
		locationDidChange.dispatch(this, location);
	}

	/**
	 * Called when the user wishes to draw a Route with options
	 * {@link MODE_DRIVING} or {@link MODE_WALKING} AsyncTask is necessary
	 * because network operations cannot be done on the main thread
	 * 
	 * @author Cameron Wyatt
	 * @author Tyler Lenig
	 */
	private class GetDirectionsTask extends
			AsyncTask<LatLng, Void, ArrayList<LatLng>> {
		protected ArrayList<LatLng> doInBackground(LatLng... points) {
			MapDirection direction = new MapDirection();
			Document doc = null;
			if (MapScreen.mode.equals(MapDirection.MODE_DRIVING)) {
				doc = direction.getDocument(points[0], points[1],
						MapDirection.MODE_DRIVING);
			} else if (MapScreen.mode.equals(MapDirection.MODE_WALKING)) {
				doc = direction.getDocument(points[0], points[1],
						MapDirection.MODE_WALKING);
			}

			ArrayList<LatLng> directionPoint = direction.getDirection(doc);
			return directionPoint;
		}
	}
}
