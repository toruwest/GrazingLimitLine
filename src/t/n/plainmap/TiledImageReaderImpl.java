package t.n.plainmap;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import t.n.map.IFetchingStatusObserver;
import t.n.map.http.ImageGetter;
import t.n.map.common.LightWeightTile;
import t.n.plainmap.util.TiledMapUtil2;

public class TiledImageReaderImpl implements ITiledImageReceiver {
	private final IFetchingStatusObserver observer;
	private final ImageGetter imageGetter;

	private static final String IMAGE_DIR = "img/";
	private static Image NO_DATA_IMG = null;
	private static Image ERROR_IMG = null;
	private static Image FETCHING_IMG = null;
	private static File standardImageSavingDir;
	private static File planePhotoImageSavingDir;
	private File currentImageSavingDir;
	private static final boolean noAccess = false;
	private final Set<String> downloadingSet;
	private MapType type;

	//地図のタイプ毎に分ける
	private Set<LightWeightTile> noStandardImageDataSet;
	private Set<LightWeightTile> noPhotoImageDataSet;

	public TiledImageReaderImpl(IFetchingStatusObserver observer, MapPreference pref) {
		this.observer = observer;
		//JARファイル内に同梱できるように、classpath経由でロードできるフォルダーに格納する。
		try {
			FETCHING_IMG = ImageIO.read(getClass().getClassLoader().getResource(IMAGE_DIR + "fetching.png"));
			NO_DATA_IMG = ImageIO.read(getClass().getClassLoader().getResource(IMAGE_DIR + "no_data.png"));
			ERROR_IMG = ImageIO.read(getClass().getClassLoader().getResource(IMAGE_DIR + "error.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Not foundとなったURL(tile)を記憶しておき、二度とアクセスしないようにする。
		//これはshutdown時に永続化し、起動時に復元する。
		//永続化されたファイルから復元。
		try {
			noStandardImageDataSet = NoImageDataManager.loadStandardData();
			noPhotoImageDataSet = NoImageDataManager.loadPhotoData();
		} catch (Exception e) {
			e.printStackTrace();
		}

		downloadingSet = Collections.synchronizedSet(new HashSet<String>());

		//標準地図と航空写真に対応。 日本全土を表示する場合、白地図を表示する。
		//typeはアプリの起動中に切り替えられるようにする。
		standardImageSavingDir = new File(AppConfig.getImageCacheFolder().getAbsolutePath() + File.separatorChar + MapType.std.name());
		planePhotoImageSavingDir = new File(AppConfig.getImageCacheFolder().getAbsolutePath() + File.separatorChar + MapType.photo.name());

		if(!standardImageSavingDir.exists()) {
			if (!standardImageSavingDir.mkdirs()) {
				//TODO フォルダーを作れなかったことを呼び出し元に通知。
			}
		}
		if(!planePhotoImageSavingDir.exists()) {
			if (!planePhotoImageSavingDir.mkdirs()) {
				//TODO フォルダーを作れなかったことを呼び出し元に通知。
			}
		}

		//デフォルトは標準地図
		currentImageSavingDir = standardImageSavingDir;
		imageGetter = new ImageGetter(observer, standardImageSavingDir, this, pref);
	}

	@Override
	public void setMapType(MapType type) {
		this.type = type;
		switch(type) {
		case std:
			currentImageSavingDir = standardImageSavingDir;
			break;
		case photo:
			currentImageSavingDir = planePhotoImageSavingDir;
			break;
		default:
			//mapType追加時の修正漏れ
			System.err.println("unsupported map type");
		}
		imageGetter.setImageSavingDir(currentImageSavingDir);
	}

	@Override
	public void shutdown() {
		try {
			NoImageDataManager.saveStandardData(noStandardImageDataSet);
			NoImageDataManager.savePhotoData(noPhotoImageDataSet);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			imageGetter.shutdown();
		}
	}

	//queueとthread pool(Executor service)を組み合わせて、同時にアクセスするスレッド数の上限を制限する。
	//非同期読み込み。とりあえず”読み込み中”のようなイメージを返しておき、後で差し替える。読めなかったら"NO DATA"と書かれたイメージを返す。

	//以下の順で指定されたタイルに対応するImageインスタンスを作ります。
	//(1)ディスクに保存してあるファイル (2)国土地理院のホームページから得たタイルイメージ。
	//(1)の場合即座に戻ってくる。
	//(2)の場合、このクラスに非同期で通知されるので、(A)ファイルに書き込む。(B)Viewに通知。
	@Override
	public void setupTileImage(LightWeightTile tile) {
//		if(noStandardImageDataSet.contains(tile)) {
//			tile.setImage(NO_DATA_IMG);
//			return;
//		}
		switch(type) {
		case std:
			if(noStandardImageDataSet.contains(tile)) {
				tile.setImage(NO_DATA_IMG);
				return;
			}

			break;
		case photo:
			if(noPhotoImageDataSet.contains(tile)) {
				tile.setImage(NO_DATA_IMG);
				return;
			}
			break;
		default:
			//bug
		}

		Image resultImg = null;
		if(tile.isError()) {
			throw new IllegalArgumentException(tile.getCause());
		}

		final File localFile = new File(TiledMapUtil2.generateLocalFilename(currentImageSavingDir, type, tile));
		//Imageファイルは以下の状況がある。それぞれ以下のように処理する。すべてHeightデータは後で非同期で受け取る。
		//(1)ファイルがない場合：noAccessがfalseならダウンロード開始、trueならダウンロードしない。
		//(2)ファイルがあるが、ダウンロード中の場合：特に何もしない。ダウンロードが終わるのを待つ。
		//(3)ファイルがある場合(ダウンロードが終わっている): 通知。
		if(!localFile.exists()) { //(1)
			if(!noAccess) {
				downloadingSet.add(localFile.getAbsolutePath());
				imageGetter.getImageAt(type, tile, localFile);
				resultImg = FETCHING_IMG;
			} else {
				resultImg = NO_DATA_IMG;
			}
		} else {
			if(downloadingSet.contains(localFile.getAbsolutePath())) {
				//(2)の場合。何もなし
				resultImg = FETCHING_IMG;
			} else { //(3)
				try {
					resultImg = ImageIO.read(localFile);
				} catch (IOException e) {
					System.err.println("警告：" + localFile.getAbsolutePath() + "の読み込みで" + e.getMessage());
//					e.printStackTrace();
				}
			}
		}
		tile.setImage(resultImg);
	}

	//非同期で国土地理院のサーバーからgetして作成したイメージデータを受け取る。
	@Override
	public void receiveImageData(LightWeightTile tile, File tileImageFile, ImageDataStatus status) {
		if(tileImageFile != null) {
			downloadingSet.remove(tileImageFile.getAbsolutePath());
			switch(status) {
			case success:
				try {
					tile.setImage(ImageIO.read(tileImageFile));
					//ダウンロード中の件数が０の場合に、ステータスメッセージを消去する。noData, errorの場合も同様。
					observer.notifyFetchCompleted(tile, downloadingSet.size() == 0);
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
				break;
			case noData:
				//イメージが存在しない場合、”NO DATA"と書かれた画像を代わりに表示する。
				switch(type) {
				case std:
					noStandardImageDataSet.add(tile);
					break;
				case photo:
					noPhotoImageDataSet.add(tile);
					break;
				default:
					//bug
				}
				tile.setImage(NO_DATA_IMG);
				observer.notifyFetchCompleted(tile, downloadingSet.size() == 0);
				break;
			case error:
				//サーバーからのイメージ読み取りの際にエラーが起きた場合、”ERROR"と書かれた画像を代わりに表示する。
				tile.setImage(ERROR_IMG);
				observer.notifyErrorFetching();
				break;
			}
		} else {
			tile.setImage(ERROR_IMG);
		}
	}

}
