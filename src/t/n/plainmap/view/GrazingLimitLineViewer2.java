package t.n.plainmap.view;

import gnu.io.CommPortIdentifier;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.AbstractAction;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import t.n.gps.GpsEventListener;
import t.n.gps.GpsSerialDeviceHandler;
import t.n.gps.GpsSerialDeviceUtil;
import t.n.map.IFetchingStatusObserver;
import t.n.map.IAltitudeFetchStatusObserver;
import t.n.map.LonLatFormat;
import t.n.map.OsType;
import t.n.map.common.KokudoTile;
import t.n.map.common.LightWeightTile;
import t.n.map.common.LocationInfo;
import t.n.map.common.LonLat;
import t.n.map.common.util.LonLatUtil;
import t.n.map.common.util.TileUtil;
import t.n.map.http.AltitudeDataGetter;
import t.n.plainmap.AppConfig;
import t.n.plainmap.GpsDeviceComboModel;
import t.n.plainmap.IMoveToLocationListener;
import t.n.plainmap.ITileImageManager;
import t.n.plainmap.LimitLineReader;
import t.n.plainmap.MapParam;
import t.n.plainmap.MapPreference;
import t.n.plainmap.MapType;
import t.n.plainmap.MouseMovementObserver;
import t.n.plainmap.TileImageManagerImpl;
import t.n.plainmap.dto.ILimitLineDatum;
import t.n.plainmap.dto.LimitLineDatumJCLO;
import t.n.plainmap.view.dialog.AppFolderShowDialog;
import t.n.plainmap.view.dialog.GotoUserHomeLocationDialog;
import t.n.plainmap.view.dialog.MoveToLocationDialog1;
import t.n.plainmap.view.dialog.MoveToLocationDialog2;
import t.n.plainmap.view.dialog.ObserveLocationDialog;
import t.n.plainmap.view.dialog.OpeningMsgDialog;
import t.n.plainmap.view.dialog.ProxyConfigDialog;

/**
 * occultation:掩蔽
 * Grazing:接食
 * @author toru
 * 流用・改変後のソースコードについて、オリジナル著作者の著作物であると誤解されることの無いよう、注意してください。
 */
public class GrazingLimitLineViewer2 implements IFetchingStatusObserver, MouseMovementObserver,
 GpsEventListener, IDialogStatusObserver, ILimitLineTableEventListener, IObserveTableEventListener, IMoveToLocationListener, IAltitudeFetchStatusObserver {
	private static final String 標準地図 = "標準地図";

	private static final String 航空写真 = "航空写真";

	private static final Logger logger = Logger.getLogger(GrazingLimitLineViewer2.class.getSimpleName());

	private static final int VERTICAL_SPLIT_LOCATION = 320;
	private static final int WIN_HEIGHT = 800;
	private static final int WIN_WIDTH = 1024;
	private static final int TABLE_WIDTH = 500;

	private static final String ZOOM_LEVEL_LABEL = "Zoom level:";
	private static final int INITIAL_ZOOM_LEVEL = 5;
	private static final int ZOOM_MIN = 5;
	private static final int ZOOM_MAX = 18;
	protected static final Integer PHOTO_MAP_ENABLED_LEVEL = 15;

	private static final String OBSERVE_LOCATION_DATA_FILE = "observeLocation.dat";

	private static LonLatFormat lonlatFormat = LonLatFormat.degMinSec;

//	private final LonLat initLonLat = new LonLat(139.768553d, 35.682286d);//東京駅付近
//	private LonLat initOriginLonLat = new LonLat(138.733003d, 35.3806615d);	//富士山
	private LonLat initialCenterLocation = new LonLat(135.0d, 35.00d);	//  日本地図の中心(兵庫県西脇市)

	private GrazingMapPanel2 mapPanel;
	private final ITileImageManager tileImageManager;
	private Integer zoomLevel = INITIAL_ZOOM_LEVEL;

	private GpsSerialDeviceHandler gpsHandler = null;
	private boolean isGpsDeviceFound;
	private LimitLineReader limitLineReader;

	private MapPreference pref;

	private boolean isObserveLocationAddEenabled = false;

	private final IDialogStatusObserver dialogStatusObserver = this;
	private final IMoveToLocationListener moveToListener = this;

	private File appDataDir;

	protected String imageSavingDir;

	boolean isShowInitialDialog = false;

	private final DateFormat dateFormat = new SimpleDateFormat("yy_MM_dd-HH_mm_ss");
	private ObserveLocationDialog observeLocationDialog;
	private GotoUserHomeLocationDialog userHomeInputDialog;
	private MoveToLocationDialog1 movetoLocationDialog;
	private OpeningMsgDialog openingMsgDialog;
	private ProxyConfigDialog proxyConfigDialog;

	private List<ILimitLineDatum> limitLineData;
	private MapParam mapParam;

	private Color mapTypeChangeButtonDefaultColor;

	private final AltitudeDataGetter getter;

	private static final Color mapTypeChangeButtonDisabledColor = Color.LIGHT_GRAY;

	private static final boolean DEBUG = false;

	/**
	 * http://maps.gsi.go.jp/development/ichiran.html
	 *
	 * http://astro-limovie.info/jclo/occult/occult.html
	 * http://astro-limovie.info/jclo/doccuments/Handbook/HandBook2014_Rev100.pdf
	 * http://optik2.mtk.nao.ac.jp/~somamt/graze-predict.html
	 * http://www2.wbs.ne.jp/~spica/index.htm (/~spica/だけでは「準備中」のページが出る)
	 * http://www2.wbs.ne.jp/~spica/Grazing/2015/densi/2015GrazingMap.htm
	 *
	 * WinSmap: 接食現象用のソフト
	 *  これがKMLフォーマットで限界線を出力できる。
	 *  http://www.kotenmon.com/cal/smapwin_jpn.htm
	 * かぐやのデータ(商用利用不可。研究・教育目的のみに利用可能)
	 *  http://l2db.selene.darts.isas.jaxa.jp/index.html.ja
	 *  http://l2db.selene.darts.isas.jaxa.jp/help/ja/KAGUYA_product_list_public_jp.pdf
	 * http://astro-limovie.info/jclo/GrazingZoneViewer/index.html によると、相馬氏の提供するデータは、地人書館の天文観測年表(2009年を最後に休刊)に書かれているのが出典。
	 * Occult4でも出せる？http://astro-limovie.info/jclo/occult/occult.html にある、Occult4のスクリーンショットには、"Short output"というチェックボックスがある。
	 * Occult4は http://lunar-occultations.com/iota/occult4.htm から入手できる。2016年の接食の一覧を出力してみた。
	 * 2015年版も出して、接食限界線ルートWebで用意されているデータと比べたところ、同じようなテキストファイルが出力されていることがわかった。
	 * TODO Windows版では、Occult4が決め打ちのフォルダーに各種ファイルを書き出している。限界線ルートデータは_predictionsに出されているので、
	 * ファイルを移動したりする手間を省くため、デフォルトで、もしこのフォルダーが有れば読むようにする。(フォルダーの場所は、ユーザーが設定できるので、ダイアログで選択可能とする)
     * TODO 現状では、限界線ルートデータのフォルダーにあるファイルを読み込むのは起動時だけだが、上記の対応に合わせて、設定した後で読み込むようにする。
     *
	 * http://www2.wbs.ne.jp/~spica/index.files/Page307.htmから各種ツール(Windows専用)がダウンロード可能となっている。これらのツールと接食限界線ルートWebの関係は
	 * http://www2.wbs.ne.jp/~spica/Grazing/tools/RouteWebManual.pdf の30ページ以降に書かれている。 Excelが無いので試せない。
	 * 接食限界線ルートWebで提供されているテキストファイルは、相馬氏ではなく、鈴木氏が作成したものと思われる。
	 *

	 * 使い方： このアプリで観測地点の緯度・経度を下調べ -> スマホに緯度・経度を転送(メールなどによる) -> 観測地点への移動時にGPSデバイス、あるいはスマホのGoogleマップなどで位置を確認 という流れになると思われる。
	 * このため、Googleマップが対応している緯度・経度のフォーマットでコピーする。フォーマットは、IRiot用と同じかもしれないし、違うかもしれない。違うなら別の操作にする。

	 * TODO 2016年版の接食限界線のデータの入手。
	 * DONE Occult4が出力したファイルを読み込めるようにする。Occult4が限界線を出せる
	 * TODO IRiot(接食現象のレポート作成ソフト)へ経度緯度を転記するため、クリップボードへのコピー(IRiotの「"限界線ルート"より」が認識するフォーマットが不明なので誰かに聞く。鈴木寿さん?)
	 * TODO 「限界線ルートデータ」(鈴木氏のWebアプリの名前) にある「標高補正」機能のアルゴリズムが不明。非対応で良い？
	 * TODO 緯度経度から住所へ変換するWebサービスとして、http://developer.yahoo.co.jp/webapi/map/openlocalplatform/v1/reversegeocoder.htmlというのがある。API KEYの取得が必要。
	 * TODO マウスの位置の緯度経度に対応した高度データを表示する。国土地理院のWebサービス:http://maps.gsi.go.jp/development/api.html
	 *
	 * TODO 限界線のポップアップが重なることがあるので、対策。
	 * DONE 限界線に使える色のバリエーションを最低でも８つくらいに増やしたい。
	 * DONE 航空写真の表示は特定のズームレベル(15)以上の場合のみ有効とする(広域表示時に航空写真にするのは無意味)。15以上で航空写真がないところがあるが、NO DATAと表示する。
	 * DONE 航空写真に切り替えても表示されない(標準地図のまま)。
	 * DONE 航空写真に切り替えてから、ズームレベルを下げていくと、意図せず航空写真を読み込んでしまう。強制的に標準地図に切り替える。
     * DONE 標準地図用と航空写真用のデータが無いタイルの一覧を別の変数で管理する。
	 * DONE 観測地候補のテーブルで表示する緯度・経度について、度分秒形式と実数形式を切り替える。
	 * TODO 観測地候補のテーブルの内容から、クリップボードにコピーするテキストは、上の使い方の項目を参照
	 * DONE 「ホーム」ダイアログで表示する緯度・経度について、度分秒形式と実数形式を併記する。
	 * DONE Windowsでの動作確認。(COMポート)
	 * TODO アプリのアイコン。実行形式化。バージョン番号の付与ルール。
	 * DONE アプリケーションディレクトリーを表示するダイアログを追加
	 * DONE ESCボタンでダイアログを閉じる
	 * DONE サーバーへのアクセスが集中するので、緩和(DelayQueueを使う、地図でズームレベルを変えるときにリクエストをキャンセルする)
	 * FIXME Proxy経由での動作の確認(現状ではログに"null"とだけ表示される)
	 * 以下の方法では設定が認識されるが、proxyサーバー上でアクセスが確認できない。
	 *  java -Dhttp.proxyHost=localhost -Dhttp.proxyPort=8089 -jar xxxx.jar
	 * DONE 指定された経度緯度へ移動するためのダイアログで、緯度・経度の分と秒のフィールドに60以上の値を入力してもノーチェック
	 * TODO 指定された経度緯度へ移動するためのダイアログから移動した場所に、何かマーカーを表示した方がわかりやすい。
	 * DONE 指定された経度緯度へ移動するためのダイアログで、緯度・経度のフィールドにペーストできない
	 * DONE proxyポート番号のフィールドに対し、以前の数字の設定ができない
	 * DONE 特定の限界線データで、線が乱れる。Y座標がゼロになってる？分の単位が６０以上となっていた。
	 * DONE 地図内の「国土地理院の...」という表記が、背後の矩形からはみ出ている。
	 * DONE 指定された経度緯度へ移動。(観測候補地のリストとは無関係に)
	 * DONE マウスのダブルクリックでも、上と同じように移動できるようにする。
	 * DONE 初回起動時の表示範囲を、与那国島から宗谷岬、北方四島までが表示されるように微調整する。
	 * DONE マーカー機能：マウスでクリックされた緯度・経度・zoomレベルを覚えておく(複数)。後でマーカーリストから選んだ地点へジャンプし、zoomも復元する。
	 * DONE GPSデバイスからの入力を受け取れることを確認済み。(秋月電子のFT-232RL経由で繋いだPA6C。ブレッドボード上に組んだ)
	 * DONE ズームインしていったとき、中央の位置を維持するようにしたい。(現状は、ずれてしまう)
	 * DONE 印刷の代わりに画像ファイルへの保存
	 * 2015年版は、http://www2.wbs.ne.jp/~spica/Grazing/2015/densi/2015GrazingMap.htmの、左の現象名の右にある"*"をクリックすると別のタブで開く。
	 *
	 * DONE 相馬氏作の月縁図?を表示するか？ テキストファイルとは拡張子が違うだけ。できるだろうけど、メリットがあるか？
	 *
	 * DONE 複数の限界線を表示するので、接食現象との対応が分かるようにする。限界線の線種(破線)を変えたり、色分けしたり、文字列を表示したりする。
	 * とりあえず破線は使わない。色分けのみ。
	 * DONE 表の各行の色を、地図上の限界線の色に合わせる。
	 * DONE 観測地の一覧をクリップボードへコピーする。テキストファイル(CSV)への保存はやめた。
	 * DONE 緯度・経度の表示形式について、度分秒/度.xxxxxxを切り替えられるようにする。(モバイル版Googleマップのマーカーは後者の形式)
	 * DONE ５未満のズームレベルは不要。(日本専用でOK)
	 * DONE 縮尺が大きい場合は、白地図の方が見やすい。(9までは白地図、10から標準地図）
	 * DONE 本ソフトから、データを含むテキストファイルを取り込むUIをどうするか？UIは無しにした。
	 * 特定のフォルダーに格納されている、接食現象毎のデータファイルをリストアップして表で候補として表示し、チェックされたデータについて限界線を表示する。
	 * ファイルは、年が違っても全て同じフォルダーに格納する。
	 * DONE 接食限界線と画像ファイルの保存先をappDirの配下にする。
	 * DONE デフォルトの地図画像(NO DATA, fetching...)の保存先をJARファイルにする
	 * DONE 地図画像のキャッシュの保存先をappDirにする。FolderConfig.javaでハードコーディングしている。
	 * DONE 「ホーム」ボタンのようなものを用意して、ユーザーが設定した地域を表示する。毎回、日本全図からズームインしていくのはつらい。
	 * DONE win/macのプラットフォーム毎にOK/Cancelボタンの並びが違うのに対応。
	 * DONE pref,シリアライズして保存するファイルはapplication data(win), あるいは/Users/home/Library/ApplicationSupport/(mac)、~/.grazingLimitLine(Unix)に格納する。
	 * DONE 国土地理データの利用規約を再確認。起動時に規約を表示するようにする。
	 * DONE 国土地理データの利用規約を、about画面からも表示できるようにする。
	 * DONE 初回起動時にProxyの設定を行う。proxyの設定画面を追加。
	 * @throws IOException
     */
    public GrazingLimitLineViewer2() throws IOException {
    	appDataDir = new File(AppConfig.getAppDataDir());
    	if(!appDataDir.exists()) appDataDir.mkdirs();

		pref = new MapPreference(appDataDir);

		if(pref.exists()) {
			pref.load();
			LonLat restoredLonLat = pref.getInitialLocation();
			if(!LonLatUtil.isOutOfJapanRegion(restoredLonLat)) {
				initialCenterLocation = restoredLonLat;
			}
		} else {
			isShowInitialDialog = true;
		}

		if(!pref.isHideOpeningMessage()) {
			if(openingMsgDialog == null) {
				openingMsgDialog = new OpeningMsgDialog(frame);
			}
			openingMsgDialog.setVisible(true);
		}

		if(isShowInitialDialog) {
			new AppFolderShowDialog(null, true).setVisible(true);
			//proxy設定画面を開く。
			if(proxyConfigDialog == null) createProxyConfigDialog();
			proxyConfigDialog.setVisible(true);
		}

		mapParam = new MapParam(KokudoTile.TILE_SIZE, MapType.std, INITIAL_ZOOM_LEVEL, initialCenterLocation);
		tileImageManager = new TileImageManagerImpl(this, mapParam, pref);
		mapPanel = new GrazingMapPanel2(mapParam, tileImageManager, this);
		mapParam.setMapRect(mapPanel.getBounds());
        getter = new AltitudeDataGetter(this);

		initComponents();

		setLimitLineDir();
        limitLineTable.addTableEventListener(this);
        limitLineTable.addTableEventListener(mapPanel);

        File observeLocationDataFile = new File(appDataDir, OBSERVE_LOCATION_DATA_FILE);
        if(observeLocationDataFile.exists()) {
        	observeLocationTable.restoreFromFile(observeLocationDataFile);
        }
        copyObserveLocationsButton.setEnabled(observeLocationTable.getRowCount() > 0);

        //インスタンスを共有することにより、変更時に反映されるようにする。
        mapPanel.setObserveLocations(observeLocationTable.getObserveLocations());

        lonlatFormatButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//緯度・経度の表示形式を変える。
				if(lonlatFormat == LonLatFormat.degMinSec) {
					lonlatFormat = LonLatFormat.jissu;
				} else if(lonlatFormat == LonLatFormat.jissu) {
					lonlatFormat = LonLatFormat.degMinSec;
				}
				observeLocationTable.setLonLatFormat(lonlatFormat);
			}
		});

        printButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
            	//格納フォルダーとして最初に一回だけ指定すれば次回以降はそこに保存するようにする。
            	//また、ファイル名は日時から自動で生成することも、ユーザーによる指定もできるようにする。
				try {
					BufferedImage img = mapPanel.takeScreenShot();

					if(imageSavingDir == null) {
						//前回終了時の格納先フォルダーを得る。
						imageSavingDir = pref.getDefaultImageSavingDir();
						//nullなら、ユーザーのホームディレクトリーにする。
						if(imageSavingDir == null) {
							imageSavingDir = System.getProperty("user.home");
						}
					}

					//格納先フォルダーとファイル名を入力
					JFileChooser chooser = new JFileChooser(imageSavingDir);

					FileNameExtensionFilter filter = new FileNameExtensionFilter("JPG Images", "jpg");
					chooser.setFileFilter(filter);

					String suggestFilename = generateSaveImageFilename();
					File suggestedFile = new File(new File(imageSavingDir), suggestFilename);
					chooser.setSelectedFile(suggestedFile);
					int ans0 = chooser.showSaveDialog(frame);
					File file = chooser.getSelectedFile();

					if(ans0 == JFileChooser.APPROVE_OPTION) {
						if(file.exists()) {
							int ans1 = JOptionPane.showConfirmDialog(frame, "ファイルを上書きしますか？", "上書きの確認", JOptionPane.YES_NO_OPTION);
							if(ans1 == JOptionPane.YES_OPTION) {
								writeImageToFile(img, file);
							}
						} else {
							writeImageToFile(img, file);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });

        proxyConfigButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//proxy設定画面を開く。
				if(proxyConfigDialog == null) {
					createProxyConfigDialog();
				}
				proxyConfigDialog.setVisible(true);
				//この後、HttpGetterのproxyを再設定する必要がある。
				//prefのセッターが呼ばれたら、prefに設定したリスナー経由でHttpGetterのメソッドを呼ぶようにした
			}
		});

        if(dataFolderConfigButton != null) {
        	dataFolderConfigButton.addActionListener(new ActionListener() {
        		@Override
        		public void actionPerformed(ActionEvent e) {
        			//prefから現在設定されているディレクトリを得る。
        			String limitLineDataDir = pref.getLimitLineDataDir();
        			if(limitLineDataDir == null) {
        				limitLineDataDir = AppConfig.getDefaultOccult4DataDir();
        			}
        			//別の場所にある、Occult4の予測データが格納されたZIPファイルを選択。
        			JFileChooser chooser = new JFileChooser(limitLineDataDir);
        			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        			FileNameExtensionFilter filter = new FileNameExtensionFilter("Occult4 grazing prediction result (ZIP file)", "zip");
        			chooser.setFileFilter(filter);
        			chooser.setMultiSelectionEnabled(false);

        			int ans0 = chooser.showOpenDialog(frame);
        			if(ans0 == JFileChooser.APPROVE_OPTION) {
        				File newDir = chooser.getSelectedFile();
        				if(newDir.exists()) {
        					//ZIPファイルの置かれているフォルダーをデフォルトとする。
        					pref.setOccult4Dir(newDir.getParent());
        					//dataを読み取る。
        					setLimitLineDir();
        				} else {
        					JOptionPane.showMessageDialog(frame, "フォルダーが見つかりません");
        				}
        			}
        		}
        	});
        }

        aboutButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(openingMsgDialog == null) {
					openingMsgDialog = new OpeningMsgDialog(frame);
				}
				openingMsgDialog.setVisible(true);
			}
		});

        mapTypeChangeButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
            	//現在の地図のタイプとは逆にする。
        		switch(mapParam.getMapType()) {
            	case std:
            		mapTypeChangeButton.setText(標準地図);
            		mapParam.setMapType(MapType.photo);
            		break;
            	case photo:
            		mapTypeChangeButton.setText(航空写真);
            		mapParam.setMapType(MapType.std);
            		break;
            	default:
            		//地図タイプを追加したのに、ここを修正していない場合。
            		System.err.println("バグ");
            	}

            	mapPanel.repaint();
            }
        });

        moveToLocationButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
            	if(movetoLocationDialog == null) {
            		movetoLocationDialog = new MoveToLocationDialog1(frame, moveToListener);
            		AbstractAction act = new AbstractAction() {
          			  @Override public void actionPerformed(ActionEvent e) {
          				movetoLocationDialog.setVisible(false);
          			  }
          			};
          			InputMap imap = movetoLocationDialog.getRootPane().getInputMap(
          					JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
          			imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
          			movetoLocationDialog.getRootPane().getActionMap().put("close-it", act);
            	}
            	movetoLocationDialog.setVisible(true);
            }
        });

        moveToHomeButton.addActionListener(new java.awt.event.ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		//ホームが設定済みでも、未設定でもダイアログを出して、それからジャンプできるようにする。
        		//位置やズームレベルの更新も可能とする。(現在の地図の設定が使われる)
        		if(userHomeInputDialog == null) {
        			userHomeInputDialog = new GotoUserHomeLocationDialog(frame, pref.getUserHomeLocation(), pref.getUserHomeZoomLevel(), mapParam.getCenterLonLat(), mapParam.getZoomLevel(), moveToListener);
        			AbstractAction act = new AbstractAction() {
        				  @Override public void actionPerformed(ActionEvent e) {
        					  userHomeInputDialog.setVisible(false);
        				  }
        				};
        			InputMap imap = userHomeInputDialog.getRootPane().getInputMap(
        					  JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        			imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
        			userHomeInputDialog.getRootPane().getActionMap().put("close-it", act);
        		}
        		userHomeInputDialog.setVisible(true);
        	}
        });

        addObserveLocationButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
            	if(observeLocationDialog == null) {
            		observeLocationDialog = new ObserveLocationDialog(frame, dialogStatusObserver);
            		//テーブルに日付と現象名を設定する。
            		observeLocationDialog.setLimitLineData(limitLineData);
        			AbstractAction act = new AbstractAction() {
      				  @Override public void actionPerformed(ActionEvent e) {
      					observeLocationDialog.setVisible(false);
      				  }
      				};
      			InputMap imap = observeLocationDialog.getRootPane().getInputMap(
      					  JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
      			imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
      			observeLocationDialog.getRootPane().getActionMap().put("close-it", act);
            	}
            	observeLocationDialog.setVisible(true);
            	isObserveLocationAddEenabled = true;
            }
        });

        removeObserveLocationButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
            	observeLocationTable.removeSelectedEventDateLocation();
            	observeLocationTable.saveLocationInfo(new File(appDataDir, OBSERVE_LOCATION_DATA_FILE));
            	boolean b = observeLocationTable.getRowCount() > 0;
            	copyObserveLocationsButton.setEnabled(b);
            }
        });

        copyObserveLocationsButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
			public void actionPerformed(java.awt.event.ActionEvent evt) {
            	JOptionPane.showMessageDialog(frame, "観測地候補のリストをクリップボードにコピーしました。");
            	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selection = new StringSelection(observeLocationTable.getClipboardData());
                clipboard.setContents(selection, selection);
            }
        });

        showAllLinesCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				boolean selected = showAllLinesCheckBox.isSelected();
				limitLineTable.setShowAllItems(selected);
				mapPanel.setShowAllStatusChanged(selected);
			}
		});

        gpsEnableCheckBox.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		boolean selected = gpsEnableCheckBox.isSelected();
				gpsPortSelect.setEditable(selected);
				setGpsEnabled(selected);
        	}
        });

		final GpsEventListener l = this;
		final List<CommPortIdentifier> serialPortCandidates = GpsSerialDeviceUtil.getCandidateSerialPorts(AppConfig.getOsType());

		Vector<String> ports = new Vector<>();
		for(CommPortIdentifier cpi : serialPortCandidates) {
			ports.add(cpi.getName());
		}

		ComboBoxModel<String> gpsPortModel = new DefaultComboBoxModel<>(ports);
		gpsPortSelect.setModel(gpsPortModel);
		gpsPortSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int targetPortIndex = gpsPortSelect.getSelectedIndex();
				final CommPortIdentifier targetPort = serialPortCandidates.get(targetPortIndex);

				if(targetPort != null) {
					SwingWorker<Object, Object> sw = new SwingWorker<Object, Object>() {
						@Override
						protected Object doInBackground() throws Exception {
							gpsHandler = new GpsSerialDeviceHandler(targetPort, l);
							return null;
						}
					};
					sw.execute();
				} else {
					log("GPSデバイスの接続されたシリアルポートが見つかりませんでした。");
					isGpsDeviceFound = false;
				}
			}
		});

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				log("shutdown:Save and release resources");
				try {
					pref.setInitialLocation(mapParam.getCenterLonLat());
					pref.setDefaultImageSaveDir(imageSavingDir);
					pref.save();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					tileImageManager.close();
					if(gpsHandler != null)	gpsHandler.close();
				}
			}
		});
    }

	private void setLimitLineDir() {
		//Occult4のデフォルトの予測データの格納先から読む
		try {
			String limitLineDataDir = pref.getLimitLineDataDir();
			limitLineReader = new LimitLineReader(new File(limitLineDataDir));
			limitLineData = limitLineReader.getLimitLineData();
			mapPanel.setLimitLines(limitLineData);
			limitLineTable.setData(limitLineData);
			frame.repaint();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void createProxyConfigDialog() {
		proxyConfigDialog = new ProxyConfigDialog(frame, pref);
		AbstractAction act = new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				proxyConfigDialog.setVisible(false);
			}
		};
		InputMap imap = proxyConfigDialog.getRootPane().getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
		proxyConfigDialog.getRootPane().getActionMap().put("close-it", act);
	}

	private String generateSaveImageFilename() {
		Date date = new Date();
		String tmp = dateFormat.format(date);
		tmp = tmp + ".jpg";
		return tmp;
	}

	private void writeImageToFile(BufferedImage img, File file)
			throws IOException {
		imageSavingDir = file.getParent();
		ImageIO.write(img, "jpg", file);
	}

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
					updateProgressStatus(isAllDownloadDone);
				}
			});
		} else {
			updateProgressStatus(isAllDownloadDone);
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

	private void updateProgressStatus(boolean isAllDownloadDone) {
		if(isAllDownloadDone) {
			progressBar.setIndeterminate(false);
			statusMessageLabel.setText("");
		}
	}

	@Override
	public void notifyMouseMovingLonLat(LonLat lonlat) {
		// 緯度経度のフォーマットを変えるのはここで行う。
		String lon = null;
		String lat = null;
		switch(lonlatFormat) {
		case degMinSec:
			lon = LonLatUtil.getLongitudeJapaneseString(lonlat.getLongitude());
			lat = LonLatUtil.getLatitudeJapaneseString(lonlat.getLatitude());
			break;
		case jissu:
			//桁数を制限
			lon = LonLatUtil.getFormattedLongitudeString(lonlat.getLongitude());
			lat = LonLatUtil.getFormattedLatitudeString(lonlat.getLatitude());
			break;
		}
		longitudeText.setText(lon);
		latitudeText.setText(lat);
		if(DEBUG) {
			System.out.println(getClass().getSimpleName() + ": X:" + TileUtil.getTileNoX(zoomLevel, lonlat) + ", Y:" + TileUtil.getTileNoY(zoomLevel, lonlat));
		}
	}

	@Override
	public void notifyMouseClickedLonLat(LonLat lonlat) {
		if(isObserveLocationAddEenabled) {
			//マウスをクリックして観測地を指定された。
			String[] tmp = observeLocationDialog.getEventDateName();
			String eventDate = tmp[0];
			String eventName = tmp[1];
			String observeLocation = observeLocationDialog.getObserveLocation();
			//上の２つが未入力の場合、追加しない。なお、観測地名は後で編集できる。
			if(!eventDate.isEmpty() && !eventName.isEmpty() && !observeLocation.isEmpty()) {
				//lonlatをそのまま渡す。
				observeLocationTable.addEventDateLocation(eventDate, eventName, observeLocation, lonlat, zoomLevel);

				//テーブルが更新されたら､直ちに保存する。常に同じファイルに上書きする。保存はシリアライゼーションによる。
				//読み込みは起動時に自動的に行う。
                observeLocationTable.saveLocationInfo(new File(appDataDir, OBSERVE_LOCATION_DATA_FILE));

				//観測候補地の表が一行以上あったら、クリップボードにコピーできるようにする。
				boolean b = observeLocationTable.getRowCount() > 0;
				copyObserveLocationsButton.setEnabled(b);
			}
		}
		//クリックされた地点の標高を国土地理院のサーバーからgetして表示する。
		getter.startFetching((float)lonlat.getLongitude(), (float)lonlat.getLatitude());

		mapPanel.repaint();
	}

	@Override
	public void notifiGpsLocationInfo(LocationInfo locationInfo) {
		if(locationInfo != null) {
			final LonLat currentLocation = new LonLat(locationInfo.getLonInDegMinSec(), locationInfo.getLatInDegMinSec());
			log(currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
			log("GPS :" + LonLatUtil.getLongitudeJapaneseString(currentLocation.getLongitude()) + ", " + LonLatUtil.getLatitudeJapaneseString(currentLocation.getLatitude()));
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					mapPanel.setCurrentGpsLocation(currentLocation);
					gpsStatusLabel.setText("GPS:接続");
				}
			});
			logger.log(Level.WARNING, "locationInfoがnullでした。");
		}
	}

	@Override
	public void notifySuccess(double lon, double lat, double altitude) {
		altitudeTextField.setText(String.valueOf(altitude));
	}

	@Override
	public void notifyNotFound(double lon, double lat) {
		altitudeTextField.setText("-----");
	}

	@Override
	public void notifyShowStatusChanged(int row, boolean b) {
		showAllLinesCheckBox.setSelected(false);
	}

	@Override
	public void notifyHilightStatusChanged(int row, boolean b) {}

	@Override
	public void notifyObserveLocationDialogClosed() {
		isObserveLocationAddEenabled = false;
	}

	@Override
	public void setRemoveButtonEnabled(boolean b) {
		removeObserveLocationButton.setEnabled(b);
	}

	@Override
	public void notifyLocationRemoved() {
		frame.repaint();
	}

	@Override
	public void notifyMoveToLocation(LonLat lonlat, int newZoomLevel) {
		mapParam.setCenterLonLat(lonlat);
		mapParam.setZoomLevel(newZoomLevel);
		this.zoomLevel = newZoomLevel;
		zoomLevelSlider.setValue(newZoomLevel);
		mapPanel.repaint();
	}

	@Override
	public void notifyGotoHomeLocation(LonLat homeLocation, int zoomLevel) {
		pref.setUserHomeLocation(homeLocation);
		pref.setUserHomeZoomLevel(zoomLevel);
		mapParam.setCenterLonLat(homeLocation);
		mapParam.setZoomLevel(zoomLevel);
		//zoomLevelとuserHomeZoomLevelは別の変数とする。
		this.zoomLevel = zoomLevel;
		zoomLevelSlider.setValue(zoomLevel);
		mapPanel.repaint();
	}

	@Override
	//文字列の検証は呼び出し元で行うこと。
	public void notifyMoveToLocation(double lon, double lat) {
		LonLat lonlat = new LonLat(lon, lat);
		mapParam.setCenterLonLat(lonlat);
		mapPanel.repaint();
	}

    @SuppressWarnings("unchecked")
    private void initComponents() {
		frame = new JFrame();
		frame.setTitle("星食限界線");
		frame.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addComponentListener(mapPanel);

        upperPanel = new JPanel();
        horizonSplitPane = new JSplitPane();
        horizonSplitPane.setDividerLocation(TABLE_WIDTH);
        vertSplitPane = new JSplitPane();
        limitLinesPanel = new JPanel();
    	limitLineLabel = new JLabel("星食現象");
        showAllLinesCheckBox = new JCheckBox();
        showAllLinesCheckBox.setSelected(true);
        showAllLinesCheckBox.setText("全表示");

        locationPanel = new JPanel();
        locLabel = new JLabel("観測地候補");

        addObserveLocationButton = new JButton();
        addObserveLocationButton.setText("追加");
        addObserveLocationButton.setToolTipText("観測地候補の表に、追加した位置を追加します。");

        removeObserveLocationButton = new JButton();
        removeObserveLocationButton.setText("削除");
        removeObserveLocationButton.setToolTipText("観測地候補の表から、選択した行を削除します");
        removeObserveLocationButton.setEnabled(false);

//        saveObserveLocationsButton = new JButton();
//        saveObserveLocationsButton.setText("保存");
//        saveObserveLocationsButton.setEnabled(false);

        copyObserveLocationsButton = new JButton();
        copyObserveLocationsButton.setText("コピー");
        copyObserveLocationsButton.setToolTipText("観測地候補の表の内容をクリップボードにコピーします");
        //以下はobserveLocationTableの内容をファイルから復元した後に行う。
        //copyObserveLocationsButton.setEnabled(false);

        jToolBar = new JToolBar();
        jToolBar.setFloatable(false);
        jToolBar.setRollover(true);

        mapTypeChangeButton = new JButton();
        mapTypeChangeButton.setText(航空写真);
        mapTypeChangeButton.setToolTipText("地図のタイプを変えます");
        mapTypeChangeButtonDefaultColor = mapTypeChangeButton.getBackground();
        mapTypeChangeButton.setEnabled(false);
        mapTypeChangeButton.setForeground(mapTypeChangeButtonDisabledColor);
        mapTypeChangeButton.setHorizontalTextPosition(SwingConstants.CENTER);
        mapTypeChangeButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        jToolBar.add(mapTypeChangeButton);

        moveToLocationButton = new JButton("移動...");
        moveToLocationButton.setToolTipText("入力した緯度・経度へ移動します");
        jToolBar.add(moveToLocationButton);

        moveToHomeButton = new JButton("ホーム...");
        moveToHomeButton.setToolTipText("ホームとして設定した緯度・経度へ移動します");
        jToolBar.add(moveToHomeButton);

        printButton = new JButton();
        printButton.setText("地図を保存...");
        printButton.setToolTipText("表示されている地図をJPEG画像ファイルとして保存します");
        printButton.setFocusable(false);
        printButton.setHorizontalTextPosition(SwingConstants.CENTER);
        printButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        jToolBar.add(printButton);

        jToolBar.addSeparator();

        proxyConfigButton = new JButton();
        proxyConfigButton.setText("Proxy...");
        proxyConfigButton.setToolTipText("Proxyの設定を行います");
        proxyConfigButton.setFocusable(false);
        proxyConfigButton.setHorizontalTextPosition(SwingConstants.CENTER);
        proxyConfigButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        jToolBar.add(proxyConfigButton);

        if(AppConfig.getOsType() == OsType.win) {
        	dataFolderConfigButton = new JButton();
        	dataFolderConfigButton.setText("data..");
        	dataFolderConfigButton.setToolTipText("Occult4の限界線ルートデータを格納しているフォルダーの設定を行います");
        	dataFolderConfigButton.setFocusable(false);
        	dataFolderConfigButton.setHorizontalTextPosition(SwingConstants.CENTER);
        	dataFolderConfigButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        	jToolBar.add(dataFolderConfigButton);
        }

        aboutButton = new JButton();
        aboutButton.setText("About...");
        aboutButton.setFocusable(false);
        aboutButton.setHorizontalTextPosition(SwingConstants.CENTER);
        aboutButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        jToolBar.add(aboutButton);

        buttonPanel = new JPanel();
        lonlatFormatButton = new JButton("書式");
        //formatButton.setEnabled(false);
        lonlatFormatButton.setToolTipText("緯度・経度の表示形式を度分秒あるいは実数表記(度と小数点)に切り替えます。");

        longitudeLabel = new JLabel();
        longitudeText = new JTextField();
        latitudeLabel = new JLabel();
        latitudeText = new JTextField();

        zoomLevelLabel = new JLabel(ZOOM_LEVEL_LABEL + INITIAL_ZOOM_LEVEL);

        zoomLevelSlider = new JSlider(JSlider.HORIZONTAL, ZOOM_MIN, ZOOM_MAX, INITIAL_ZOOM_LEVEL);

        zoomLevelSlider.setMinimum(ZOOM_MIN);
        zoomLevelSlider.setMaximum(ZOOM_MAX);
        zoomLevelSlider.setMinorTickSpacing(1);
        zoomLevelSlider.setSnapToTicks(true);
        zoomLevelSlider.setPaintTicks(true);
        zoomLevelSlider.setPaintLabels(true);
        zoomLevelSlider.addChangeListener(new ChangeListener() {

			@Override
        	public void stateChanged(ChangeEvent e) {
        		JSlider source = (JSlider)e.getSource();
        		if (!source.getValueIsAdjusting()) {
        			zoomLevel = source.getValue();
        			zoomLevelLabel.setText(ZOOM_LEVEL_LABEL + zoomLevel);
        			mapParam.setZoomLevel(zoomLevel);
        			boolean isEnabled = zoomLevel >= PHOTO_MAP_ENABLED_LEVEL;
					mapTypeChangeButton.setEnabled(isEnabled);
        			mapTypeChangeButton.setForeground(isEnabled?mapTypeChangeButtonDefaultColor:Color.GRAY);
        			if(!isEnabled) {
        				mapParam.setMapType(MapType.std);
        				mapTypeChangeButton.setText(航空写真);
        			}
        		}
        	}
        });

        statusPanel = new JPanel();
        statusMessageLabel = new JLabel();
        progressBar = new JProgressBar();

        vertSplitPane.setDividerLocation(VERTICAL_SPLIT_LOCATION);
        vertSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        limitLineTable = new LimitLineTable();
        jScrollPane1 = new JScrollPane();
        jScrollPane1.setViewportView(limitLineTable);

        GroupLayout eventPanelLayout = new GroupLayout(limitLinesPanel);
        limitLinesPanel.setLayout(eventPanelLayout);
        eventPanelLayout.setHorizontalGroup(
                eventPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(eventPanelLayout.createSequentialGroup()
                    .addGap(17, 17, 17)
                    .addComponent(limitLineLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(showAllLinesCheckBox)
                    .addContainerGap(246, Short.MAX_VALUE))
                .addGroup(GroupLayout.Alignment.TRAILING, eventPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
            );
            eventPanelLayout.setVerticalGroup(
                eventPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, eventPanelLayout.createSequentialGroup()
                    .addGroup(eventPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(limitLineLabel)
                        .addComponent(showAllLinesCheckBox))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE))
            );
        vertSplitPane.setTopComponent(limitLinesPanel);
        observeLocationTable = new ObserveLocationTable(this);
        observeLocationTable.setLonLatFormat(lonlatFormat);

        jScrollPane2 = new JScrollPane();
        jScrollPane2.setViewportView(observeLocationTable);

        GroupLayout locationPanelLayout = new GroupLayout(locationPanel);
        locationPanel.setLayout(locationPanelLayout);
        locationPanelLayout.setHorizontalGroup(
                locationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(locationPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(locationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(locationPanelLayout.createSequentialGroup()
                            .addComponent(locLabel, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(addObserveLocationButton)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(removeObserveLocationButton)
                            .addContainerGap(146, Short.MAX_VALUE)
//                            .addComponent(saveObserveLocationsButton)
//                            .addContainerGap(146, Short.MAX_VALUE)
                            .addComponent(copyObserveLocationsButton)
                            .addContainerGap(146, Short.MAX_VALUE))
                        .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
            );
            locationPanelLayout.setVerticalGroup(
                locationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, locationPanelLayout.createSequentialGroup()
                    .addGroup(locationPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(locLabel, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
                        .addComponent(addObserveLocationButton)
                        .addComponent(removeObserveLocationButton)
//                        .addComponent(saveObserveLocationsButton)
                        .addComponent(copyObserveLocationsButton))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE)
                    .addGap(13, 13, 13))
            );
        vertSplitPane.setBottomComponent(locationPanel);
        horizonSplitPane.setLeftComponent(vertSplitPane);

        GroupLayout mapPanelLayout = new GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGap(0, 461, Short.MAX_VALUE)
        );

        horizonSplitPane.setRightComponent(mapPanel);

        GroupLayout upperPanelLayout = new GroupLayout(upperPanel);
        upperPanel.setLayout(upperPanelLayout);
        upperPanelLayout.setHorizontalGroup(
            upperPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addComponent(jToolBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(horizonSplitPane))
        );
        upperPanelLayout.setVerticalGroup(
            upperPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(upperPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jToolBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizonSplitPane))
        );

        buttonPanel.setBackground(new java.awt.Color(255, 204, 204));
        longitudeLabel = new JLabel("経度");
        longitudeText = new JTextField();
        longitudeText.setEditable(false);
        longitudeText.setText(""); // NOI18N
        longitudeText.setName("lon"); // NOI18N

        latitudeLabel = new JLabel("緯度");
        latitudeText = new JTextField();
        latitudeText.setEditable(false);
        latitudeText.setText(""); // NOI18N
        latitudeText.setName("Lat"); // NOI18N
        altitudeLabel = new JLabel("標高");
        altitudeTextField = new javax.swing.JTextField();
        altitudeTextField.setEditable(false);

        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
                buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(buttonPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(lonlatFormatButton)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(longitudeLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(longitudeText, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(latitudeLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(latitudeText, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(altitudeLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(altitudeTextField, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(zoomLevelLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(zoomLevelSlider, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addContainerGap())
            );
            buttonPanelLayout.setVerticalGroup(
                buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(buttonPanelLayout.createSequentialGroup()
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lonlatFormatButton)
                            .addComponent(longitudeLabel)
                            .addComponent(longitudeText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(latitudeLabel)
                            .addComponent(latitudeText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(zoomLevelLabel)
                            .addComponent(altitudeLabel)
                            .addComponent(altitudeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addComponent(zoomLevelSlider, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
            );

        gpsPanel = new JPanel();
        gpsEnableCheckBox = new JCheckBox();
		gpsEnableCheckBox.setText("GPS有効");

        gpsPortSelect = new JComboBox<>();
        jLabel4 = new JLabel();
        jLabel4.setText("経度");
        jLabel5 = new JLabel();
        jLabel5.setText("緯度");
        gpsLongitudejTextField = new JTextField();
        gpsLatitudeTextField = new JTextField();
        gpsStatusLabel = new JLabel("未接続");
        gpsLongitudejTextField.setEditable(false);
        gpsLatitudeTextField.setEditable(false);

        moveGpsLocationButton = new JButton();
        moveGpsLocationButton.setText("GPSの位置へ移動");

        setGpsEnabled(false);

        gpsPanel.setBackground(new java.awt.Color(255, 204, 204));
        GroupLayout gpsPanelLayout = new GroupLayout(gpsPanel);
        gpsPanel.setLayout(gpsPanelLayout);
        gpsPanelLayout.setHorizontalGroup(
            gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(gpsPanelLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(gpsEnableCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(gpsStatusLabel, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gpsPortSelect, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gpsLongitudejTextField, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38)
                .addComponent(jLabel5)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gpsLatitudeTextField, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(moveGpsLocationButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        gpsPanelLayout.setVerticalGroup(
            gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, gpsPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(gpsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(gpsPortSelect, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(gpsEnableCheckBox)
                    .addComponent(gpsLongitudejTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(moveGpsLocationButton)
                    .addComponent(gpsLatitudeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(gpsStatusLabel))
                .addContainerGap())
        );

//        statusPanel.setBackground(new java.awt.Color(153, 255, 153));
        statusPanel.setBackground(new java.awt.Color(255, 204, 204));

//        statusMessageLabel.setBackground(new java.awt.Color(255, 204, 204));
        statusMessageLabel.setText("");

        GroupLayout statusPanelLayout = new GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, GroupLayout.PREFERRED_SIZE, 388, GroupLayout.PREFERRED_SIZE))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(progressBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(statusMessageLabel, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
        );

        GroupLayout layout = new GroupLayout(frame.getContentPane());
        frame.getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(upperPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addComponent(statusPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(gpsPanel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(upperPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gpsPanel, GroupLayout.PREFERRED_SIZE, 39, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        frame.pack();
    }// </editor-fold>

	private void setGpsEnabled(boolean b) {
		gpsPortSelect.setEnabled(true);
		moveGpsLocationButton.setEnabled(b);
		if(b) {
//			gpsStatusLabel.setText("GPS:未接続");
			gpsStatusLabel.setForeground(Color.BLACK);
			jLabel4.setForeground(Color.BLACK);
			jLabel5.setForeground(Color.BLACK);
			gpsPortSelect.setForeground(Color.BLACK);
		} else {
			gpsStatusLabel.setForeground(Color.GRAY);
			jLabel4.setForeground(Color.GRAY);
			jLabel5.setForeground(Color.GRAY);
			gpsLongitudejTextField.setText("");
			gpsLatitudeTextField.setText("");
			gpsPortSelect.setForeground(Color.GRAY);
		}
	}

	private void log(String msg) {
		if(logger.isLoggable(Level.FINE)) {
			logger.info(msg);
		}
	}

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GrazingLimitLineViewer2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GrazingLimitLineViewer2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GrazingLimitLineViewer2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GrazingLimitLineViewer2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //http://stackoverflow.com/questions/7252749/how-to-use-command-c-command-v-shortcut-in-mac-to-copy-paste-text
        InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
        System.out.println("http.proxyPort:" + System.getProperty("http.proxyPort"));

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
            	try {
					new GrazingLimitLineViewer2();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
    }

    private JFrame frame = null;

    // Variables declaration - do not modify
    private JPanel buttonPanel;
    private JButton lonlatFormatButton;
    private JSplitPane horizonSplitPane;
    private JButton addObserveLocationButton;
//    private JCheckBox addObserveLocationCheckbox;
    private JButton removeObserveLocationButton;
//    private JButton saveObserveLocationsButton;
    private JButton copyObserveLocationsButton;
    private JButton mapTypeChangeButton;
	private JLabel limitLineLabel;

//	private JButton configButton;
    private javax.swing.JButton proxyConfigButton;
    private javax.swing.JButton dataFolderConfigButton;
    private JButton aboutButton;

    private JLabel locLabel;
    private JLabel longitudeLabel;
    private JLabel latitudeLabel;
    private JLabel zoomLevelLabel;
    private JPanel locationPanel;
    private JPanel limitLinesPanel;
    private LimitLineTable limitLineTable;
    private ObserveLocationTable observeLocationTable;
    private JTextField longitudeText;
    private JTextField latitudeText;
    private JToolBar jToolBar;
    private JButton printButton;
    private JButton moveToLocationButton;
    private JButton moveToHomeButton;
    private JProgressBar progressBar;
    private JLabel statusMessageLabel;
    private JPanel statusPanel;
    private JPanel upperPanel;
    private JSplitPane vertSplitPane;
    private JSlider zoomLevelSlider;
    private JLabel jLabel4;
    private JLabel jLabel5;

    private JCheckBox gpsEnableCheckBox;
    private JTextField gpsLatitudeTextField;
    private JTextField gpsLongitudejTextField;
    private JPanel gpsPanel;
    private JComboBox<String> gpsPortSelect;
    private JLabel gpsStatusLabel;
    private JButton moveGpsLocationButton;
	private JScrollPane jScrollPane2;
	private JScrollPane jScrollPane1;
	private JCheckBox showAllLinesCheckBox;
    private javax.swing.JLabel altitudeLabel;
    private javax.swing.JTextField altitudeTextField;
    // End of variables declaration
}
