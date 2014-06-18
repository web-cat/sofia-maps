package sofia.maps;

import sofia.graphics.Color;

import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Represents a Google Maps API V2 Polyline on a Map The Route object can then
 * be drawn on a map using MapScreen.drawRoute(...)
 * 
 * @author Cameron Wyatt
 * @author Tyler Lenig
 */
public class Route {

	// Fields
	// The Google Maps API V2 PolylineOptions object that is used to specify
	// options for a Polyline
	private PolylineOptions myRoute = null;

	private MapItem source, destination;

	// Available PolylineOptions options
	private Color color;
	private float width, zIndex;
	private boolean visible;

	/**
	 * Default constructor </br></br> Creates a Polyline with the given source
	 * and destination. The default line is colored black with a width of
	 * 
	 * @param source
	 * @param destination
	 */
	public Route(MapItem source, MapItem destination) {
		myRoute = new PolylineOptions();
		this.source = source;
		this.destination = destination;
		myRoute.add(source.getPosition());
		myRoute.add(destination.getPosition());
		this.color = Color.black;
		this.myRoute.color(this.color.toRawColor());
		this.width = 5;
		this.myRoute.width(this.width);
	}

	/**
	 * @return the Google Maps API V2 PolylineOptions object
	 */
	public PolylineOptions getMyRoute() {
		return myRoute;
	}

	/**
	 * @param myRoute
	 *            the myRoute to set
	 */
	public void setMyRoute(PolylineOptions myRoute) {
		this.myRoute = myRoute;
	}

	/**
	 * @return the source MapItem
	 */
	public MapItem getSource() {
		return source;
	}

	/**
	 * @return the destination MapItem
	 */
	public MapItem getDestination() {
		return destination;
	}

	/**
	 * Get the color of the Route
	 * 
	 * @return the Color
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Set the color of the Route
	 * 
	 * @param color
	 */
	public void setColor(Color color) {
		this.color = color;
		this.myRoute.color(color.toRawColor());
	}

	/**
	 * Get the width of the Route
	 * 
	 * @return
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * Set the width of the Route
	 * 
	 * @param width
	 */
	public void setWidth(float width) {
		this.width = width;
		this.myRoute.width(width);
	}

	/**
	 * @return
	 */
	public float getzIndex() {
		return zIndex;
	}

	/**
	 * Specifies the Route's zIndex, i.e., the order in which it will be drawn.
	 * 
	 * @param zIndex
	 */
	public void setzIndex(float zIndex) {
		this.zIndex = zIndex;
		this.myRoute.zIndex(zIndex);
	}

	/**
	 * Whether the Route is visible
	 * 
	 * @return
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Specifies the visibility for the Route. The default visibility is true.
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		this.myRoute.visible(visible);
	}
}
