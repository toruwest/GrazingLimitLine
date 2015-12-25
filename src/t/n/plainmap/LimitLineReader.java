package t.n.plainmap;

import static t.n.plainmap.view.LimitLineTable.TEXT_EXT;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import t.n.plainmap.dto.ILimitLineDatum;
import t.n.plainmap.dto.LimitDataLineType;
import t.n.plainmap.dto.LimitLineDatumJCLO;
import t.n.plainmap.dto.LimitLineDatumOccult4;
import t.n.plainmap.util.LimitLineColorUtil;

public class LimitLineReader {

	private static final String JCLO_HEADER = "  Long     Lat       -1\"     JST     Alt   Azi   tanZ   AS   PA     AA    CA    ";
	private static final String OCCULT4_HEADER = "E. Longit.   Latitude       U.T.    Sun  Moon   TanZ   PA    AA      CA";

	private final List<ILimitLineDatum> data;

	public LimitLineReader(File dataDir) throws IOException {
		data = new ArrayList<>();
		if(dataDir.exists() && dataDir.isDirectory()) {
			int colorIndex = 0;
			for(File f : dataDir.listFiles()) {
				if(f.getName().endsWith(TEXT_EXT)) {
					Color c = LimitLineColorUtil.getLineColor(colorIndex++);
					List<String> lines = Files.readAllLines(f.toPath());
					ILimitLineDatum datum = null;
					switch(checkDataFileType(lines)) {
						case jclo:
							datum = new LimitLineDatumJCLO(f, lines, c);
							break;
						case occult4:
							datum = new LimitLineDatumOccult4(f, lines, c);
							break;
						default:
							datum = null;
					}
					if(datum != null) {
						data.add(datum);
					}
				}
			}
		}
	}

	private LimitDataLineType checkDataFileType(List<String> lines) {
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

	public List<ILimitLineDatum> readLimitLines() {
		return data;
	}
}
