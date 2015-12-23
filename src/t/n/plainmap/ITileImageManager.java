package t.n.plainmap;

import java.util.List;

import t.n.map.common.LightWeightTile;
import t.n.map.common.TilePosition;

public interface ITileImageManager {
	public void updateTiles(MapType type);

	LightWeightTile getTileAt(TilePosition pos);
	List<TilePosition> getTilePositionList();

	int getTilePositionNearOriginX();
	int getTilePositionNearOriginY();
	int getOriginTileNoX();
	int getOriginTileNoY();

	void close();

}
