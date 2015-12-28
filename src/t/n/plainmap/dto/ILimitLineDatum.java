package t.n.plainmap.dto;

import java.awt.Color;
import java.util.List;

import t.n.map.common.LonLat;

public interface ILimitLineDatum {
	public static final String TEXT_EXT = ".txt";
	public static final String IMAGE_EXT = ".png";

	String getLabel();

	String getListData();

	String getEventDate();

	String getEventName();

	String getEventTime();

	String getStarName();

	String getStarMagnitude();

	String getK();

	String getNorthOrSouth();

	String getPB();

	String getFilename();

	List<LonLat> getLonlatList();

	Color getColor();

	boolean isVisible();
	void setVisible(boolean b);

	void setHilighted(boolean b);
	boolean isHilighted();

	String getImageFileAbsPath();

}