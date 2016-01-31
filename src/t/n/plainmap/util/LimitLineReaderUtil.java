package t.n.plainmap.util;

import java.util.List;

import t.n.plainmap.dto.LimitDataLineType;

public class LimitLineReaderUtil {
	private static final String JCLO_HEADER = "  Long     Lat       -1\"     JST     Alt   Azi   tanZ   AS   PA     AA    CA    ";
	private static final String OCCULT4_HEADER = "E. Longit.   Latitude       U.T.    Sun  Moon   TanZ   PA    AA      CA";

	public static LimitDataLineType checkDataFileType(List<String> lines) {
		if(lines.size() > 7) {
			//JCLO
			String line = lines.get(3);
			if(line.equals(JCLO_HEADER)) {
				return LimitDataLineType.jclo;
			} else {
				line = lines.get(6);
				if(line.equals(OCCULT4_HEADER)) {
					return LimitDataLineType.occult4;
				}
			}
		}
		return LimitDataLineType.unknown;
	}

}
