package t.n.map;

public interface IAltitudeFetchStatusObserver {

	void notifySuccess(double lon, double lat, double altitude);

	void notifyNotFound(double lon, double lat);

}
