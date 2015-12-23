package t.n.plainmap.util;

import java.awt.Color;

public class LimitLineColorUtil {

	//Color.BLUE, Color.GREEN, ORANGEは見にくいので使わない。
	private static final Color[] colors = {Color.BLACK, Color.RED, Color.MAGENTA, Color.PINK, Color.BLUE, Color.GREEN, Color.ORANGE};

	//線の数の方が色数より多い場合、色は再利用される。
	public static Color getLineColor(int index) {
		return colors[index % colors.length];
	}

}
