import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;

import t.n.gps.GpsLocationInfoDetector;
import t.n.map.common.LocationInfo;


public class GpsSentenceTest {

	@Test
	public void test() throws IOException {
		File f = new File("GPS-data.txt");
		List<String> lines = Files.readAllLines(f.toPath());
		for(String line : lines) {
			if(GpsLocationInfoDetector.isContainsLocationInfo(line)) {
				LocationInfo locationInfo = GpsLocationInfoDetector.detect(line);
				if(!Float.isNaN((locationInfo.getLatInDegrees())) && !Float.isNaN((locationInfo.getLonInDegrees())) ) {
					System.out.println(locationInfo.getLonInDegMinSec() + ", " + locationInfo.getLatInDegMinSec());
				}
			}
		}
	}

}
