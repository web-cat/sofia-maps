package sofia.maps;

import java.util.List;
import sofia.internal.JarResources;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

//-------------------------------------------------------------------------
/**
 * An overlay that displays one or more markers on a map, from a list of
 * arbitrary objects. These objects can have annotations on methods to provide
 * marker information ({@link ProvidesMarkerGeoPoint},
 * {@link ProvidesMarkerImage}, {@link ProvidesMarkerTitle}, etc.), or
 * alternatively a "decorator" object can be specified that provides this
 * information given an item on the map.
 *
 * @author Tony Allevato
 * @author  Last changed by $Author: edwards $
 * @version $Date: 2012/08/04 16:01 $
 */
public class MarkerOverlay<Item>
    extends AbstractListOverlay<Item>
{
	//~ Instance/static variables .............................................

	private static Drawable defaultMarker = null;


	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	public MarkerOverlay(MapView mapView, List<Item> list)
	{
		super(mapView, list);
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	private static Drawable getDefaultMarker(Context context)
	{
		if (defaultMarker == null)
		{
            Bitmap bitmap = JarResources.getBitmap(context, "marker_red.png",
                MarkerOverlay.class.getPackage().getName());

			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			int dx = width / 2;
			int dy = height;

			defaultMarker = new BitmapDrawable(context.getResources(), bitmap);
			defaultMarker.setBounds(-dx, -dy, -dx + width, -dy + height);
		}

		return defaultMarker;
	}


	// ----------------------------------------------------------
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		List<Item> list = getList();

		int size = list.size();

		Projection proj = mapView.getProjection();
		Point pt = new Point();

		for (int i = 0; i < size; i++)
		{
			Item item = list.get(i);

			GeoPoint geoPoint = getItemGeoPoint(item);

			if (geoPoint != null)
			{
				proj.toPixels(geoPoint, pt);

				Drawable marker = getItemImage(item, mapView.getContext());
				drawAt(canvas, marker, pt.x, pt.y, shadow);
			}
		}
	}


	// ----------------------------------------------------------
	@Override
    protected Rect getItemBounds(Point point, Item item)
    {
		Drawable marker = getItemImage(item, getMapView().getContext());
		Rect bounds = marker.copyBounds();
		bounds.offset(point.x, point.y);

		return bounds;
    }


	// ----------------------------------------------------------
	protected Drawable getItemImage(Object item, Context context)
	{
		Drawable drawable; /*= decorate(decorator, "getMarkerImage",
				Drawable.class, item);

		if (drawable == null)*/
		{
			drawable = defaultDecorator.getItemImage(item);
		}

		if (drawable == null)
		{
			drawable = getDefaultMarker(context);
		}

		return drawable;
	}
}
