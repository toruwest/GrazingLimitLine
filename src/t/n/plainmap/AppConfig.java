package t.n.plainmap;

import java.awt.Toolkit;
import java.io.File;

import t.n.map.OsType;

public class AppConfig {
	private static final String APP_NAME = "GrazingLimitLine";
	public static String appVersion = "1.3";
	public static final String releaseDate = "2016年2月1日";

	private static final String MAP_CACHE_DIR = "map-cache";
	private static final String GRAZING_LIMIT_LINE_DATA_DIR = "grazingLimitLineData";
	private static String appDataDir;
	private static OsType osType;
	private static File moonLimbImageDir;


	private static final File cacheDir;
	private static final File grazingLimitLineDataDir;
	public static final String NO_STANDARD_IMAGE_AREA_DAT = "noStandardImageArea.dat";
	public static final String NO_PHOTO_IMAGE_AREA_DAT = "noPhotoImageArea.dat";
//	private static final File DEFAULT_OCCULT4_DAT_ = null;
	private static final String DEFAULT_OCCULT4_DATA_DIR = "C:\\Occult4\\AutoGenerated Grazes\\_Predictions";
	private static final String MOON_LIMB_IMAGE_DIR = "moonLimbImage";

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

		moonLimbImageDir = new File(appDataDir, MOON_LIMB_IMAGE_DIR);
		if(!moonLimbImageDir.exists()) {
			moonLimbImageDir.mkdirs();
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

	public static File getMoonLimbImageDir() {
		return moonLimbImageDir;
	}

	public static File getImageCacheFolder() {
		return new File(cacheDir + File.separator + "img");
	}

	public static String getDefaultOccult4DataDir() {
		return DEFAULT_OCCULT4_DATA_DIR;
	}
}
