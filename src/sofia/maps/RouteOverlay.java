package sofia.maps;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

//-------------------------------------------------------------------------
/**
 * An overlay that displays a line on a map that connects the items in a list
 * of arbitrary objects. These objects can have annotations on methods to
 * provide marker information ({@link ProvidesMarkerGeoPoint},
 * {@link ProvidesMarkerImage}, {@link ProvidesMarkerTitle}, etc.), or
 * alternatively a "decorator" object can be specified that provides this
 * information given an item on the map.
 * 
 * @author Tony Allevato
 * @version 2012.04.22
 */
public class RouteOverlay<Item> extends AbstractListOverlay<Item>
{
	//~ Instance/static variables .............................................

	private Paint routePaint;


	//~ Constructors ..........................................................

	// ----------------------------------------------------------
	/**
	 * Initializes a new {@code RouteOverlay}.
	 * 
	 * @param mapView the {@link MapView} on which the overlay will be
	 *     displayed
	 * @param list the list of objects that represent the route to be displayed
	 * @param routePaint the {@link Paint} object that determines the graphical
	 *     properties of the line that will be drawn
	 */
	public RouteOverlay(MapView mapView, List<Item> list, Paint routePaint)
	{
		super(mapView, list);
		
		this.routePaint = routePaint;
	}


	//~ Methods ...............................................................

	// ----------------------------------------------------------
	/**
	 * Gets the {@link Paint} object that will be used to draw the line on the
	 * map.
	 * 
	 * @return the {@link Paint} object that will be used to draw the line on
	 *     the map
	 */
	public Paint getRoutePaint()
	{
		return routePaint;
	}


	// ----------------------------------------------------------
	/**
	 * Sets the {@link Paint} object that will be used to draw the line on the
	 * map.
	 * 
	 * @param newPaint the {@link Paint} object that will be used to draw the
	 *     line on the map
	 */
	public void setRoutePaint(Paint newPaint)
	{
		this.routePaint = newPaint;
	}


	// ----------------------------------------------------------
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow)
	{
		if (!shadow)
		{
			return;
		}

		List<Item> list = getList();

		int size = list.size();

		Projection proj = mapView.getProjection();
		Point pt = new Point();

		Path path = new Path();

		for (int i = 0; i < size; i++)
		{
			Item item = list.get(i);
			GeoPoint geoPoint = getItemGeoPoint(item);
			
			if (geoPoint != null)
			{
				proj.toPixels(geoPoint, pt);

				if (i == 0)
				{
					path.moveTo(pt.x, pt.y);
				}
				else
				{
					path.lineTo(pt.x, pt.y);
				}
			}
		}
		
		canvas.drawPath(path, routePaint);
	}
}
