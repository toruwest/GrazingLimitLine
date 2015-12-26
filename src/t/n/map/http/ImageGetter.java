package t.n.map.http;

import java.io.File;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.ning.http.client.ListenableFuture;

import lombok.Getter;
import t.n.map.IFetchingStatusObserver;
import t.n.map.common.LightWeightTile;
import t.n.plainmap.ITiledImageReceiver;
import t.n.plainmap.MapPreference;
import t.n.plainmap.MapType;
import t.n.plainmap.util.TiledMapUtil2;
import static java.util.concurrent.TimeUnit.*;

//HttpClientで"Too many connections 5"というメッセージが出ていたので、負荷を低減するため、控えめにリクエストする。
// getImageAt()が呼ばれたら、queueに追加する。queueから取り出して実際のリクエストを行うのは、定期的に起動する別のワーカースレッドとする。
//このときに、HttpGetterでレスポンス待ちのリクエストの数を考慮する。
//ワーカースレッドがqueueからデータを取り出したら、queueからそのデータは削除される。
//levelが切り替わったら、それより前にqueueにたまっていたリクエストはキャンセルする。
//このときに、levelが連続的に切り替えられる可能性があることを考慮する。
//ワーカースレッドの起動はコンストラクタで、停止はshutdown()メソッドを呼んで行う。

public class ImageGetter {
	//以下はUQ WiMAXと、Firefox 43.0.2(2015年12月の最新)の組み合わせでレスポンスを測って決めた。1回目は300ミリ秒、2回目以降は150ミリ秒程度。
	private static final int EXEC_PERIOD = 300;
	private static final int INIT_QUEUE_SIZE = 200;
	private static final boolean DEBUG = false;

	@Getter
	public class Param {
		private final String uri;
		private final File imageFile;
		private final LightWeightTile tile;

		public Param(String uri, File imageFile, LightWeightTile tile) {
			super();
			this.uri = uri;
			this.imageFile = imageFile;
			this.tile = tile;
		}
	}

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	private final LinkedBlockingQueue<Param> queue;
	private final HttpGetter httpGetter;
	private ScheduledFuture<?> getterHandle;
	private int zoomLevel;
	protected List<ListenableFuture> futures = new ArrayList<>();

	public ImageGetter(IFetchingStatusObserver observer, File heightSavingDir, ITiledImageReceiver receiver, MapPreference pref) {
		queue = new LinkedBlockingQueue<>(INIT_QUEUE_SIZE);
		httpGetter = new HttpGetter(observer, heightSavingDir, receiver, pref);
		initQueueHandler();
	}

	public void getImageAt(MapType type, LightWeightTile tile, File imageFile) {
		int newZoomLevel = tile.getZoomLevel();
		if(this.zoomLevel != newZoomLevel) {
			if(DEBUG) {
				System.out.println("clear. queue size:" + queue.size());
			}
			synchronized (this) {
				this.zoomLevel = newZoomLevel;
				queue.clear();
				//ここでfutures.clear()すると、レスポンス待ちのリクエストの情報がなくなり、
				//MAX_CONNECTIONを超えてアクセスすることになる。
			}
		}

		String uri = TiledMapUtil2.generateImageURI(type, tile);
		Param param = new Param(uri, imageFile, tile);
		synchronized (this) {
			queue.offer(param);
		}
	}

	public void setImageSavingDir(File currentImageSavingDir) {
		httpGetter.setImageSavingDir(currentImageSavingDir);
	}

	public void shutdown() {
		getterHandle.cancel(true);
		httpGetter.shutdown();
	}

	private void initQueueHandler() {
		final Runnable getter = new Runnable() {
			@Override
			public void run() {
				//注意：synchronizedの範囲は狭められない。queue.size()を外すと誤動作する。
				synchronized (this) {
					short onGoingCount = 0;
					for(ListenableFuture<Integer> lf : futures) {
						if(!lf.isDone()) onGoingCount++;
					}
					if(DEBUG) {
						System.out.println("poll. queue size:" + queue.size() + ", on going:" + onGoingCount);
					}
					if(queue.size() == 0) {
						//これをどこかでやっておかないとサイズが無限に増えていく。
						futures.clear();
					} else {
						for(int i = 0; i < HttpGetter.MAX_CONNECTIONS - onGoingCount; i++) {
							Param param = queue.poll();
							if(param != null) {
								ListenableFuture<Integer> future = httpGetter.startFetching(param.uri, param.imageFile, param.tile);
								futures.add(future);
							}
						}
					}
				}
			}
		};
		getterHandle = scheduler.scheduleAtFixedRate(getter, EXEC_PERIOD, EXEC_PERIOD, MILLISECONDS);
	}
}
