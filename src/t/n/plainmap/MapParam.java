package t.n.plainmap;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import t.n.map.common.LonLat;
import t.n.plainmap.view.IMapParamChangeListener;

@Getter
public class MapParam {
	private final int tileSize;
	private int zoomLevel;
	private LonLat centerLonLat;
	private Rectangle mapRect;
	private MapType mapType;

	private final List<IMapParamChangeListener> listeners;

	public MapParam(int tileSize, MapType type, int zoomLevel, LonLat centerLonlat) {
		super();
		this.tileSize = tileSize;
		this.mapType = type;
		this.zoomLevel = zoomLevel;
		this.centerLonLat = centerLonlat;
		listeners = new ArrayList<>();
	}

	public void addMapParamChangeListener(IMapParamChangeListener l) {
		listeners.add(l);
	}

	public void setCenterLonLat(LonLat location) {
		if(location != null) {
			//System.out.println(getClass().getSimpleName() + ": location:" + location.toString());
			this.centerLonLat = location;
			fireMapParamChangeEvent();
		}
	}

	public void setMapRect(Rectangle mapRect) {
		this.mapRect = mapRect;
		fireMapParamChangeEvent();
	}

	public void setZoomLevel(int newZoomLevel) {
		this.zoomLevel = newZoomLevel;
		fireMapParamChangeEvent();
	}

	private void fireMapParamChangeEvent() {
		for(IMapParamChangeListener l : listeners) {
			l.notifyMapParamChanged();
		}
	}

	public void setMapType(MapType type) {
		this.mapType = type;
		fireMapParamChangeEvent();
	}

}
