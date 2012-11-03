package sofia.gps;

import sofia.app.ActivityStarter;
import sofia.app.internal.LifecycleInjection;
import sofia.app.internal.ScreenMixin;
import sofia.internal.events.EventDispatcher;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

//-------------------------------------------------------------------------
/**
 * TODO document
 *
 * @author Tony Allevato
 */
public class LocationTracker
{
    //~ Fields ................................................................

    private static final String GPS_OPTIONS_DID_FINISH_CALLBACK =
            "gpsOptionsDidFinish";

    private Activity context;
    private LocationManager locationManager;
    private Criteria criteria;
    private double timeInterval;
    private double minimumDistance;
    private boolean wasExplicitlyStarted;
    private boolean isTracking;


    //~ Events ................................................................

    // ----------------------------------------------------------
    /**
     * Called when the location tracker detects that the user's location has
     * changed.
     *
     * @param location a {@link Location} object that describes the
     *     geographical location of the user
     */
    private EventDispatcher locationDidChange =
            new EventDispatcher("locationDidChange");


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Creates a new {@code LocationTracker} that sends its location
     * notifications to the specified context (such as a
     * {@link sofia.app.Screen} or {@link android.app.Activity}).
     *
     * @param context the context (screen or activity) that will receive the
     *     location notifications
     */
    public LocationTracker(Activity context)
    {
        this.context = context;

        locationManager = (LocationManager) context.getSystemService(
                Context.LOCATION_SERVICE);

        criteria = new Criteria();

        setAccuracy(Accuracy.COARSE);
        setAltitudeRequired(false);
        setBearingRequired(false);
        setCostAllowed(false);
        setPowerRequirement(Power.LOW);
        setSpeedRequired(false);

        timeInterval = 60;
        minimumDistance = 0;
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether location tracking is currently active.
     *
     * @return true if location tracking is currently active, otherwise false
     */
    public boolean isTracking()
    {
        return isTracking;
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the current device is capable of
     * tracking locations, using either GPS hardware or wireless networks.
     *
     * @return true if the current device is capable of tracking locations,
     *     otherwise false
     */
    public boolean isTrackingPossible()
    {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER);
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Presents the GPS options screen to the user. Applications that use the
     * location tracker should make this screen available in some way so that
     * users can enable/disable GPS if needed.
     * </p><p>
     * When the user has finished configuring the tracker, the
     * {@code gpsOptionsDidFinish} method will be called on the owning
     * activity.
     * </p>
     */
    public void presentLocationSettings()
    {
        Intent gpsOptionsIntent = new Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        ActivityStarter starter = new ActivityStarter(gpsOptionsIntent,
                GPS_OPTIONS_DID_FINISH_CALLBACK);
        starter.start(context);
    }


    // ----------------------------------------------------------
    /**
     * Gets the desired accuracy (fine or coarse) of the location tracker.
     *
     * @return the desired {@link Accuracy} of the location tracker
     */
    public Accuracy getAccuracy()
    {
        return Accuracy.valueOf(criteria.getAccuracy());
    }


    // ----------------------------------------------------------
    /**
     * Sets the desired accuracy (fine or coarse) of the location tracker.
     *
     * @param accuracy the desired {@link Accuracy} of the location tracker
     */
    public void setAccuracy(Accuracy accuracy)
    {
        criteria.setAccuracy(accuracy.value());
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the user's altitude must be reported
     * along with his or her geographical location. Not all devices and
     * location providers are able to provide this information.
     *
     * @return true if altitude information is required, otherwise false
     */
    public boolean isAltitudeRequired()
    {
        return criteria.isAltitudeRequired();
    }


    // ----------------------------------------------------------
    /**
     * Sets a value indicating whether the user's altitude must be reported
     * along with his or her geographical location. Not all devices and
     * location providers are able to provide this information.
     *
     * @param altitude true if altitude information is required, otherwise
     *     false
     */
    public void setAltitudeRequired(boolean altitude)
    {
        criteria.setAltitudeRequired(altitude);
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the user's bearing (compass direction)
     * must be reported along with his or her geographical location. Not all
     * devices and location providers are able to provide this information.
     *
     * @return true if bearing information is required, otherwise false
     */
    public boolean isBearingRequired()
    {
        return criteria.isBearingRequired();
    }


    // ----------------------------------------------------------
    /**
     * Sets a value indicating whether the user's bearing (compass direction)
     * must be reported along with his or her geographical location. Not all
     * devices and location providers are able to provide this information.
     *
     * @param bearing true if bearing information is required, otherwise false
     */
    public void setBearingRequired(boolean bearing)
    {
        criteria.setBearingRequired(bearing);
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether monetary costs (such as data charges)
     * may be incurred in order to track the user's location.
     *
     * @return true if monetary costs may be incurred in order to track the
     *     user's location, otherwise false
     */
    public boolean isCostAllowed()
    {
        return criteria.isCostAllowed();
    }


    // ----------------------------------------------------------
    /**
     * Sets a value indicating whether monetary costs (such as data charges)
     * may be incurred in order to track the user's location.
     *
     * @param cost true if monetary costs may be incurred in order to track the
     *     user's location, otherwise false
     */
    public void setCostAllowed(boolean cost)
    {
        criteria.setCostAllowed(cost);
    }


    // ----------------------------------------------------------
    /**
     * Gets the maximum power level that can be used to track the user's
     * location.
     *
     * @return the maximum {@link Power} level that can be used to track the
     *     user's location
     */
    public Power getPowerRequirement()
    {
        return Power.valueOf(criteria.getPowerRequirement());
    }


    // ----------------------------------------------------------
    /**
     * Sets the maximum power level that can be used to track the user's
     * location.
     *
     * @param power the maximum {@link Power} level that can be used to track
     *     the user's location
     */
    public void setPowerRequirement(Power power)
    {
        criteria.setPowerRequirement(power.value());
    }


    // ----------------------------------------------------------
    /**
     * Gets a value indicating whether the user's speed should be reported in
     * addition to his or her geographical position. Not all devices and
     * location providers are able to provide this information.
     *
     * @return true if speed information is required, otherwise false
     */
    public boolean isSpeedRequired()
    {
        return criteria.isSpeedRequired();
    }


    // ----------------------------------------------------------
    /**
     * Sets a value indicating that the user's speed should be reported in
     * addition to his or her geographical position. Not all devices and
     * location providers are able to provide this information.
     *
     * @param speed true if speed information is required, otherwise false
     */
    public void setSpeedRequired(boolean speed)
    {
        criteria.setSpeedRequired(speed);
    }


    // ----------------------------------------------------------
    /**
     * Gets the minimum amount of time, in seconds, that should pass between
     * location notifications. Applications that need to track the user's
     * location in real-time (for example, to display it in an active GUI),
     * should set this low with the understanding that it will put more strain
     * on the battery.
     *
     * @return the minimum amount of time, in seconds, that should pass between
     *     location notifications
     */
    public double getTimeInterval()
    {
        return timeInterval;
    }


    // ----------------------------------------------------------
    /**
     * Sets the minimum amount of time, in seconds, that should pass between
     * location notifications. Applications that need to track the user's
     * location in real-time (for example, to display it in an active GUI),
     * should set this low with the understanding that it will put more strain
     * on the battery.
     *
     * @param seconds the minimum amount of time, in seconds, that should pass
     *     between location notifications
     */
    public void setTimeInterval(double seconds)
    {
        timeInterval = seconds;
    }


    // ----------------------------------------------------------
    /**
     * Gets the minimum distance, in meters, that the user must travel before
     * another location notification is sent.
     *
     * @return the minimum distance, in meters, that the user must travel
     *     before another location notification is sent
     */
    public double getMinimumDistance()
    {
        return minimumDistance;
    }


    // ----------------------------------------------------------
    /**
     * Sets the minimum distance, in meters, that the user must travel before
     * another location notification is sent.
     *
     * @param distance the minimum distance, in meters, that the user must
     *     travel before another location notification is sent
     */
    public void setMinimumDistance(double distance)
    {
        minimumDistance = distance;
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Begins tracking the user's location.
     * </p><p>
     * If this {@code LocationTracker}'s context is a Sofia
     * {@link sofia.app.Screen}, then tracking will automatically be stopped
     * when the screen is paused (leaves the display) and restarted when the
     * screen is resumed.
     * </p>
     */
    public void startTracking()
    {
        wasExplicitlyStarted = true;

        _startTracking();

        ScreenMixin mixin = ScreenMixin.getMixin(context);
        if (mixin != null)
        {
            mixin.addLifecycleInjection(injection);
        }
    }


    // ----------------------------------------------------------
    private void _startTracking()
    {
        if (!isTracking)
        {
            String provider = locationManager.getBestProvider(criteria, true);
            locationManager.requestLocationUpdates(provider,
                    (long) (timeInterval * 1000), (float) minimumDistance,
                    listener);

            isTracking = true;
        }
    }


    // ----------------------------------------------------------
    /**
     * <p>
     * Stops tracking the user's location.
     * </p><p>
     * If this {@code LocationTracker}'s context is a {@link sofia.app.Screen},
     * then this method is called automatically when that screen is paused
     * (removed from the display). You can call it directly if you wish to stop
     * tracking at some other point (such as when a destination is reached, or
     * if the user requests it).
     * </p>
     */
    public void stopTracking()
    {
        _stopTracking();

        wasExplicitlyStarted = false;

        ScreenMixin mixin = ScreenMixin.getMixin(context);
        if (mixin != null)
        {
            mixin.removeLifecycleInjection(injection);
        }
    }


    // ----------------------------------------------------------
    private void _stopTracking()
    {
        if (isTracking)
        {
            isTracking = false;
            locationManager.removeUpdates(listener);
        }
    }


    /*public void notifyWhenNear(double latitude, double longitude,
            double radius)
    {
        addProximityAlert(latitude, longitude, (float) radius, -1);
    }


    public void notifyWhenNear(double latitude, double longitude,
            double radius, double expireTime)
    {
        addProximityAlert(latitude, longitude, (float) radius,
                (long) (expireTime * 1000));
    }


    private void addProximityAlert(double latitude, double longitude,
            float radius, long expireTime)
    {
        locationManager.addProximityAlert(latitude, longitude, radius,
                expireTime, null);
    }*/


    //~ Enumerated types ......................................................

    // ----------------------------------------------------------
    /**
     * Indicates the desired accuracy used for location tracking.
     */
    public enum Accuracy
    {
        //~ Constants .........................................................

        /**
         * Indicates that any GPS accuracy is acceptable.
         */
        ANY(Criteria.NO_REQUIREMENT),

        /**
         * Indicates that finer GPS accuracy is desired.
         */
        FINE(Criteria.ACCURACY_FINE),

        /**
         * Indicates that coarse GPS accuracy is sufficient.
         */
        COARSE(Criteria.ACCURACY_COARSE);


        //~ Fields ............................................................

        private int value;


        //~ Constructors ......................................................

        // ----------------------------------------------------------
        /**
         * Creates a new {@code Accuracy} value with the specified
         * {@link Criteria} constant.
         *
         * @param value the {@link Criteria} constant that this accuracy
         *     represents
         */
        private Accuracy(int value)
        {
            this.value = value;
        }


        //~ Public methods ....................................................

        // ----------------------------------------------------------
        /**
         * Gets the Android {@link Criteria} integer constant that corresponds
         * to this accuracy level.
         *
         * @return the Android {@link Criteria} integer constant that
         *     corresponds to this accuracy level
         */
        public int value()
        {
            return value;
        }


        // ------------------------------------------------------
        /*package*/ static Accuracy valueOf(int value)
        {
            for (Accuracy accuracy : values())
            {
                if (accuracy.value() == value)
                {
                    return accuracy;
                }
            }

            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * TODO comment
     */
    public enum Power
    {
        //~ Constants .........................................................

        /**
         * Indicates that any power requirement is acceptable.
         */
        ANY(Criteria.NO_REQUIREMENT),

        /**
         * Indicates a low power requirement.
         */
        LOW(Criteria.POWER_LOW),

        /**
         * Indicates a medium power requirement.
         */
        MEDIUM(Criteria.POWER_MEDIUM),

        /**
         * Indicates a high power requirement.
         */
        HIGH(Criteria.POWER_HIGH);


        //~ Fields ............................................................

        private int value;


        //~ Constructors ......................................................

        // ----------------------------------------------------------
        /**
         * Creates a new {@code Power} value with the specified
         * {@link Criteria} constant.
         *
         * @param value the {@link Criteria} constant that this power level
         *     represents
         */
        private Power(int value)
        {
            this.value = value;
        }


        //~ Public methods ....................................................

        // ----------------------------------------------------------
        /**
         * Gets the Android {@link Criteria} integer constant that corresponds
         * to this power level.
         *
         * @return the Android {@link Criteria} integer constant that
         *     corresponds to this power level
         */
        public int value()
        {
            return value;
        }


        // ------------------------------------------------------
        /*package*/ static Power valueOf(int value)
        {
            for (Power power : values())
            {
                if (power.value() == value)
                {
                    return power;
                }
            }

            return null;
        }
    }


    //~ Inner classes .........................................................

    // ----------------------------------------------------------
    private LocationListener listener = new LocationListener()
    {
        //~ Methods ...........................................................

        // ----------------------------------------------------------
        public void onLocationChanged(Location location)
        {
            locationDidChange.dispatch(context, location);
        }


        // ----------------------------------------------------------
        public void onProviderDisabled(String provider)
        {
            // TODO Auto-generated method stub
        }


        // ----------------------------------------------------------
        public void onProviderEnabled(String provider)
        {
            // TODO Auto-generated method stub
        }


        // ----------------------------------------------------------
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            // TODO Auto-generated method stub
        }
    };


    // ----------------------------------------------------------
    private LifecycleInjection injection = new LifecycleInjection()
    {
        // ----------------------------------------------------------
        @Override
        public void pause()
        {
            _stopTracking();
        }


        // ----------------------------------------------------------
        @Override
        public void resume()
        {
            if (wasExplicitlyStarted)
            {
                _startTracking();
            }
        }
    };
}
