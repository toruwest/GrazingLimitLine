package t.n.plainmap.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import t.n.map.common.LonLat;
import t.n.map.common.util.TileImageManagerUtil;
import t.n.plainmap.EventDescription;
import t.n.plainmap.ITileImageManager;
import t.n.plainmap.MapParam;
import t.n.plainmap.MouseMovementObserver;
import t.n.plainmap.dto.ILimitLineDatum;

public class GrazingMapPanel2 extends MapPanel implements ILimitLineTableEventListener {
	private static final int MARKER_RAD = 10;
	private static final int MARKER_DIAM = MARKER_RAD * 2;
	private static float DASH[] = {10.0f, 3.0f};
	private static final Stroke HILIGHTED_STROKE = new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);;
	private static final Stroke NORMAL_STROKE    = new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
	private static final Stroke MINUS_ONE_DEG_LINE_STROKE = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1f, DASH, 0f);

	//TableModelとインスタンスを共有する。
	private List<LonLat> observerLocationList = null;
	private LonLat gpsLocation;
	private List<ILimitLineDatum> limitLineData;

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
		ILimitLineDatum datum = limitLineData.get(row);
		datum.setVisible(b);
		repaint();
	}

	@Override
	public void notifyHilightStatusChanged(int row, boolean b) {
		ILimitLineDatum datum = limitLineData.get(row);
		datum.setHilighted(b);
		repaint();
	}

	public void setShowAllStatusChanged(boolean b) {
		for(ILimitLineDatum datum : limitLineData) {
			datum.setVisible(b);
		}
		repaint();
	}

	public void setLimitLines(List<ILimitLineDatum> limitLineData) {
		this.limitLineData = limitLineData;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(tileImageManager == null) {
			return;
		}
		Graphics2D g2d = (Graphics2D)g;
		int originTileNoX = tileImageManager.getOriginTileNoX();
		int originTileNoY = tileImageManager.getOriginTileNoY();

		renderMarker(g2d, originTileNoX, originTileNoY, gpsLocation, true);

		for(LonLat lonlat : observerLocationList) {
			renderMarker(g2d, originTileNoX, originTileNoY, lonlat, false);
		}

		List<EventDescription> descList = new ArrayList<>();

		for(ILimitLineDatum datum : limitLineData) {
			//非表示のlistはスキップする。
			if(!datum.isVisible()) {
				continue;
			}

			GeneralPath path1 = new GeneralPath();
			if(datum.isHilighted()) {
				//強調表示
				g2d.setStroke(HILIGHTED_STROKE);
			} else {
				g2d.setStroke(NORMAL_STROKE);
			}
			//線色を設定
			g2d.setColor(datum.getColor());

			AffineTransform saveXform = g2d.getTransform();
			g2d.translate(moveX, moveY);

			boolean first1 = true;
			EventDescription desc = null;
			//限界線データは、JCLOのも、Occult4のも、概ね経度が小さい順に格納されている。日本だと、説明文の位置を、経度が大きい(日付変更線に近い)点の近くにした方が、
			//海上に表示され、地図と重ならず、見やすいことが多いので、大きい方から始める。
			//以下は緯度・経度ともにソートされていない。descを重ならないよう描画するために、緯度(y座標)でソートしておく。
			List<LonLat> l = datum.getLonlatList();
			for(int i = l.size() - 1; i >= 0; i--) {
				LonLat lonlat1 = l.get(i);
				Point screenCoord1 = TileImageManagerUtil.getScreenCoordFromLonlat(lonlat1, originTileNoX, originTileNoY, originX, originY, mapParam.getZoomLevel());

				if(first1) {
					path1.moveTo(screenCoord1.x, screenCoord1.y);
					//他のdescの矩形と重ならないよう、位置を調整する。
					//星食現象の説明を初期化
					desc = new EventDescription(screenCoord1, datum);
					//後でy座標順にソート
					descList.add(desc);

					first1 = false;
				} else {
					path1.lineTo(screenCoord1.x, screenCoord1.y);
				}
			}
			//限界線の描画
			g2d.draw(path1);

			GeneralPath path2 = new GeneralPath();
			g2d.setStroke(MINUS_ONE_DEG_LINE_STROKE);
			List<LonLat> l2 = datum.getMinusOneDegLineList();
			boolean first2 = true;
			for(int i = l2.size() - 1; i >= 0; i--) {
				LonLat lonlat2 = l2.get(i);
				Point screenCoord2 = TileImageManagerUtil.getScreenCoordFromLonlat(lonlat2, originTileNoX, originTileNoY, originX, originY, mapParam.getZoomLevel());
				if(first2) {
					path2.moveTo(screenCoord2.x, screenCoord2.y);
					first2 = false;
				} else {
					path2.lineTo(screenCoord2.x, screenCoord2.y);
				}
			}
			//-1度ラインの描画
			g2d.draw(path2);

			g2d.setTransform(saveXform);
		}

		//descの矩形どうしが重ならないよう、位置を調整する。
		adjust(g2d, descList);

		//説明の描画
		for(EventDescription desc : descList) {
			AffineTransform saveXform = g2d.getTransform();
			g2d.translate(moveX, moveY);
			desc.draw(g2d);
			g2d.setTransform(saveXform);
		}
	}

	private void adjust(Graphics2D g2d, List<EventDescription> descList) {
		//y座標により、昇順でソートする。
		Collections.sort(descList);
		//dump(g2d, descList);
		//矩形をだんだん大きくしていく。初期の大きさは、先頭のdescの大きさ。
		//後から出現するdescの位置が、この矩形と重なっていれば、descの位置をずらす。
		//この後、矩形の大きさを、ずらした後のdescの下の辺まで大きくする。
		Rectangle rect = null;
		for(EventDescription desc : descList) {
			Rectangle descRect = desc.getRect(g2d);
			if(rect != null && rect.intersects(descRect)) {
				int newY = rect.y + rect.height + descRect.height/2;
				desc.move(descRect.x, newY);
				descRect.y = newY;
				rect = rect.union(descRect);
			} else {
				rect = descRect;
				desc.move(descRect.x, descRect.y);
			}
		}
	}

	private void dump(Graphics2D g2d, List<EventDescription> descList) {
		for(EventDescription desc : descList) {
			System.out.println(desc.getRect(g2d));
		}
	}

	private void renderMarker(Graphics2D g2d, int originTileNoX, int originTileNoY, LonLat lonlat, boolean isGps) {
		Point screenCoord;
		if(lonlat != null && lonlat.getLongitude() != Float.NaN) {
			screenCoord = TileImageManagerUtil.getScreenCoordFromLonlat(lonlat, originTileNoX, originTileNoY, originX, originY, mapParam.getZoomLevel());
			g2d.setStroke(HILIGHTED_STROKE);
			g2d.setColor(Color.RED);
			if(isGps) {
				//GPSデバイスから受信した緯度経度によるマーカーを丸印で表示。
				g2d.drawArc(moveX + screenCoord.x - MARKER_RAD, moveY + screenCoord.y - MARKER_RAD, MARKER_DIAM, MARKER_DIAM, 0, 360);
			} else {
				//マウスでクリックされたマーカーは"X"のような形で表示する。
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
