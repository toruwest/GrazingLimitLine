package t.n.plainmap;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lombok.Getter;
import t.n.map.common.LonLat;

public class MapPreference {
	private static final String DEFAULT_PREF_FILENAME ="limitline.pref";
	private static final String INITIAL_LOCATION_LONGITUDE_KEY = "initialLongitude";
	private static final String INITIAL_LOCATION_LATITUDE_KEY = "initialLatitude";
	private static final String DEFAULT_IMAGE_SAVING_DIR = "DEFAULT_IMAGE_SAVING_DIR";
	private static final String USER_HOME_LOCATION_LON = "USER_HOME_LOCATION_LON";
	private static final String USER_HOME_LOCATION_LAT = "USER_HOME_LOCATION_LAT";
	private static final String USER_HOME_ZOOM_LEVEL = "USER_HOME_ZOOM_LEVEL";
	private static final String LIMIT_LINE_DATA_DIR = "LIMIT_LINE_DATA_DIR";
	private static final String USE_PROXY= "USE_PROXY";
	private static final String PROXY_HOST = "PROXY_HOST";
	private static final String PROXY_PORT = "PROXY_PORT";
	private static final String HIDE_OPENING_MSG = "HIDE_OPENING_MSG";

	private String prefFilename = null;
	private final Properties prop = new Properties();
	private final File dataDir;

	private final List<IProxyConfigurationChangedListener> listeners;

	@Getter
	private boolean isLoaded = false;
	@Getter
	private LonLat initialLocation = null;
	@Getter
	private String defaultImageSavingDir;
	@Getter
	private LonLat userHomeLocation;
	@Getter
	private int userHomeZoomLevel;
	@Getter
	private boolean isUseProxy;
	@Getter
	private String proxyHostname;
	@Getter
	private long proxyPort;
	@Getter
	private boolean isHideOpeningMessage;
	@Getter
	private String limitLineDataDir;

	public MapPreference(File dataDir){
		this.dataDir = dataDir;
		this.prefFilename = DEFAULT_PREF_FILENAME;
		listeners = new ArrayList<>();
		limitLineDataDir = AppConfig.getGrazingLimitLineDataDir().getAbsolutePath();
	}

	//	//JUnit によるテスト用。
	//	public Preference(File dataDir, String prefFilename){
	//		this(dataDir);
	//		if(DEFAULT_PREF_FILENAME.equals(prefFilename)) {
	//			throw new IllegalArgumentException("Please do not specify the default property file name. Try another one in order to protect default preference file that is used for application");
	//		} else {
	//			this.prefFilename = prefFilename;
	//		}
	//	}

	public boolean exists() {
		File f = new File(dataDir, prefFilename);
		return f.exists();
	}

	public void load() {
		FileReader reader = null;
		try {
			File prefFile = new File(dataDir, prefFilename);
			if(prefFile.exists()) {
				reader = new FileReader(prefFile);
				prop.load(reader);

				Double lon = Double.valueOf((String)prop.get(INITIAL_LOCATION_LONGITUDE_KEY));
				Double lat = Double.valueOf((String)prop.get(INITIAL_LOCATION_LATITUDE_KEY));
				initialLocation = new LonLat(lon, lat);

				defaultImageSavingDir = (String)prop.get(DEFAULT_IMAGE_SAVING_DIR);

				String lon1String = (String)prop.get(USER_HOME_LOCATION_LON);
				if(lon1String != null) 	{
					Double lon1 = Double.valueOf(lon1String);
					String lat1String = (String)prop.get(USER_HOME_LOCATION_LAT);
					if(lat1String != null) {
						Double lat1 = Double.valueOf(lat1String);
						userHomeLocation = new LonLat(lon1, lat1);
					}
				}

				String userHomeLevelStr = (String)prop.get(USER_HOME_ZOOM_LEVEL);
				if(userHomeLevelStr != null) {
					userHomeZoomLevel = (Integer.parseInt(userHomeLevelStr));
				}

				isUseProxy = Boolean.parseBoolean((String)prop.get(USE_PROXY));
				proxyHostname = (String)prop.get(PROXY_HOST);
				String proxyPortStr = (String)prop.get(PROXY_PORT);
				if(proxyPortStr != null) {
					proxyPort = Integer.parseInt(proxyPortStr);
				}
				isHideOpeningMessage = Boolean.parseBoolean((String)prop.get(HIDE_OPENING_MSG));
				limitLineDataDir = (String)prop.get(LIMIT_LINE_DATA_DIR);
				if(limitLineDataDir == null) limitLineDataDir = AppConfig.getGrazingLimitLineDataDir().getAbsolutePath();
				reader.close();
//			} else {
//				limitLineDataDir = AppConfig.getGrazingLimitLineDataDir().getAbsolutePath();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		isLoaded  = true;
	}

	public void save() throws IOException {
		FileWriter writer = null;
		if(!dataDir.exists())dataDir.mkdirs();
		File prefFile = new File(dataDir, prefFilename);
		writer = new FileWriter(prefFile);
		prop.store(writer, "");
		writer.close();
	}

	public void setDefaultImageSaveDir(String imageDefaultDir) {
		if(imageDefaultDir != null) {
			this.defaultImageSavingDir = imageDefaultDir;
			prop.setProperty(DEFAULT_IMAGE_SAVING_DIR, imageDefaultDir);
		}
	}

	public void setInitialLocation(LonLat location) {
		prop.setProperty(INITIAL_LOCATION_LONGITUDE_KEY, String.valueOf(location.getLongitude()));
		prop.setProperty(INITIAL_LOCATION_LATITUDE_KEY, String.valueOf(location.getLatitude()));
	}

	public void setUserHomeLocation(LonLat lonLat) {
		if(lonLat != null) {
			this.userHomeLocation = lonLat;
			prop.setProperty(USER_HOME_LOCATION_LON, String.valueOf(lonLat.getLongitude()));
			prop.setProperty(USER_HOME_LOCATION_LAT, String.valueOf(lonLat.getLatitude()));
		}
	}

	public void setUserHomeZoomLevel(int userHomeZoomLevel) {
		this.userHomeZoomLevel = userHomeZoomLevel;
		prop.setProperty(USER_HOME_ZOOM_LEVEL, String.valueOf(userHomeZoomLevel));
	}

	public void setIsUseProxy(boolean b) {
		this.isUseProxy = b;
		prop.setProperty(USE_PROXY, String.valueOf(b));
		fireIProxyConfigurationChangedEvent();
	}


	public void setProxyHostname(String proxyHostname) {
		this.proxyHostname = proxyHostname;
		prop.setProperty(PROXY_HOST, proxyHostname);
	}

	public void setProxyPort(long proxyPort) {
		this.proxyPort = proxyPort;
		prop.setProperty(PROXY_PORT, String.valueOf(proxyPort));
	}

	public void setIsHideOpeningMessage(boolean b) {
		this.isHideOpeningMessage = b;
		prop.setProperty(HIDE_OPENING_MSG, String.valueOf(b));
	}

	public void addProxyConfigurationChangedListener(IProxyConfigurationChangedListener listener) {
		listeners.add(listener);
	}

	public void setOccult4Dir(String dir) {
		this.limitLineDataDir = dir;
		prop.setProperty(LIMIT_LINE_DATA_DIR, String.valueOf(dir));
	}

	private void fireIProxyConfigurationChangedEvent() {
		if(listeners != null) {
			for(IProxyConfigurationChangedListener l : listeners) {
				l.reconfigureProxy();
			}
		}
	}
}
