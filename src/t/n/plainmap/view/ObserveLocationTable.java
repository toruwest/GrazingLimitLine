package t.n.plainmap.view;

import static t.n.plainmap.view.ObserveLocationTableModel.CHECKBOX_COL;
import static t.n.plainmap.view.ObserveLocationTableModel.GOTO_MAP_COL;
import static t.n.plainmap.view.ObserveLocationTableModel.LATITUDE_COL;
import static t.n.plainmap.view.ObserveLocationTableModel.LONLAT_COL;
import static t.n.plainmap.view.ObserveLocationTableModel.LONTITUDE_COL;
import static t.n.plainmap.view.ObserveLocationTableModel.ZOOM_LEVEL_COL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

import t.n.map.LonLatFormat;
import t.n.map.common.LonLat;

public class ObserveLocationTable extends JTable {
	private final ObserveLocationTableModel observeLocationTableModel;
	private final IObserveTableEventListener listener;
	private final ObserveLocationTableRenderer observeLocationTableRenderer;

	public ObserveLocationTable(final IObserveTableEventListener listener) {
		super();
		this.listener = listener;

		observeLocationTableModel = new ObserveLocationTableModel();
		setModel(observeLocationTableModel);
		//デバッグ用に用意したデータ。
//		observeLocationTableModel.addRow(false, "aa", "bb", "cc", new LonLat(135.0d, 35d), 7);
//		observeLocationTableModel.addRow(false, "aa", "bb", "cc", new LonLat(135.5d, 34d), 7);

		observeLocationTableRenderer = new ObserveLocationTableRenderer();
		setTableHeader(new JTableHeader(getColumnModel()));

		TableColumn column = null;
		for (int i = 0; i < observeLocationTableModel.getColumnCount(); i++) {
			column = getColumnModel().getColumn(i);
			column.setPreferredWidth(observeLocationTableModel.getColumnPrefferedSize(i));
			if(i == GOTO_MAP_COL || i == LONTITUDE_COL || i == LATITUDE_COL) {
				column.setCellRenderer(observeLocationTableRenderer);
			}
		}

		addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				int col = columnAtPoint(e.getPoint());
				Object comp = observeLocationTableModel.getValueAt(row, col);

				//「地図へ」ボタンをクリックされた時の処理
				if(col == GOTO_MAP_COL) {
					JButton btn = ((JButton)comp);
					Object zoomColObj = observeLocationTableModel.getValueAt(row, ZOOM_LEVEL_COL);
					Object lonlatObj = observeLocationTableModel.getValueAt(row, LONLAT_COL);
					LonLat lonlat = (LonLat)lonlatObj;
					int zoomLevel = (Integer)zoomColObj;
//					System.out.println(getClass().getSimpleName() + ": lon, lat:" + lonlat.toString() + ", zoom: " + zoomLevel);
					//この表に追加されたときのzoomLevelを復元
					//zoomLevelは表の隠しアイテムとして保持する。
					listener.notifyMoveToLocation(lonlat, zoomLevel);
				}

				//チェックボックスをクリックされた時の処理
				if(col == CHECKBOX_COL) {
					// リスナーを呼び出す。
					fireCheckedStatusChanged();
				}
			}
		});
	}

	//一つでもチェックされている行があれば削除ボタンを有効にする。
	protected void fireCheckedStatusChanged() {
		boolean isBreaked = false;
		for(int rowIndex = 0; rowIndex < observeLocationTableModel.getRowCount(); rowIndex++) {
			Object obj = observeLocationTableModel.getValueAt(rowIndex, 0);
			if(obj instanceof Boolean) {
				if((Boolean)(obj)) {
					listener.setRemoveButtonEnabled(true);
					isBreaked = true;
					break;
				}
			}
		}
		if(!isBreaked) {
			listener.setRemoveButtonEnabled(false);
		}
		//削除されたマーカーを消すため、repaint()する
		listener.notifyLocationRemoved();
	}

	//日付と場所はダイアログで入力して貰ったのを受け取る。
	public void addEventDateLocation(String eventDate, String eventName,
			String observeLocation, LonLat lonlat, int zoomLevel) {
		observeLocationTableModel.addRow(false, eventDate, eventName, observeLocation, lonlat, zoomLevel);
	}

	//チェックされた行を削除
	//FIXME 複数チェックされていても、最初の一行しか削除されない。
	public void removeSelectedEventDateLocation() {
		for(int rowIndex = 0; rowIndex < observeLocationTableModel.getRowCount(); rowIndex++) {
			Object obj = observeLocationTableModel.getValueAt(rowIndex, 0);
			if(obj instanceof Boolean) {
				if((Boolean)(obj)) {
					observeLocationTableModel.removeRow(rowIndex);
				}
			}
		}
		fireCheckedStatusChanged();
	}

	@SuppressWarnings("unchecked")
	public void restoreFromFile(File file) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
			observeLocationTableModel.setBList((List<Boolean>) ois.readObject());
			observeLocationTableModel.setDateList((List<String>) ois.readObject());
			observeLocationTableModel.setNameList((List<String>) ois.readObject());
			observeLocationTableModel.setLocationList((List<String>) ois.readObject());
			observeLocationTableModel.setLonlatList((List<LonLat>) ois.readObject());
			observeLocationTableModel.setLongitudeList((List<Double>) ois.readObject());
			observeLocationTableModel.setLatitudeList((List<Double>) ois.readObject());
			observeLocationTableModel.setZoomLevelList((List<Integer>) ois.readObject());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveLocationInfo(File file) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			oos.writeObject(observeLocationTableModel.getBList());
			oos.writeObject(observeLocationTableModel.getDateList());
			oos.writeObject(observeLocationTableModel.getNameList());
			oos.writeObject(observeLocationTableModel.getLocationList());
			oos.writeObject(observeLocationTableModel.getLonlatList());
			oos.writeObject(observeLocationTableModel.getLongitudeList());
			oos.writeObject(observeLocationTableModel.getLatitudeList());
			oos.writeObject(observeLocationTableModel.getZoomLevelList());
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getClipboardData() {
		return observeLocationTableModel.getClipboardData();
	}

	public List<LonLat> getObserveLocations() {
		return observeLocationTableModel.getLonlatList();
	}

	public void setLonLatFormat(LonLatFormat lonlatFormat) {
		observeLocationTableRenderer.setLonLatFormat(lonlatFormat);
		repaint();
	}
}
