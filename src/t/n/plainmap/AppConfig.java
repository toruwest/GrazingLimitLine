package t.n.plainmap;

import java.awt.Toolkit;
import java.io.File;

import t.n.map.OsType;

public class AppConfig {
	private static final String APP_NAME = "GrazingLimitLine";
	public static String appVersion = "1.1";
	public static final String releaseDate = "2015年12月23日";

	private static final String MAP_CACHE_DIR = "map-cache";
	private static final String GRAZING_LIMIT_LINE_DATA_DIR = "grazingLimitLineData";
	private static String appDataDir;
	private static OsType osType;


	private static final File cacheDir;
	private static final File grazingLimitLineDataDir;
	public static final String NO_STANDARD_IMAGE_AREA_DAT = "noStandardImageArea.dat";
	public static final String NO_PHOTO_IMAGE_AREA_DAT = "noPhotoImageArea.dat";

	static {
		String OS = (System.getProperty("os.name")).toUpperCase();
		if (OS.contains("WIN")) {
			//it is simply the location of the "AppData" directory
			appDataDir = System.getenv("AppData");
			appDataDir += "/" + APP_NAME;
			osType = OsType.win;
		} else if (OS.contains("OS X")) {
			//Otherwise, we assume Linux or Mac
			//in either case, we would start in the user's home directory
			appDataDir = System.getProperty("user.home");
			//if we are on a Mac, we are not done, we look for "Application Support"
			appDataDir += "/Library/Application Support/" + APP_NAME;
			osType = OsType.osx;
		} else {
			appDataDir = System.getProperty("user.home");
			appDataDir += "/." + APP_NAME;
			osType = OsType.unix;
		}

		cacheDir = new File(appDataDir, MAP_CACHE_DIR);
		if(!cacheDir.exists()) {
			cacheDir.mkdirs();
		}

		grazingLimitLineDataDir = new File(appDataDir, GRAZING_LIMIT_LINE_DATA_DIR);
		if(!grazingLimitLineDataDir.exists()) {
			grazingLimitLineDataDir.mkdirs();
		}
	}

	public static OsType getOsType() {
		return osType;
	}

	public static int getShortCutKey() {
		 Toolkit tk = Toolkit.getDefaultToolkit();
         int shotcutKey = tk.getMenuShortcutKeyMask();
         return shotcutKey;
	}

	public static String getAppDataDir() {
		return appDataDir;
	}

	public static File getCacheDir() {
		return cacheDir;
	}

	public static File getGrazingLimitLineDataDir() {
		return grazingLimitLineDataDir;
	}

	public static File getImageCacheFolder() {
		return new File(cacheDir + File.separator + "img");
	}
}
