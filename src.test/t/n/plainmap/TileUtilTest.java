package t.n.plainmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import t.n.map.common.KokudoTile;
import t.n.map.common.LonLat;
import t.n.map.common.util.TileUtil;

public class TileUtilTest {

	@Test
	public void testNeighborTile() {
		KokudoTile prevTile = new KokudoTile(5, 28, 12);

		double leftUpperLon = prevTile.getLeftUpperLon();
		double leftUpperLat = prevTile.getLeftUpperLat();
		double rightDownLon = prevTile.getRightDownLon();
		double rightDownLat = prevTile.getRightDownLat();

		//北半球では、緯度は高い(北極に近い)ほど数値も大きい。南半球では？
		assertTrue(leftUpperLon < rightDownLon);
		assertTrue(rightDownLat < leftUpperLat);

		//引数の数字はマウスでドラッグしたピクセル数であり、タイルの大きさの256を超えない限り
		//このタイルの緯度経度の範囲内に納まるはず？？
		//タイル内の座標系は？原点は左下
		//Y軸は下側が正
		//
		LonLat newLoc = TileUtil.getNewCenterLonLat(prevTile , 25, 31);

		assertTrue(newLoc.getLongitude() > leftUpperLon);
		assertTrue(newLoc.getLatitude()  > rightDownLat);
		assertTrue(newLoc.getLongitude() < rightDownLon);
		assertTrue(newLoc.getLatitude()  < leftUpperLat);
	}

	@Test
	//moveX, moveYにゼロを指定して呼び出すと、元の座標が得られるはず。
	public void testMouseDragOffsetZero() {
		KokudoTile prevTile = new KokudoTile(5, 28, 12);
		LonLat newLoc = TileUtil.getNewCenterLonLat(prevTile, 0, 0);

		assertEquals(prevTile.getLonLat().getLongitude(), newLoc.getLongitude(), 1e-5);
		assertEquals(prevTile.getLonLat().getLatitude(), newLoc.getLatitude(), 1e-5);
	}

	@Test
	//moveXのみを与えて、座標を求めると、元のタイルのピクセルあたりの経度にピクセル数を掛けた座標に等しくなるはず。
	public void testMouseDragOffsetX() {
		KokudoTile prevTile = new KokudoTile(5, 28, 12);
		int moveX = 30;
		LonLat newLoc = TileUtil.getNewCenterLonLat(prevTile, moveX, 0);
		double lonDelta = prevTile.getLongitudeWidth() * moveX / 256;
		assertEquals(prevTile.getLonLat().getLongitude() + lonDelta, newLoc.getLongitude(), 1e-5);
		assertEquals(prevTile.getLonLat().getLatitude(), newLoc.getLatitude(), 1e-5);
	}

	@Test
	//moveYのみを与える。
	public void testMouseDragOffsetY() {
		KokudoTile prevTile = new KokudoTile(5, 28, 12);
		int moveY = 123;
		LonLat newLoc = TileUtil.getNewCenterLonLat(prevTile, 0, moveY);
		double latDelta = prevTile.getLatitudeHeight() * moveY / 256;
		assertEquals(prevTile.getLonLat().getLongitude(), newLoc.getLongitude(), 1e-5);
		assertEquals(prevTile.getLonLat().getLatitude(), newLoc.getLatitude() + latDelta, 1e-5);
	}

	@Test
	//moveXのみを与えて、座標を求めると、元のタイルのピクセルあたりの経度にピクセル数を掛けた座標に等しくなるはず。
	//moveXは256を超える値とする。
	public void testMouseDragOffsetExceedX() {
		KokudoTile prevTile = new KokudoTile(5, 28, 12);
		int moveX = 300;
		LonLat newLoc = TileUtil.getNewCenterLonLat(prevTile, moveX, 0);
		double lonDelta = prevTile.getLongitudeWidth() * moveX / 256;
		assertEquals(prevTile.getLonLat().getLongitude(), newLoc.getLongitude() - lonDelta, 1e-5);
		assertEquals(prevTile.getLonLat().getLatitude(), newLoc.getLatitude(), 1e-5);
	}

	@Test
	//Yは256を超える値とする。
	public void testMouseDragOffsetExceedY() {
		KokudoTile prevTile = new KokudoTile(5, 28, 12);
		int moveY = 400;
		LonLat newLoc = TileUtil.getNewCenterLonLat(prevTile, 0, moveY);
		double latDelta = prevTile.getLatitudeHeight() * moveY / 256;
		assertEquals(prevTile.getLonLat().getLongitude(), newLoc.getLongitude(), 1e-5);
		assertEquals(prevTile.getLonLat().getLatitude(), newLoc.getLatitude() + latDelta, 1e-5);
	}

	@Test
	//Xのみを与えて、座標を求めると、元のタイルのピクセルあたりの経度にピクセル数を掛けた座標に等しくなるはず。
	//Xはタイルの範囲より負の値とする。
	public void testMouseDragOffsetNegativeX() {
		KokudoTile prevTile = new KokudoTile(5, 28, 12);
		int moveX = -300;
		LonLat newLoc = TileUtil.getNewCenterLonLat(prevTile, moveX, 0);
		double lonDelta = prevTile.getLongitudeWidth() * moveX / 256;
		assertEquals(prevTile.getLonLat().getLongitude(), newLoc.getLongitude() - lonDelta, 1e-5);
		assertEquals(prevTile.getLonLat().getLatitude(), newLoc.getLatitude(), 1e-5);
	}

	@Test
	//Yはタイルの範囲より負の値とする。
	public void testMouseDragOffsetNegativeY() {
		KokudoTile prevTile = new KokudoTile(5, 28, 12);
		int moveY = -300;
		LonLat newLoc = TileUtil.getNewCenterLonLat(prevTile, 0, moveY);
		double latDelta = prevTile.getLatitudeHeight() * moveY / 256;
		assertEquals(prevTile.getLonLat().getLongitude(), newLoc.getLongitude(), 1e-5);
		assertEquals(prevTile.getLonLat().getLatitude(), newLoc.getLatitude() + latDelta, 1e-5);
	}

	@Test
	public void testTileOffset() {
		KokudoTile prevTile = new KokudoTile(5, 28, 12);
		LonLat lonlat = prevTile.getLonLat();
		assertEquals(0, TileUtil.getPixelCoordX(prevTile.getLeftUpperLon(), lonlat.getLongitude(), prevTile.getLongitudeWidth()));
		assertEquals(0, TileUtil.getPixelCoordY(prevTile.getLeftUpperLat(), lonlat.getLatitude(),  prevTile.getLatitudeHeight()));
		assertEquals(256, TileUtil.getPixelCoordX(prevTile.getRightDownLon(), lonlat.getLongitude(), prevTile.getLongitudeWidth()));
		assertEquals(256, TileUtil.getPixelCoordY(prevTile.getRightDownLat(), lonlat.getLatitude(),  prevTile.getLatitudeHeight()));
	}

	@Test
		public void testZoom5() {
		KokudoTile tile0 = new KokudoTile(5, 28, 12);
		KokudoTile tile1 = new KokudoTile(5, 29, 13);
		assertEquals(tile0.getRightDownLat(), tile1.getLeftUpperLat(), 1e-5);
		assertEquals(tile0.getRightDownLon(), tile1.getLeftUpperLon(), 1e-5);
	}

	@Test
	public void testZoom7() {
		KokudoTile prevTile = new KokudoTile(7, 112, 50);
		TileUtil.getNewCenterLonLat(prevTile , 25, 31);
	}

}
