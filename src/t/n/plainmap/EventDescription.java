package t.n.plainmap;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.GeneralPath;

import t.n.plainmap.dto.ILimitLineDatum;

public class EventDescription implements Comparable<EventDescription> {
	private static final int LEADER_LINE_DELTA_X = 100;
	private static float dash[] = {10.0f, 3.0f};
	private static Stroke drawingStroke = new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, dash, 0f);

	private final Point firstPointCoord;
	private final Point descPointCoord;
	private final ILimitLineDatum datum;
	private final String label;

	private final GeneralPath line = new GeneralPath();

	public EventDescription(Point screenCoord, ILimitLineDatum datum) {
		this.firstPointCoord = screenCoord;
		this.descPointCoord = new Point(firstPointCoord.x, firstPointCoord.y);
		this.datum = datum;
		//現象名、日付、時刻など
		label = datum.getLabel();
	}

	public Rectangle getRect(Graphics2D g2d) {
		Font font = new Font(Font.SERIF, Font.PLAIN, 14);
		FontRenderContext frc = g2d.getFontRenderContext();
		TextLayout layout = new TextLayout(label, font, frc);
		Rectangle textBack = layout.getBounds().getBounds();
//		Bound bound = new Bound(screenCoord.x, screenCoord.y, textBack.width, textBack.height);
		textBack.setBounds(firstPointCoord.x, firstPointCoord.y, textBack.width, textBack.height);
		return textBack;
	}

	public void move(int x, int y) {
		descPointCoord.x = x + LEADER_LINE_DELTA_X;
		descPointCoord.y = y;
	}

	public void draw(Graphics2D g2d) {
		g2d.setColor(datum.getColor());
		//引き出し線
		g2d.setStroke(drawingStroke);
		line.moveTo(firstPointCoord.x, firstPointCoord.y);
		line.lineTo(descPointCoord.x, descPointCoord.y);
		g2d.draw(line);
        //文字列
		g2d.drawString(label, descPointCoord.x, descPointCoord.y);
	}

	@Override
	public int compareTo(EventDescription obj) {
		int result = 0;
		int opponent = obj.firstPointCoord.y;
		result = opponent - this.firstPointCoord.y;
		//これでソート順が昇順(小さい方が前)になる
		return -result;
	}
}
