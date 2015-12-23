package t.n.map.common;

import t.n.map.common.util.TileUtil;
import t.n.plainmap.MapParam;

public class ImageTileGroup2 extends TileGroup {
	/** package-info.javaの図1のxに相当。原点にあるタイルの左上の角（図1のo これは画面の原点より外にあり、見えない）と、画面の原点p（左上)とのオフセット。値の範囲は-(タイルの幅)から0まで。 */
	private int tilePositionNearOrginX;

	/** package-info.javaの図1のyに相当。原点にあるタイルの左上の角（図1のo これは画面の原点より外にあり、見えない）と、画面の原点p（左上)とのオフセット。値の範囲は-(タイルの高さ)から0まで。 */
	private int tilePositionNearOrginY;

	private int centerTileLeftUpperCornerX;
	private int centerTileLeftUpperCornerY;
	private int tilesCountBetweenOriginAndCenterX;
	private int tilesCountBetweenOriginAndCenterY;

	/* パネル中央にあるtileの原点(左下)と、パネル中央のオフセット */
	private int centerPixelOffsetX;
	private int centerPixelOffsetY;

	/**
	 */
	public ImageTileGroup2() {
		super();
	}

	public ImageTileGroup2(MapParam mapParam) {
		super(mapParam);
		computeCenterPixelOffset();
	}

	public int getTilePositionNearOrginY() {
		return tilePositionNearOrginY;
	}

	public int getTilePositionNearOrginX() {
		return tilePositionNearOrginX;
	}

	public void createTileList() {
		//これについては初期化しないとパネルの表示がおかしくなるはず。
		reset();

//		System.out.println(getClass().getSimpleName() + ": before: pixelOffset X:" + centerPixelOffsetX + ", Y:" + centerPixelOffsetY + ", zoom:" + mapParam.getZoomLevel());
		computeCenterPixelOffset();
//		System.out.println(getClass().getSimpleName() + ": after:  pixelOffset X:" + centerPixelOffsetX + ", Y:" + centerPixelOffsetY + ", zoom:" + mapParam.getZoomLevel());
		computeTilesCountBetweenOriginAndCenter();

		int centerTileNoX = TileUtil.getTileNoX(getZoomLevel(), getCenterLonlat());
		int centerTileNoY = TileUtil.getTileNoY(getZoomLevel(), getCenterLonlat());
		tileNoAtOriginX = centerTileNoX - tilesCountBetweenOriginAndCenterX;
		tileNoAtOriginY = centerTileNoY - tilesCountBetweenOriginAndCenterY;

		computeTilePosNearOrigin();
	}

	//表示領域の中央と、中央に配置されるタイルの左上の角のオフセットを計算
	private void computeCenterPixelOffset() {
		int zoomLevel = mapParam.getZoomLevel();
		double scale = TileUtil.computeScale(zoomLevel);
		LonLat centerLonLat = mapParam.getCenterLonLat();
		int tileNoX = TileUtil.getTileNoX(zoomLevel, centerLonLat);
		int tileNoY = TileUtil.getTileNoY(zoomLevel, centerLonLat);

		double leftUpperLon = TileUtil.getLongitudeFromTileNoX(tileNoX, scale);
		double rightDownLon = TileUtil.getLongitudeFromTileNoX(tileNoX + 1, scale);
		double leftUpperLat = TileUtil.getLatitudeFromTileNoY(tileNoY, scale);
		double rightDownLat = TileUtil.getLatitudeFromTileNoY(tileNoY + 1, scale);
		double lonDiff = rightDownLon - leftUpperLon;
		double latDiff = leftUpperLat - rightDownLat;
		centerPixelOffsetX = TileUtil.getPixelCoordX(centerLonLat.getLongitude(), leftUpperLon, lonDiff);
		centerPixelOffsetY = TileUtil.getPixelCoordY(centerLonLat.getLatitude(), leftUpperLat, latDiff);
	}

	private void computeTilesCountBetweenOriginAndCenter() {
		int tileSize = getTileSize();
		int width  = getRegionWidth();
		int height = getRegionHeight();

		centerTileLeftUpperCornerX = ((width - tileSize) / 2 ) - centerPixelOffsetX;
		centerTileLeftUpperCornerY = ((height - tileSize) / 2) - centerPixelOffsetY;

		tilesCountBetweenOriginAndCenterX = 1 + (int) Math.ceil(centerTileLeftUpperCornerX / (double) tileSize);
		tilesCountBetweenOriginAndCenterY = 1 + (int) Math.ceil(centerTileLeftUpperCornerY / (double) tileSize );
	}

	//原点の周りに表示されているタイルの左上の角の、画面原点に対する座標。X,Y共に マイナスtileSize から0 の範囲にあるはず。
	private void computeTilePosNearOrigin() {
		int tileSize = getTileSize();
		tilePositionNearOrginX = centerTileLeftUpperCornerX - tilesCountBetweenOriginAndCenterX * tileSize;
		tilePositionNearOrginY = centerTileLeftUpperCornerY - tilesCountBetweenOriginAndCenterY * tileSize;
	}
}
