package t.n.map.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import t.n.map.IFetchingStatusObserver;
import t.n.map.IAltitudeFetchStatusObserver;
import t.n.map.common.LightWeightTile;
import t.n.plainmap.IProxyConfigurationChangedListener;
import t.n.plainmap.ITiledImageReceiver;
import t.n.plainmap.ImageDataStatus;
import t.n.plainmap.MapPreference;
import t.n.plainmap.view.dialog.Util;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Response;
import com.ning.http.client.providers.jdk.JDKAsyncHttpProvider;

public class AltitudeDataGetter {

	protected static final int MAX_CONNECTIONS = 1;
	private static final String HTTP_PROXY_PORT = "http.proxyPort";
	private static final String HTTP_PROXY_HOST = "http.proxyHost";
	private static final boolean DEBUG = false;
	private final IAltitudeFetchStatusObserver observer;
	//FIXME 正常にプロキシが動作しないので、Systemプロパティを設定する方法を試している。これもまだ動いていない。
	private ProxyServer proxy;
	private static AsyncHttpClient httpClient;
	private static AsyncHttpClientConfig config;

	static {
		httpClient = getAsyncHttpClient();
	}

	private static AsyncHttpClient getAsyncHttpClient() {
		if (config == null) {
			config = new AsyncHttpClientConfig.Builder().setAllowPoolingConnections(true).setMaxConnections(MAX_CONNECTIONS).build();
			//TODO ここでconfigに対しproxyの設定ができるかもしれない。
		}
		return new AsyncHttpClient(new JDKAsyncHttpProvider(config), config);
	}

	public AltitudeDataGetter(IAltitudeFetchStatusObserver observer) {
		this.observer = observer;
	}

	public void startFetching(float lon, float lat) {
		String uri = Util.generateURI(lon, lat);

		//以下はレスポンスを待たずに直ちにリターンしてくる。本来はFutureを返すが使っていない。
		httpClient.prepareGet(uri).execute(new MyAsyncCompletionHandler(lon, lat));
	}

	public void shutdown() {
		if(httpClient != null) httpClient.close();
	}

	class MyAsyncCompletionHandler extends AsyncCompletionHandler<Integer> {
		private final float lon;
		private final float lat;

		public MyAsyncCompletionHandler(float lon, float lat) {
			this.lon = lon;
			this.lat = lat;
		}

		@Override
		public Integer onCompleted(Response response) {
			int stat = 0;
			try {
				// レスポンスヘッダーの取得
				stat = response.getStatusCode();
				if(DEBUG) {
					System.out.println("onComplete(): " + response.getUri() + ": " + stat);
				}
				if(stat == 200) {
					InputStream is;
					is = response.getResponseBodyAsStream();
					//TODO プレインテキストとして受け取る
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					int b;
					while((b = is.read()) != -1){
						out.write(b);
					}
					out.close();
					is.close();
					String text = new String(out.toByteArray());
					float value = parseAltitude(text);
					observer.notifySuccess(lon, lat, value);
				} else if(stat == 404){
					observer.notifyNotFound(lon, lat);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return stat;
		}

		@Override
		//サーバーに接続できなかった場合、タイムアウトになり、ここが実行される。
		public void onThrowable(Throwable t) {
			//"Connection Reset"となる場合と,"No response received after 60000"となる場合がある。後者はネットワークが接続できない場合と、
			//リクエストを出し過ぎて待たされている場合がある。どちらなのか区別して、前者なら以降のアクセスを中止したい。
			//なお、後者については、getAsyncHttpClient()で同時アクセス数を制限しているつもりなのにお構いなしにリクエストされているようだ。
			if(t instanceof ConnectException) {
				System.err.println("connection failed:" + t.getMessage());
			} else {
				System.err.println(t.getMessage());
			}
		}
	}

//	private static String arg0 = "({\"elevation\":949.3,\"hsrc\":\"5m\uff08\u30ec\u30fc\u30b6\uff09\"})";
//	private static String arg1 = "({\"elevation\":\"-----\",\"hsrc\":\"-----\"})";
	private static Pattern p = Pattern.compile("\\(\\{\\\"elevation\\\":(\\S+),\\\"hsrc\\\":\\\"(\\S+)\\\"\\}\\)");

//	public static void main(String... args) {
//		Float result0 = parseAltitude(arg0);
//		System.out.println("result:" + result0);
//
//		Float result1 = parseAltitude(arg1);
//		System.out.println("result:" + result1);
//	}

	private static Float parseAltitude(String arg) {
		Matcher m = p.matcher(arg);

		Float result = Float.NaN;
		if(m.matches()) {
			String extraced = m.group(1);
			if(!extraced.equals("\"-----\"")) {
				result = Float.parseFloat(extraced);
			}
		}
		return result;
	}
}