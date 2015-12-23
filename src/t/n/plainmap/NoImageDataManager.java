package t.n.plainmap;

import static t.n.plainmap.AppConfig.NO_PHOTO_IMAGE_AREA_DAT;
import static t.n.plainmap.AppConfig.NO_STANDARD_IMAGE_AREA_DAT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import t.n.map.common.LightWeightTile;

public class NoImageDataManager {

	private static final File serializedStandardFile = new File(AppConfig.getAppDataDir(), NO_STANDARD_IMAGE_AREA_DAT);
	private static final File serializedPhotoFile = new File(AppConfig.getAppDataDir(), NO_PHOTO_IMAGE_AREA_DAT);

	public static Set<LightWeightTile> loadStandardData() {
		return load(serializedStandardFile);
	}

	public static Set<LightWeightTile> loadPhotoData() {
		return load(serializedPhotoFile);
	}

	public static void saveStandardData(Set<LightWeightTile> data) throws IOException {
		save(data, serializedStandardFile);
	}

	public static void savePhotoData(Set<LightWeightTile> data) throws IOException {
		save(data, serializedPhotoFile);
	}

	private static Set<LightWeightTile> load(File file) {
		if(file.exists()) {
			HashSet<LightWeightTile> obj = null;
			try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
				obj = (HashSet<LightWeightTile>) ois.readObject();
			} catch (Exception e) {
				return new HashSet<>();
			}
			return obj;
		} else {
			return new HashSet<>();
		}
	}

	private static void save(Set<LightWeightTile> noImageDataSet, File file) throws IOException {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
			oos.writeObject(noImageDataSet);
			oos.close();
		}
	}

}
