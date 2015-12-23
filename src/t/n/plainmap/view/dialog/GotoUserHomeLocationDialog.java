package t.n.plainmap.view.dialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import t.n.map.OsType;
import t.n.map.common.LonLat;
import t.n.map.common.util.LonLatUtil;
import t.n.plainmap.AppConfig;
import t.n.plainmap.IMoveToLocationListener;

public class GotoUserHomeLocationDialog extends JDialog implements ActionListener {
    private JButton homeUpdateButton;
    private JButton leftButton;
    private JButton rightButton;

	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
//	private JTextField longitudeTextField;
//	private JTextField latitudeTextField;

    private JTextField latitudeTextField0;
    private JTextField latitudeTextField1;
    private JTextField longitudeTextField0;
    private JTextField longitudeTextField1;

	private LonLat newHomeLonLat;
	private int newHomeZoomLevel;
	private final OsType os;
	private final IMoveToLocationListener observer;

	public static void main(String args[]) {
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

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
            	GotoUserHomeLocationDialog dialog = new GotoUserHomeLocationDialog(new JFrame(), null, 5, new LonLat(137, 35), 7, null);
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

	public GotoUserHomeLocationDialog(final JFrame frame, final LonLat homeLonLat, final int homeZoomLevel, final LonLat currentLonLat, final int currentZoomLevel, final IMoveToLocationListener observer) {
		super(frame, true);
		this.observer = observer;
		os = AppConfig.getOsType();
		initComponents();

		homeUpdateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int ans = JOptionPane.showConfirmDialog(frame, "既にホームロケーションが設定されていますが、現在表示されている地図に更新してもよろしいですか？", "確認", JOptionPane.YES_NO_OPTION);
				if(ans == JOptionPane.YES_OPTION) {
					newHomeLonLat = currentLonLat;
					newHomeZoomLevel = currentZoomLevel;
				} else if(ans == JOptionPane.NO_OPTION) {
					newHomeLonLat = homeLonLat;
					newHomeZoomLevel = homeZoomLevel;
				}
			}
		});

		leftButton.addActionListener(this);
		rightButton.addActionListener(this);

		//homeが未設定なら、現在地図に表示されている中心の緯度経度とzoomLevelを用いたい。
		//homeが設定済みなら、このまま「ホームへ移動」ボタンと「キャンセル」ボタンが使える。
		//このダイアログの中に別のボタン（例えば、「ホームの再設定」)を追加して、現在の地図から得た情報で既存のホームを上書きできるようにする。

		if(homeLonLat == null) {
			int ans = JOptionPane.showConfirmDialog(this, "ホームロケーションが未設定です。現在表示されている地図をホームとして設定してもよろしいですか？", "確認", JOptionPane.YES_NO_OPTION);
			if(ans == JOptionPane.YES_OPTION) {
				//上記情報を設定する。また、「ホームへ移動」ボタンと「キャンセル」ボタンが使える。
				newHomeLonLat = currentLonLat;
				newHomeZoomLevel = currentZoomLevel;
				setGotoButtonEnabled(true);
			} else if(ans == JOptionPane.NO_OPTION) {
				//いったん戻って地図を変えてから再度ダイアログを開いて貰う。
				setVisible(false);
				newHomeLonLat = null;
				newHomeZoomLevel = 0;
				setGotoButtonEnabled(false);
			}
		} else {
			newHomeLonLat = homeLonLat;
			newHomeZoomLevel = homeZoomLevel;
			setGotoButtonEnabled(true);
		}

		//緯度・経度を両方のフォーマットで表示。
		if(newHomeLonLat != null) {
			String lon0 = LonLatUtil.getLongitudeJapaneseString(newHomeLonLat.getLongitude());
			longitudeTextField0.setText(lon0);
			String lat0 = LonLatUtil.getLatitudeJapaneseString(newHomeLonLat.getLatitude());
			latitudeTextField0.setText(lat0);
			//桁数を制限した実数形式で表示。
			String lon1 = LonLatUtil.getFormattedLongitudeString(newHomeLonLat.getLongitude());
			longitudeTextField1.setText(lon1);
			String lat1 = LonLatUtil.getFormattedLatitudeString(newHomeLonLat.getLatitude());;
			latitudeTextField1.setText(lat1);
		}
	}

	private void setGotoButtonEnabled(boolean b) {
        switch(os) {
        case win:
        	leftButton.setEnabled(b);
        	break;
        case osx:
        case unix:
        	rightButton.setEnabled(b);
        	break;
        }
	}

	@Override
	//cancel/gotoボタンのどちらか。ボタンに付けた名前で処理を分ける。
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		switch(action) {
		case "goto":
			if(observer != null) {
				setVisible(false);
				if(newHomeLonLat != null) {
					observer.notifyGotoHomeLocation(newHomeLonLat, newHomeZoomLevel);
				}
			}
			break;
		case "cancel":
			setVisible(false);
			break;
		}
	}

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        jLabel3 = new JLabel();
        longitudeTextField0 = new JTextField();
        latitudeTextField0 = new JTextField();
        longitudeTextField1 = new JTextField();
        latitudeTextField1 = new JTextField();

        leftButton = new JButton();
        rightButton = new JButton();
        homeUpdateButton = new JButton();

        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setResizable(false);

        jLabel1.setText("経度");

//        longitudeTextField0.setText("");
        longitudeTextField0.setEditable(false);
        longitudeTextField0.setBackground(Color.LIGHT_GRAY);
        longitudeTextField1.setEditable(false);
        longitudeTextField1.setBackground(Color.LIGHT_GRAY);

        jLabel2.setText("緯度");
        jLabel3.setText("現在設定されているホーム");

//        latitudeTextField0.setText("");
        latitudeTextField0.setEditable(false);
        latitudeTextField0.setBackground(Color.LIGHT_GRAY);
        latitudeTextField1.setEditable(false);
        latitudeTextField1.setBackground(Color.LIGHT_GRAY);

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

        homeUpdateButton.setText("ホーム再設定");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addGap(27, 27, 27)
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 286, GroupLayout.PREFERRED_SIZE)
//                    .addGroup(layout.createSequentialGroup()
//                        .addComponent(jLabel1)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                            .addComponent(longitudeTextField, GroupLayout.PREFERRED_SIZE, 208, GroupLayout.PREFERRED_SIZE)
//                            .addComponent(homeUpdateButton)
//                            .addGroup(layout.createSequentialGroup()
//                                .addComponent(leftButton)
//                                .addGap(18, 18, 18)
//                                .addComponent(rightButton))))
//                    .addGroup(layout.createSequentialGroup()
//                        .addComponent(jLabel2)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(latitudeTextField, GroupLayout.PREFERRED_SIZE, 208, GroupLayout.PREFERRED_SIZE)))
//                .addContainerGap(61, Short.MAX_VALUE))
//        );
//        layout.setVerticalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addGap(15, 15, 15)
//                .addComponent(jLabel3)
//                .addGap(16, 16, 16)
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(jLabel1)
//                    .addComponent(longitudeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addGap(18, 18, 18)
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(jLabel2)
//                    .addComponent(latitudeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addGap(18, 18, 18)
//                .addComponent(homeUpdateButton)
//                .addGap(14, 14, 14)
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(leftButton)
//                    .addComponent(rightButton))
//                .addContainerGap(14, Short.MAX_VALUE))
//        );
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, GroupLayout.PREFERRED_SIZE, 286, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(longitudeTextField0, GroupLayout.PREFERRED_SIZE, 208, GroupLayout.PREFERRED_SIZE)
                            .addComponent(homeUpdateButton)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(leftButton)
                                .addGap(18, 18, 18)
                                .addComponent(rightButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(latitudeTextField1, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                            .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(longitudeTextField1)
                                .addComponent(latitudeTextField0, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3)
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(longitudeTextField0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(longitudeTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(latitudeTextField0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(latitudeTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(homeUpdateButton)
                .addGap(14, 14, 14)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(leftButton)
                    .addComponent(rightButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }

}
