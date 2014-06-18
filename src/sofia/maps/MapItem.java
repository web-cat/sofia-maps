package sofia.maps;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Represents a Google Maps API V2 Marker on a map
 * 
 * @author Cameron Wyatt
 * @author Tyler Lenig
 */
public class MapItem {

	// Fields
	// The Google Maps API V2 MarkerOptions object that is used to specify
	// options for a Marker
	private MarkerOptions mapItem;

	// Available MarkerOptions options
	private double lat = 0.00, lon = 0.00;
	private boolean draggable, flat, visible;
	private float alpha, rotation;
	private LatLng position;
	private String title = "";
	private String snippet = "";
	private BitmapDescriptor icon;

	/**
	 * Default constructor
	 */
	public MapItem() {
		mapItem = null;
	}

	/**
	 * Creates a MapItem with the given lat and lon
	 * 
	 * @param lat
	 *            The latitude of the MapItem
	 * @param lon
	 *            The longitude of the MapItem
	 */
	public MapItem(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
		position = new LatLng(lat, lon);
		mapItem = new MarkerOptions().position(position);
	}

	/**
	 * @return the Google Maps API V2 MarkerOptions object
	 */
	public MarkerOptions getMapItem() {
		return mapItem;
	}

	/**
	 * @param myItem
	 *            the myItem to set
	 */
	public void setMapItem(MarkerOptions myItem) {
		this.mapItem = myItem;
	}

	/**
	 * @return whether the MapItem is draggable
	 */
	public boolean isDraggable() {
		return draggable;
	}

	/**
	 * Sets the draggability for the MapItem
	 * 
	 * @param draggable
	 */
	public void setDraggable(boolean draggable) {
		this.draggable = draggable;
		this.mapItem.draggable(draggable);
	}

	/**
	 * @return whether the MapItem is flat
	 */
	public boolean isFlat() {
		return flat;
	}

	/**
	 * Sets whether this MapItem should be flat against the map true or a
	 * billboard facing the camera false. If the MapItem is flat against the map,
	 * it will remain stuck to the map as the camera rotates and tilts but will
	 * still remain the same size as the camera zooms, unlike a GroundOverlay.
	 * If the MapItem is a billboard, it will always be drawn facing the camera
	 * and will rotate and tilt with the camera. The default value is false.
	 * 
	 * @param flat
	 */
	public void setFlat(boolean flat) {
		this.flat = flat;
		this.mapItem.flat(flat);
	}

	/**
	 * @return whether the MapItem is visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * Sets the visibility for the MapItem
	 * 
	 * @param visible
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		this.mapItem.visible(visible);
	}

	/**
	 * @return the alpha
	 */
	public float getAlpha() {
		return alpha;
	}

	/**
	 * Sets the alpha (opacity) of the MapItem. This is a value from 0 to 1,
	 * where 0 means the MapItem is completely transparent and 1 means the MapItem
	 * is completely opaque.
	 * 
	 * @param alpha
	 */
	public void setAlpha(float alpha) {
		this.alpha = alpha;
		this.mapItem.alpha(alpha);
	}

	/**
	 * Specifies the anchor to be at a particular point in the MapItem image.
	 * </br></br> The anchor specifies the point in the icon image that is
	 * anchored to the MapItem's position on the Earth's surface. </br></br> The
	 * anchor point is specified in the continuous space [0.0, 1.0] x [0.0,
	 * 1.0], where (0, 0) is the top-left corner of the image, and (1, 1) is the
	 * bottom-right corner. The anchoring point in a W x H image is the nearest
	 * discrete grid point in a (W + 1) x (H + 1) grid, obtained by scaling the
	 * then rounding. For example, in a 4 x 2 image, the anchor point (0.7, 0.6)
	 * resolves to the grid point at (3, 1).
	 * 
	 * @param anchorU
	 * @param anchorV
	 */
	public void setAnchor(float anchorU, float anchorV) {
		this.mapItem.anchor(anchorU, anchorV);
	}

	/**
	 * @return the rotation
	 */
	public float getRotation() {
		return rotation;
	}

	/**
	 * Sets the rotation of the MapItem in degrees clockwise about the MapItem's
	 * anchor point. The axis of rotation is perpendicular to the MapItem. A
	 * rotation of 0 corresponds to the default position of the MapItem. When the
	 * MapItem is flat on the map, the default position is North aligned and the
	 * rotation is such that the MapItem always remains flat on the map. When the
	 * MapItem is a billboard, the default position is pointing up and the
	 * rotation is such that the MapItem is always facing the camera. The default
	 * value is 0.
	 * 
	 * @param rotation
	 */
	public void setRotation(float rotation) {
		this.rotation = rotation;
		this.mapItem.rotation(rotation);
	}

	/**
	 * @return the LatLng object representing the position of a MapItem
	 */
	public LatLng getPosition() {
		return position;
	}

	/**
	 * Sets the location for the MapItem
	 * 
	 * @param position
	 */
	public void setPosition(LatLng position) {
		this.position = position;
		this.mapItem.position(position);
	}

	/**
	 * @return the title of the MapItem
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets the title for the MapItem
	 * 
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
		this.mapItem.title(title);
	}

	/**
	 * @return the snippet of the MapItem
	 */
	public String getSnippet() {
		return snippet;
	}

	/**
	 * Sets the snippet for the MapItem
	 * 
	 * @param snippet
	 */
	public void setSnippet(String snippet) {
		this.snippet = snippet;
		this.mapItem.snippet(snippet);
	}

	/**
	 * @return the icon for the MapItem
	 */
	public BitmapDescriptor getIcon() {
		return icon;
	}

	/**
	 * Sets the icon for the MapItem
	 * 
	 * @param icon if null, the default MapItem is used
	 */
	public void setIcon(BitmapDescriptor icon) {
		this.icon = icon;
		this.mapItem.icon(icon);
	}

	/**
	 * A MapItem is equal to a Marker if they have the same latitude and longitude
	 * @param marker
	 * @return
	 */
	public boolean equals(Marker marker) {
		if (marker.getPosition().latitude == this.lat
				&& marker.getPosition().longitude == this.lon) {
			return true;
		}
		return false;
	}

}
