package t.n.plainmap.view;

import gnu.io.CommPortIdentifier;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import t.n.gps.GpsEventListener;
import t.n.gps.GpsSerialDeviceHandler;
import t.n.gps.GpsSerialDeviceUtil;
import t.n.map.IFetchingStatusObserver;
import t.n.map.common.KokudoTile;
import t.n.map.common.LightWeightTile;
import t.n.map.common.LocationInfo;
import t.n.map.common.LonLat;
import t.n.map.common.util.LonLatUtil;
import t.n.plainmap.ITileImageManager;
import t.n.plainmap.MapParam;
import t.n.plainmap.MapPreference;
import t.n.plainmap.MapType;
import t.n.plainmap.MouseMovementObserver;
import t.n.plainmap.TileImageManagerImpl;

public class KokudoTiledMap implements IFetchingStatusObserver, MouseMovementObserver {
	private static final Logger logger = Logger.getLogger(KokudoTiledMap.class.getSimpleName());

	private static final String ZOOM_LEVEL_LABEL = "Zoom level:";
	private static final int INITIAL_ZOOM_LEVEL = 1;
	private static final int ZOOM_MIN = 0;
	private static final int ZOOM_MAX = 18;
//	private final LonLat initLonLat = new LonLat(139.768553d, 35.682286d);//東京駅付近
	private LonLat initCenterLonLat = new LonLat(138.733003d, 35.3806615d);	//富士山
//	private final LonLat initLonLat = new LonLat(137.76855468750003, 35.389049966911664);//, 137.79052734374997,35.371135022801006);//138.733003d, 35.3806615d);	//富士山
	private final ExtendedMapPanel mapPanel;

    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel buttonPanel;

    private JLabel longitudeLabel;
    private JTextField longitudeText;
    private JLabel latitudeLabel;
    private JTextField latitudeText;
    private javax.swing.JSlider zoomLevelChange;
    private javax.swing.JPanel statusPanel;
	private JProgressBar progressBar;
	private JLabel statusMessageLabel;
	private JFrame frame;
	private JLabel sliderLabel;
	private final ITileImageManager tileImageManager;

	private final GpsSerialDeviceHandler handler = null;
//	private final List<LonLat> markerCoordList = new ArrayList<>();

	private final MapPreference pref;

	private final MapParam mapParam;

	public static void main(String[] args) throws IOException {
		new KokudoTiledMap();
	}

	public KokudoTiledMap() throws IOException {
		logger.setLevel(Level.ALL);

		pref = new MapPreference(new File("."));
		if(pref.exists()) {
			LonLat restoredLonLat = pref.getInitialLocation();
			if(restoredLonLat != null) {
				if(!LonLatUtil.isOutOfJapanRegion(restoredLonLat)) {
					initCenterLonLat = restoredLonLat;
				}
			}
		}

//		final GpsEventListener l = this;
//		final CommPortIdentifier targetPort = GpsSerialDeviceUtil.getDevice();
//		if(targetPort != null) {
//			SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
//
//				@Override
//				protected Object doInBackground() throws Exception {
//					handler = new GpsSerialDeviceHandler(targetPort, l);
//					return null;
//				}
//			};
//			sw.execute();
//		} else {
//			log("GPSデバイスの接続されたシリアルポートが見つかりませんでした。");
//		}

		mapParam = new MapParam(KokudoTile.TILE_SIZE, MapType.std, INITIAL_ZOOM_LEVEL, initCenterLonLat);
		tileImageManager = new TileImageManagerImpl(this, mapParam, pref);
//		mapPanel = new ExtendedMapPanel(INITIAL_ZOOM_LEVEL, KokudoTile.TILE_SIZE, tileImageManager, this);
		mapPanel = new ExtendedMapPanel(mapParam, tileImageManager, this);
		mapPanel.setName("mapPanel");
		mapParam.setMapRect(mapPanel.getBounds());
//		mapParam.setLocation(initCenterLonLat);

//		LonLat marker1 = new LonLat(138.7114906311035, 35.35657620196122);
//		log(getClass().getSimpleName() + ": marker1: lon:" + LonLatUtil.getLongitudeString(marker1.getLongitude()) + ", lat:" + LonLatUtil.getLatitudeString(marker1.getLatitude()));
		//KokudoTiledMap: marker1: lon:東経138度42分41.366秒, lat:北緯35度21分23.674秒
//		coordList.add(marker1);

//		mapPanel.setMarkerPosition(markerCoordList);

		initComponents();
		frame.pack();
		frame.setVisible(true);

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				log("shutdown:Save and release resources");
				try {
//					pref.setInitialLocation(tileImageManager.getNewCenterLocation());
					pref.setInitialLocation(mapParam.getCenterLonLat());
//					pref.setDefaultImageSaveDir(imageDefaultSavingDir);
					pref.save();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					tileImageManager.close();
					if(handler != null)	handler.close();
				}
			}
		});

	}

	//隣接するイメージが必要かどうか判断するのは呼び出し側の責務とする。
	//画素と画面の大きさを元に、画面を埋めるために必要となる、隣接するタイルを求める。
	//必要な情報：今の画面サイズ、表示されているタイルの画面上での大きさと位置、タイルの識別、
	//画面上の端に対応した緯度経度

	@Override
	public void notifyStartFetching(String uri) {
//		log("isEDT:" + SwingUtilities.isEventDispatchThread());
		progressBar.setIndeterminate(true);
		statusMessageLabel.setText("Reading " + uri + " ...");
	}

	@Override
	public void notifyFetchCompleted(LightWeightTile tile, final boolean isAllDownloadDone) {
		if(!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					updateProgressStatus();
				}
			});
		} else {
			updateProgressStatus();
		}
		mapPanel.notifyFetchingCompleted(tile);
		frame.repaint();
	}

	@Override
	public void notifyErrorFetching() {
		progressBar.setIndeterminate(false);
		statusMessageLabel.setText("ネットワークエラー");
		mapPanel.notifyFetchingCompleted(null);
	}

	private void updateProgressStatus() {
		progressBar.setIndeterminate(false);
		statusMessageLabel.setText("");
	}

	@Override
	public void notifyMouseMovingLonLat(LonLat lonlat) {
		String lon = LonLatUtil.getLongitudeJapaneseString(lonlat.getLongitude());
		String lat = LonLatUtil.getLatitudeJapaneseString(lonlat.getLatitude());

		longitudeText.setText(lon);
		latitudeText.setText(lat);
	}

	@Override
	public void notifyMouseClickedLonLat(LonLat lonlat) {
		// 何もしない。
	}

    private void initComponents() {
		frame = new JFrame();
		frame.setTitle("国土地図 sample");
		frame.setPreferredSize(new Dimension(800, 550));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addComponentListener(mapPanel);

        mainPanel = new javax.swing.JPanel();
        mainPanel.setName("mainPanel"); // NOI18N

        buttonPanel = new javax.swing.JPanel();
        longitudeLabel = new JLabel("経緯:");
        longitudeText = new JTextField();
        longitudeText.setEditable(false);
        longitudeText.setText("経緯:"); // NOI18N
        longitudeText.setName("lon"); // NOI18N

        latitudeLabel = new JLabel("緯度:");
        latitudeText = new JTextField();
        latitudeText.setEditable(false);
        latitudeText.setText("緯度:"); // NOI18N
        latitudeText.setName("Lat"); // NOI18N

        sliderLabel = new JLabel(ZOOM_LEVEL_LABEL + INITIAL_ZOOM_LEVEL);
        zoomLevelChange = new JSlider(JSlider.HORIZONTAL, ZOOM_MIN, ZOOM_MAX, INITIAL_ZOOM_LEVEL);

        zoomLevelChange.setMinimum(0);
        zoomLevelChange.setMaximum(18);
        zoomLevelChange.setMinorTickSpacing(1);
        zoomLevelChange.setSnapToTicks(true);
        zoomLevelChange.setPaintTicks(true);
        zoomLevelChange.setPaintLabels(true);
        zoomLevelChange.addChangeListener(new ChangeListener() {
        	@Override
        	public void stateChanged(ChangeEvent e) {
        		JSlider source = (JSlider)e.getSource();
        		if (!source.getValueIsAdjusting()) {
        			int zoomLevel = source.getValue();
        			sliderLabel.setText(ZOOM_LEVEL_LABEL + zoomLevel);
        			//mapPanel.zoomLevelChange(zoomLevel);
        			mapParam.setZoomLevel(zoomLevel);
        		}
        	}
        });
        statusPanel = new javax.swing.JPanel();
        statusMessageLabel = new javax.swing.JLabel();
        statusMessageLabel.setBackground(Color.LIGHT_GRAY);
        statusMessageLabel.setText("");
        progressBar = new javax.swing.JProgressBar();
        progressBar.setName("progressBar"); // NOI18N
        buttonPanel.setName("buttonPanel"); // NOI18N

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            (buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(longitudeLabel, 30, 30, 30)
                .addComponent(longitudeText, 170, 170, 170)
                .addComponent(latitudeLabel, 30, 30, 30)
                .addComponent(latitudeText, 170, 170, 170)
                .addComponent(sliderLabel, 100, 100, 100)
                .addComponent(zoomLevelChange))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(longitudeLabel, 30, 30, 30)
                .addComponent(longitudeText, 30, 30, 30)
                .addComponent(latitudeLabel, 30, 30, 30)
                .addComponent(latitudeText, 30, 30, 30)
                .addComponent(sliderLabel, 30, 30, 30)
                .addComponent(zoomLevelChange, 30, 30, 30)
        );

        statusPanel.setName("statusPanel"); // NOI18N
        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 244, Short.MAX_VALUE)
                .addComponent(progressBar)
                .addContainerGap()
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusMessageLabel, 30, 30, 30)
            .addComponent(progressBar, 30, 30, 30)
            .addGap(23, 23, 23)
        );

      javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
      mainPanel.setLayout(mainPanelLayout);
      mainPanelLayout.setHorizontalGroup(
          mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGap(10)
          .addComponent(mapPanel, 750, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(buttonPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 10, Short.MAX_VALUE)
          .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 10, Short.MAX_VALUE)
          .addGap(10)
      );
      mainPanelLayout.setVerticalGroup(
    		  mainPanelLayout.createSequentialGroup()
          .addComponent(mapPanel, 450, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
              .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(buttonPanel, 30, javax.swing.GroupLayout.DEFAULT_SIZE, 30)
          .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
          .addComponent(statusPanel, 50, javax.swing.GroupLayout.DEFAULT_SIZE, 50)
          .addContainerGap()
      );


      frame.getContentPane().add(mainPanel);
    }

//	@Override
//	public void notifiGpsLocationInfo(LocationInfo locationInfo) {
//
//		if(locationInfo != null) {
//			LonLat currentLocation = new LonLat(locationInfo.getLonInDegMinSec(), locationInfo.getLatInDegMinSec());
//			log(currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
//			log("GPS :" + LonLatUtil.getLongitudeJapaneseString(currentLocation.getLongitude()) + ", " + LonLatUtil.getLatitudeJapaneseString(currentLocation.getLatitude()));
//			mapParam.setCenterLonLat(currentLocation);
////			tileImageManager.update();//To(currentLocation);
//		} else {
//			logger.log(Level.WARNING, "locationInfoがnullでした。");
//		}
//	}

	private void log(String msg) {
		if(logger.isLoggable(Level.FINE)) {
			logger.info(msg);
		}
	}

}
