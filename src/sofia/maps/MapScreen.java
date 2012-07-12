package sofia.maps;

import sofia.app.Screen;
import sofia.app.internal.PersistenceManager;
import sofia.app.internal.ScreenMixin;
import sofia.app.internal.SofiaLayoutInflater;
import sofia.internal.SofiaUtils;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

// -------------------------------------------------------------------------
/**
 * Due to implementation details in the Android library, {@code MapScreen}
 * cannot actually extend the {@link Screen} class. It does, however, support
 * all the same helper methods that {@code Screen} supports.
 * 
 * TODO make sure this class is in-sync with Screen
 *
 * @author  Tony Allevato
 * @version 2011.12.16
 */
public class MapScreen extends MapActivity
{
    //~ Instance/static variables .............................................

    private ScreenMixin mixin;
    private MapView mapView;
    private boolean viewHierarchyWasModified;
    private SofiaLayoutInflater layoutInflater;


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new {@code MapScreen} object.
     */
    public MapScreen()
    {
        mixin = new ScreenMixin(this);
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * <p>
     * By default, this method returns the String resource with the ID
     * {@code google_maps_api_key}, which acts as a convenient place for
     * developers to stash this key.
     * </p><p>
     * If desired, users who subclass {@code MapScreen} can also override this
     * method and return their Google Maps API key instead.
     * </p>
     *
     * @return the user's Google Maps API key
     */
    protected String mapsApiKey()
    {
        String apiKey = null;

        try
        {
            int id = SofiaUtils.getResourceId(
                this, "string", "google_maps_api_key");
            apiKey = getResources().getString(id);
        }
        catch (Exception e)
        {
            // Do nothing.
        }

        return apiKey;
    }


    // ----------------------------------------------------------
    /**
     * Called before {@link #initialize()} during the screen creation process.
     * Most users typically will not need to override this method; it is
     * intended for Sofia's own subclasses of {@link Screen} so that users can
     * override {@link #initialize()} without being required to call the
     * superclass implementation.
     */
    protected void beforeInitialize()
    {
        // Do nothing.
    }


    // ----------------------------------------------------------
    /**
     * Called once the screen has been created and made visible.
     */
    protected void afterInitialize()
    {
        // Do nothing.
    }


    // ----------------------------------------------------------
    @Override
    public Object getSystemService(String service)
    {
    	if (LAYOUT_INFLATER_SERVICE.equals(service))
    	{
    		if (layoutInflater == null)
    		{
    			layoutInflater = new SofiaLayoutInflater(this);
    		}
    		
    		return layoutInflater;
    	}
    	else
    	{
    		return super.getSystemService(service);
    	}
    }


    // ----------------------------------------------------------
    /**
     * Called when the activity is created.
     *
     * @param savedInstanceState instance data previously saved by this
     *     activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mixin.restoreInstanceState(savedInstanceState);

        // Grab the input arguments, if there were any.
        final Object[] args = mixin.getScreenArguments(getIntent());

        beforeInitialize();
        mixin.invokeInitialize(args);

        // Force the map view to be created if the user didn't already create
        // one in initialize.
        getMapView();

        // Post a call to afterInitialize() in the message queue so that it
        // gets called as soon as possible after the screen has been made
        // visible.

        getWindow().getDecorView().post(new Runnable()
        {
            public void run()
            {
                afterInitialize();
            }
        });
    }


    // ----------------------------------------------------------
    @Override
    protected void onStop()
    {
    	PersistenceManager.getInstance().savePersistentContext(this);
    	super.onStop();
    }


    // ----------------------------------------------------------
    @Override
    protected void onSaveInstanceState(Bundle bundle)
    {
        mixin.saveInstanceState(bundle);
        super.onSaveInstanceState(bundle);
    }


    // ----------------------------------------------------------
    @Override
    protected void onPause()
    {
    	mixin.runPauseInjections();
    	super.onPause();
    }


    // ----------------------------------------------------------
    @Override
    protected void onResume()
    {
    	mixin.runResumeInjections();
    	super.onResume();
    }


    // ----------------------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
    	return mixin.onCreateOptionsMenu(menu)
    			|| super.onCreateOptionsMenu(menu);
    }


    // ----------------------------------------------------------
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	return mixin.onOptionsItemSelected(item)
    			|| super.onOptionsItemSelected(item);
    }


    // ----------------------------------------------------------
    public ScreenMixin getScreenMixin()
    {
    	return mixin;
    }


    // ----------------------------------------------------------
    @Override
    public void addContentView(View view, ViewGroup.LayoutParams layout)
    {
    	viewHierarchyWasModified = true;
    	super.addContentView(view, layout);
    }


    // ----------------------------------------------------------
    @Override
    public void setContentView(int layoutResID)
    {
    	viewHierarchyWasModified = true;
    	super.setContentView(layoutResID);
    }


    // ----------------------------------------------------------
    @Override
    public void setContentView(View view)
    {
    	viewHierarchyWasModified = true;
    	super.setContentView(view);
    }


    // ----------------------------------------------------------
    @Override
    public void setContentView(View view, ViewGroup.LayoutParams layout)
    {
    	viewHierarchyWasModified = true;
    	super.setContentView(view, layout);
    }


    // ----------------------------------------------------------
    protected MapView getMapView()
    {
    	if (viewHierarchyWasModified)
    	{
    		int id = SofiaUtils.getResourceId(this, "id", "mapView");

    		if (id != 0)
    		{
    			mapView = (MapView) findViewById(id);
    		}
    	}
    	else if (mapView == null)
    	{
        	mapView = new MapView(this, mapsApiKey());
        	mapView.setClickable(true);
        	mapView.setBuiltInZoomControls(true);
        	super.setContentView(mapView);
    	}

    	if (mapView != null)
    	{
    		mapView.setClickable(true);
    		mapView.setBuiltInZoomControls(true);
    	}
    	else
    	{
    		throw new IllegalStateException("If you use a custom layout for "
    				+ "your MapScreen, you must provide a MapView in that "
    				+ "layout with the identifier 'mapView'.");
    	}

    	return mapView;
    }


    // ----------------------------------------------------------
    /**
     * Prints an informational message to the system log, tagged with the
     * "User Log" tag so that it can be easily identified in the LogCat view.
     *
     * @param message the message to log
     */
    public void log(String message)
    {
        Log.i("User Log", message);
    }


    // ----------------------------------------------------------
    /**
     * Displays a confirmation dialog and waits for the user to select an
     * option.
     *
     * @param title the title to display in the dialog
     * @param message the message to display in the dialog
     * @return true if the user clicked the "Yes" option; false if the user
     *     clicked the "No" option or cancelled the dialog (for example, by
     *     pressing the Back button)
     */
    public boolean showConfirmationDialog(
        final String title, final String message)
    {
        return mixin.showConfirmationDialog(title, message);
    }


    // ----------------------------------------------------------
    /**
     * Display a popup list to the user and waits for them to select an item
     * from it. Items in the list will be rendered simply by calling the
     * {@link Object#toString()} method. To control the item renderer used to
     * display the list, see
     * {@link #selectItemFromList(String, List, ItemRenderer)}.
     *
     * @param <Item> the type of items in the list, which is inferred from the
     *     {@code list} parameter
     * @param title the title of the popup dialog
     * @param list the list of items to display in the popup
     * @return the item that was selected from the list, or null if the dialog
     *     was cancelled
     */
    /*public <Item> Item selectItemFromList(
        String title, List<? extends Item> list)
    {
        return selectItemFromList(title, list, new SimpleItemRenderer());
    }*/


    // ----------------------------------------------------------
    /**
     * Display a popup list to the user and waits for them to select an item
     * from it.
     *
     * @param <Item> the type of items in the list, which is inferred from the
     *     {@code list} parameter
     * @param title the title of the popup dialog
     * @param list the list of items to display in the popup
     * @param itemRenderer the item renderer to use to display each item
     * @return the item that was selected from the list, or null if the dialog
     *     was cancelled
     */
    /*public <Item> Item selectItemFromList(
        String title,
        List<? extends Item> list,
        ItemRenderer itemRenderer)
    {
        return internals.selectItemFromList(title, list, itemRenderer);
    }*/


    // ----------------------------------------------------------
    /**
     * Starts the activity with the specified intent. This method will not
     * return until the new activity is dismissed by the user.
     *
     * @param intent an {@code Intent} that describes the activity to start
     */
    public void presentActivity(Intent intent)
    {
        mixin.presentActivity(intent);
    }


    // ----------------------------------------------------------
    /**
     * Starts the activity represented by the specified {@code Screen} subclass
     * and slides it into view. This method will not return until the new
     * screen is dismissed by the user.
     *
     * @param screenClass the subclass of {@code Screen} that will be displayed
     * @param args the arguments to be sent to the screen's {@code initialize}
     *     method
     */
    public void presentScreen(Class<? extends Screen> screenClass,
        Object... args)
    {
        mixin.presentScreen(screenClass, Void.class, args);
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
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data)
    {
        mixin.handleOnActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }


    // ----------------------------------------------------------
    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
    }
}
