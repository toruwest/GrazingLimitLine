package t.n.map;

import java.io.File;

import t.n.map.common.LightWeightTile;
import t.n.mapdata.http.HttpGetter;
import t.n.plainmap.ITiledImageReceiver;
import t.n.plainmap.MapPreference;
import t.n.plainmap.MapType;
import t.n.plainmap.util.TiledMapUtil2;

public class ImageGetter extends HttpGetter {

	public ImageGetter(IFetchingStatusObserver observer, File heightSavingDir, ITiledImageReceiver receiver, MapPreference pref) {
		super(observer, heightSavingDir, receiver, pref);
	}

	public void getImageAt(MapType type, LightWeightTile tile, File imageFile) {
		String uri = TiledMapUtil2.generateImageURI(type, tile);
		startFetching(uri, imageFile, tile);
	}

}
