package t.n.plainmap.view.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import t.n.plainmap.MapPreference;

public class ProxyConfigDialog extends javax.swing.JDialog {

    /**
     * Creates new form ProxyConfigDialog
     * @param
     */
    public ProxyConfigDialog(JFrame parent, final MapPreference pref) {
        super(parent, true);

        initComponents();

        useRadioButton.setSelected(pref.isUseProxy());
        proxyHostTextField.setText(pref.getProxyHostname());
        long proxyPort = pref.getProxyPort();
        if(proxyPort == 0) {
        	proxyPortTextField.setText("");
        } else {
        	proxyPortTextField.setText(String.valueOf(proxyPort));
        }

        useRadioButton.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO proxy使用時だけpingボタンを有効にする。
				pingButton.setEnabled(useRadioButton.isSelected());
			}
		});

        pingButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String host = proxyHostTextField.getText();
				if(!host.isEmpty()) {
					try {
						InetAddress inetAddress = InetAddress.getByName(host);
						// isReachableメソッドでpingが実現できます。引数はタイムアウト(ミリ秒指定)。
						boolean b = inetAddress.isReachable(1000);
						pingResultCheckBox.setSelected(b);
						if(b) {
							pingResultCheckBox.setText("OK");
						} else {
							pingResultCheckBox.setText("NG");
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent evt) {
            	String proxyHostText = proxyHostTextField.getText();
            	try {
					proxyPortTextField.commitEdit();
				} catch (Exception e) {
					e.printStackTrace();
				}
            	// proxyPortText = proxyPortTextField.getText();
            	Long proxyPort = (Long)proxyPortTextField.getValue();
            	if(useRadioButton.isSelected()) {
//            		if(!proxyHostText.isEmpty() && !proxyPortText.isEmpty() ) {
            		if(!proxyHostText.isEmpty()) {
            			pref.setProxyHostname(proxyHostText);
//            			long proxyPort = Integer.parseInt(proxyPortText);
            			pref.setProxyPort(proxyPort);
            			pref.setIsUseProxy(true);
//            			msgLabel.setText("ホスト:" + proxyHostText + ",port:" + proxyPort + "を使う");
            		} else {
            			msgLabel.setText("ホスト名とポート番号を入力してください。");
            			return;
            		}
            	} else {
//            		msgLabel.setText("プロキシーを使わないよう設定します。");
            		pref.setIsUseProxy(false);
            	}
            	msgLabel.setText("");
            	setVisible(false);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        buttonGroup1 = new ButtonGroup();
        notUseRadioButton = new JRadioButton();
        useRadioButton = new JRadioButton();
        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        msgLabel = new JLabel();

        proxyHostTextField = new JTextField();
        DecimalFormat df = new DecimalFormat("#####");
        proxyPortTextField = new JFormattedTextField(df);
        proxyPortTextField.setColumns(5);
        closeButton = new JButton();
        pingButton = new JButton("ping");
        pingButton.setEnabled(false);
        pingResultCheckBox = new JCheckBox();
        pingResultCheckBox.setText("??");
        pingResultCheckBox.setEnabled(false);

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);

        buttonGroup1.add(notUseRadioButton);
        notUseRadioButton.setText("プロキシを使わない");
        notUseRadioButton.setSelected(true);

        buttonGroup1.add(useRadioButton);
        useRadioButton.setText("プロキシを使う");

        jLabel1.setText("ホスト");
        jLabel2.setText("ポート");
        closeButton.setText("閉じる");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(useRadioButton)
                    .addComponent(notUseRadioButton)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(proxyPortTextField, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                    .addComponent(closeButton)
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(pingResultCheckBox)
                                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(pingButton, GroupLayout.PREFERRED_SIZE, 77, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(proxyHostTextField, GroupLayout.PREFERRED_SIZE, 173, GroupLayout.PREFERRED_SIZE))))
                            .addComponent(msgLabel, GroupLayout.PREFERRED_SIZE, 246, GroupLayout.PREFERRED_SIZE)))))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] {proxyHostTextField, proxyPortTextField});

        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(notUseRadioButton)
                .addGap(18, 18, 18)
                .addComponent(useRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(proxyHostTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(pingButton)
                    .addComponent(pingResultCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(proxyPortTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(msgLabel, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(closeButton)
                .addGap(34, 34, 34))
        );
        pack();
    }

    //TODO 入力したproxyサーバー名が正しいかチェックするため、pingできる？
    private ButtonGroup buttonGroup1;
    private JButton closeButton;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel msgLabel;
    private JRadioButton notUseRadioButton;
    private JTextField proxyHostTextField;
    private JFormattedTextField proxyPortTextField;
    private JRadioButton useRadioButton;
    private JButton pingButton;
    private JCheckBox pingResultCheckBox;

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ProxyConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProxyConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProxyConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProxyConfigDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
                MapPreference dummy = new MapPreference(new File("."));

				ProxyConfigDialog dialog = new ProxyConfigDialog(new JFrame(), dummy);
                dialog.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
}
