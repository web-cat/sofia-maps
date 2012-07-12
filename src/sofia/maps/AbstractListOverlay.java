package sofia.maps;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import sofia.internal.MethodDispatcher;
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
 * By default, this class does provide support for displaying an informational
 * "balloon" when an item in the list is tapped. The balloon will display the
 * title and snippet text of the item. To disable this feature, see
 * {@link #setShowsBalloonWhenClicked(boolean)}.
 * </p>
 * 
 * <h3>Event Handling</h3>
 * <p>
 * Users can implement the following methods in the context that owns this
 * overlay's {@code MapView} (for example, their {@link MapScreen} class) to
 * receive event notifications:
 * <dl>
 * <dt><code>[void|boolean] onMapItemClicked(Item item)</code></dt>
 * <dd>Called when the user clicks the location on the map representing one of
 * the items in the list. The type of the "item" parameter must be compatible
 * with the type of objects in the overlay's list. If this method is boolean
 * and returns true, the event is assumed to be "consumed" and the default
 * balloon popup will not appear.
 * </dd>

 * <dt><code>void onMapBalloonClicked(Item item)</code></dt>
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
					mapView.getController().animateTo(itemPoint);

					MethodDispatcher dispatcher = new MethodDispatcher(
							"onMapItemClicked", 1);

					if (showsBalloonWhenClicked
							&& !dispatcher.callMethodOn(
									mapView.getContext(), item))
					{
						openBalloon(itemPoint, item);
						opened = true;
						break;
					}
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
				item, getItemTitle(item), getItemSnippet(item));
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
	protected String getItemSnippet(Item item)
	{
		String snippet; /*= decorate(decorator, "getMarkerDrawable",
				Drawable.class, item);
		
		if (snippet == null)*/
		{
			snippet = defaultDecorator.getItemSnippet(item);
		}
		
		return snippet;
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
				try
				{
					return (GeoPoint) geoPointMethod.invoke(item);
				}
				catch (Exception e)
				{
					return null;
				}
			}
			else
			{
				Method latitudeMethod = getAnnotatedMethod(
						item.getClass(), ProvidesMarkerLatitude.class);
				Method longitudeMethod = getAnnotatedMethod(
						item.getClass(), ProvidesMarkerLongitude.class);
				
				if (latitudeMethod != null && longitudeMethod != null)
				{
					try
					{
						Number latitude = (Number) latitudeMethod.invoke(item);
						Number longitude = (Number) longitudeMethod.invoke(item);
						
						return new GeoPoint(
								(int) (latitude.doubleValue() * 1e6),
								(int) (longitude.doubleValue() * 1e6));
					}
					catch (Exception e)
					{
						return null;
					}
				}
			}
			
			return null;
		}


		// ----------------------------------------------------------
		public String getItemTitle(Object item)
		{
			String title = null;
			
			Method method = getAnnotatedMethod(
					item.getClass(), ProvidesMarkerTitle.class);
			
			if (method != null)
			{
				try
				{
					Object result = method.invoke(item);
					return result != null ? result.toString() : null;
				}
				catch (Exception e)
				{
					// Do nothing; fall through to the default below.
				}
			}
			else
			{
				title = item.toString();
			}

			return title;
		}


		// ----------------------------------------------------------
		public String getItemSnippet(Object item)
		{
			String snippet = null;
			
			Method method = getAnnotatedMethod(
					item.getClass(), ProvidesMarkerSnippet.class);
			
			if (method != null)
			{
				try
				{
					Object result = method.invoke(item);
					return result != null ? result.toString() : null;
				}
				catch (Exception e)
				{
					// Do nothing; fall through to the default below.
				}
			}

			return snippet;
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
