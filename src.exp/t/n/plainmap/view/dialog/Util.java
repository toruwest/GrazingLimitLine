package t.n.plainmap.view.dialog;

public class Util {
	private static final String BASE_URI = "http://cyberjapandata2.gsi.go.jp/general/dem/scripts/getelevation.php?";

	public static String generateURI(double lon, double lat) {
		StringBuilder sb = new StringBuilder();
		sb.append(BASE_URI);
		sb.append("lon=");
		sb.append(lon);
		sb.append("&lat=");
		sb.append(lat);
		sb.append("&outType=JSON");
		return sb.toString();
	}


}
