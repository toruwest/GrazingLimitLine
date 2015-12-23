package t.n.plainmap;

import java.io.File;

import t.n.map.common.LightWeightTile;

public interface ITiledImageReceiver {

	void receiveImageData(LightWeightTile tile, File tileImageFile, ImageDataStatus status);

	void setupTileImage(LightWeightTile tile);

	void setMapType(MapType mapType);

	void shutdown();

}
