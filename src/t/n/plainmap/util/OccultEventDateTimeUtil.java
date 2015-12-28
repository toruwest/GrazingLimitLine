package t.n.plainmap.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class OccultEventDateTimeUtil {

	static SimpleDateFormat parseFormatter;
	static SimpleDateFormat dateFormatter;
	static SimpleDateFormat timeFormatter;

	static {
		//MMMにより、米国式のJan, Febなどを月に変換する。
		parseFormatter = new SimpleDateFormat("yyyy MMM dd HH mm", Locale.ENGLISH);
		//TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
		//上は実行中のJavaプログラム全てに影響するようだ。
		parseFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		dateFormatter = new SimpleDateFormat("yyyyMMdd");
		timeFormatter = new SimpleDateFormat("HH:mm");
	}

	public static Date parseDateTime(String line) throws ParseException {
		return parseFormatter.parse(line.replace("h", "").replace("m", ""));
	}

	public static String formatDate(Date firstEventTime) {
		return dateFormatter.format(firstEventTime);
	}

	public static String formatTime(Date firstEventTime, Date lastEventTime) {
		StringBuilder sb = new StringBuilder();

		sb.append(timeFormatter.format(firstEventTime));
		sb.append(" - ");
		sb.append(timeFormatter.format(lastEventTime));

		return sb.toString();
	}

}
