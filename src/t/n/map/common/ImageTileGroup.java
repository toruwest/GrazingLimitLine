package t.n.map.common;

import t.n.map.common.util.TileUtil;
import t.n.plainmap.MapParam;

public class ImageTileGroup extends TileGroup {
	/** package-info.javaの図1のxに相当。原点にあるタイルの左上の角（図1のo これは画面の原点より外にあり、見えない）と、画面の原点p（左上)とのオフセット。値の範囲は-(タイルの幅)から0まで。 */
	private int tilePositionNearOrginX;

	/** package-info.javaの図1のyに相当。原点にあるタイルの左上の角（図1のo これは画面の原点より外にあり、見えない）と、画面の原点p（左上)とのオフセット。値の範囲は-(タイルの高さ)から0まで。 */
	private int tilePositionNearOrginY;

	private int centerTileLeftUpperCornerX;
	private int centerTileLeftUpperCornerY;
	private int tilesCountBetweenOriginAndCenterX;
	private int tilesCountBetweenOriginAndCenterY;

	private int moveX;
	private int moveY;

	private KokudoTile centerTile;

	private int pixelOffsetX;
	private int pixelOffsetY;

	public ImageTileGroup() {
		super();
	}

	public ImageTileGroup(MapParam mapParam) {
		super(mapParam);
	}

	public void drag(int deltaX, int deltaY) {
		moveX += deltaX;
		moveY += deltaY;
	}

	public void dragDone() {
		LonLat centerLonLat = TileUtil.getNewCenterLonLat(centerTile, moveX, moveY);
		moveX = 0;
		moveY = 0;
		mapParam.setCenterLonLat(centerLonLat);
	}

	public void prepareUpdate() {
		//これについては初期化しないとパネルの表示がおかしくなるはず。
		reset();
		centerTile = new KokudoTile(getZoomLevel(), getCenterLonlat());

		pixelOffsetX = centerTile.getPixelOffsetX();
		pixelOffsetY = centerTile.getPixelOffsetY();
		System.out.println(getClass().getSimpleName() + ": pixelOffset X:" + pixelOffsetX + ", Y:" + pixelOffsetY + ", zoom:" + centerTile.getZoomLevel());
		computeTilesCountBetweenOriginAndCenter(pixelOffsetX, pixelOffsetY);
		int centerTileNoX = TileUtil.getTileNoX(getZoomLevel(), getCenterLonlat());
		int tileNoY = TileUtil.getTileNoY(getZoomLevel(), getCenterLonlat());
		tileNoAtOriginX = centerTileNoX - tilesCountBetweenOriginAndCenterX;
		tileNoAtOriginY = tileNoY - tilesCountBetweenOriginAndCenterY;
		computeTilePosNearOrigin();
	}

	public int getTilePositionNearOrginY() {
		return tilePositionNearOrginY;
	}

	public int getTilePositionNearOrginX() {
		return tilePositionNearOrginX;
	}

	//原点の周りに表示されているタイルの左上の角の、画面原点に対する座標。X,Y共に マイナスtileSize から0 の範囲にあるはず。
	private void computeTilePosNearOrigin() {
		int tileSize = getTileSize();
		tilePositionNearOrginX = centerTileLeftUpperCornerX - tilesCountBetweenOriginAndCenterX * tileSize;
		tilePositionNearOrginY = centerTileLeftUpperCornerY - tilesCountBetweenOriginAndCenterY * tileSize;
	}

	private void computeTilesCountBetweenOriginAndCenter(int offsetX, int offsetY) {
		int tileSize = getTileSize();
		int width  = getRegionWidth();
		int height = getRegionHeight();

		centerTileLeftUpperCornerX = ((width - tileSize) / 2 ) - offsetX;
		centerTileLeftUpperCornerY = ((height - tileSize) / 2) - offsetY;

		tilesCountBetweenOriginAndCenterX = (int) Math.ceil(centerTileLeftUpperCornerX / (double) tileSize);
		tilesCountBetweenOriginAndCenterY = (int) Math.ceil(centerTileLeftUpperCornerY / (double) tileSize );
	}
}
