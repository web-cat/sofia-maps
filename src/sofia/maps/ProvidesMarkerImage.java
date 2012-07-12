package sofia.maps;

//-------------------------------------------------------------------------
/**
* <p>
* Indicates that a {@link MarkerOverlay} should use the annotated method to
* retrieve the marker image to be displayed on the map for the object.
* </p><p>
* The method bearing this annotation must be public, take no arguments, and
* return one of the following types:
* </p>
* <dl>
* <dt>{@code Bitmap}</dt>
* <dd>A bitmap image. The bitmap will be centered on the location of the
* object. No scaling will be performed.</dd>
* <dt>{@code Drawable}</dt>
* <dd>A {@code Drawable} object (such as a {@code BitmapDrawable}). The
* drawable is drawn respecting its bounds, which allows you to position it in
* a manner other than the center (for example, the bottom-center, for "pin"
* images).</dd>
* <dt>{@code int}</dt>
* <dd>A drawable resource identifier. This is similar to the previous option,
* except that the drawable will be loaded from the application's resources
* rather than being created programatically.</dd>
* </dl>
* <p>
* If the annotated method returns null, then the default marker (a red pin with
* an appearance similar to the one in the built-in Maps application) will be
* used instead.
* </p>
* 
* @author Tony Allevato
* @version 2012.05.07
*/
public @interface ProvidesMarkerImage
{
}
