package t.n.map;

import t.n.map.common.LightWeightTile;

public interface IFetchingStatusObserver {
	public void notifyStartFetching(String uri);
	public void notifyFetchCompleted(LightWeightTile tile, boolean isAllDownloadDone);
	public void notifyErrorFetching();
}
