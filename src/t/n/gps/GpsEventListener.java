package t.n.gps;

import t.n.map.common.LocationInfo;

public interface GpsEventListener {

	void notifiGpsLocationInfo(LocationInfo locationInfo);

}
