package t.n.plainmap.view.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.MaskFormatter;
import javax.swing.text.PlainDocument;

import t.n.map.OsType;
import t.n.map.common.LonLat;
import t.n.map.common.util.LonLatUtil;
import t.n.plainmap.AppConfig;
import t.n.plainmap.IMoveToLocationListener;

public class MoveToLocationDialog2 extends JDialog implements ActionListener {
	// WindowsとMacで"OK", "Cancel"のボタンの配置を逆にする。
	//プログラマが意識しなくても良きに計らってくれる仕組みはないか？
	//ボタンに名前を設定し、これで機能を切り替える。

	private static final String ILLEGAL_NUMBER = "分あるいは秒の欄は60未満を入力してください";
	private static final String OUT_OF_JAPAN_RANGE = "範囲外です。日本国内の緯度経度のみ有効です。";
	private static final String NOT_INPUT = "未入力です。";

	private final OsType os;

    private JButton rightButton;
    private JButton leftButton;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel5;
	private JLabel msgLabel;

    //度分秒形式と、実数形式のどちらでも入力できるようにする
    //テキストフィールドを、あらかじめ数値だけ入力可能に設定する。
	//FIXME 度分秒形式について、全桁数を入力しないとならないので、面倒。
	//未入力の場合に自動的に0を設定できないか？
    private JFormattedTextField latitudeTextField1;
    private JFormattedTextField latitudeTextField2;
    private JFormattedTextField longitudeTextField1;
    private JFormattedTextField longitudeTextField2;

	private final IMoveToLocationListener listener;

	public MoveToLocationDialog2(JFrame frame, final IMoveToLocationListener listener) {
		super(frame, true);
		this.listener = listener;
		os = AppConfig.getOsType();

		initComponents();

		leftButton.addActionListener(this);
		rightButton.addActionListener(this);

		pack();
	}

    @Override
    public void actionPerformed(ActionEvent e) {

    	String action = e.getActionCommand();
    	switch(action) {
    	case "goto":
    		//経度
    		double lon = 0;
    		String lonText = longitudeTextField1.getText();
    		if(lonText.isEmpty()) {
    			//度分秒形式のフィールドに入力されている文字を使う。
        		try {
        			longitudeTextField2.commitEdit();
    			} catch (ParseException e1) {
    				msgLabel.setText(NOT_INPUT);
    				return;
    			}

				lonText = (String)longitudeTextField2.getValue();
				if(lonText != null && !lonText.isEmpty()) {
					//先頭の"東経"
					lon = LonLatUtil.parseDegMinSec(lonText);
					if(Double.isNaN(lon)) {
						msgLabel.setText(ILLEGAL_NUMBER);
						return;
					}
				} else {
					msgLabel.setText(NOT_INPUT);
					return;
				}
    		} else {
    			//実数形式のフィールドに入力されている値を使う。
    			lon = Double.parseDouble(lonText);
    		}

    		//緯度
    		double lat = 0;
    		String latText = latitudeTextField1.getText();
    		if(latText != null && latText.isEmpty()) {
    			//度分秒形式のフィールドに入力されている文字を使う。
        		try {
    				latitudeTextField2.commitEdit();
    			} catch (ParseException e1) {
    				msgLabel.setText(NOT_INPUT);
    				return;
    			}

    			latText = (String)latitudeTextField2.getValue();
    			if(!latText.isEmpty()) {
    				latText = latText.substring(2, latText.length());
    				lat = LonLatUtil.parseDegMinSec(latText);
    				if(Double.isNaN(lat)) {
    					msgLabel.setText(ILLEGAL_NUMBER);
    					return;
    				}
    			} else {
    				msgLabel.setText(NOT_INPUT);
					return;
				}
    		} else {
    			//実数形式のフィールドに入力されている値を使う。
    			lat = Double.parseDouble(latText);
    		}

    		//数値の検証。日本の緯度経度の範囲内(離島を含む)か？
    		if(LonLatUtil.isOutOfJapanRegion(lon, lat)) {
    			msgLabel.setText(OUT_OF_JAPAN_RANGE);
    			return;
    		} else {
    			listener.notifyMoveToLocation(lon, lat);
    		}
    		break;
    	case "cancel":
    		break;
    	default:
    		System.err.println("bug");
    	}
    	setVisible(false);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        jLabel4 = new JLabel();
        jLabel5 = new JLabel();
        msgLabel = new JLabel();

        DecimalFormat df = new DecimalFormat("#,###");
        MaskFormatter mfLon = null;
        MaskFormatter mfLat = null;
        try{
            mfLon = new MaskFormatter(LonLatUtil.longitudeDegMinSec);
            mfLon.setPlaceholderCharacter('_');
            mfLat = new MaskFormatter(LonLatUtil.latitudeDegMinSec);
            mfLat.setPlaceholderCharacter('_');
        }catch(ParseException pe){
        }

        longitudeTextField1 = new JFormattedTextField(df);
        longitudeTextField2 = new JFormattedTextField(mfLon);
//        longitudeTextField2.setPreferredSize(new Dimension(70, 25));
//        longitudeTextField1.setPreferredSize(new Dimension(70, 25));

        latitudeTextField1 = new JFormattedTextField(df);
//        latitudeTextField1.setPreferredSize(new Dimension(70, 25));
        latitudeTextField2 = new JFormattedTextField(mfLat);
//        latitudeTextField2.setPreferredSize(new Dimension(70, 25));

        rightButton = new JButton();
        leftButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        jLabel1.setText("経度(XXX.XXXXX)");
        jLabel2.setText("緯度(XX.XXXXX)");
        jLabel3.setText("日本国内の緯度・経度を指定して移動できます。");
        jLabel4.setText("経度(度分秒)");
        jLabel5.setText("緯度(度分秒)");

        //TODO macで、Command + Vでペーストできるようにする。VK_PASTEだとControl + Vでのペーストになる。
        //マウスでのペーストは出来る。
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        menuItem.setText("貼付け");
        switch(AppConfig.getOsType()) {
        case osx:
//        	int shotcutKey = AppConfig.getShortCutKey();
//        	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, shotcutKey));
        	//
        	menuItem.setMnemonic(KeyEvent.VK_V);
//        	menuItem.setMnemonic(KeyEvent.VK_PASTE);
        	break;
        case unix:
        case win:
        	menuItem.setMnemonic(KeyEvent.VK_PASTE);
        	break;
        }
		popupMenu.add(menuItem);

        longitudeTextField1.setComponentPopupMenu(popupMenu);
        latitudeTextField1.setComponentPopupMenu(popupMenu);

        switch(os) {
        case win:
        	leftButton.setText("移動");
        	leftButton.setActionCommand("goto");
        	rightButton.setText("キャンセル");
        	rightButton.setActionCommand("cancel");
        	break;
        case osx:
        case unix:
        	leftButton.setText("キャンセル");
        	leftButton.setActionCommand("cancel");
        	rightButton.setText("移動");
        	rightButton.setActionCommand("goto");
        	break;
        }

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                        .addComponent(msgLabel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2)
                                .addComponent(jLabel5)
                                .addComponent(jLabel4))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(longitudeTextField1, GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                                .addComponent(longitudeTextField2)
                                .addComponent(latitudeTextField1)
                                .addComponent(latitudeTextField2))))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(56, 56, 56)
                        .addComponent(leftButton, GroupLayout.PREFERRED_SIZE, 98, GroupLayout.PREFERRED_SIZE)
                        .addGap(29, 29, 29)
                        .addComponent(rightButton, GroupLayout.PREFERRED_SIZE, 94, GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 294, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 18, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(longitudeTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(longitudeTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(latitudeTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(latitudeTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(msgLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(leftButton)
                    .addComponent(rightButton))
                .addGap(23, 23, 23))
        );
        pack();
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MoveToLocationDialog2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MoveToLocationDialog2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MoveToLocationDialog2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MoveToLocationDialog2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        //http://stackoverflow.com/questions/7252749/how-to-use-command-c-command-v-shortcut-in-mac-to-copy-paste-text
        InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
                IMoveToLocationListener dummyListener = new IMoveToLocationListener() {

					@Override
					public void notifyMoveToLocation(double lon, double lat) {
						JOptionPane.showMessageDialog(null, "入力内容 経度:" + lon + ", 経度:" + lat);
					}

					@Override
					public void notifyGotoHomeLocation(LonLat homeLocation, int zoomLevel) {
					}
				};
				MoveToLocationDialog2 dialog = new MoveToLocationDialog2(new JFrame(), dummyListener );
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
}

