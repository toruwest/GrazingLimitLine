package t.n.plainmap;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;

import t.n.plainmap.dto.ILimitLineDatum;
import t.n.plainmap.dto.LimitLineDatumJCLO;

public class EventDescrition {
	private static final int LEADER_LINE_DELTA_Y = 10;
	private static final int LEADER_LINE_DELTA_X = 10;
	private final Point screenCoord;
	private final ILimitLineDatum datum;
	GeneralPath line = new GeneralPath();
	private final String label;

	public EventDescrition(Point screenCoord, int moveX, int moveY, ILimitLineDatum datum) {
		this.screenCoord = screenCoord;
		this.datum = datum;
		line.moveTo(screenCoord.x + moveX, screenCoord.y + moveY);
		line.lineTo(screenCoord.x + LEADER_LINE_DELTA_X + moveX, screenCoord.y + LEADER_LINE_DELTA_Y + moveY);
		//現象名、日付、時刻など
		label = datum.getLabel();
	}

	public void draw(Graphics2D g2d) {
		//引き出し線
		g2d.draw(line);
		//矩形で囲む？
//		g2d.draw3DRect(screenCoord.x + LEADER_LINE_DELTA_X, screenCoord.y + LEADER_LINE_DELTA_Y, LEADER_LINE_WIDTH, LEADER_LINE_HEIGHT, true);
		//文字列
		g2d.drawString(label, screenCoord.x + LEADER_LINE_DELTA_X, screenCoord.y + LEADER_LINE_DELTA_Y);
	}

}
