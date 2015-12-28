package t.n.plainmap;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.sun.xml.internal.bind.api.impl.NameConverter.Standard;

import lombok.Getter;
import t.n.plainmap.dto.ILimitLineDatum;
import t.n.plainmap.dto.LimitDataLineType;
import t.n.plainmap.dto.LimitLineDatumJCLO;
import t.n.plainmap.dto.LimitLineDatumOccult4;
import t.n.plainmap.util.LimitLineColorUtil;
import static t.n.plainmap.dto.ILimitLineDatum.*;

public class LimitLineReader {

	private static final String JCLO_HEADER = "  Long     Lat       -1\"     JST     Alt   Azi   tanZ   AS   PA     AA    CA    ";
	private static final String OCCULT4_HEADER = "E. Longit.   Latitude       U.T.    Sun  Moon   TanZ   PA    AA      CA";
	@Getter
	private final List<ILimitLineDatum> limitLineData;

	// Occult4のZIPで圧縮されたファイルも扱かう。
	public LimitLineReader(File dataDirOrZipFile) throws IOException {
		limitLineData = new ArrayList<>();

		if(dataDirOrZipFile.exists()) {
			if(dataDirOrZipFile.isDirectory()) {
				try (DirectoryStream<Path> stream = Files.newDirectoryStream(dataDirOrZipFile.toPath())) {
					for(Path entry :stream) {
						//textファイルかどうかの判定は、メソッド内で行う。
						gen(entry);
					}
				}
			} else {
				//zipファイルを扱う。いったんZipファイルを開いたら、中のファイルを処理するのはフォルダー内のファイルを扱うのと同じように扱えるはず。
				//FileSystemクラスが使える(http://www.ne.jp/asahi/hishidama/home/tech/java/zip.html#h_ZipFileSystem)
				Path zipPath = dataDirOrZipFile.toPath();
				try (FileSystem fs = FileSystems.newFileSystem(zipPath, ClassLoader.getSystemClassLoader())) {
					for (Path rootPath : fs.getRootDirectories()) {
						Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
								gen(file);
								if(file.toString().endsWith(IMAGE_EXT)) {
									//ZIPファイルの中にあるイメージをappDirの下のフォルダー内に解凍しておく。
									unzipImage(file);
								}
								return FileVisitResult.CONTINUE;
							}
						});
					}
				}
			}
		}
	}

	private int colorIndex = 0;
	private void gen(Path entry) throws IOException {
		String fileName = entry.getFileName().toString();
		if(fileName.endsWith(TEXT_EXT)) {
			Color c = LimitLineColorUtil.getLineColor(colorIndex++);
			List<String> lines = Files.readAllLines(entry);
			ILimitLineDatum datum = null;
			switch(checkDataFileType(lines)) {
			case jclo:
				datum = new LimitLineDatumJCLO(fileName, fileName.replace(TEXT_EXT, IMAGE_EXT), lines, c);
				break;
			case occult4:
				//2番目の引数は、ZIPを展開後のパスを指すようにする。このファイルが存在するかどうかは、呼び出し元で判断しているので、ここでは気にする必要がない。
				datum = new LimitLineDatumOccult4(fileName, getImageAbsPath(fileName), lines, c);
				break;
			default:
				datum = null;
			}
			if(datum != null) {
				limitLineData.add(datum);
			}
		}
	}

	private void unzipImage(Path file) {
		//ファイルがあったとしても上書きする
		File moonLimbImageFile = new File(AppConfig.getMoonLimbImageDir(), file.toString());
		try {
			byte[] bytes = Files.readAllBytes(file);
			//TODO 以下のオプションではファイルが作成されない。
//			Files.write(moonLimbImageFile.toPath(), bytes,
//					StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.DELETE_ON_CLOSE);
			Files.write(moonLimbImageFile.toPath(), bytes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//ZIPファイルの中のイメージを解凍して格納するファイル名を決める（解凍は別途)
	private String getImageAbsPath(String textFileName) {
		File moonLimbImageFile = new File(AppConfig.getMoonLimbImageDir(), textFileName.replace(TEXT_EXT,  IMAGE_EXT));
		String path = moonLimbImageFile.getAbsolutePath();
		return path;
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

}
