package sofia.maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import sofia.internal.events.EventDispatcher;
import sofia.widget.ProvidesTitle;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.Overlay;

//-------------------------------------------------------------------------
/**
 * <p>
 * An abstract overlay that supports displaying elements in a list of arbitrary
 * objects. These objects can have annotations on methods to provide marker
 * information ({@link ProvidesMarkerGeoPoint}, {@link ProvidesMarkerTitle},
 * etc.), or alternatively a "decorator" object can be specified that provides
 * this information given an item on the map.
 * </p><p>
 * {@code AbstractListOverlay} provides no rendering of its own, only methods
 * by which subclasses can access the decorations for the items that should be
 * displayed on the map. Sofia comes with two concrete subclasses that should
 * be suitable for most users:
 * </p>
 * <ul>
 * <li>{@link MarkerOverlay}: displays each of the items in the list as a
 * marker or "pin" on the map</li>
 * <li>{@link RouteOverlay}: treats the items in the list as a sequence of
 * waypoints or stops and draws a line that connects them</li>
 * </ul>
 * <p>
 * This class provides support for displaying an informational "balloon"
 * containing the title and content of the item when it is tapped, but it is
 * disabled by default for this class. The {@link MarkerOverlay} class enables
 * it by default, and the {@link RouteOverlay} class leaves is disabled. Custom
 * subclasses that want to enable this functionality should call
 * {@link #setShowsBalloonWhenClicked(boolean)}.
 * </p>
 *
 * <h3>Event Handling</h3>
 * <p>
 * Users can implement the following methods in the context that owns this
 * overlay's {@code MapView} (for example, their {@link MapScreen} class) to
 * receive event notifications:
 * <dl>
 * <dt><code>[void|boolean] mapItemWasClicked(Item item)</code></dt>
 * <dd>Called when the user clicks the location on the map representing one of
 * the items in the list. The type of the "item" parameter must be compatible
 * with the type of objects in the overlay's list. If this method is boolean
 * and returns true, the event is assumed to be "consumed" and the default
 * balloon popup will not appear.
 * </dd>

 * <dt><code>void mapBalloonWasClicked(Item item)</code></dt>
 * <dd>Called when the balloon popup attached to the specified item is clicked.
 * The type of the "item" parameter must be compatible with the type of objects
 * in the overlay's list.</dd>
 * </dl>
 * </p>
 *
 * @param <Item> the type of object in the list
 *
 * @author Tony Allevato
 * @version 2012.04.22
 */
public abstract class AbstractListOverlay<Item> extends Overlay
{
    //~ Instance/static variables .............................................

    private static final EventDispatcher mapItemWasClicked =
            new EventDispatcher("mapItemWasClicked");

    private List<Item> list;

    protected static final AnnotationDecorator defaultDecorator =
            new AnnotationDecorator();
    private Object decorator;

    private boolean showsBalloonWhenClicked;
    private MapView mapView;
    private MapBalloonView balloonView;


    //~ Constructors ..........................................................

    // ----------------------------------------------------------
    /**
     * Initializes a new {@code AbstractListOverlay}.
     *
     * @param mapView the {@link MapView} to which this overlay will be added
     * @param list the list of objects to display in the overlay
     */
    public AbstractListOverlay(MapView mapView, List<Item> list)
    {
        this.mapView = mapView;
        this.list = list;
        this.showsBalloonWhenClicked = true;
    }


    //~ Methods ...............................................................

    // ----------------------------------------------------------
    /**
     * Gets the {@link MapView} on which this overlay will be displayed.
     *
     * @return the {@link MapView} containing this overlay
     */
    public MapView getMapView()
    {
        return mapView;
    }


    // ----------------------------------------------------------
    /**
     * Gets the list of objects to display in the overlay.
     *
     * @return the list of objects to display in the overlay
     */
    public List<Item> getList()
    {
        return list;
    }


    // ----------------------------------------------------------
    /**
     * Sets the list of objects to display in the overlay. Calling this method
     * will automatically repaint the map view; you do not need to do so
     * separately.
     *
     * @param newList the new list of objects to display in the overlay
     */
    public void setList(List<Item> newList)
    {
        list = newList;
        mapView.postInvalidate();
    }


    // ----------------------------------------------------------
    public Object getDecorator()
    {
        return decorator;
    }


    // ----------------------------------------------------------
    public void setDecorator(Object decorator)
    {
        this.decorator = decorator;
    }


    // ----------------------------------------------------------
    public boolean showsBalloonWhenClicked()
    {
        return showsBalloonWhenClicked;
    }


    // ----------------------------------------------------------
    public void setShowsBalloonWhenClicked(boolean showBalloon)
    {
        this.showsBalloonWhenClicked = showBalloon;
    }


    // ----------------------------------------------------------
    @Override
    public boolean onTap(GeoPoint tapPoint, MapView mapView)
    {
        List<Item> list = getList();

        boolean opened = false;

        for (int i = list.size() - 1; i >= 0; i--)
        {
            Item item = list.get(i);

            GeoPoint itemPoint = getItemGeoPoint(item);

            if (itemPoint != null)
            {
                if (hitTest(tapPoint, itemPoint, item))
                {
                    if (showsBalloonWhenClicked
                            && !mapItemWasClicked.dispatch(
                                    mapView.getContext(), item))
                    {
                        openBalloon(itemPoint, item);
                        opened = true;
                        break;
                    }

                    mapView.getController().animateTo(itemPoint);
                }
            }
        }

        // If no marker was touched, we should hide the balloon.
        if (showsBalloonWhenClicked && !opened)
        {
            mapView.removeView(balloonView);
            balloonView = null;
        }

        return super.onTap(tapPoint, mapView);
    }


    // ----------------------------------------------------------
    /**
     * Determines if the specified geopoint falls anywhere within the bounds of
     * the marker image.
     *
     * @param point the geopoint to test
     * @return true if the geopoint is within the bounds of the marker image,
     *     otherwise false
     */
    protected boolean hitTest(GeoPoint tapPoint, GeoPoint queryPoint, Item item)
    {
        Point bottomCenter = mapView.getProjection().toPixels(queryPoint, null);
        Point tapped = mapView.getProjection().toPixels(tapPoint, null);

        Rect bounds = getItemBounds(bottomCenter, item);
        return bounds.contains(tapped.x, tapped.y);
    }


    // ----------------------------------------------------------
    protected Rect getItemBounds(Point point, Item item)
    {
        return new Rect(point.x - 10, point.y - 10,
                point.x + 10, point.y + 10);
    }


    // ----------------------------------------------------------
    protected void openBalloon(GeoPoint geoPoint, Item item)
    {
        MapView.LayoutParams params = new MapView.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                geoPoint, MapView.LayoutParams.BOTTOM_CENTER);
        params.mode = MapView.LayoutParams.MODE_MAP;

        if (balloonView == null)
        {
            balloonView = new MapBalloonView(mapView.getContext());
            mapView.addView(balloonView, params);
        }
        else
        {
            balloonView.setLayoutParams(params);
        }

        balloonView.setFields(
                item, getItemTitle(item), getItemContent(item));
        balloonView.post(new Runnable() {
            @Override
            public void run()
            {
                int cx = balloonView.getLeft() + balloonView.getWidth() / 2;
                int cy = balloonView.getTop() + balloonView.getHeight() / 2;

                GeoPoint shiftedPoint = mapView.getProjection().fromPixels(cx, cy);
                mapView.getController().animateTo(shiftedPoint);
            }
        });
    }


    // ----------------------------------------------------------
    protected GeoPoint getItemGeoPoint(Item item)
    {
        GeoPoint geoPoint; /*= decorate(decorator, "getMarkerDrawable",
                Drawable.class, item);

        if (title == null)*/
        {
            geoPoint = defaultDecorator.getItemGeoPoint(item);
        }

        return geoPoint;
    }


    // ----------------------------------------------------------
    protected String getItemTitle(Item item)
    {
        String title; /*= decorate(decorator, "getMarkerDrawable",
                Drawable.class, item);

        if (title == null)*/
        {
            title = defaultDecorator.getItemTitle(item);
        }

        return title;
    }


    // ----------------------------------------------------------
    protected Object getItemContent(Item item)
    {
        Object content; /*= decorate(decorator, "getMarkerDrawable",
                Drawable.class, item);

        if (snippet == null)*/
        {
            content = defaultDecorator.getItemContent(item);
        }

        return content;
    }


    //~ Inner classes .........................................................

    // ----------------------------------------------------------
    protected static class AnnotationDecorator
    {
        // ----------------------------------------------------------
        public GeoPoint getItemGeoPoint(Object item)
        {
            Method geoPointMethod = getAnnotatedMethod(
                    item.getClass(), ProvidesMarkerGeoPoint.class);

            if (geoPointMethod != null)
            {
                // First, look for a method with the @ProvidesMarkerGeoPoint
                // annotation. If found, call it and use the GeoPoint object
                // that it returns.

                try
                {
                    Object result = geoPointMethod.invoke(item);

                    if (result == null)
                    {
                        return null;
                    }
                    else if (result instanceof GeoPoint)
                    {
                        return (GeoPoint) result;
                    }
                    else
                    {
                        throw new IllegalStateException(
                                "The method annotated with "
                                + "@ProvidesMarkerGeoPoint must return a "
                                + "GeoPoint object.");
                    }
                }
                catch (IllegalAccessException e)
                {
                    throw new IllegalStateException(e);
                }
                catch (InvocationTargetException e)
                {
                    throw new IllegalStateException(e.getCause());
                }
            }
            else
            {
                // If there was no method annotated with
                // @ProvidesMarkerGeoPoint, then look for a pair of methods
                // annotated with @ProvidesMarkerLatitude and
                // @ProvidesMarkerLongitude. If both are found and both return
                // numeric values, use those for the coordinates.

                Method latitudeMethod = getAnnotatedMethod(
                        item.getClass(), ProvidesMarkerLatitude.class);
                Method longitudeMethod = getAnnotatedMethod(
                        item.getClass(), ProvidesMarkerLongitude.class);

                if (latitudeMethod == null && longitudeMethod == null)
                {
                    return null;
                }
                else if (latitudeMethod != null && longitudeMethod != null)
                {
                    try
                    {
                        Object latitude = latitudeMethod.invoke(item);
                        Object longitude = longitudeMethod.invoke(item);

                        if (latitude == null && longitude == null)
                        {
                            return null;
                        }
                        else if (latitude instanceof Number
                                && longitude instanceof Number)
                        {
                            return new GeoPoint(
                                    (int) (((Number) latitude)
                                            .doubleValue() * 1e6),
                                    (int) (((Number) longitude)
                                            .doubleValue() * 1e6));
                        }
                        else if (latitude == null || longitude == null)
                        {
                            throw new IllegalStateException(
                                    "If either @ProvidesMarkerLatitude or "
                                    + "@ProvidesMarkerLongitude returns null, "
                                    + "then the other must as well.");
                        }
                        else
                        {
                            throw new IllegalStateException(
                                    "@ProvidesMarkerLatitude and "
                                    + "@ProvidesMarkerLongitude must return "
                                    + "a numeric value.");
                        }
                    }
                    catch (IllegalAccessException e)
                    {
                        throw new IllegalStateException(e);
                    }
                    catch (InvocationTargetException e)
                    {
                        throw new IllegalStateException(e.getCause());
                    }
                }
                else
                {
                    throw new IllegalStateException(
                            "If either @ProvidesMarkerLatitude or "
                            + "@ProvidesMarkerLongitude is provided, then the "
                            + "other must be as well.");
                }
            }
        }


        // ----------------------------------------------------------
        public String getItemTitle(Object item)
        {
            String title = null;

            Method method = getAnnotatedMethod(
                    item.getClass(), ProvidesTitle.class);

            if (method != null)
            {
                try
                {
                    Object result = method.invoke(item);
                    return result != null ? result.toString() : null;
                }
                catch (IllegalAccessException e)
                {
                    throw new IllegalStateException(e);
                }
                catch (InvocationTargetException e)
                {
                    throw new IllegalStateException(e.getCause());
                }
            }
            else
            {
                title = item.toString();
            }

            return title;
        }


        // ----------------------------------------------------------
        public Object getItemContent(Object item)
        {
            Object content = null;

            Method method = getAnnotatedMethod(
                    item.getClass(), ProvidesMarkerContent.class);

            if (method != null)
            {
                try
                {
                    content = method.invoke(item);
                }
                catch (IllegalAccessException e)
                {
                    throw new IllegalStateException(e);
                }
                catch (InvocationTargetException e)
                {
                    throw new IllegalStateException(e.getCause());
                }
            }

            return content;
        }


        // ----------------------------------------------------------
        public Drawable getItemImage(Object item)
        {
            // TODO handle various types: Drawable, Bitmap, File, Uri

            return null;
        }
    }


    // ----------------------------------------------------------
    /**
     * TODO Replace with reflecton API.
     */
    private static Method getAnnotatedMethod(
            Class<?> itemClass, Class<? extends Annotation> annotation)
    {
        Method method = null;

        for (Method currentMethod : itemClass.getMethods())
        {
            if (currentMethod.getAnnotation(annotation) != null)
            {
                method = currentMethod;
                break;
            }
        }

        return method;
    }
}
