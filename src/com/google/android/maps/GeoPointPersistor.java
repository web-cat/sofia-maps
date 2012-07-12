package com.google.android.maps;

import java.util.Map;

public class GeoPointPersistor
{
	// ----------------------------------------------------------
	public static void represent(Object obj, Map<String, Object> rep)
	{
		GeoPoint geoPoint = (GeoPoint) obj;

		rep.put("lat", geoPoint.getLatitudeE6());
		rep.put("lon", geoPoint.getLongitudeE6());
	}


	// ----------------------------------------------------------
	public static Object construct(Map<String, Object> rep)
	{
		int lat = (Integer) rep.get("lat");
		int lon = (Integer) rep.get("lon");

		return new GeoPoint(lat, lon);
	}
}
