package t.n.plainmap.dto;

import static org.junit.Assert.*;
import static t.n.plainmap.dto.ILimitLineDatum.IMAGE_EXT;
import static t.n.plainmap.dto.ILimitLineDatum.TEXT_EXT;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;

import org.junit.Test;

import t.n.plainmap.LimitLineReader;
import t.n.plainmap.util.LimitLineColorUtil;
import t.n.plainmap.util.LimitLineReaderUtil;
import t.n.plainmap.util.OccultEventDateTimeUtil;

public class LimitLineDatumOccult4Test {

	private static final int 時差 = 9;

	@Test
	public void testParse() throws IOException {
		List<ILimitLineDatum> data = new ArrayList<>();
		File dataDir = new File("file");
		LimitLineReader reader = new LimitLineReader(dataDir);

		if(dataDir .exists() && dataDir.isDirectory()) {
			int colorIndex = 0;
			for(File f : dataDir.listFiles()) {
				Color c = LimitLineColorUtil.getLineColor(colorIndex++);
				List<String> lines = Files.readAllLines(f.toPath());
				String fileName = f.getAbsolutePath();

				ILimitLineDatum datum = null;
				//内容でJCLO, Occult4のどちらのフォーマットかを判定する。
				switch(getPredictionDataFileType(f, reader, lines)) {
				case jclo:
					datum = new LimitLineDatumJCLO(fileName, fileName.replace(TEXT_EXT, IMAGE_EXT), lines, c);
					break;
				case occult4:
					datum = new LimitLineDatumOccult4(fileName, fileName.replace(TEXT_EXT, IMAGE_EXT), lines, c);
					break;
				default:
					//TODO
				}
				data.add(datum);
			}
		} else {
			fail("ディレクトリfileが見つかりません。ここにテスト対象のファイルを格納してあります。");
		}
	}

	private LimitDataLineType getPredictionDataFileType(File f, LimitLineReader reader, List<String> lines) {
		LimitDataLineType type = LimitDataLineType.unknown;

		if(f.getName().endsWith(ILimitLineDatum.TEXT_EXT)) {
			type = LimitLineReaderUtil.checkDataFileType(lines);
		}
		return type;
	}

	@Test
	public void testRexexp() {
		String arg = "Date: 2015 Dec 16 10h 54m,  to  2015 Dec 16 10h 57m";
		Matcher m = LimitLineDatumOccult4.EVENT_PERIOD_PATTERN.matcher(arg);
		assertTrue(m.matches());
		assertEquals(2, m.groupCount());
		assertEquals("2015 Dec 16 10h 54m", m.group(1));
		assertEquals("2015 Dec 16 10h 57m", m.group(2));
	}

	@Test
	public void testUtil() throws ParseException {
		System.out.println(new Date());
		Date time00 = OccultEventDateTimeUtil.parseDateTime("2015 Dec 16 10h 54m");
		Calendar time0 = Calendar.getInstance();
		time0.setTime(time00);
		//TODO
		System.out.println(time0.getTime().toString());
		Date time10 = OccultEventDateTimeUtil.parseDateTime("2015 Dec 16 10h 57m");
		Calendar time1 = Calendar.getInstance();
		time1.setTime(time10);
		System.out.println(time1.getTime());

		assertEquals(2015, time0.get(Calendar.YEAR));
		//月は0から始まる。アホかい！
		assertEquals(11, time0.get(Calendar.MONTH));
		assertEquals(16, time0.get(Calendar.DAY_OF_MONTH));
		//以下はUTCとJSTの時差により
		assertEquals(10 + 時差, time0.get(Calendar.HOUR_OF_DAY));
		assertEquals(54, time0.get(Calendar.MINUTE));

		assertEquals(2015, time1.get(Calendar.YEAR));
		assertEquals(11, time1.get(Calendar.MONTH));
		assertEquals(16, time1.get(Calendar.DAY_OF_MONTH));
		assertEquals(10 + 時差, time1.get(Calendar.HOUR_OF_DAY));
		assertEquals(57, time1.get(Calendar.MINUTE));
	}

}
