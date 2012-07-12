package sofia.gps;

import sofia.app.internal.LifecycleInjection;
import sofia.app.internal.ScreenMixin;
import sofia.internal.MethodDispatcher;
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
	//~ Instance/static variables .............................................

	private Context context;
	private LocationManager locationManager;
	private Criteria criteria;
	private double timeInterval;
	private double minimumDistance;
	private boolean wasExplicitlyStarted;
	private boolean isTracking;


	// ----------------------------------------------------------
	public LocationTracker(Context context)
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
	public boolean isTracking()
	{
		return isTracking;
	}


	// ----------------------------------------------------------
	public boolean isTrackingPossible()
	{
		return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| locationManager.isProviderEnabled(
						LocationManager.NETWORK_PROVIDER);
	}
	
	
	// ----------------------------------------------------------
	public void presentLocationSettings()
	{
		Intent gpsOptionsIntent = new Intent(
				Settings.ACTION_LOCATION_SOURCE_SETTINGS);  
        context.startActivity(gpsOptionsIntent);
	}


	// ----------------------------------------------------------
	public void setAccuracy(Accuracy accuracy)
	{
		criteria.setAccuracy(accuracy.value());
	}

	
	// ----------------------------------------------------------
	public void setAltitudeRequired(boolean altitude)
	{
		criteria.setAltitudeRequired(altitude);
	}


	// ----------------------------------------------------------
	public void setBearingRequired(boolean bearing)
	{
		criteria.setBearingRequired(bearing);
	}


	// ----------------------------------------------------------
	public void setCostAllowed(boolean cost)
	{
		criteria.setCostAllowed(cost);
	}


	// ----------------------------------------------------------
	public void setPowerRequirement(Power power)
	{
		criteria.setPowerRequirement(power.value());
	}


	// ----------------------------------------------------------
	public void setSpeedRequired(boolean speed)
	{
		criteria.setSpeedRequired(speed);
	}


	// ----------------------------------------------------------
	public void setTimeInterval(double seconds)
	{
		timeInterval = seconds;
	}


	// ----------------------------------------------------------
	public void setMinimumDistance(double distance)
	{
		minimumDistance = distance;
	}


	// ----------------------------------------------------------
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


	// ----------------------------------------------------------
	public enum Accuracy
	{
		FINE(Criteria.ACCURACY_FINE),
		COARSE(Criteria.ACCURACY_COARSE);
		
		
		private int value;


		// ----------------------------------------------------------
		private Accuracy(int value)
		{
			this.value = value;
		}
		
		
		// ----------------------------------------------------------
		public int value()
		{
			return value;
		}
	}

	
	// ----------------------------------------------------------
	public enum Power
	{
		LOW(Criteria.POWER_LOW),
		MEDIUM(Criteria.POWER_MEDIUM),
		HIGH(Criteria.POWER_HIGH);

	
		private int value;


		// ----------------------------------------------------------
		private Power(int value)
		{
			this.value = value;
		}
		
		
		// ----------------------------------------------------------
		public int value()
		{
			return value;
		}
	}

	
	// ----------------------------------------------------------
	private LocationListener listener = new LocationListener()
	{
		// ----------------------------------------------------------
		@Override
		public void onLocationChanged(Location location)
		{
			MethodDispatcher dispatcher = new MethodDispatcher(
					"onLocationChanged", 1);
			dispatcher.callMethodOn(context, location);
		}


		// ----------------------------------------------------------
		@Override
		public void onProviderDisabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}


		// ----------------------------------------------------------
		@Override
		public void onProviderEnabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}

		
		// ----------------------------------------------------------
		@Override
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
