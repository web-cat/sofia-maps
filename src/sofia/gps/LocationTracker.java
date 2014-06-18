package sofia.gps;

import com.google.android.gms.location.LocationRequest;

/**
 * Allows the user to specify options associated with location tracking by
 * encapsulating the Google Maps API V2 LocationRequest class
 * 
 * @author Cameron Wyatt
 * @author Tyler Lenig
 */
public class LocationTracker {

	// Fields
	// The Google Maps API V2 LocationRequest class that is used to specify
	// location tracking options
	private LocationRequest locationRequest = null;

	// Available location tracking options
	long expirationDuration, expirationTime, fastestInterval, interval;
	int numUpdates, priority;
	float smallestDisplacementMeters;

	// Constants used to specify options for Google Maps API V2
	public static final int PRIORITY_BALANCED_POWER_ACCURACY = 102,
			PRIORITY_HIGH_ACCURACY = 100, PRIORITY_LOW_POWER = 104,
			PRIORITY_NO_POWER = 105;

	//Constructor
	public LocationTracker() {
		locationRequest = LocationRequest.create();
	}

	/**
	 * Return the Google Maps API V2 LocationRequest object
	 * 
	 * @return the locationRequest
	 */
	public LocationRequest getLocationRequest() {
		return locationRequest;
	}

	/**
	 * @param locationRequest
	 *            the locationRequest to set
	 */
	public void setLocationRequest(LocationRequest locationRequest) {
		this.locationRequest = locationRequest;
	}

	/**
	 * @return the expirationDuration
	 */
	public long getExpirationDuration() {
		return expirationDuration;
	}

	/**
	 * Set the duration of this request, in milliseconds. </br></br> The
	 * duration begins immediately (and not when the request is passed to the
	 * location client), so call this method again if the request is re-used at
	 * a later time. </br></br> The location client will automatically stop
	 * updates after the request expires. </br></br> The duration includes
	 * suspend time. Values less than 0 are allowed, but indicate that the
	 * request has already expired.
	 * 
	 * @param expirationDuration
	 *            duration of request in milliseconds
	 */
	public void setExpirationDuration(long expirationDuration) {
		this.expirationDuration = expirationDuration;
		this.locationRequest.setExpirationDuration(expirationDuration);
	}

	/**
	 * @return the expirationTime
	 */
	public long getExpirationTime() {
		return expirationTime;
	}

	/**
	 * Set the request expiration time, in millisecond since boot. </br></br>
	 * This expiration time uses the same time base as elapsedRealtime().
	 * </br></br> The location client will automatically stop updates after the
	 * request expires. </br></br> The duration includes suspend time. Values
	 * before elapsedRealtime() are allowed, but indicate that the request has
	 * already expired.
	 * 
	 * @param expirationTime
	 *            expiration time of request, in milliseconds since boot
	 *            including suspend
	 */
	public void setExpirationTime(long expirationTime) {
		this.expirationTime = expirationTime;
		this.locationRequest.setExpirationTime(expirationTime);
	}

	/**
	 * @return the fastestInterval
	 */
	public long getFastestInterval() {
		return fastestInterval;
	}

	/**
	 * Explicitly set the fastest interval for location updates, in
	 * milliseconds. </br></br> This controls the fastest rate at which your
	 * application will receive location updates, which might be faster than
	 * setInterval(long) in some situations (for example, if other applications
	 * are triggering location updates). </br></br> This allows your application
	 * to passively acquire locations at a rate faster than it actively acquires
	 * locations, saving power. </br></br> Unlike setInterval(long), this
	 * parameter is exact. Your application will never receive updates faster
	 * than this value. </br></br> If you don't call this method, a fastest
	 * interval will be selected for you. It will be a value faster than your
	 * active interval (setInterval(long)). </br></br> An interval of 0 is
	 * allowed, but not recommended, since location updates may be extremely
	 * fast on future implementations. </br></br> If setFastestInterval(long) is
	 * set slower than setInterval(long), then your effective fastest interval
	 * is setInterval(long).
	 * 
	 * @param fastestInterval
	 *            fastest interval for updates in milliseconds, exact
	 */
	public void setFastestInterval(long fastestInterval) {
		this.fastestInterval = fastestInterval;
		this.locationRequest.setFastestInterval(fastestInterval);
	}

	/**
	 * @return the interval
	 */
	public long getInterval() {
		return interval;
	}

	/**
	 * Set the desired interval for active location updates, in milliseconds.
	 * </br></br> The location client will actively try to obtain location
	 * updates for your application at this interval, so it has a direct
	 * influence on the amount of power used by your application. Choose your
	 * interval wisely. </br></br> This interval is inexact. You may not receive
	 * updates at all (if no location sources are available), or you may receive
	 * them slower than requested. You may also receive them faster than
	 * requested (if other applications are requesting location at a faster
	 * interval). The fastest rate that that you will receive updates can be
	 * controlled with setFastestInterval(long). By default this fastest rate is
	 * 6x the interval frequency. </br></br> Applications with only the coarse
	 * location permission may have their interval silently throttled.
	 * </br></br> An interval of 0 is allowed, but not recommended, since
	 * location updates may be extremely fast on future implementations.
	 * </br></br> setPriority(int) and setInterval(long) are the most important
	 * parameters on a location request.
	 * 
	 * @param interval
	 *            desired interval in millisecond, inexact
	 */
	public void setInterval(long interval) {
		this.interval = interval;
		this.locationRequest.setInterval(interval);
	}

	/**
	 * @return the numUpdates
	 */
	public int getNumUpdates() {
		return numUpdates;
	}

	/**
	 * Set the number of location updates. </br></br> By default locations are
	 * continuously updated until the request is explicitly removed, however you
	 * can optionally request a set number of updates. For example, if your
	 * application only needs a single fresh location, then call this method
	 * with a value of 1 before passing the request to the location client.
	 * </br></br> When using this option care must be taken to either explicitly
	 * remove the request when no longer needed or to set an expiration with
	 * (setExpirationDuration(long) or setExpirationTime(long). Otherwise in
	 * some cases if a location can't be computed, this request could stay
	 * active indefinitely consuming power.
	 * 
	 * @param numUpdates
	 *            the number of location updates requested
	 */
	public void setNumUpdates(int numUpdates) {
		this.numUpdates = numUpdates;
		this.locationRequest.setNumUpdates(numUpdates);
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * Set the priority of the request. </br></br> Use with a priority constant
	 * such as PRIORITY_HIGH_ACCURACY. No other values are accepted. </br></br>
	 * The priority of the request is a strong hint to the LocationClient for
	 * which location sources to use. For example, PRIORITY_HIGH_ACCURACY is
	 * more likely to use GPS, and PRIORITY_BALANCED_POWER_ACCURACY is more
	 * likely to use WIFI & Cell tower positioning, but it also depends on many
	 * other factors (such as which sources are available) and is implementation
	 * dependent. </br></br> setPriority(int) and setInterval(long) are the most
	 * important parameters on a location request.
	 * 
	 * @param priority
	 *            an accuracy or power constant
	 */
	public void setPriority(int priority) {
		this.priority = priority;
		this.locationRequest.setPriority(priority);
	}

	/**
	 * @return the smallestDisplacementMeters
	 */
	public float getSmallestDisplacementMeters() {
		return smallestDisplacementMeters;
	}

	/**
	 * Set the minimum displacement between location updates in meters
	 * </br></br>
	 * By default this is 0.
	 * 
	 * @param smallestDisplacementMeters
	 *            the smallest displacement in meters the user must move between location updates.
	 */
	public void setSmallestDisplacementMeters(float smallestDisplacementMeters) {
		this.smallestDisplacementMeters = smallestDisplacementMeters;
		this.locationRequest
				.setSmallestDisplacement(smallestDisplacementMeters);
	}

}