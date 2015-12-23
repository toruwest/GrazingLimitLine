package t.n.plainmap.view;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import t.n.map.common.LonLat;
import t.n.map.common.util.TileImageManagerUtil;
import t.n.plainmap.ITileImageManager;
import t.n.plainmap.MapParam;
//import t.n.plainmap.util.TileImageManagerUtil;
import t.n.plainmap.MouseMovementObserver;

//public class ExtendedMapPanel extends GLMapPanel {
public class ExtendedMapPanel extends MapPanel {
	private static final int MARKER_RAD = 10;
	private static final int MARKER_DIAM = MARKER_RAD * 2;
	private final List<LonLat> markerCoordList = new ArrayList<>();
	private LonLat currentLocation;

	public ExtendedMapPanel() {
		super();
	}

//	public ExtendedMapPanel(int initialZoomLevel, int tileSize, ITileImageManager tileImageManager, MouseMovementObserver observer) {
//		super(initialZoomLevel, tileSize, tileImageManager, observer);
//	}

	public ExtendedMapPanel(MapParam mapParam, ITileImageManager tileImageManager, MouseMovementObserver observer) {
		super(mapParam, tileImageManager, observer);
	}

//	public void setCurrentLocation(LonLat location) {
//		currentLocation = location;
//	}

	public void addMarker(LonLat marker) {
		// TODO 位置がほぼ同じマーカーは同じとして扱う。そうしないと複数のマーカーが重なって表示される。
		markerCoordList.add(marker);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		java.awt.Point screenCoord;
		int originTileNoX = tileImageManager.getOriginTileNoX();
		int originTileNoY = tileImageManager.getOriginTileNoY();

		if(currentLocation != null && currentLocation.getLongitude() != Float.NaN) {
			screenCoord = TileImageManagerUtil.getScreenCoordFromLonlat(currentLocation, originTileNoX, originTileNoY, originX, originY, mapParam.getZoomLevel());
			g2d.drawArc(screenCoord.x - MARKER_RAD, screenCoord.y - MARKER_RAD, MARKER_DIAM, MARKER_DIAM, 0, 360);
		}

		if(markerCoordList != null) {
			List<LonLat> coordList = new ArrayList<>(markerCoordList);
			for(LonLat lonlat : coordList) {
				screenCoord = TileImageManagerUtil.getScreenCoordFromLonlat(lonlat, originTileNoX, originTileNoY, originX, originY, mapParam.getZoomLevel());
				g2d.drawArc(screenCoord.x - MARKER_RAD, screenCoord.y - MARKER_RAD, MARKER_DIAM, MARKER_DIAM, 0, 360);
			}
		}
	}
}
