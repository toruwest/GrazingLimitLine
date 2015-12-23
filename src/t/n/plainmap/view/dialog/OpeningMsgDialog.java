package t.n.plainmap.view.dialog;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import t.n.plainmap.AppConfig;

public class OpeningMsgDialog extends JDialog {
	private static final String TEXT = "<html>"+
"<center>GazingLimitLine バージョン " + AppConfig.appVersion + "</center>"+
"<center>公開日: " + AppConfig.releaseDate + "</center>"+
"<center>著作権者：toruwest</center>"+
"<h3>このソフトについて</h3>"+
"このソフトは、星食の限界線データを用いて、地図上で観測地の候補を検討するために使います。<br>" +
"従来は、同じ目的で、鈴木寿氏提供の<a href=\"http://www2.wbs.ne.jp/~spica/Grazing/2015/densi/2015GrazingMap.htm\">Webアプリ</a>が使われていましたが、これが依存している国土地理院のサービスが停止したため使えなくなりましたので、<br>"+
"代わりに使えるソフトが必要だろうということで、開発しました。<br>" +
"利用にあたっては、星食限界線のデータファイルが必要となります。月縁図についても、もしあれば表示します。<br>" +
"このソフトで観測地としてサポートしているのは、日本国内だけとなります。(ソフトの利用自体は海外在住の方でも問題ありません)<br><br>" +
"このアプリケーションソフトウェア(以下、本ソフト)は、無償で利用できます。ソースコードについても、<a href=\"http://github.com/toruwest/GazingLimitLine\">Github</a>にて、オープンソースとして公開しました。<br>"+
"<h3>インターネット接続について</h3>" +
"本ソフトでは、国土地理院の提供する地図データを参照するために、インターネットへの接続を必要とします。<br>" +
"プロキシーサーバ経由での接続については、現時点では正常に使えないという問題があり、修正を急いでいるところです。申し訳ありません。<br>" +
"<h3>データの格納先について</h3>" +
"本ソフトでは、各種データを格納するのに、" + AppConfig.getAppDataDir() + " を使っています。<br>(パソコンのOSの種類・バージョンや、ユーザーにより変わります)<br>" +
"本ソフトを使わなくなった場合は、アプリケーション本体のほか、このフォルダーを削除してください。<br>Windowsのレジストリは使っていないはずです。(これから確認します)<br>" +
"<h3>免責事項</h3>"+
"著作者は、本ソフトの計算結果が正確であることを保証しません。また、利用に伴ういかなる形での損害も保障しません。<br>"+
"<h3>地図データの利用規約について</h3>"+
"本ソフトでは、国土地理院の提供する基本測量成果を利用しています。<br>一部に、データソース：Landsat8画像(GSI,TSIC,GEO Grid/AIST), 海底地形(GEBCO)を使っています。<br>"+
"本ソフトの地図画像を、インターネットあるいは刊行物などに掲載する場合、<a href=\"http://www.gsi.go.jp/LAW/2930-index.html\">国土地理院の地図の利用手続き</a>に従い、<br>"+
"申請・承認が必要な場合があります。申請・承認が必要かどうかの判断は、各利用者の責任において行ってください。<br>"+
"<h3>謝辞</h3>" +
"限界線ルートデータを提供してくださった、<a href=\"http://www2.wbs.ne.jp/~spica/index.htm\">鈴木寿氏</a>、国立天文台の<a href=\"http://optik2.mtk.nao.ac.jp/~somamt/\">相馬氏</a>、<a href=\"http://www.chijinshokan.co.jp/\">地人書館</a>、その他<a href=\"http://astro-limovie.info/jclo/index.html\">星食観測日本地域コーディネーター</a>の関係各位に感謝いたします。<br>" +
"Occult4を提供してくださった、<a href=\"http://lunar-occultations.com/iota/occult4.htm\">David Herald氏</a>に感謝いたします。<br>" +
"<h3>本ソフトの著作権者の連絡先</h3>"+
"nishino.tooru@gmail.com<br>" +

"</html>";

	private final String title = "このソフトについて";
	private final JEditorPane ep;

	public OpeningMsgDialog(JFrame frame) {
		super(frame, true);
//		System.out.println(TEXT);
		ep = new JEditorPane("text/html", TEXT);
		ep.setEditable(false);

		ep.addHyperlinkListener(new HyperlinkListener() {
			@Override public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						Desktop.getDesktop().browse(e.getURL().toURI());
					} catch (IOException | URISyntaxException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void show() {
		JOptionPane.showMessageDialog(null, ep, title, JOptionPane.PLAIN_MESSAGE);
	}

	public static void main(String args[]) {
		try {
			for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(OpeningMsgDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(OpeningMsgDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(OpeningMsgDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(OpeningMsgDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}

		/* Create and display the dialog */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				OpeningMsgDialog dialog = new OpeningMsgDialog(new JFrame());
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