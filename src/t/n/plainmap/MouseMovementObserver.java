package t.n.plainmap;

import t.n.map.common.LonLat;

public interface MouseMovementObserver {
	public void notifyMouseMovingLonLat(LonLat lonlat);

	public void notifyMouseClickedLonLat(LonLat lonlat);
}
