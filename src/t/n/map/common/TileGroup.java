package t.n.map.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import t.n.plainmap.MapParam;

public class TileGroup {
	protected int tileNoAtOriginX;
	protected int tileNoAtOriginY;
	protected final Set<Integer> tileNoXset;
	protected final Set<Integer> tileNoYset;

	protected boolean useAsterDem;
	protected final Map<TilePosition, LightWeightTile> tileMap;
	protected int tileNoXmin = 0;
	protected int tileNoYmin = 0;
	protected int tileNoXmax = 0;
	protected int tileNoYmax = 0;

	protected MapParam mapParam;

	public TileGroup() {
		tileMap = new HashMap<>();
		tileNoXset = new TreeSet<>();
		tileNoYset = new TreeSet<>();
	}

	public TileGroup(MapParam mapParam) {
		this();
		this.mapParam = mapParam;
	}

	protected int getZoomLevel() {
		return mapParam.getZoomLevel();
	}

	protected LonLat getCenterLonlat() {
		return mapParam.getCenterLonLat();
	}

	public void check() {

		if(tileNoAtOriginX < 0) {
			System.out.println();
		}
		if(tileNoAtOriginY < 0) {
			System.out.println();
		}
	}

	public List<Integer> getXTilesList() {
		List<Integer> result = new ArrayList<>();
		Iterator<Integer> iterator = tileNoXset.iterator();
		while(iterator.hasNext()) {
			result.add(iterator.next());
		}
		return result;
	}

	public List<Integer> getYTilesList() {
		List<Integer> result = new ArrayList<>();
		Iterator<Integer> iterator = tileNoYset.iterator();
		while(iterator.hasNext()) {
			result.add(iterator.next());
		}
		return result;
	}

	protected void reset() {
		tileNoXset.clear();
		tileNoYset.clear();
		tileNoXmin = Integer.MAX_VALUE;
		tileNoXmax = Integer.MIN_VALUE;
		tileNoYmin = Integer.MAX_VALUE;
		tileNoYmax = Integer.MIN_VALUE;
	}

	public int getTileNoAtOriginY() {
		return tileNoAtOriginY;
	}

	public int getTileNoAtOriginX() {
		return tileNoAtOriginX;
	}

	public int getRegionWidth() {
		return mapParam.getMapRect().width;
	}

	public int getRegionHeight() {
		return mapParam.getMapRect().height;
	}

	public int getTileSize() {
		return mapParam.getTileSize();
	}

	public int getTileCountX() {
		return tileNoXset.size();
	}

	public int getTileCountY() {
		return tileNoYset.size();
	}

	public void add(LightWeightTile tile) {
		int x = tile.getTileNoX();
		int y = tile.getTileNoY();
		TilePosition position = new TilePosition(x, y);
		tileMap.put(position, tile);
		tileNoXset.add(x);
		tileNoYset.add(y);
		tileNoXmin = Math.min(x, tileNoXmin);
		tileNoYmin = Math.min(y, tileNoYmin);
		tileNoXmax = Math.max(x, tileNoXmax);
		tileNoYmax = Math.max(y, tileNoYmax);
	}

	public LightWeightTile getTileAt(int tileNoX, int tileNoY) {
		Integer[] xarray = tileNoXset.toArray(new Integer[tileNoXset.size()]);
		int minx = xarray[0];
		int maxx = xarray[xarray.length - 1];
		Integer[] yarray = tileNoYset.toArray(new Integer[tileNoYset.size()]);
		int miny = yarray[0];
		int maxy = yarray[yarray.length - 1];
		if(minx <= tileNoX && tileNoX <= maxx && miny <= tileNoY && tileNoY <= maxy) {
			TilePosition position = new TilePosition(tileNoX, tileNoY);
			return tileMap.get(position);
		} else {
			return null;
		}
	}

	public List<TilePosition> getTilePositionList() {
		List<TilePosition> result = new ArrayList<>();
		for(int tileNoY : getYTilesList()) {
			for(int tileNoX : getXTilesList()) {
				result.add(new TilePosition(tileNoX, tileNoY));
			}
		}

		return result;
	}
}
