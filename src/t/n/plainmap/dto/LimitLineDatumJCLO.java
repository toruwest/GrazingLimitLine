package t.n.plainmap.dto;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import lombok.Getter;
import lombok.Setter;
import t.n.map.common.LonLat;
import t.n.map.common.util.LonLatUtil;

public class LimitLineDatumJCLO implements ILimitLineDatum {
	private static final int HEADER_LINES_COUNT = 5;
	//年月日。
	@Getter
	private String eventDate;
	@Getter
	private String eventName;

	//接食の時刻。限界線の全範囲をスキャンして、イベントの始まりと終わりの時刻を表示する。
	@Getter
	private String eventTime;

	//以下はeventTimeの元のデータとして使う
	//最初の接食の時刻
	private StringBuilder firstEventTime;
	//最後の接食の時刻
	private StringBuilder lastEventTime;

	//恒星名
	@Getter
	private String starName;
	//恒星の等級。floatだけど、Stringのまま扱う
	@Getter
	private String starMagnitude;

	//輝面比? floatだけど、Stringのまま扱う。
	@Getter
	private String k;
//	private String luminanceRatio;

	//月の北・南どちらでの接食か？２行目の"Southern limit"、"Northern limit"で判断。
	@Getter
	private String northOrSouth;

	//接食の角度・傾き?
	@Getter
	private String PB;

	//読み取ったファイル名
	@Getter
	private final String filename;

	//月縁図
	@Getter
	private final String imageFileAbsPath;

	//緯度・経度(複数)、結ぶと直線状になる
	@Getter
	private final List<LonLat> lonlatList;

	@Getter
	private final Color color;

	@Getter @Setter
	private boolean isVisible;

	//強調表示
	@Getter @Setter
	private boolean isHilighted;

//	public LimitLineDatumJCLO(File limitLineFile, List<String> lines, Color color) {
	public LimitLineDatumJCLO(String limitLineFilename, String imageAbsPath, List<String> lines, Color color) {
//		this.filename = limitLineFile.getName();
		this.filename = limitLineFilename;
//		this.imageFileAbsPath = limitLineFile.getAbsolutePath().replace(TEXT_EXT, IMAGE_EXT);
		this.imageFileAbsPath = imageAbsPath;//.replace(TEXT_EXT, IMAGE_EXT);
		this.isVisible = true;
		this.isHilighted = false;
		this.color = color;

		for(int i = 0; i < HEADER_LINES_COUNT; i++) {
			String line = lines.get(i);

			if(i == 0) {
				String[] line0 = parseLine0(line);

				StringBuilder sb = new StringBuilder();
				sb.append(line0[1]);
				sb.append(line0[2]);
				sb.append(line0[3]);
				eventDate = sb.toString();
				eventName = line0[4];
				starName = line0[5];
				starMagnitude = line0[6];
			} else if(i == 1) {
				Scanner sc = new Scanner(line);
				String ns = sc.next();
				if("Northern".equals(ns)) {
					northOrSouth = "北";
				} else if("Southern".equals(ns)) {
					northOrSouth = "南";
				}
				sc.next();//"limit"のはず
				sc.next(); //"PB" 恒星が月のどの方向で接食となるか、なのか？潜入・出現のどちらか？
				//角度だとすると、原点はどこ？値の範囲は？(0-360 or 0から±180) 右周り・左回り？単位は多分degree
				PB = sc.next(); //"PB"の後の数値
				//二行目に"k 0.358" (多分月の輝面比luminanceRatio)のような文字列があるので、これをパースする。
				//"k"に続けて、空白なしで"-X.XXX"の形式の数字が入っていることがある。輝面比だとするとマイナスはおかしい？
				String k0 = sc.next(); //"k"あるいは"k-N.NNN"

				if(sc.hasNext()) {
					//"k X.XXX"の"X.XXX"を取り出す
					k = sc.next();
				} else {
					//先頭の"k"を取り除く
					k = k0.substring(1, k0.length());
				}

				sc.close();
			}
		}

		//以下の容量は行数から分かるので、あらかじめ確保しておく。
		lonlatList = new ArrayList<>(lines.size());
		readLimitLine(lines);
	}

	private String[] parseLine0(String line) {
		String[] result = new String[7];
		result[0] = line.substring(0, 1);
		result[1] = line.substring(2, 6);
		result[2] = line.substring(7, 9);
		result[3] = line.substring(10, 12);
		result[4] = line.substring(15, 26).trim();
		//現象名が"X"だけの場合はそのまま。"X"の前に何かある場合は"X"を除く。
		if(result[4].length() > 1 && result[4].endsWith("X")) {
			result[4] = result[4].substring(0, result[4].length() - 1).trim();
		}
		result[5] = line.substring(29, 46).trim();
		result[6] = line.substring(54, 57);

		return result;
	}

	private void readLimitLine(List<String> lines) {
		boolean isFirstEvent = true;

		for(int i = HEADER_LINES_COUNT; i < lines.size(); i++) {
			String line = lines.get(i);
			//テキストファイルの最後の数行は空白＋改行だけになっているので、取り除く。
			if(line.trim().length() > 0) {
				String[] split = line.split("\\s+");
				LonLat lonlat = parseLonLat(split);
				//TODO フォーマット異常の場合の対処
				//lonlat.isInvalid();
				lonlatList.add(lonlat);

				//最初の行が最も早い時刻で、最後の行が最も遅いようなので、最初と最後の行だけを使う。
				if(isFirstEvent) {
					firstEventTime = parseEventTime(split);
					isFirstEvent = false;
				} else {
					//最後の実行結果だけが残る。毎回処理されて無駄だけど、目をつむる。
					lastEventTime = parseEventTime(split);
				}
			}
		}
		eventTime = firstEventTime.append("-").append(lastEventTime).toString();
	}

	private StringBuilder parseEventTime(String[] split) {
		StringBuilder sb = new StringBuilder();
		sb.append(split[7]);
		sb.append(":");
		sb.append(split[8]);
		sb.append(":");
		sb.append(split[9]);

		return sb;
	}

	/**
	 * 注意：２０１５年の接食限界線のテキストファイルに、緯度が60以上となっているデータがあった。
	 * 本来は59以下のはずだが、これをエラーとして扱うと不都合なことになるので、目をつむるようにした。
	 * @param split
	 * @return
	 */
	private LonLat parseLonLat(String[] split) {
		double lon = LonLatUtil.parseDegMinSec(split[0], split[1], "0");
		double lat = LonLatUtil.parseDegMinSec(split[2], split[3], split[4]);
		LonLat lonLat = new LonLat(lon, lat);
		if(lonLat.isInvalid()) {
			System.out.println();
		}
		return lonLat;
	}

	@Override
	public String getLabel() {
		StringBuffer sb = new StringBuffer();
		sb.append(eventDate);
		sb.append(" ");
		sb.append(eventTime);
		sb.append(",");
		sb.append(eventName);
		sb.append(",");
		sb.append(starMagnitude);
		sb.append(",");
		sb.append(northOrSouth);

		return sb.toString();
	}

	@Override
	public String getListData() {
		StringBuffer sb = new StringBuffer();
		sb.append(eventDate);
		sb.append(" : ");
		sb.append(eventName);

		return sb.toString();
	}
}
