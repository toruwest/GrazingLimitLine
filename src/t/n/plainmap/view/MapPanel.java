package t.n.plainmap.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import t.n.map.common.LightWeightTile;
import t.n.map.common.LonLat;
import t.n.map.common.TilePosition;
import t.n.map.common.util.TileImageManagerUtil;
import t.n.plainmap.ITileImageManager;
import t.n.plainmap.MapParam;
import t.n.plainmap.MouseMovementObserver;

public class MapPanel extends JPanel implements MouseListener, MouseMotionListener, ComponentListener, IMapParamChangeListener {
	private static final Logger logger = Logger.getLogger(MapPanel.class.getSimpleName());
	//以下の表記は、国土地理院の利用規程上必要なため、削除しないでください。
	private static final String COPYRIGHT = "この地図には、国土地理院発行の電子国土基本図などを使っています。";

	private static final boolean DEBUG = false;
	protected ITileImageManager tileImageManager;
	private MouseMovementObserver observer;

	private boolean isDragging = false;
	private Point prevMouse;

	private int deltaX;
	private int deltaY;
	protected int moveX;
	protected int moveY;

	//原点にあるタイルの、左上の角のX座標。負の値あるいはゼロになる。
	//これは、ImageTileGroupクラスの変数tilePositionNearOrginXと同じ値になる？
	protected int originX;

	//原点にあるタイルの、左上の角のY座標。負の値あるいはゼロになる。
	protected int originY;

	private int tileSize;

	protected MapParam mapParam;

	private int mouseClickedPointY;
	private int mouseClickedPointX;

	public MapPanel() {
		setLayout(new BorderLayout());
		logger.setLevel(Level.WARNING);
	}

	public MapPanel(MapParam param, ITileImageManager tileImageManager, MouseMovementObserver observer) {
		this();
		this.mapParam = param;
		mapParam.setMapRect(getBounds());
		this.mapParam.addMapParamChangeListener(this);
		tileSize = param.getTileSize();
		this.tileImageManager = tileImageManager;
		this.observer = observer;
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	public void setTileImageManager(ITileImageManager tileImageManager2) {
		this.tileImageManager = tileImageManager2;
	}

	public void notifyFetchingCompleted(LightWeightTile tile) {
//		repaintCause = RepaintCause.fetchComplete;
	}

	@Override
	public void notifyMapParamChanged() {
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if(tileImageManager == null) return;
		tileImageManager.updateTiles(mapParam.getMapType());

		List<TilePosition> tilePositionList = new ArrayList<>(tileImageManager.getTilePositionList());
		if (tilePositionList == null ) {
			return;
		}

		//tileSize/2を足すことにより、指定した位置がパネルの中心になった。
		int offsetX = tileImageManager.getTilePositionNearOriginX() + (tileSize/2);
		int offsetY = tileImageManager.getTilePositionNearOriginY() + (tileSize/2);
		int originTileNoX = tileImageManager.getOriginTileNoX();
		int originTileNoY = tileImageManager.getOriginTileNoY();

		int x = 0, y = 0;
		boolean isFirstLoop = true;
		for(TilePosition pos : tilePositionList) {
			LightWeightTile tile = tileImageManager.getTileAt(pos);

			x = (pos.getX() - originTileNoX) * tileSize + offsetX + moveX;
			y = (pos.getY() - originTileNoY) * tileSize + offsetY + moveY;
			//マウスカーソルを動かしたときに緯度・経度を表示するために以下の情報が必要となるので、保存しておく。
			//forループの最初の一回だけ必要。
			if(isFirstLoop) {
				originX = x - moveX;
				originY = y - moveY;

				isFirstLoop = false;
			}

			if(tile != null) {
				g.drawImage(tile.getImage(), x, y, null, null);
				//デバッグのため、各タイルの境界を標示
				if(DEBUG) {
					g.drawRect(x, y, tileSize, tileSize);
				}
			}
		}

		//デバッグのため、パネルの中央に赤い十字線を描く
		if(DEBUG) {
			g.setColor(Color.RED);
			g.drawLine(0, getHeight()/2, getWidth(), getHeight() / 2);
			g.drawLine(getWidth()/2, 0, getWidth()/2, getHeight());
		}

		showCopyrightNotice(g);
	}

	//国土地理院の電子国土地図の利用規約を表示する。
	//警告：地図になんらかの加工を施す場合、このソフトを改変・流用する者の責任において、適切な表記を行ってください。
	//（加工後の地図が、国土地理院の著作物であると誤解されるような表記は、不適切である可能性があります)
	//詳細は、http://www.gsi.go.jp/LAW/2930-index.htmlを参照。
	protected void showCopyrightNotice(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		AffineTransform saveXform = g2d.getTransform();
		Font font = new Font(Font.SERIF, Font.PLAIN, 14);
		FontRenderContext frc = g2d.getFontRenderContext();
		TextLayout layout = new TextLayout(COPYRIGHT, font, frc);
		Rectangle textBack = layout.getBounds().getBounds();

		//右寄せ。描画領域の背景の色を変える。
        g2d.setPaint(Color.CYAN);//文字の背景
        g2d.translate(getWidth() - textBack.width, getHeight() - textBack.height);
		g2d.fill(textBack);
		g2d.setPaint(Color.BLACK);//文字色
        g2d.draw(textBack);
        g2d.drawString(COPYRIGHT, 5, 0);
        //transformをリセット
        g2d.setTransform(saveXform);
	}

	//マウスをクリックせずに動かしたときに、その場所の緯度経度を表示する。
	@Override
	public void mouseMoved(MouseEvent e) {
		prevMouse = e.getPoint();
		int originTileNoX = tileImageManager.getOriginTileNoX();
		int originTileNoY = tileImageManager.getOriginTileNoY();

		// originX,originY(パネルの左上の原点の世界座標)はpaintComponent()実行時に保存しておいたのを使う。
		LonLat lonlat = TileImageManagerUtil.getLonTatFromScreenCoord(originTileNoX, originTileNoY, originX, originY, prevMouse.x, prevMouse.y, mapParam.getZoomLevel());
		observer.notifyMouseMovingLonLat(lonlat);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		Point p = e.getPoint();

		if(isDragging) {
			deltaX = p.x - prevMouse.x;
			deltaY = p.y - prevMouse.y;
			moveX += deltaX;
			moveY += deltaY;
		} else {
			isDragging = true;
		}

		// 現在のマウスの位置を保存
		prevMouse = p;
		repaint();
	}

	@Override
	//mouseMoved()と同じ方法でマウスがドラッグされ、最後に解放された点に移動する。
	public void mouseReleased(MouseEvent e) {
		isDragging  = false;
		int originTileNoX = tileImageManager.getOriginTileNoX();
		int originTileNoY = tileImageManager.getOriginTileNoY();
//		System.out.println(getClass().getSimpleName() + ": originX:" + originX + ", y:" + originY);
//		System.out.println(getClass().getSimpleName() + ":click   x:" + mouseClickedPointX + ", y:" + mouseClickedPointY);
//		System.out.println(getClass().getSimpleName() + ":release x:" + e.getX() + ", y:" + e.getY());
		//マウスを押した緯度経度と、放した緯度経度のベクトルを、元のセンターに平行移動してやる。
		LonLat lonlat = TileImageManagerUtil.getLonTatFromScreenCoord(originTileNoX, originTileNoY, originX, originY, getWidth()/2 + mouseClickedPointX - e.getX(), getHeight()/2 + mouseClickedPointY - e.getY(), mapParam.getZoomLevel());
		//注意：以下でpaintComponent()が実行される。
		mapParam.setCenterLonLat(lonlat);
		moveX = 0;
		moveY = 0;
	}

	@Override
	//観測地の追加ダイアログが出ているときに一回クリックすると、その地点を観測地候補のテーブルに追加する。
	//また、ダブルクリックされた地点へ移動する。
	public void mouseClicked(MouseEvent e) {
		int clickCount = e.getClickCount();
		if(clickCount > 0) {
			prevMouse = e.getPoint();
			int originTileNoX = tileImageManager.getOriginTileNoX();
			int originTileNoY = tileImageManager.getOriginTileNoY();

			LonLat lonlat = TileImageManagerUtil.getLonTatFromScreenCoord(originTileNoX, originTileNoY, originX, originY, prevMouse.x, prevMouse.y, mapParam.getZoomLevel());
			if(clickCount == 1) {
				observer.notifyMouseClickedLonLat(lonlat);
			} else if(clickCount >= 2) {
				//注意：以下でpaintComponent()が実行される。
				mapParam.setCenterLonLat(lonlat);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mouseClickedPointX = e.getX();
		mouseClickedPointY = e.getY();
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	// 画面のリサイズによるイベントを捕捉して、地図を更新。
	@Override
	public void componentResized(ComponentEvent evt) {
		//注意：以下でpaintComponent()が実行される。
		mapParam.setMapRect(getBounds());
	}

	@Override
	public void componentMoved(ComponentEvent e) {}

	@Override
	public void componentShown(ComponentEvent e) {}

	@Override
	public void componentHidden(ComponentEvent e) {}

}