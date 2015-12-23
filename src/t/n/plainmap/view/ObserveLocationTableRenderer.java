package t.n.plainmap.view;

import static t.n.plainmap.view.ObserveLocationTableModel.GOTO_MAP_COL;
import static t.n.plainmap.view.ObserveLocationTableModel.LATITUDE_COL;
import static t.n.plainmap.view.ObserveLocationTableModel.LONTITUDE_COL;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import t.n.map.LonLatFormat;
import t.n.map.common.util.LonLatUtil;

public class ObserveLocationTableRenderer extends DefaultTableCellRenderer {
	private final JLabel label = new JLabel();
    // 行の高さ
    private int rowHeight = 0;
	private LonLatFormat lonlatFormat;

	public ObserveLocationTableRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
//		Component comp = (JButton)value;
		Component comp = null;
		Color defaultBackgroundColor = table.getBackground();

		if(column == GOTO_MAP_COL) {
			//ボタンをクリックしたら該当地点へ移動する。
			comp = new JButton("移動");
		}

		if(column == LONTITUDE_COL) {
			String lon = null;
			switch(lonlatFormat) {
			case degMinSec:
				lon = LonLatUtil.getLongitudeJapaneseString((Double)value);
				break;
			case jissu:
				lon = LonLatUtil.getFormattedLongitudeString((Double)value);
				break;
			}
			comp = new JLabel(lon);
		}

		if(column == LATITUDE_COL) {
			String lat = null;
			switch(lonlatFormat) {
			case degMinSec:
				lat = LonLatUtil.getLatitudeJapaneseString((Double)value);
				break;
			case jissu:
				lat = LonLatUtil.getFormattedLatitudeString((Double)value);;
				break;
			}
			comp = new JLabel(lat);
		}

        //行の高さをそろえる。
        Dimension dimension = comp.getPreferredSize();
        if(dimension.height > rowHeight) {
            table.setRowHeight(dimension.height);
            rowHeight = dimension.height;
        }

		comp.setBackground(defaultBackgroundColor);
		return comp;
	}

	public void setLonLatFormat(LonLatFormat lonlatFormat) {
		this.lonlatFormat = lonlatFormat;
	}

}
