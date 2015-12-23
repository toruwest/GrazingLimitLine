package t.n.plainmap;

import static t.n.plainmap.view.LimitLineTable.TEXT_EXT;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import t.n.plainmap.dto.LimitLineDatum;
import t.n.plainmap.util.LimitLineColorUtil;

public class LimitLineReader {

//	private static final String TEXT_SUFFIX = ".txt";
	private final List<LimitLineDatum> data;

	public LimitLineReader(File dataDir) throws IOException {
		data = new ArrayList<>();
		if(dataDir.exists() && dataDir.isDirectory()) {
			int colorIndex = 0;
			for(File f : dataDir.listFiles()) {
				if(f.getName().endsWith(TEXT_EXT)) {
					Color c = LimitLineColorUtil.getLineColor(colorIndex++);
					data.add(new LimitLineDatum(f, c));
				}
			}
		}
	}

	public List<LimitLineDatum> readLimitLines() {
		return data;
	}
}
