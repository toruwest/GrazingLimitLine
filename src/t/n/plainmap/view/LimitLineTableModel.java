package t.n.plainmap.view;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

import t.n.plainmap.dto.ILimitLineDatum;
import t.n.plainmap.dto.LimitLineDatumJCLO;

public class LimitLineTableModel extends AbstractTableModel {
	private final List<Boolean> bList;
	private final List<Boolean> hilightButtonList;
	private final ArrayList<JButton> moonEdgeDiagramButtonList;
	private List<ILimitLineDatum> limitLineData;
	protected static final int BLIST_COL = 0;
	protected static final int HILIGHT_COL = 1;
	protected static final int SHOW_EDGE_COL = 2;
	public static final int EVENT_NAME_COL = 3;

	// 限界線と表の対応が分かるように、線の色や種類（破線、一点鎖線など）で区別する。表にも凡例を出す。
	// また、地図上の線の近くに、吹き出しにより名称と年月日を表示する。
	private final String[] columnNames = new String [] {
			"表示", "強調", "月縁図", "現象名", "年月日", "時間", "恒星", "等級", "k", "北/南", "PB", "ファイル名"
	};

	private static int[] columnPrefferedWidth  = {120, 120, 220, 300, 300, 300, 100, 50, 50, 60, 50, 200};
	private static int[] columnMaxWidth        = {120, 120, 220, 300, 390, 350, 150, 50, 50, 60, 50, 200};

	private final Class[] types = new Class [] {
			Boolean.class, Boolean.class, JButton.class, String.class, String.class, String.class, String.class, String.class, String.class, Object.class, Object.class, String.class
	};

	public LimitLineTableModel() {
		super();
		bList = new ArrayList<>();
		hilightButtonList = new ArrayList<>();
		moonEdgeDiagramButtonList = new ArrayList<>();
	}

	@Override
	public int getRowCount() {
		return bList.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return types[columnIndex];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ILimitLineDatum row = limitLineData.get(rowIndex);

		switch(columnIndex) {
		case 0:
			return bList.get(rowIndex);
		case 1:
			return hilightButtonList.get(rowIndex);
		case 2:
			return moonEdgeDiagramButtonList.get(rowIndex);
		case 3:
			return row.getEventName();
		case 4:
			return row.getEventDate();
		case 5:
			return row.getEventTime();
		case 6:
			return row.getStarName();
		case 7:
			return row.getStarMagnitude();
		case 8:
			// K (輝面比だとすると、luminanceRatio)
			return row.getK();
		case 9:
			return row.getNorthOrSouth();
		case 10:
			return row.getPB();
		case 11:
			return row.getFilename();
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object obj, int rowIndex, int columnIndex) {

	}

	public void setShowState(int row, boolean b) {
		bList.set(row, b);
		fireTableCellUpdated(row, BLIST_COL);
	}

	public void setAllCheckState(boolean b) {
		for(int i = 0; i < bList.size(); i++) {
			bList.set(i, b);
		}
		fireTableDataChanged();
	}

	public void setHilightState(int row, boolean b) {
		hilightButtonList.set(row, b);
		fireTableCellUpdated(row, HILIGHT_COL);
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex <= 1) {
			return true;
		} else {
			return false;
		}
	}

	public int getColumnPrefferedSize(int columnIndex) {
		return columnPrefferedWidth[columnIndex];
	}

	public int getColumnMaxSize(int columnIndex) {
		return columnMaxWidth[columnIndex];
	}

	public void setData(List<ILimitLineDatum> limitLineData) {
		this.limitLineData = limitLineData;
		for(int i = 0; i < limitLineData.size(); i++) {
			bList.add(true);

			hilightButtonList.add(false);

			JButton showDiagramBtn = new JButton("表示");
			showDiagramBtn.setOpaque(true);
			showDiagramBtn.setName("show");
			moonEdgeDiagramButtonList.add(showDiagramBtn);
		}
	}

	public Color getColor(int row) {
		return limitLineData.get(row).getColor();
	}

	public String getFilename(int row) {
		return limitLineData.get(row).getFilename();
	}

}
