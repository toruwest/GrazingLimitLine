package t.n.plainmap.util;

import java.io.File;

import t.n.map.common.LightWeightTile;
import t.n.plainmap.MapType;

//
public class TiledMapUtil2 {
	private static final String BASE_URI = "http://cyberjapandata.gsi.go.jp/xyz/";

	//zoomLevelが異なるファイルが重複しないように、zoomLevelによって格納するフォルダを分ける。
	public static String generateLocalFilename(File savingdir, MapType type, LightWeightTile tile) {
		int zoomLevel = tile.getZoomLevel();
		int tileNoX = tile.getTileNoX();
		int tileNoY = tile.getTileNoY();
		return generateLocalFilename(savingdir, type, zoomLevel, tileNoX, tileNoY);
	}

	public static String generateLocalFilename(File savingdir, MapType type, int zoomLevel, int tileNoX, int tileNoY) {
		StringBuilder sb = new StringBuilder();
		sb.append(savingdir.getPath());
		sb.append(File.separator);
		sb.append(zoomLevel);
		sb.append(File.separator);
		sb.append(generateFilename(type, tileNoX, tileNoY));
		return sb.toString();
	}

	private static StringBuilder generateFilename(MapType type, int tileNoX, int tileNoY) {
		StringBuilder sb = format(tileNoX);
		sb.append(File.separator);
		sb.append(format(tileNoY));
		switch(type) {
		case std:
			sb.append(".png");
			break;
		case photo:
			sb.append(".jpg");
			break;
		default:
			throw new IllegalArgumentException("範囲外:" + type);
		}

//		sb.append(".png");
		return sb;
	}

	private static StringBuilder format(int tileNo) {
		StringBuilder sb = new StringBuilder();
		String num = String.valueOf(tileNo);
		for(int i = 0; i < 7 - num.length(); i++) {
			sb.append("0");
		}
		sb.append(num);
		return sb;
	}

	//http://portal.cyberjapan.jp/portalsite/version/v4/directoryindex.html
//	private static String generateDirectoryIndex(String filename) {
//		StringBuilder sb = new StringBuilder();
//		for(int i = 0; i < 6; i++) {
//			sb.append(filename.substring(i, i+1));
//			sb.append(filename.substring(i+7, i+8));
//			sb.append("/");
//		}
//		return sb.toString();
//	}

	public static String generateImageURI(MapType type, LightWeightTile tile) {
		return generateImageURI(type, tile.getZoomLevel(), tile.getTileNoX(), tile.getTileNoY());
	}

	public static String generateImageURI(MapType type, int zoomLevel, int tileNoX, int tileNoY) {
		StringBuilder sb = new StringBuilder();
		sb.append(BASE_URI);
		String dataID = getDataIDForZoomLevel(type, zoomLevel);
		sb.append(dataID);
		sb.append("/");
		sb.append(zoomLevel);
		sb.append("/");
		sb.append(tileNoX);
		sb.append("/");
		sb.append(tileNoY);
		switch(type) {
		case std:
			sb.append(".png");
			break;
		case photo:
			sb.append(".jpg");
			break;
		default:
			throw new IllegalArgumentException("範囲外:" + type);
		}
		return sb.toString();
	}

	/**
	 * @See http://portal.cyberjapan.jp/portalsite/version/v4/haishin.html
	 * 上はリンク切れ。
	 * http://maps.gsi.go.jp/development/siyou.html に変わった。
	 */
	private static String getDataIDForZoomLevel(MapType type, int zoomLevel) {
		switch(type) {
		case std:
			switch(zoomLevel) {
			//0,1は、国土地理院のサーバーに対応するデータが存在しない？
//			case 0:
//			case 1:
//			case 2:
//			case 3:
//			case 4:
			//５未満のズームレベルは不要。(日本専用でOK)
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
				//白地図
				return "blank";
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
				return "std";
			default:
				throw new IllegalArgumentException("範囲外:" + zoomLevel);
			}
		case photo:
			switch(zoomLevel) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
				//該当データなし?
				return null;
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
				/*
				 * level 5-12を使う場合の注意事項(	http://maps.gsi.go.jp/development/ichiran.htmlに記載あり)
				 * 本タイル画像を利用する場合は、 「地理院コンテンツ利用規約」で定める方法での出所明示に加え、 以下の出所も合わせて明示してください。
				 * 「データソース：Landsat8画像(GSI,TSIC,GEO Grid/AIST), 海底地形(GEBCO)」
				 */
				return "ort";

			case 13:
				return "ort";
			case 14:
//				return "ort-1";
				return "ort";
//				return "gazo1-1";
			case 15:
				//レベル15-17では両方で提供されている？
//				return "airphoto";
//				return "ort-1";
				return "ort";
//				return "gazo4-1";

			case 16:
				return "ort";
//				return "ort-1";
//				return "gazo4-1";
//				return "airphoto";
			case 17:
				return "ort";
//				return "gazo4-1";
//				return "airphoto";
			case 18:
				return "ort";
//				return "airphoto";
			default:
				throw new IllegalArgumentException("範囲外:" + type);
			}
		}

		return "";
	}
}
