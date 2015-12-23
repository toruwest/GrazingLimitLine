package t.n.map.common;


public abstract class AbstractTile {

	protected int tileNoX;
	protected int tileNoY;
	protected double scale;
	protected double leftUpperLon;
	protected double leftUpperLat;
	protected double rightDownLon;
	protected double rightDownLat;

	public AbstractTile(int tileNoX, int tileNoY) {
		this.tileNoX = tileNoX;
		this.tileNoY = tileNoY;
	}

//	public AbstractTile(int zoomLevel, LonLat lonlat) {
//		computeScale(zoomLevel);
//		tileNoX = TileUtil.getTileNoX(scale, lonlat);
//		tileNoY = TileUtil.getTileNoY(scale, lonlat);
//	}
//
//	protected void computeScale(int zoomLevel) {
//		scale = Math.pow(2, zoomLevel);
//	}

	public double getScale() {
		return scale;
	}

	public int getTileNoX() {
		return tileNoX;
	}

	public int getTileNoY() {
		return tileNoY;
	}

	public double getLeftUpperLon() {
		return leftUpperLon;
	}

	public double getLeftUpperLat() {
		return leftUpperLat;
	}

	public double getRightDownLon() {
		return rightDownLon;
	}

	public double getRightDownLat() {
		return rightDownLat;
	}
}
