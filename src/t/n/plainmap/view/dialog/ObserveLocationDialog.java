package t.n.plainmap.view.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;

import t.n.plainmap.dto.ILimitLineDatum;
import t.n.plainmap.dto.LimitLineDatumJCLO;
import t.n.plainmap.view.IDialogStatusObserver;

public class ObserveLocationDialog extends JDialog {
	class MyListModel extends DefaultListModel<String> {
		public MyListModel() {
			super();
		}

		public void setData(List<ILimitLineDatum> limitLineData) {
			for(ILimitLineDatum l : limitLineData) {
				addElement(l.getListData());
			}
		}
	}

	private JButton closeButton;
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JList<String> eventNameDateList;
	private JScrollPane jScrollPane1;
	private JTextField locationTextField;
	private MyListModel model;

	public ObserveLocationDialog(JFrame frame, final IDialogStatusObserver observer) {
		super(frame);

		initComponents();

		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				eventNameDateList.clearSelection();
				locationTextField.setText("");
				setVisible(false);
				observer.notifyObserveLocationDialogClosed();
			}
		});

		pack();
	}

	public void setLimitLineData(List<ILimitLineDatum> limitLineData) {
		model.setData(limitLineData);
	}

	public String[] getEventDateName() {
		String tmp = eventNameDateList.getSelectedValue();
		return tmp.split(" : ");
	}

	//以前に入力した観測地は、このタイミングでクリアする。
	public String getObserveLocation() {
		String tmp = locationTextField.getText();
		return tmp;
	}

	@SuppressWarnings("unchecked")
	private void initComponents() {

		closeButton = new JButton();
		jScrollPane1 = new JScrollPane();
		eventNameDateList = new JList<>();
		locationTextField = new JTextField();
		jLabel1 = new JLabel();
		jLabel1.setText("観測地");
		jLabel2 = new JLabel();
		jLabel2.setText("日付と現象名");
		jLabel3 = new JLabel();
		jLabel3.setText("日付と現象名を選び、観測地を入力してから、地図をクリックすると、観測地の一覧に反映されます。");

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		closeButton.setText("閉じる");

		model = new MyListModel();
		eventNameDateList.setModel(model);

		jScrollPane1.setViewportView(eventNameDateList);


		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel1, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(locationTextField, GroupLayout.DEFAULT_SIZE, 313, Short.MAX_VALUE))
                        .addComponent(jScrollPane1)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(jLabel2)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(jLabel3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGap(18, 18, 18)
                            .addComponent(closeButton)))
                    .addContainerGap())
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                    .addGap(9, 9, 9)
                    .addComponent(jLabel2)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(locationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(closeButton)
                        .addComponent(jLabel3))
                    .addGap(10, 10, 10))
            );

            pack();
		pack();
	}
}

