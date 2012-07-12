package sofia.maps;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//-------------------------------------------------------------------------
/**
* <p>
* Indicates that a {@link MarkerOverlay} should use the annotated method to
* retrieve the object's title when displaying a pop-up balloon for the marker.
* </p><p>
* The method bearing this annotation must be public, take no arguments, and
* return a non-void value. The returned value does not necessarily have to be a
* {@code String}; if it is not, the {@code toString} method will be called. If
* this annotation is not present on any method for the marker object, then
* {@code toString} will be called on the object itself to generate the title.
* </p>
* 
* @author Tony Allevato
* @version 2012.05.07
*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProvidesMarkerTitle
{
}
