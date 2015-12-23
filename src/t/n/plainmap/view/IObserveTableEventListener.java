package t.n.plainmap.view;

import t.n.map.common.LonLat;

public interface IObserveTableEventListener {
	public void setRemoveButtonEnabled(boolean b);
	public void notifyMoveToLocation(LonLat lonlat, int zoomLevel);
	public void notifyLocationRemoved();
}
