package t.n.map.common.util;

import static t.n.map.common.KokudoTile.R;
import t.n.map.common.KokudoTile;
import t.n.map.common.LonLat;

public class TileUtil {
	private static int[] MAX_TILE_NO;

	static {
		MAX_TILE_NO = new int[20];
		for(int i = 0; i <= 19; i++) {
			MAX_TILE_NO[i] = (int)Math.pow(2, i);
		}
	}

	public static boolean isRangeError(int zoomLevel, int tileNoX, int tileNoY) {
		if(zoomLevel < 0 || zoomLevel > 19) {
//			errorCause  = "zoomLevelは0以上18以下である必要があります。zoomLevel: " + zoomLevel;
			return true;
		}
		if(tileNoX < 0 || tileNoY < 0) {
//			errorCause = "どちらかのtileNoが0より小さくなっています。" + tileNoX + "," + tileNoY;
			return true;
		}
		if(tileNoX >= MAX_TILE_NO[zoomLevel] || tileNoY >= MAX_TILE_NO[zoomLevel]) {
//			errorCause = "tileNoはx,y共に" + MAX_TILE_NO[zoomLevel] + "より小さくする必要があります。 zoomLevel:" + zoomLevel + ", x:" + tileNoX + ", y:" + tileNoY;
			return true;
		}
		return false;
	}

	public static String getErrorCause(int zoomLevel, int tileNoX, int tileNoY) {
		String errorCause = "";
		if(zoomLevel < 0 || zoomLevel > 19) {
			errorCause  = "zoomLevelは0以上18以下である必要があります。zoomLevel: " + zoomLevel;
//			return true;
		}
		if(tileNoX < 0 || tileNoY < 0) {
			errorCause = "どちらかのtileNoが0より小さくなっています。" + tileNoX + "," + tileNoY;
//			return true;
		}
		if(tileNoX >= MAX_TILE_NO[zoomLevel] || tileNoY >= MAX_TILE_NO[zoomLevel]) {
			errorCause = "tileNoはx,y共に" + MAX_TILE_NO[zoomLevel] + "より小さくする必要があります。 zoomLevel:" + zoomLevel + ", x:" + tileNoX + ", y:" + tileNoY;
//			return true;
		}
		return errorCause;
	}

	public static boolean isRangeError(int zoomLevel, LonLat lonlat) {
		double longitude = lonlat.getLongitude();
		double latitude  = lonlat.getLatitude();
		if(0 <= zoomLevel && zoomLevel <= 18) {
			if(KokudoTile.WEST_BOUND <= longitude && longitude < KokudoTile.EAST_BOUND) {
				if(KokudoTile.SOUTH_BOUND < latitude && latitude <= KokudoTile.NORTH_BOUND) {
					return false;
				} else {
//					errorCause = "latitudeが範囲外です。許容範囲は" + SOUTH_BOUND + "(これを含まない)から" + NORTH_BOUND + "(これを含む)、引数:" + leftUpperLat;
					return true;
				}
			} else {
//				errorCause = "longitudeが範囲外です。許容範囲は-180(これを含む) から+180(これを含まない)、引数:" + leftUpperLon;
				return true;
			}
		} else {
//			errorCause = "zoomLevelが範囲外です。許容範囲は0以上18以下、引数: " + zoomLevel;
			return true;
		}
	}

	public static String getErrorCause(int zoomLevel, LonLat lonlat) {
		String errorCause = "";
		double longitude = lonlat.getLongitude();
		double latitude  = lonlat.getLatitude();
		if(0 <= zoomLevel && zoomLevel <= 18) {
			if(KokudoTile.WEST_BOUND <= longitude && longitude < KokudoTile.EAST_BOUND) {
				if(KokudoTile.SOUTH_BOUND < latitude && latitude <= KokudoTile.NORTH_BOUND) {
//					return false;
				} else {
					errorCause = "latitudeが範囲外です。許容範囲は" + KokudoTile.SOUTH_BOUND + "(これを含まない)から" + KokudoTile.NORTH_BOUND + "(これを含む)、引数:" + latitude;
//					return true;
				}
			} else {
				errorCause = "longitudeが範囲外です。許容範囲は-180(これを含む) から+180(これを含まない)、引数:" + longitude;
//				return true;
			}
		} else {
			errorCause = "zoomLevelが範囲外です。許容範囲は0以上18以下、引数: " + zoomLevel;
//			return true;
		}
		return errorCause;
	}

	public static double computeScale(int zoomLevel) {
		return Math.pow(2, zoomLevel);
	}

	/**
	 * @param lonlat
	 * @return 経度の整数部分。東経はプラス、西経はマイナス。東経１８０度、西経１８０度は？
	 */
	public static int getGdemTileNoX(LonLat lonlat) {
		int tileNoX = (int) ( 10 * Math.floor(lonlat.getLongitude()));
		return tileNoX;
	}

	/**
	 * @param lonlat
	 * @return 緯度の整数部分。北緯はプラス、南緯はマイナス。赤道上は０。
	 */
	public static int getGdemTileNoY(LonLat lonlat) {
		int tileNoY = (int) (10 * Math.floor(lonlat.getLatitude()));
		return tileNoY;
	}

	public static int getTileNoX(double scaledWorldX) {
		int tileNoX = (int) (scaledWorldX / 256);
		return tileNoX;
	}

	public static int getTileNoY(double scaledWorldY) {
		int tileNoY = (int) (scaledWorldY / 256);
		return tileNoY;
	}

	public static int getTileNoX(int zoomLevel, LonLat lonlat) {
		double scale = Math.pow(2, zoomLevel);
		return getTileNoX(toScaledWorldX(scale, lonlat));
	}

	public static int getTileNoY(int zoomLevel, LonLat lonlat) {
		double scale = Math.pow(2, zoomLevel);
		return getTileNoY(toScaledWorldY(scale, lonlat));
	}

	public static int getTileNoX(double scale, LonLat lonlat) {
		return getTileNoX(toScaledWorldY(scale, lonlat.getLongitude()));
	}

	public static int getTileNoY(double scale, LonLat lonlat) {
		return getTileNoY(toScaledWorldY(scale, lonlat.getLatitude()));
	}

	public static double toWorldX(double lon) {
		double worldX = R * (Math.toRadians(lon) + Math.PI);
		return worldX;
	}

	public static double toWorldY(double lat) {
		double latRad = Math.toRadians(lat);
		double worldY = - R / 2 * Math.log( (1 + Math.sin(latRad)) / ( 1 - Math.sin(latRad)) ) + 128;
		return worldY;
	}

//	public static double toScaledWorldX(double scale, LonLat lonlat) {
////		double worldX = R * (Math.toRadians() + Math.PI);
////		return worldX * scale;
//		return toScaledWorldX(scale, lonlat.getLongitude());
//	}

	public static double toScaledWorldX(double scale, double lon) {
		double worldX = R * (Math.toRadians(lon) + Math.PI);
		return worldX * scale;
	}

	public static double toScaledWorldX(double scale, LonLat lonlat) {
//		double worldX = R * (Math.toRadians() + Math.PI);
//		return worldX * scale;
		return toScaledWorldX(scale, lonlat.getLongitude());
	}

	public static double toScaledWorldY(double scale, double lat) {
		double latRad = Math.toRadians(lat);
		double worldY = - R / 2 * Math.log( (1 + Math.sin(latRad)) / ( 1 - Math.sin(latRad)) ) + 128;
		return worldY * scale;
	}

	public static double toScaledWorldY(double scale, LonLat lonlat) {
//		double latRad = Math.toRadians(lonlat.getLatitude());
//		double worldY = - R / 2 * Math.log( (1 + Math.sin(latRad)) / ( 1 - Math.sin(latRad)) ) + 128;
//		return worldY * scale;
		return toScaledWorldY(scale, lonlat.getLatitude());
	}

	public static double getLongitudeFromWorldX(double worldX) {
		return Math.toDegrees((worldX / R) - Math.PI);
	}

	public static double getLatitudeFromWorldY(double worldY) {
		return Math.toDegrees(Math.atan(Math.sinh((128 - worldY) / R)));
	}

	public static double getLongitudeFromTileNoX(int tileNoX, double scale) {
		return getLongitudeFromWorldX(tileNoX * 256 / scale);
	}

	public static double getLongitudeFromGdemTileNoX(int tileNoX) {
		return tileNoX * 1f;
	}

	public static double getLatitudeFromGdemTileNoY(int tileNoY) {
		return tileNoY * 1f;
	}

	public static double getLatitudeFromTileNoY(int tileNoY, double scale) {
		return getLatitudeFromWorldY(tileNoY * 256 / scale);
	}

	//leftUpperとrightDownの経度を256分割して、引数のlonを比例配分したint値を返す。
	public static int getPixelCoordX(double lon, double leftUpperLon, double lonDiff) {
		return (int) (Math.round(256 * (lon - leftUpperLon)/(lonDiff)));
	}

	//leftUpperとrightDownの緯度を256分割して、引数のlatを比例配分したint値を返す。
	public static int getPixelCoordY(double lat, double leftUpperLat, double latDiff) {
		return (int) (Math.round(256 * (leftUpperLat - lat)/(latDiff)));
	}

	//2015/12/15 多分つかわない。
	public static LonLat getNewCenterLonLat(KokudoTile prevTile, int moveX, int moveY) {
		int pixelOffsetX = prevTile.getPixelOffsetX();
		int pixelOffsetY = prevTile.getPixelOffsetY();

		LonLat prev = prevTile.getLonLat();
		double prevLongitude = prev.getLongitude();
		double prevLatitude = prev.getLatitude();
//		double offsetX = (moveX / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLongitudeWidth() / prevTile.getScale();
//		double offsetY = (moveY / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLatitudeHeight() / prevTile.getScale();
//		double offsetX = (moveX / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLongitudeWidth();
//		double offsetY = (moveY / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLatitudeHeight();
		double offsetX = (moveX / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLongitudeWidth() + (KokudoTile.TILE_SIZE / 2);
		double offsetY = (moveY / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLatitudeHeight() + (KokudoTile.TILE_SIZE / 2);
//		double offsetX = ((moveX + pixelOffsetX) / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLongitudeWidth();
//		double offsetY = ((moveY + pixelOffsetY) / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLatitudeHeight();
//		double offsetX = pixelOffsetX + (moveX / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLongitudeWidth();
//		double offsetY = pixelOffsetY + (moveY / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLatitudeHeight();
//		moveX = 0;
//		moveY = 0;

		LonLat centerLonLat = new LonLat(prevLongitude + offsetX, prevLatitude - offsetY);
		return centerLonLat;
	}

	//2015/12/16 デバッグ中
	//これはマウスドラッグによる移動がタイル内に納まっている場合には問題ないが、タイルの境界を越える場合に対処できなさそうだ。
	public static LonLat getNewCenterLonLat(LonLat centerLocation, final int moveX, final int moveY, final int centerPixelOffsetX, final int centerPixelOffsetY) {
		double prevLongitude = centerLocation.getLongitude();
		double prevLatitude = centerLocation.getLatitude();

//		double offsetX = (moveX / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLongitudeWidth() + (KokudoTile.TILE_SIZE / 2);
//		double offsetY = (moveY / KokudoTile.TILE_SIZE_DOUBLE) * prevTile.getLatitudeHeight() + (KokudoTile.TILE_SIZE / 2);
		LonLat newCenterLocation = new LonLat(moveX + centerPixelOffsetX, moveY + centerPixelOffsetY);

		return newCenterLocation;
	}

}
