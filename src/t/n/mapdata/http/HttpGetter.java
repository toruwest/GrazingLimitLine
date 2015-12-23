package t.n.mapdata.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.concurrent.Future;

import javax.swing.SwingUtilities;

import t.n.map.IFetchingStatusObserver;
import t.n.map.common.LightWeightTile;
import t.n.plainmap.IProxyConfigurationChangedListener;
import t.n.plainmap.ITiledImageReceiver;
import t.n.plainmap.ImageDataStatus;
import t.n.plainmap.MapPreference;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Response;
import com.ning.http.client.providers.jdk.JDKAsyncHttpProvider;

public class HttpGetter implements IProxyConfigurationChangedListener {

	private static final String HTTP_PROXY_PORT = "http.proxyPort";
	private static final String HTTP_PROXY_HOST = "http.proxyHost";
	private final IFetchingStatusObserver observer;
	private final ITiledImageReceiver imageReceiver;
	protected File heightSavingDir;
	protected File imageSavingDir;
	//FIXME 正常にプロキシが動作しないので、Systemプロパティを設定する方法を試している。これもまだ動いていない。
	private ProxyServer proxy;
	private final MapPreference pref;
	private static AsyncHttpClient httpClient;
	private static AsyncHttpClientConfig config;

	static {
		httpClient = getAsyncHttpClient();//new AsyncHttpClientConfig.Builder().build());
	}

	private static AsyncHttpClient getAsyncHttpClient() {
		if (config == null) {
			config = new AsyncHttpClientConfig.Builder().setAllowPoolingConnections(true).setMaxConnections(5).build();
		}
		return new AsyncHttpClient(new JDKAsyncHttpProvider(config), config);
	}

	public HttpGetter(IFetchingStatusObserver observer, File imageSavingDir, ITiledImageReceiver imageReceiver, MapPreference pref) {
		this.observer = observer;
		this.imageSavingDir = imageSavingDir;
		this.imageReceiver = imageReceiver;
		this.pref = pref;
		//MapPreferenceクラスのセッターが呼ばれたら、以下のリスナー経由でこのクラスが呼ばれるようにした。
		pref.addProxyConfigurationChangedListener(this);
		reconfigureProxy();
	}

//	public HttpGetter(IFetchingStatusObserver observer, File imageSavingDir, ITiledImageReceiver imageReceiver, String proxyHost, int proxyPort) {
//		this.observer = observer;
//		this.imageSavingDir = imageSavingDir;
//		this.imageReceiver = imageReceiver;
//		proxy = new ProxyServer(proxyHost, proxyPort);
//	}

	@Override
	public void reconfigureProxy() {
		if(pref.isUseProxy()) {
			//FIXME 以下の方法では正常にプロキシが動作しないので、Systemプロパティを設定する方法を試している。これもまだ動いていない。
			//余談だけど、portがintということは32bitなので、IPV6には未対応ということか？
			proxy = new ProxyServer(pref.getProxyHostname(), (int)pref.getProxyPort());
			System.setProperty(HTTP_PROXY_HOST, pref.getProxyHostname());
			System.setProperty(HTTP_PROXY_PORT, String.valueOf(pref.getProxyPort()));
		} else {
//			proxy = null;
			//unset
			//FIXME http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.htmlに書いてある方法なのに、例外が投げられる。
//			System.setProperty(HTTP_PROXY_HOST, null);
//			System.setProperty(HTTP_PROXY_PORT, null);
		}
		//Systemプロパティを更新したあとは、以下を再実行して変更が適用された状態にする。
		httpClient = HttpGetter.getAsyncHttpClient();
		System.out.println("proxying :" + System.getProperty(HTTP_PROXY_HOST) + ":" + System.getProperty(HTTP_PROXY_PORT));
	}

	public void setImageSavingDir(File imageSavingDir) {
		this.imageSavingDir = imageSavingDir;
	}

	protected void startFetching(final String uri, final File localFile, final LightWeightTile tile) {
		System.out.println("EDT:" + SwingUtilities.isEventDispatchThread() + ": will fetch: " + uri);
		if(observer != null) {
			observer.notifyStartFetching(uri);
		}
		//以下はレスポンスを待たずに直ちにリターンしてくる。
		final Future<Integer> future;
		//FIXME 以下の方法では正常にプロキシが動作しないので、Systemプロパティを設定する方法を試している。これもまだ動いていない。
//		if(proxy == null) {
//			future = httpClient.prepareGet(uri).execute(new MyAsyncCompletionHandler(localFile, tile));
//		} else {
//			future = httpClient.prepareGet(uri).setProxyServer(proxy).execute(new MyAsyncCompletionHandler(localFile, tile));
//		}
		future = httpClient.prepareGet(uri).execute(new MyAsyncCompletionHandler(localFile, tile));
		//FIXME "Too many connections 5"というメッセージが出ているが、レスポンスがあったらしばらく待ち、リクエストするようにできないか？
		//
//		future.
//		config.get
	}

	public void shutdown() {
		if(httpClient != null) httpClient.close();
	}

	class MyAsyncCompletionHandler extends AsyncCompletionHandler<Integer> {
		private final File localFile;
		private final LightWeightTile tile;
		private boolean isMkdirFailed = false;

		public MyAsyncCompletionHandler(File localFile, LightWeightTile tile) {
			this.localFile = localFile;
			this.tile = tile;
		}

		@Override
		public Integer onCompleted(Response response) {
			int stat = 0;
			isMkdirFailed = false;

			try {
				// レスポンスヘッダーの取得
				stat = response.getStatusCode();
				System.out.println("onComplete(): " + response.getUri() + ": " + stat);
				if(stat == 200) {
					// ファイルへの保存
					InputStream is;
					is = response.getResponseBodyAsStream();
					File parentDir = localFile.getParentFile();
					boolean isMkdirSuccess = false;
					boolean isParentDirExists = parentDir.exists();
					if(parentDir != null && !isParentDirExists) {
						isMkdirSuccess = parentDir.mkdirs();
					}
					if(isParentDirExists || !isParentDirExists && isMkdirSuccess) {
						FileOutputStream out = new FileOutputStream(localFile, false);
						int b;
						while((b = is.read()) != -1){
							out.write(b);
						}
						out.close();
						is.close();
					} else {
						//TODO 格納フォルダが作れなかったので、呼び出し元へ通知。
						//TODO ディスクがいっぱいで書き込めない状況への対処。ディスクの使用を制限する。
						isMkdirFailed  = true;
					}
				}
				if(isMkdirFailed) {
					//TODO heightDataReceiver.receiveHeighTextFile(tile, localFile, HeightDataStatus.success);
				} else if(stat == 200) {
					imageReceiver.receiveImageData(tile, localFile, ImageDataStatus.success);
				} else if(stat == 404){
					//イメージが存在しない場合、”NO DATA"と書かれた画像を代わりに表示する。
					imageReceiver.receiveImageData(tile, localFile, ImageDataStatus.noData);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return stat;
		}

		@Override
		public void onThrowable(Throwable t) {
			//サーバーに接続できなかった場合、タイムアウトになり、ここが実行される。
			//"Connection Reset"となる場合と,"No response received after 60000"となる場合がある。後者はネットワークが接続できない場合と、
			//リクエストを出し過ぎて待たされている場合がある。どちらなのか区別して、前者なら以降のアクセスを中止したい。
			//なお、後者については、getAsyncHttpClient()で同時アクセス数を制限しているつもりなのにお構いなしにリクエストされているようだ。
			if(t instanceof ConnectException) {
				System.err.println("connection failed:" + t.getMessage());
			} else {
				System.err.println(t.getMessage());
			}
			imageReceiver.receiveImageData(tile, localFile, ImageDataStatus.error);
		}
	}
}