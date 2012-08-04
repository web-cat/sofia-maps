package sofia.maps;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//-------------------------------------------------------------------------
/**
* <p>
* Indicates that a {@link MarkerOverlay} should use the annotated method to
* retrieve the object's content when displaying a pop-up balloon for the
* marker. Two types of content are currently supported; {@code String} and
* {@code Bitmap}.
* </p><p>
* The method bearing this annotation must be public, take no arguments, and
* return a non-void value. If this annotation is not present on any method for
* the marker object, then no content (only the title) will be displayed in
* the pop-up balloon.
* </p>
* 
* @author Tony Allevato
* @version 2012.05.07
*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ProvidesMarkerContent
{
}
