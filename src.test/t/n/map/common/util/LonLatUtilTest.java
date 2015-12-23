package t.n.map.common.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import t.n.map.common.LonLat;

public class LonLatUtilTest {

	//以下はhttp://www.gsi.go.jp/KOKUJYOHO/center.htmより抜粋（世界測地系)
	//  「′」と「″」は引用符とは別の文字
	private final static String eastBoundaryString = "153°59′11″";//最東端 	東京都　南鳥島
	private final static String westBoundaryString = "122°56′01″";//最西端 	沖縄県　与那国島
	private final static String southBoundaryString = "20°25′31″";//最南端 	東京都　沖ノ鳥島
	private final static String northBoundaryString = "45°33′28″";//最北端 	北海道　択捉島
	private final static double northBoundary;
	private final static double eastBoundary;
	private final static double southBoundary;
	private final static double westBoundary;

	static {
		eastBoundary = LonLatUtil.parseDegMinSec(eastBoundaryString);
		westBoundary = LonLatUtil.parseDegMinSec(westBoundaryString);
		northBoundary = LonLatUtil.parseDegMinSec(northBoundaryString);
		southBoundary = LonLatUtil.parseDegMinSec(southBoundaryString);
	}

	@Test
	public void testDegMinSecFraction0() {
		assertEquals(153.98638888888888, LonLatUtil.parseDegMinSec(eastBoundaryString), 1e-5);
	}

	@Test
	public void testDegMinSecFraction1() {
		//http://www.benricho.org/map_latlng_10-60conv/
		assertEquals(139.74544277777778, LonLatUtil.parseDegMinSec("139°44′43.594″"), 1e-5);
	}

	@Test
	public void testIsOutOfBoundsNorthEast0() {
		assertFalse(LonLatUtil.isOutOfJapanRegion(new LonLat(eastBoundary - 1, northBoundary - 1)));
	}

	@Test
	public void testIsOutOfBoundsNorthEast1() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(eastBoundary - 1, northBoundary + 1)));
	}

	@Test
	public void testIsOutOfBoundsNorthEast2() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(eastBoundary + 1, northBoundary - 1)));
	}

	@Test
	public void testIsOutOfBoundsNorthEast3() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(eastBoundary + 1, northBoundary + 1)));
	}

	@Test
	public void testIsOutOfBoundsNorthWest0() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(westBoundary - 1, northBoundary - 1)));
	}

	@Test
	public void testIsOutOfBoundsNorthWest1() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(westBoundary - 1, northBoundary + 1)));
	}

	@Test
	public void testIsOutOfBoundsNorthWest2() {
		assertFalse(LonLatUtil.isOutOfJapanRegion(new LonLat(westBoundary + 1, northBoundary - 1)));
	}

	@Test
	public void testIsOutOfBoundsNorthWest3() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(westBoundary + 1, northBoundary + 1)));
	}

	@Test
	public void testIsOutOfBoundsSouthEast0() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(eastBoundary - 1, southBoundary - 1)));
	}

	@Test
	public void testIsOutOfBoundsSouthEast1() {
		assertFalse(LonLatUtil.isOutOfJapanRegion(new LonLat(eastBoundary - 1, southBoundary + 1)));
	}

	@Test
	public void testIsOutOfBoundsSouthEast2() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(eastBoundary + 1, southBoundary - 1)));
	}

	@Test
	public void testIsOutOfBoundsSouthEast3() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(eastBoundary + 1, southBoundary + 1)));
	}

	@Test
	public void testIsOutOfBoundsSouthWest0() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(westBoundary - 1, southBoundary - 1)));
	}

	@Test
	public void testIsOutOfBoundsSouthWest1() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(westBoundary - 1, southBoundary + 1)));
	}

	@Test
	public void testIsOutOfBoundsSouthWest2() {
		assertTrue(LonLatUtil.isOutOfJapanRegion(new LonLat(westBoundary + 1, southBoundary - 1)));
	}

	@Test
	public void testIsOutOfBoundsSouthWest3() {
		assertFalse(LonLatUtil.isOutOfJapanRegion(new LonLat(westBoundary + 1, southBoundary + 1)));
	}

	@Test
	public void testUnparsableFormatText() {
		LonLat actual = LonLatUtil.parseLonLatDegMinSec("abc def");
		assertEquals(Double.NaN, actual.getLongitude(), 0);
		assertEquals(Double.NaN, actual.getLatitude(), 0);
	}
}
