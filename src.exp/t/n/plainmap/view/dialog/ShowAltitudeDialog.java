package t.n.plainmap.view.dialog;

import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.ning.http.client.ListenableFuture;

import t.n.map.IAltitudeFetchStatusObserver;
import t.n.map.common.util.LonLatUtil;
import t.n.map.http.AltitudeDataGetter;

public class ShowAltitudeDialog extends javax.swing.JDialog implements IAltitudeFetchStatusObserver {

    private javax.swing.JTextField altitudeTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private MyPanel panel;
    private javax.swing.JTextField latTextField;
    private javax.swing.JTextField lonTextField;
	private double lonWidth;
	private double latHeight;
	private final AltitudeDataGetter getter;

    public ShowAltitudeDialog(java.awt.Frame parent, boolean modal) throws IOException {
        super(parent, modal);
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
				if(getter != null) getter.shutdown();
			}
		});

        initComponents();
        computeRegionMap();

        panel.addMouseListener(new MouseAdapter() {
        	@Override
        	public void mouseClicked(MouseEvent evt) {
        		//マウスをクリックしたときに、その場所の緯度経度を表示する。
        		Point p = evt.getPoint();
        		//標高をクリア
        		//altitudeTextField.setText("");
        		Double lon = LonLatUtil.westRegion + p.x * lonWidth / panel.getWidth();
        		Double lat = LonLatUtil.northRegion - p.y * latHeight / panel.getHeight();
        		System.out.println("access");
        		getter.startFetching(lon.floatValue(), lat.floatValue());
        	}
		});
        getter = new AltitudeDataGetter(this);
    }

	private void computeRegionMap() {
		//panelの左端をwest,右端をeast, 上をnorth, 下をsouthに対応させる。
		lonWidth = LonLatUtil.eastRegion - LonLatUtil.westRegion;
		latHeight = LonLatUtil.northRegion - LonLatUtil.southRegion;
	}

	@Override
	public void notifySuccess(double lon, double lat, double altitude) {
		lonTextField.setText(LonLatUtil.getFormattedLongitudeString(lon));
		latTextField.setText(LonLatUtil.getFormattedLatitudeString(lat));
		altitudeTextField.setText(String.valueOf(altitude));
	}

	@Override
	public void notifyNotFound(double lon, double lat) {
		lonTextField.setText(LonLatUtil.getFormattedLongitudeString(lon));
		latTextField.setText(LonLatUtil.getFormattedLatitudeString(lat));
		altitudeTextField.setText("不明");
	}

    private void initComponents() throws IOException {
        jLabel1 = new javax.swing.JLabel();
        latTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lonTextField = new javax.swing.JTextField();
        altitudeTextField = new javax.swing.JTextField();
        panel = new MyPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("緯度");

        latTextField.setEnabled(false);

        jLabel2.setText("経度");

        jLabel3.setText("標高");

        lonTextField.setEnabled(false);

        altitudeTextField.setEnabled(false);

        panel.setBackground(new java.awt.Color(204, 255, 204));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(panel);
        panel.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 161, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(49, 49, 49)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(32, 32, 32)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(latTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                            .addComponent(lonTextField)
                            .addComponent(altitudeTextField)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(38, 38, 38))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(latTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(lonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(altitudeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(15, 15, 15))
        );

        pack();
    }// </editor-fold>

    /**
     * @param args the command line arguments
     */
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
            java.util.logging.Logger.getLogger(ShowAltitudeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ShowAltitudeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ShowAltitudeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ShowAltitudeDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
			public void run() {
                ShowAltitudeDialog dialog;
				try {
					dialog = new ShowAltitudeDialog(new javax.swing.JFrame(), true);
					dialog.addWindowListener(new java.awt.event.WindowAdapter() {
						@Override
						public void windowClosing(java.awt.event.WindowEvent e) {
							System.exit(0);
						}
					});
					dialog.setVisible(true);
				} catch (HeadlessException | IOException e1) {
					e1.printStackTrace();
				}
            }
        });
    }
}
