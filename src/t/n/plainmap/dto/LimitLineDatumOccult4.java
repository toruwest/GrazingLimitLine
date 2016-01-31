package t.n.plainmap.dto;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import t.n.map.common.LonLat;
import t.n.map.common.util.LonLatUtil;
import t.n.plainmap.util.OccultEventDateTimeUtil;

public class LimitLineDatumOccult4 implements ILimitLineDatum {
	private static final String ILLUMINATION_OF_MOON = "Illumination of moon";
	private static final int HEADER_LINES_COUNT = 8;
	protected static final Pattern EVENT_PERIOD_PATTERN = Pattern.compile("Date: ([\\s|\\S]+),  to  ([\\s|\\S]+)");

	//年月日。書式は"2015MMDD"
	@Getter
	private final String eventDate;
	@Getter
	private String eventName;

	//接食の時刻。タイムゾーンはJSTとする。
	//限界線の全範囲をスキャンして、イベントの始まりと終わりの時刻を表示する。
	//書式はHH:MM - HH:MM (秒単位はデータがない)
	@Getter
	private final String eventTime;

	//以下はeventTimeの元のデータとして使う
	//最初の接食の時刻
//	private StringBuilder firstEventTime;
	private Date firstEventTime;
	//最後の接食の時刻
//	private StringBuilder lastEventTime;
	private Date lastEventTime;

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

	private List<LonLat> minusOneDegLineList;

	public LimitLineDatumOccult4(String limitLineFile, String absPath, List<String> lines, Color color) {
		this.filename = limitLineFile;
		// ZIPファイルの中にあるイメージを、appDirの下に展開しておいて、それを使う。
		this.imageFileAbsPath = absPath;
		this.isVisible = true;
		this.isHilighted = false;
		this.color = color;

		for(int i = 0; i < HEADER_LINES_COUNT; i++) {
			String line = lines.get(i);

			switch(i) {
			case 0:
				String[] line0 = parseLine0(line);
				eventName = line0[0];
				starName = line0[0];
				starMagnitude = line0[1];
				break;
			case 1:
				//skip
				break;
			case 2:
				//TODO 正規表現で分解してからDateFormatで解析
				//"Date: 2015 Dec 16 10h 54m,  to  2015 Dec 16 10h 57m"
				Matcher m = EVENT_PERIOD_PATTERN.matcher(line);
				if(m.matches()) {
					try {
						firstEventTime = OccultEventDateTimeUtil.parseDateTime(m.group(1));
						lastEventTime = OccultEventDateTimeUtil.parseDateTime(m.group(2));
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				break;
			case 3:
				//skip
				break;
			case 4:
				//"Nominal site altitude 0m" skip
				break;
			case 5:
				//空行、skip
				break;
			case 6:
				//ヘッダー行、skip
				//"E. Longit.   Latitude       U.T.    Sun  Moon   TanZ   PA    AA      CA"
				break;
			case 7:
				//ヘッダー行、skip
				//"   o  '  "    o  '   "    h  m  s   Alt Alt Az          o     o      o"
				break;
			default:
			}
		}

		//以下の容量は行数から分かるので、あらかじめ確保しておく。
		lonlatList = new ArrayList<>(lines.size());
		readLimitLines(lines);
		eventDate = OccultEventDateTimeUtil.formatDate(firstEventTime);
		eventTime = OccultEventDateTimeUtil.formatTime(firstEventTime, lastEventTime);
	}

	private String[] parseLine0(String line) {
		if(line.length() < 63) return null;

		String[] result = new String[3];
		String[] tmp = new String[4];

		tmp[0] = line.substring(0, 7);
		tmp[1] = line.substring(8, 19);
		tmp[2] = line.substring(20, 22);
		result[0] = line.substring(23, 38).trim();
		tmp[3] = line.substring(38, 48);
		result[1] = line.substring(49, 52).trim();
		result[2] = line.substring(60, 63);

		assert(tmp[0].equals("Grazing"));
		assert(tmp[1].equals("Occultation"));
		assert(tmp[2].equals("of"));
		assert(tmp[3].equals("Magnitude"));

		return result;
	}

	private void readLimitLines(List<String> lines) {
		int i = 0;
		for(i = HEADER_LINES_COUNT; i < lines.size(); i++) {
			String line = lines.get(i).trim();
			//テキストファイルの最後の数行は空白＋改行だけになっているので、取り除く。
			if(line.length() > 0) {
				String[] split = line.split("\\s+");
				LonLat lonlat = parseLonLat(split);
				//TODO フォーマット異常の場合の対処
				lonlatList.add(lonlat);
			} else {
				//限界線データの終わりは空白行になっていて、この後に別のテキストが続いている。
				//これらは無視する。
				break;
			}
		}
		//TODO "Illumination of moon"という行があるので、輝面比として使う。
		for(int j = i; j < lines.size(); j++) {
			String line = lines.get(j).trim();
			if(line.startsWith(ILLUMINATION_OF_MOON)) {
				k = parseIlluminateRatio(line);
			}
		}
	}

	/**
	 * 注意：２０１５年の接食限界線のテキストファイルに、緯度が60以上となっているデータがあった。
	 * 本来は59以下のはずだが、これをエラーとして扱うと不都合なことになるので、目をつむるようにした。
	 * @param split
	 * @return
	 */
	private LonLat parseLonLat(String[] split) {
		double lon = LonLatUtil.parseDegMinSec(split[0], split[1], split[2]);
		double lat = LonLatUtil.parseDegMinSec(split[3], split[4], split[5]);
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

//	public static void main(String[] args) {
//		String illuminateRatio = parseIlluminateRatio("      Illumination of moon  26%+");
//		System.out.println(illuminateRatio);
//	}

	private static final Pattern ILLUMINATE_PARSE_PATTERN = Pattern.compile("Illumination of moon  (\\d+)%+");

	private static String parseIlluminateRatio(String string) {
		Matcher m = ILLUMINATE_PARSE_PATTERN.matcher(string);
		if(m.find()) {
			return m.group(1);
		}
		return null;
	}

	/**
	 * 未対応なので、空のListを返す。
	 */
	@Override
	public List<LonLat> getMinusOneDegLineList() {
		if(minusOneDegLineList == null) minusOneDegLineList = new ArrayList<>();
		return minusOneDegLineList;
	}
}
