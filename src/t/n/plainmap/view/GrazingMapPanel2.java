package t.n.plainmap.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.List;

import t.n.map.common.LonLat;
import t.n.map.common.util.TileImageManagerUtil;
import t.n.plainmap.EventDescrition;
import t.n.plainmap.ITileImageManager;
import t.n.plainmap.MapParam;
import t.n.plainmap.MouseMovementObserver;
import t.n.plainmap.dto.LimitLineDatum;

public class GrazingMapPanel2 extends MapPanel implements ILimitLineTableEventListener {
	private static final int MARKER_RAD = 10;
	private static final int MARKER_DIAM = MARKER_RAD * 2;

	private static final Stroke HILIGHTED_STROKE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);;
	private static final Stroke NORMAL_STROKE    = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);

	//TableModelとインスタンスを共有する。
	private List<LonLat> observerLocationList = null;
	private LonLat gpsLocation;
	private List<LimitLineDatum> limitLineData;

	public GrazingMapPanel2() {
		super();
	}

	public GrazingMapPanel2(MapParam param, ITileImageManager tileImageManager, MouseMovementObserver observer) {
		super(param, tileImageManager, observer);
	}

	public void setCurrentGpsLocation(LonLat location) {
		gpsLocation = location;
	}

	public void setObserveLocations(List<LonLat> observeLocations) {
		this.observerLocationList = observeLocations;
	}

	@Override
	public void notifyShowStatusChanged(int row, boolean b) {
		LimitLineDatum datum = limitLineData.get(row);
		datum.setVisible(b);
		repaint();
	}

	@Override
	public void notifyHilightStatusChanged(int row, boolean b) {
		LimitLineDatum datum = limitLineData.get(row);
		datum.setHilighted(b);
		repaint();
	}

	public void setShowAllStatusChanged(boolean b) {
		for(LimitLineDatum datum : limitLineData) {
			datum.setVisible(b);
		}
		repaint();
	}

	public void setLimitLines(List<LimitLineDatum> limitLineData) {
		this.limitLineData = limitLineData;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(tileImageManager == null) {
			return;
		}
		Graphics2D g2d = (Graphics2D)g;
		Point screenCoord;
		int originTileNoX = tileImageManager.getOriginTileNoX();
		int originTileNoY = tileImageManager.getOriginTileNoY();

		renderMarker(g2d, originTileNoX, originTileNoY, gpsLocation, true);

		for(LonLat lonlat : observerLocationList) {
			renderMarker(g2d, originTileNoX, originTileNoY, lonlat, false);
		}

		for(LimitLineDatum datum : limitLineData) {
			//非表示のlistはスキップする。
			if(!datum.isVisible()) {
				continue;
			}

			GeneralPath path = new GeneralPath();
			if(datum.isHilighted()) {
				//強調表示
				g2d.setStroke(HILIGHTED_STROKE);
			} else {
				g2d.setStroke(NORMAL_STROKE);
			}
			//線色を設定
			g2d.setColor(datum.getColor());

			boolean first = true;
			EventDescrition desc = null;
			//概ね経度が小さい順に格納されている。日本だと、説明文の位置を、経度が大きい(日付変更線に近い)点の近くにした方が、
			//地図と重ならずに表示され、見やすいことが多い。なので、大きい方から始める。
			List<LonLat> l = datum.getLonlatList();
			for(int i = l.size() - 1; i >= 0; i--) {
				LonLat lonlat = l.get(i);
				screenCoord = TileImageManagerUtil.getScreenCoordFromLonlat(lonlat, originTileNoX, originTileNoY, originX, originY, mapParam.getZoomLevel());
				if(first) {
					path.moveTo(screenCoord.x + moveX, screenCoord.y + moveY);
					//星食現象の説明を初期化
					//FIXME 説明が重なることがあるので、対策。
					desc = new EventDescrition(screenCoord, moveX, moveY, datum);
					first = false;
				} else {
					path.lineTo(screenCoord.x + moveX, screenCoord.y + moveY);
				}
			}
			//限界線の描画
			g2d.draw(path);
			//説明の描画
			desc.draw(g2d);
		}
	}

	private void renderMarker(Graphics2D g2d, int originTileNoX, int originTileNoY, LonLat lonlat, boolean isGps) {
		Point screenCoord;
		if(lonlat != null && lonlat.getLongitude() != Float.NaN) {
//			screenCoord = TileImageManagerUtil.getScreenCoordFromLonTat(lonlat, originTileNoX, originTileNoY, moveX + originX, moveY + originY, mapParam.getZoomLevel());
			screenCoord = TileImageManagerUtil.getScreenCoordFromLonlat(lonlat, originTileNoX, originTileNoY, originX, originY, mapParam.getZoomLevel());
			g2d.setStroke(HILIGHTED_STROKE);
			g2d.setColor(Color.RED);
			if(isGps) {
				//GPSデバイスから受信した緯度経度によるマーカーを丸印で表示。
//				g2d.drawArc(screenCoord.x - MARKER_RAD, screenCoord.y - MARKER_RAD, MARKER_DIAM, MARKER_DIAM, 0, 360);
				g2d.drawArc(moveX + screenCoord.x - MARKER_RAD, moveY + screenCoord.y - MARKER_RAD, MARKER_DIAM, MARKER_DIAM, 0, 360);
			} else {
				//マウスでクリックされたマーカーは"X"のような形で表示する。
//				g2d.drawLine(screenCoord.x - MARKER_RAD, screenCoord.y - MARKER_RAD, screenCoord.x + MARKER_RAD, screenCoord.y + MARKER_RAD);
//				g2d.drawLine(screenCoord.x - MARKER_RAD, screenCoord.y + MARKER_RAD, screenCoord.x + MARKER_RAD, screenCoord.y - MARKER_RAD);
				g2d.drawLine(moveX + screenCoord.x - MARKER_RAD, moveY + screenCoord.y - MARKER_RAD, moveX + screenCoord.x + MARKER_RAD, moveY + screenCoord.y + MARKER_RAD);
				g2d.drawLine(moveX + screenCoord.x - MARKER_RAD, moveY + screenCoord.y + MARKER_RAD, moveX + screenCoord.x + MARKER_RAD, moveY + screenCoord.y - MARKER_RAD);
			}
		}
	}

	//http://stackoverflow.com/questions/5853879/swing-obtain-image-of-jframe
	public BufferedImage takeScreenShot() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);
		paint( image.getGraphics() );

		return image;
	}
}
