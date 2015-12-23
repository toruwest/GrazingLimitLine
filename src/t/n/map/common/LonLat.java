package t.n.map.common;

import java.io.Serializable;

public class LonLat implements Serializable {

	private final double longitude;
	private final double latitude;

	public LonLat(String longitude, String latitude) {
		this(Double.parseDouble(longitude), Double.parseDouble(latitude));
	}

	public LonLat(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	/**
	 * @return longitude 経度
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @return latitude 緯度
	 */
	public double getLatitude() {
		return latitude;
	}

	public boolean isInvalid() {
		return Double.isNaN(longitude) || Double.isNaN(latitude);
	}

	@Override
	public String toString() {
		return "経度:" + longitude + ", 緯度:" + latitude;
	}
}
