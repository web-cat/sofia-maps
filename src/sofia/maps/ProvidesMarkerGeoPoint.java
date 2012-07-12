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
 * retrieve the object's location on the map.
 * </p><p>
 * The method bearing this annotation must be public, take no arguments, and
 * return a {@link GeoPoint}.
 * </p><p>
 * If your objects already have separate methods that return the latitude and
 * longitude in decimal degrees (as floats or doubles), then you may want to
 * use the {@link ProvidesMarkerLatitude} and {@link ProvidesMarkerLongitude}
 * annotations instead.
 * </p><p>
 * If no method bears this annotation (or the equivalent
 * {@code ProvidesMarkerLatitude} and {@code ProvidesMarkerLongitude} ones), or
 * if the annotated method returns null, the object will not be displayed on
 * the map. 
 * </p>
 * 
 * @author Tony Allevato
 * @version 2012.05.07
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProvidesMarkerGeoPoint
{
}
