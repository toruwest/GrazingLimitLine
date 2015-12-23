package t.n.plainmap.view.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;

import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import t.n.map.OsType;
import t.n.map.common.LonLat;
import t.n.map.common.util.LonLatUtil;
import t.n.plainmap.AppConfig;
import t.n.plainmap.IMoveToLocationListener;

public class MoveToLocationDialog1 extends JDialog implements ActionListener {
	// WindowsとMacで"OK", "Cancel"のボタンの配置を逆にする。
	//プログラマが意識しなくても良きに計らってくれる仕組みはないか？
	//ボタンに名前を設定し、これで機能を切り替える。

	private final OsType os;

    private javax.swing.JButton rightButton;
    private javax.swing.JButton leftButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;

    //度分秒形式と、実数形式のどちらでも入力できるようにする
    //テキストフィールドを、あらかじめ数値だけ入力可能に設定する。
    private JFormattedTextField latitudeTextField0;
    private NumberField latitudeTextField1;
    private NumberField latitudeTextField3;
    private NumberField latitudeTextField2;
    private JFormattedTextField longitudeTextField0;
    private NumberField longitudeTextField1;
    private NumberField longitudeTextField2;
    private NumberField longitudeTextField3;

	private final IMoveToLocationListener listener;

	public MoveToLocationDialog1(JFrame frame, final IMoveToLocationListener listener) {
		super(frame, true);
		this.listener = listener;
		os = AppConfig.getOsType();
		initComponents();

		//TODO 状況に応じてGotoボタンの使用可否を切り替える。
		//rightButton.setEnabled(!longitudeTextField0.getText().isEmpty() && !latitudeTextField0.getText().isEmpty());

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
    		String lonText = longitudeTextField0.getText();
    		if(lonText.isEmpty()) {
    			//度分秒形式のフィールドに入力されている文字を使う。
				String lonDegText = longitudeTextField1.getText();
				String lonMinText = longitudeTextField2.getText();
				String lonSecText = longitudeTextField3.getText();
				if(lonMinText.isEmpty()) {
    				lonMinText = "0";
    			}
    			if(lonSecText.isEmpty()) {
    				lonSecText = "0";
    			}
				if(!lonDegText.isEmpty()) {
					lon = LonLatUtil.parseDegMinSec(lonDegText, lonMinText, lonSecText);
					if(Double.isNaN(lon)) {
						JOptionPane.showMessageDialog(getParent(), "分あるいは秒の欄は60未満を入力してください");
						return;
					}
				} else {
					JOptionPane.showMessageDialog(getParent(), "未入力です。");
					return;
				}
    		} else {
    			lon = Double.parseDouble(lonText);
    		}

    		//緯度
    		double lat = 0;
    		String latText = latitudeTextField0.getText();
    		if(latText.isEmpty()) {
    			//度分秒形式のフィールドに入力されている文字を使う。
    			String latDegText = latitudeTextField1.getText();
    			String latMinText = latitudeTextField2.getText();
    			String latSecText = latitudeTextField3.getText();
    			if(latMinText.isEmpty()) {
    				latMinText = "0";
    			}
    			if(latSecText.isEmpty()) {
    				latSecText = "0";
    			}
    			if(!latDegText.isEmpty()) {
    				lat = LonLatUtil.parseDegMinSec(latDegText, latMinText, latSecText);
    				if(Double.isNaN(lat)) {
    					JOptionPane.showMessageDialog(getParent(), "分あるいは秒の欄は60未満を入力してください");
    					return;
    				}
    			} else {
					JOptionPane.showMessageDialog(getParent(), "未入力です。");
					return;
				}
    		} else {
    			lat = Double.parseDouble(latText);
    		}

    		//数値の検証。日本の緯度経度の範囲内(離島を含む)か？
    		if(LonLatUtil.isOutOfJapanRegion(lon, lat)) {
    			JOptionPane.showMessageDialog(getParent(), "<html>範囲外です。日本国内の緯度経度のみ有効です。<br><br>"
    					+ " 東経:" + LonLatUtil.westRegionString + " - " + LonLatUtil.eastRegionString + "<br>"
    					+ " 北緯:" + LonLatUtil.southRegionString + " - " + LonLatUtil.northRegionString + "<br></html>");
    			return;
    		} else {
    			listener.notifyMoveToLocation(lon, lat);
    		}
    		break;
    	case "cancel":
    		break;
    	default:
    		System.err.println(getClass().getName() + ": bug");
    	}
    	setVisible(false);
    }

    private final DecimalFormat df = new DecimalFormat("#,###.#####");

    @SuppressWarnings("unchecked")
    private void initComponents() {
            jLabel1 = new javax.swing.JLabel();
            jLabel2 = new javax.swing.JLabel();
            jLabel3 = new javax.swing.JLabel();
            jLabel4 = new javax.swing.JLabel();
            jLabel5 = new javax.swing.JLabel();

            longitudeTextField0 = new JFormattedTextField(df);
            longitudeTextField1 = new NumberField();
            longitudeTextField2 = new NumberField();
            longitudeTextField3 = new NumberField();
            latitudeTextField0 = new JFormattedTextField(df);
            latitudeTextField1 = new NumberField();
            latitudeTextField2 = new NumberField();
            latitudeTextField3 = new NumberField();

            rightButton = new javax.swing.JButton();
            leftButton = new javax.swing.JButton();

            setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
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
//            	int shotcutKey = AppConfig.getShortCutKey();
//            	menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, shotcutKey));
            	//
            	menuItem.setMnemonic(KeyEvent.VK_V);
//            	menuItem.setMnemonic(KeyEvent.VK_PASTE);
            	break;
            case unix:
            case win:
            	menuItem.setMnemonic(KeyEvent.VK_PASTE);
            	break;
            }
    		popupMenu.add(menuItem);

            longitudeTextField0.setComponentPopupMenu(popupMenu);
            latitudeTextField0.setComponentPopupMenu(popupMenu);

//            longitudeTextField0.setText("-ddd.dddd");
//            longitudeTextField1.setText("-ddd");
//            longitudeTextField2.setText("dd");
//            longitudeTextField3.setText("dd.ddd");
//
//            latitudeTextField0.setText("nn.nnnn");
//            latitudeTextField1.setText("-nn");
//            latitudeTextField2.setText("nn");
//            latitudeTextField3.setText("nn.nnn");

            longitudeTextField3.setPreferredSize(new Dimension(70, 25));
            latitudeTextField3.setPreferredSize(new Dimension(70, 25));

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

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(27, 27, 27)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2)
                                .addComponent(jLabel5)
                                .addComponent(jLabel4))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(longitudeTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(latitudeTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGap(1, 1, 1)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(longitudeTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(latitudeTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(longitudeTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(latitudeTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addComponent(longitudeTextField0)
                                .addComponent(latitudeTextField0)))
                        .addGroup(layout.createSequentialGroup()
                            .addGap(79, 79, 79)
                            .addComponent(leftButton)
                            .addGap(29, 29, 29)
                            .addComponent(rightButton)))
                    .addContainerGap(27, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(15, 15, 15)
                    .addComponent(jLabel3)
                    .addGap(16, 16, 16)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(longitudeTextField0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(longitudeTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel4))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(latitudeTextField0, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(3, 3, 3)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(latitudeTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel5)))
                        .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(longitudeTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(longitudeTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGap(35, 35, 35)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(latitudeTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(latitudeTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(rightButton)
                        .addComponent(leftButton))
                    .addContainerGap(14, Short.MAX_VALUE))
            );

            pack();
    }

    class NumberField extends JTextField {
        public NumberField() {
            super();
        }

        @Override
		protected Document createDefaultModel() {
            return new NumberDocument();
        }
    }

    static class NumberDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;
		private static final String validChars = "0123456789.+-";

		@Override
		public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {

            if (validChars.contains(str)) {
            	super.insertString(offs, str, a);
            }
        }
    }

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MoveToLocationDialog1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MoveToLocationDialog1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MoveToLocationDialog1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MoveToLocationDialog1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

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
				MoveToLocationDialog1 dialog = new MoveToLocationDialog1(new javax.swing.JFrame(), dummyListener );
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

