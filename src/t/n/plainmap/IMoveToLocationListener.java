package t.n.plainmap;

import t.n.map.common.LonLat;

public interface IMoveToLocationListener {
	public void notifyMoveToLocation(double lon, double lat);
	public void notifyGotoHomeLocation(LonLat homeLocation, int zoomLevel);

}
