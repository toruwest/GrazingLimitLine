package t.n.plainmap.view;

import static t.n.plainmap.view.LimitLineTableModel.BLIST_COL;
import static t.n.plainmap.view.LimitLineTableModel.EVENT_NAME_COL;
import static t.n.plainmap.view.LimitLineTableModel.HILIGHT_COL;
import static t.n.plainmap.view.LimitLineTableModel.SHOW_EDGE_COL;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import t.n.plainmap.AppConfig;
import t.n.plainmap.dto.LimitLineDatum;

public class LimitLineTable extends JTable {
	public static final String TEXT_EXT = ".txt";
	public static final String IMAGE_EXT = ".png";

	private final LimitLineTableModel limitLineTableModel;
	private final TableButtonCellRenderer buttonCellRenderer = new TableButtonCellRenderer();
	private final EventLineColumnTableRenderer eventLineCellRenderer = new EventLineColumnTableRenderer();

	private final List<ILimitLineTableEventListener> tableEventListeners;
	//ダイアログの親コンポーネントとして使う
	private final LimitLineTable thisTable;

	public LimitLineTable() {
		super();
		thisTable = this;
		tableEventListeners = new ArrayList<>();

		limitLineTableModel = new LimitLineTableModel();
		setModel(limitLineTableModel);

		setRowSelectionAllowed(false);
		setCellSelectionEnabled(false);

		addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				int col = columnAtPoint(e.getPoint());
				Object comp = getValueAt(row, col);
				if(comp instanceof JButton) {
					JButton btn = ((JButton)comp);
					System.out.println("cell clicked :" + btn.getName() + ":" + row + ":" + btn.getActionCommand());
					//ボタンクリック時の処理
					if(btn.getName().equals("show")) {
						//月縁図のイメージを表示
						String filename = limitLineTableModel.getFilename(row);
						filename = filename.replace(TEXT_EXT, IMAGE_EXT);
						filename = AppConfig.getGrazingLimitLineDataDir() + File.separator + filename;
						File f = new File(filename);

						//pngファイルがない場合、テーブル上のボタンを無効化してある。
						if(f.exists()) {
							try {
								// イメージが大きすぎるので、縮小して表示。
								//TODO 元の画像の比率を維持したい。
								// キャッシュした方がいい？
								BufferedImage rawImage = ImageIO.read(f);
								BufferedImage resized = resize(rawImage, 600, 600);
								ImageIcon icon = new ImageIcon(resized);
								JOptionPane.showMessageDialog(null, icon, filename, JOptionPane.INFORMATION_MESSAGE);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}

				//チェックボックスをクリックされた時の処理
				if(comp instanceof Boolean) {
					Boolean check = ((Boolean)comp);
					// チェックボックスを反転、リスナーを呼び出す。
					switch(col) {
					case BLIST_COL:
						limitLineTableModel.setShowState(row, !check);
						fireShowStatusChanged(row, !check);
						break;
					case HILIGHT_COL:
						limitLineTableModel.setHilightState(row, !check);
						fileHilightStatusChanged(row, !check);
						break;
					}
				}
			}
		});

		TableColumn column = null;
		for (int i = 0; i < limitLineTableModel.getColumnCount(); i++) {
			column = getColumnModel().getColumn(i);
			column.setPreferredWidth(limitLineTableModel.getColumnPrefferedSize(i));
			//テーブルの幅を広げたときに、カラムのサイズが必要以上に広がらないように制限する。
			//そうしないと、あるカラムには余白があるのに、他のカラムが狭くて、内容が見えない状況になる。
			//				column.setMaxWidth(limitLineTableModel.getColumnMaxSize(i));
			if(i <= HILIGHT_COL) {
				column.setResizable(false);
			}
			if(i == SHOW_EDGE_COL ) {
				column.setCellRenderer(buttonCellRenderer);
//				column.setCellEditor(buttonCellEditor);
			}
			if(i == EVENT_NAME_COL) {
				column.setCellRenderer(eventLineCellRenderer);
			}
		}
	}

	protected void fireShowStatusChanged(int row, boolean b) {
		for(ILimitLineTableEventListener l : tableEventListeners) {
			l.notifyShowStatusChanged(row, b);
		}
	}

	protected void fileHilightStatusChanged(int row, boolean b) {
		for(ILimitLineTableEventListener l : tableEventListeners) {
			l.notifyHilightStatusChanged(row, b);
		}
	}

	public void setData(List<LimitLineDatum> readLimitLines) {
		limitLineTableModel.setData(readLimitLines);
	}

	public void addTableEventListener(ILimitLineTableEventListener listener) {
		tableEventListeners.add(listener);
	}

	public void setShowAllItems(boolean b) {
		limitLineTableModel.setAllCheckState(b);
	}

	private static BufferedImage resize(BufferedImage image, int width, int height) {
	    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TRANSLUCENT);
	    Graphics2D g2d = bi.createGraphics();
	    g2d.addRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    g2d.drawImage(image, 0, 0, width, height, null);
	    g2d.dispose();
	    return bi;
	}

	class TableButtonCellEditor extends AbstractCellEditor implements TableCellEditor {
	    protected JButton component = null;

	    @Override
	    public Object getCellEditorValue() {
	        return component;
	    }

	    @Override
	    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
	        component = (JButton)value;
	        return component;
	    }
	}

	/*
	 * JTable内にJButtonを配置する方法。
	 * http://stackoverflow.com/questions/13833688/adding-jbutton-to-jtable
	 * http://ateraimemo.com/Swing/DeleteButtonInCell.html
	 * http://www58.atwiki.jp/chapati4it/pages/76.html
	 *  上のやり方はどれもJButtonをクリックしたことを検出できなかった。以下の方法で実現できた。
	 * http://allaboutbasic.com/2010/12/29/jtable-cell-click-event-click-on-the-jtable-cell-and-show-the-value-of-that-cell/
	*/
	class TableButtonCellRenderer extends JButton implements TableCellRenderer {
	    // 行の高さ
	    private int rowHeight = 0;

	    TableButtonCellRenderer() {
	        super();
	    }

	    @Override
	    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        JButton component = (JButton)value;
	        if (isSelected) {
	        	component.setForeground(table.getSelectionForeground());
	        	component.setBackground(table.getSelectionBackground());
	        } else {
	        	component.setForeground(table.getForeground());
	        	component.setBackground(table.getBackground());
	        }

	        //行の高さをそろえる。
	        Dimension dimension = component.getPreferredSize();
	        if(dimension.height > rowHeight) {
	            table.setRowHeight(dimension.height);
	            rowHeight = dimension.height;
	        }

			String filename = limitLineTableModel.getFilename(row);
			filename = filename.replace(".txt", ".png");
			filename = AppConfig.getGrazingLimitLineDataDir() + File.separator + filename;
			File f = new File(filename);

			if(!f.exists() && column == SHOW_EDGE_COL) {
				//component.setVisible(false);
				component.setText("ない");
			}

	        return component;
	    }
	}

	class EventLineColumnTableRenderer extends JLabel implements TableCellRenderer {
		public EventLineColumnTableRenderer() {
			super();
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			Component comp = new JLabel((String)value);

			Color fg = limitLineTableModel.getColor(row);
			comp.setForeground(fg);
			return comp;
		}
	}
}
