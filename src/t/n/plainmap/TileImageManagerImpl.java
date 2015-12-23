package t.n.plainmap;

import java.util.List;

import t.n.map.IFetchingStatusObserver;
//import t.n.map.IImageReceiver;
import t.n.map.common.ImageTileGroup2;
import t.n.map.common.LightWeightTile;
import t.n.map.common.TilePosition;
import t.n.plainmap.util.OldTiledImageRemoveService;

public class TileImageManagerImpl implements ITileImageManager {
	public static final double TILE_SIZE = 256d;
	private final ITiledImageReceiver reader;
	private final ImageTileGroup2 tileGroup;

	private final MapParam mapParam;

	/**
	 * @param observer
	 * @param initialZoomLevel
	 * @param initCenterLonlat アプリ起動時に画面中央に配置する地点の緯度・経度。
	 */
	public TileImageManagerImpl(IFetchingStatusObserver observer, MapParam mapParam, MapPreference pref) {
		this.mapParam = mapParam;
//		OldTiledImageRemoveService.start();
		reader = new TiledImageReaderImpl(observer, pref);
		reader.setMapType(mapParam.getMapType());
		tileGroup = new ImageTileGroup2(mapParam);
	}

	@Override
	public List<TilePosition> getTilePositionList() {
		return tileGroup.getTilePositionList();
	}

	@Override
	public int getTilePositionNearOriginX() {
		return tileGroup.getTilePositionNearOrginX();
	}

	@Override
	public int getTilePositionNearOriginY() {
		return tileGroup.getTilePositionNearOrginY();
	}

	@Override
	public LightWeightTile getTileAt(TilePosition pos) {
		return tileGroup.getTileAt(pos.getX(), pos.getY());
	}

	//(1)緯度経度を指定して移動する場合
	//zoomLevelと表示領域の大きさを使い、以下の計算を行う。(package-info.javaのJavaDocで説明している方法)
	//①中央に配置されるタイル番号と、②このタイルの左上の頂点と、表示領域の中央の座標の位置関係を計算する。
	//これらから、③表示領域(具体的にはMapPanel)の原点（左上)に配置されるタイルと、④表示領域の原点と③のタイルの左上の頂点との位置関係を計算する。
	//番号
	//このアルゴリズムは、アプリの起動時と、ウィンドウをリサイズした時、ズームイン・アウトした時にも使う。
	//④については(2)で必要なので保持する。

	//(2)マウスのドラッグにより地図を移動した場合
	//ドラッグによる移動後、新しい中央の位置の緯度経度を、マウス移動(クリックしない)された点の緯度経度を求めるのと
	//同じ方法で計算し、指定の緯度経度へ移動するメソッドと組み合わせて実装した。

	//③のタイルの番号を表す変数がtileNoAtOrigin{X|Y}
	//④に対応した変数がtilePositionNearOrgin{X|Y}で、単位はpixel

	//TODO 以前生成したImageインスタンスについて、再利用するようにしたい。
	//TODO また、範囲外になったタイルについて、破棄してやらないといつまでもメモリ上に残り続ける。
	//(今のところ以下のとおりMap,Listを初期化しているので、参照されなくなったオブジェクトは勝手に破棄される（と思う)が、再利用するようにすると破棄も行わないとならない。）
	@Override
	public void updateTiles(MapType type) {
		//描画されなくなったImageオブジェクトは明示的にflush()してリソースを解放したほうがよいらしい。
//		if(tileImageMap != null && !tileImageMap.isEmpty()) {
//			for(Entry<Tile, Image> entry : tileImageMap.entrySet()) {
//				Image img = entry.getValue();
//				if(img != null)img.flush();
//			}
//		}
		reader.setMapType(type);

		tileGroup.createTileList();

		//この行があった場合、以下のとおりnewし直した場合、コメントアウトした場合のレスポンスの差は不明。
		//コメントアウトした場合、画面に表示されていない領域のImageオブジェクトが残るが、WeakHashMapを使っているのでメモリが少なくなったときにGCで解放されるはず。
		//弱参照、ソフト参照については以下が参考になる。
		//http://www.ibm.com/developerworks/jp/java/library/j-jtp11225/
		//http://www.ibm.com/developerworks/jp/java/library/j-jtp01246/index.html

//		dumpNoData();
		if(mapParam.getMapRect() == null) return;

		int tileNoX = tileGroup.getTileNoAtOriginX();
		for(int x = tileGroup.getTilePositionNearOrginX(); x < mapParam.getMapRect().width; x+= TILE_SIZE, tileNoX++) {
			int tileNoY = tileGroup.getTileNoAtOriginY();
			for(int y = tileGroup.getTilePositionNearOrginY(); y < mapParam.getMapRect().height; y+= TILE_SIZE, tileNoY++) {
				LightWeightTile tile = new LightWeightTile(mapParam.getZoomLevel(), tileNoX, tileNoY);
				//System.out.println(getClass().getSimpleName() + ": tileno x:" + tileNoX + ", y:" + tileNoY);
				if(!tile.isError()) {
					tileGroup.add(tile);
					//イメージが存在しない場合、”NO DATA"と書かれた画像を代わりに表示する。
					//イメージを読み取り中の場合、"Fetching..."という画像を表示する。
					//非同期で実行し、イメージが読み込めたら差し替える。その他、ネットワーク接続不可だった場合の対処を
					reader.setupTileImage(tile);
				} else {
					System.out.println(tile.getCause());
				}
			}
		}
	}

	@Override
	public void close() {
		if(reader != null) {
			reader.shutdown();
		}
	}

	@Override
	public int getOriginTileNoX() {
		return tileGroup.getTileNoAtOriginX();
	}

	@Override
	public int getOriginTileNoY() {
		return tileGroup.getTileNoAtOriginY();
	}
}