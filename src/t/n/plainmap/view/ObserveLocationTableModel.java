package t.n.plainmap.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.table.AbstractTableModel;

import lombok.Getter;
import lombok.Setter;
import t.n.map.common.LonLat;

public class ObserveLocationTableModel extends AbstractTableModel {
	private final String[] columnNames = new String [] {
			"選択", "年月日", "現象名", "観測地(修正可)", "経度", "緯度", "地図へ"
	};

	private final Class[] types = new Class [] {
			Boolean.class, String.class, String.class, String.class, String.class, String.class, Object.class, Integer.class
	};

	@Getter @Setter
	private List<Boolean> bList;
	@Getter @Setter
	private List<String> dateList;
	@Getter @Setter
	private List<String> nameList;
	@Getter @Setter
	private List<String> locationList;

	@Getter @Setter
	private List<LonLat> lonlatList;
	@Getter @Setter
	private List<Double> longitudeList;
	@Getter @Setter
	private List<Double> latitudeList;
	@Getter @Setter
	private List<Integer> zoomLevelList;

	private final List<JButton> buttonList;

	public ObserveLocationTableModel() {
		super();

		bList = new ArrayList<>();
		dateList = new ArrayList<>();
		nameList = new ArrayList<>();
		locationList = new ArrayList<>();
		lonlatList = new ArrayList<>();
		longitudeList = new ArrayList<>();
		latitudeList = new ArrayList<>();
		buttonList = new ArrayList<>();
		zoomLevelList = new ArrayList<>();
	}

	public String getClipboardData() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < bList.size(); i++) {
			sb.append(dateList.get(i));
			sb.append(", ");
			sb.append(nameList.get(i));
			sb.append(", ");
			sb.append(locationList.get(i));
			sb.append(", ");
			sb.append(longitudeList.get(i));
			sb.append(", ");
			sb.append(latitudeList.get(i));
			sb.append("\n");
		}
		return sb.toString();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return types[columnIndex];
	}

	@Override
	public int getRowCount() {
		return bList.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	//最後の２つは非表示で、データを保持するために使う。
	private static int[] columnWidth = {70, 100, 100, 100, 220, 220, 80, 0, 0};

	public int getColumnPrefferedSize(int columnIndex) {
		return columnWidth[columnIndex];
	}

	@Override
	public String getColumnName(int columnIndex) {
		return columnNames[columnIndex];
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return (columnIndex == 0 || columnIndex == 3);
	}

	public void addRow(boolean b, String eventDate, String eventName, String observeLocation, LonLat lonlat, int zoomLevel) {
		bList.add(b);
		dateList.add(eventDate);
		nameList.add(eventName);
		locationList.add(observeLocation);
		lonlatList.add(lonlat);

//		String longitude = LonLatUtil.getLongitudeJapaneseString(lonlat.getLongitude());
//		longitudeList.add(longitude);
		longitudeList.add(lonlat.getLongitude());
//		String latitude = LonLatUtil.getLatitudeJapaneseString(lonlat.getLatitude());
//		latitudeList.add(latitude);
		latitudeList.add(lonlat.getLatitude());

		JButton btn = new JButton();
		btn.setName("dummy");
		buttonList.add(btn);
		zoomLevelList.add(zoomLevel);
		fireTableDataChanged();
	}

	public void removeRow(int row) {
		bList.remove(row);
		dateList.remove(row);
		nameList.remove(row);
		locationList.remove(row);
		lonlatList.remove(row);
		longitudeList.remove(row);
		latitudeList.remove(row);
		buttonList.remove(row);
		zoomLevelList.remove(row);

		fireTableRowsDeleted(row, row);
	}

	public static final int CHECKBOX_COL = 0;
	public static final int LONTITUDE_COL = 4;
	public static final int LATITUDE_COL = 5;
	public static final int GOTO_MAP_COL = 6;
	public static final int ZOOM_LEVEL_COL = 7;
	public static final int LONLAT_COL = 8;

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if(rowIndex < bList.size()) {
			switch(columnIndex) {
			case CHECKBOX_COL:
				return bList.get(rowIndex);
			case 1:
				return dateList.get(rowIndex);
			case 2:
				return nameList.get(rowIndex);
			case 3:
				return locationList.get(rowIndex);
			case LONTITUDE_COL:
				return longitudeList.get(rowIndex);
			case LATITUDE_COL:
				return latitudeList.get(rowIndex);
			case GOTO_MAP_COL:
				JButton btn;
				if(rowIndex < buttonList.size()) {
					btn = buttonList.get(rowIndex);
				} else {
					btn = new JButton();
					buttonList.add(btn);
				}
				return btn;
			case ZOOM_LEVEL_COL:
				return zoomLevelList.get(rowIndex);
			case LONLAT_COL:
				return lonlatList.get(rowIndex);
			default:
				return null;
			}
		}

		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		switch(columnIndex) {
		case 0:
			bList.set(rowIndex, (Boolean)aValue);
			break;
//		case 1:
//			dateNameList.set(rowIndex, (String)aValue);
		case 2:
			locationList.set(rowIndex, (String)aValue);
//		case 7:
//			zoomLevelList.set(rowIndex, (Integer)aValue);
		}
	}
}
