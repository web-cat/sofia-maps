package sofia.maps;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.android.maps.GeoPoint;

//-------------------------------------------------------------------------
/**
* <p>
* Indicates that a {@link MarkerOverlay} should use the annotated method to
* retrieve the longitude component of the object's location on the map.
* </p><p>
* The method bearing this annotation must be public, take no arguments, and
* return a numeric value indicating the decimal degrees ({@code float} or
* {@code double} preferred, or the corresponding wrapper types).
* </p><p>
* If your objects already have a method that returns the latitude and
* longitude as a {@link GeoPoint} object, then you may want to use the
* {@link ProvidesMarkerGeoPoint} annotation instead.
* </p>
* 
* @author Tony Allevato
* @version 2012.05.07
*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProvidesMarkerLongitude
{
}
