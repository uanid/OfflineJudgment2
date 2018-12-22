package kr.dpmc.offlinejudgment.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import kr.dpmc.offlinejudgment.MetaDataHW;
import kr.dpmc.offlinejudgment.MetaDataHW.File2;
import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class HomeworkNormalization {

	public static void Normalization(YamlConfiguration config, int classNumber) throws Exception {
		File downloads = new File(config.getString("Á¤±ÔÈ­.´Ù¿î·ÎµåÆú´õ"));
		// File normalizationDownloads = new File(config.getString("Á¤±ÔÈ­.´Ù¿î·ÎµåÁ¤¸®Æú´õ"));
		File normalizationScore = new File(config.getString("Ã¤Á¡.Á¦ÃâÆú´õ"));
		List<MetaDataHW> metaDatas = new LinkedList<>();
		List<File> metaFiles = new LinkedList<>();

		loadHomeworkMetaData(metaDatas, metaFiles, downloads, normalizationScore, config);
		OJApi.printSwagWithStars("¸ŞÅ¸ ÆÄÀÏ ºÒ·¯¿À±â ¼º°ø", 50);
		// ¸ŞÅ¸ ÆÄÀÏ¸¸ metaDatas·Î ºÒ·¯¿Å

		// homeworkFileNormalization(metaDatas, normalizationDownloads);
		homeworkFileNormalizationWithUnzip(metaDatas, normalizationScore);
		OJApi.printSwagWithStars("°úÁ¦ ÆÄÀÏ Á¤±ÔÈ­ ¿Ï·á", 50);
		// ´Ù¿î·ÎµåÁ¤¸®Æú´õ·Î °úÁ¦ÆÄÀÏ º¹»çÇÏ°í ÀÌ¸§ Á¤±ÔÈ­ ÇÔ

		addNotAssignmentStudents(metaDatas, classNumber, config);
		OJApi.printSwagWithStars("¹ÌÁ¦ÃâÀÚ ¸ñ·Ï È®º¸", 50);
		// ¹ÌÁ¦ÃâÀÚ ¸ñ·Ï metaDatas¿¡ ³ÖÀ½

		OJApi.printSwagWithStars("¿¢¼¿ ÆÄÀÏ·Î ³»º¸³»±â ½ÃÀÛ", 50);
		SummarizeMetaDatas(metaDatas, config);
		OJApi.printSwagWithStars("Á¦Ãâ µ¥ÀÌÅÍ ¿ä¾à °á°ú ÆÄÀÏ »ı¼º", 50);
		// Á¦Ãâ ¸ŞÅ¸ µ¥ÀÌÅÍ ¿¢¼¿·Î ¿ä¾àÇØ¼­ ÀúÀå
	}

	public static void loadHomeworkMetaData(List<MetaDataHW> metaDatas, List<File> metaFiles, File downloads, File normalizationDownloads, YamlConfiguration config) {
		if (!downloads.exists())
			downloads.mkdirs();
		if (!normalizationDownloads.exists())
			normalizationDownloads.mkdirs();
		// Æú´õ ¾øÀ¸¸é »ı¼º

		for (File file : downloads.listFiles()) {
			if (file.getName().matches("^[\\s\\w°¡-ÆR]+_\\d+_È®ÀÎ_\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}.txt")) {
				metaFiles.add(file);
				metaDatas.add(new MetaDataHW(file));
			}
		}
	}

	public static void homeworkFileNormalization(List<MetaDataHW> metaDatas, File normalizationDownloads) throws Exception {
		for (MetaDataHW meta : metaDatas) {
			if (meta.files != null) {
				if (meta.files.size() == 1) {
					File2 f2 = meta.files.get(0);
					String extension = OJApi.getFileExtension(f2.original.getName());
					File to = new File(normalizationDownloads, meta.id + " " + meta.name + "." + extension);
					File source = f2.newer;
					OJApi.fileCopy(source, to);
				} else if (meta.files.size() >= 2) {
					File toParent = new File(normalizationDownloads, meta.id + " " + meta.name);
					if (!toParent.exists()) {
						toParent.mkdirs();
					}
					for (File2 f2 : meta.files) {
						File to = new File(toParent, f2.original.getName());
						File source = f2.newer;
						OJApi.fileCopy(source, to);
					}
				} else {
					OJApi.printSwagWithAccent(meta.id + " " + meta.name + " °úÁ¦ Á¦Ãâ ÆÄÀÏ 0°³ÀÓ");
				}
			} // files nullÀÌ¸é ¸ŞÅ¸ °úÁ¦ µ¥ÀÌÅÍ Ã³¸® ºÒ°¡
		}
	}// ¾ĞÃàÀº ¾ÈÇ®À½

	public static int moveFileWithUnzip(File older, File folder) throws Exception {
		// olderÀÌ zipÆÄÀÏÀÌ¶ó¸é newerÆÄÀÏÀÌ ¾Æ´Ñ, newerÆú´õ¿¡ ¾ĞÃàÇ®±â
		int count = 0;// ¿Å±ä ÆÄÀÏ °¹¼ö

		count += OJApi.unZipIt(older.getPath(), folder.getPath());
		for (File file : folder.listFiles()) {
			if (file.getName().endsWith(".zip")) {
				String newFolderName = file.getName().substring(0, file.getName().lastIndexOf('.'));
				File newFolder = new File(newFolderName);
				newFolder.mkdirs();
				count += moveFileWithUnzip(file, newFolder);
			}
		}

		return count;
	}

	public static void homeworkFileNormalizationWithUnzip(List<MetaDataHW> metaDatas, File normalizationDownloads) throws Exception {
		for (MetaDataHW meta : metaDatas) {
			if (meta.files != null) {
				if (meta.files.size() >= 1) {
					try {
						File toParent = new File(normalizationDownloads, meta.id + " " + meta.name);
						if (!toParent.exists()) {
							toParent.mkdirs();
						} // ÇĞ¹ø ÀÌ¸§ Æú´õ »ı¼º

						if (meta.files.size() == 1) {
							meta.isSubmitFileZiped = meta.files.get(0).newer.getName().endsWith(".zip");
						} // ¾ĞÃàÆÄÀÏ·Î Á¦ÃâÇß´ÂÁö °Ë»ç

						for (File2 f2 : meta.files) {
							if (f2.newer.getName().endsWith(".zip")) {
								int count = moveFileWithUnzip(f2.newer, toParent);
								if (count == 0) {
									count = 1;
								}
								meta.outputFilesCount += count;
								// zipÆÄÀÏÀÌ¸é ¾ĞÃàÇ®±â
							} else {
								OJApi.fileCopy(f2.newer, new File(toParent, f2.original.getName()));
								meta.outputFilesCount += 1;
								// ¾Æ´Ï¸é ±×³É ÆÄÀÏ Ä«ÇÇ
							}

							// ºí·¢º¸µå ÆÄÀÏ¿¡¼­ Ã¤Á¡Æú´õ·Î ÀÌµ¿
						}
					} catch (Exception e) {
						OJApi.printSwagWithAccent(meta.id + " " + meta.name + " °úÁ¦ Á¦Ãâ ¾ĞÃà ÇØÁ¦ µµÁß ¿À·ù ¹ß»ı: \"" + meta.id + " " + meta.name + "\"Æú´õ¿¡ ±×´ë·Î ¿Å°å½À´Ï´Ù.");
						File folder = new File(normalizationDownloads.getPath(), meta.id + " " + meta.name);
						folder.mkdirs();
						for (File2 file : meta.files) {
							OJApi.fileCopy(file.newer, new File(folder, file.original.getName()));
						}
					}

				} else {
					OJApi.printSwagWithAccent(meta.id + " " + meta.name + " °úÁ¦ Á¦Ãâ ÆÄÀÏ 0°³ÀÓ");
				}
			} // files nullÀÌ¸é ¸ŞÅ¸ °úÁ¦ µ¥ÀÌÅÍ Ã³¸® ºÒ°¡
		}
	}// ¾ĞÃàµµ Ç®À½

	public static void addNotAssignmentStudents(List<MetaDataHW> metaDatas, int classNumber, YamlConfiguration config) {
		if (classNumber > 0) {
			// classnumÀÌ 0,-1ÀÌ¸é ÀÌ °úÁ¤ ³Ñ¾î°¨

			List<String[]> studentList = OJApi.getStudentList(config, classNumber);

			for (int i = 0; i < studentList.size(); i++) {
				String[] args = studentList.get(i);
				String id = args[0];
				String name = args[1];
				// System.out.println(id + " " + name + " Ãâ¼®ºÎ¿¡¼­ ÀĞÀ½");

				boolean isContains = false;
				for (MetaDataHW meta : metaDatas) {
					if (meta.id.equals(id))
						isContains = true;
				} // ±âÁ¸ ¸ŞÅ¸ µ¥ÀÌÅÍ¿¡ °ãÄ¡´ÂÁö °Ë»ç

				if (!isContains) {
					metaDatas.add(new MetaDataHW(id, name));
				} // ±âÁ¸ ¸ŞÅ¸ µ¥ÀÌÅÍ¿¡ ¾øÀ¸¹Ç·Î ¹«½ÃÇÔ
			}
			metaDatas.sort(OJApi.comparatorMeta);// ÇĞ¹ø¼øÀ¸·Î ¿Ã¸²Â÷¼ø Á¤·ÄÇÏ´Â°Å
		}
	}

	public static void SummarizeMetaDatas(List<MetaDataHW> metaDatas, YamlConfiguration config) throws Exception {
		File excelFile = new File(config.getString("Á¤±ÔÈ­.°á°úÆÄÀÏ"));
		if (excelFile.exists()) {
			if (!excelFile.delete()) {
				OJApi.printSwagWithAccent("°á°úÆÄÀÏ »èÁ¦°¡ ºÒ°¡´ÉÇÕ´Ï´Ù.");
			}
		}

		XSSFWorkbook workBook = new XSSFWorkbook();
		POIXMLProperties xmlProps = workBook.getProperties();
		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
		coreProps.setCreator("DPmc");
		// ÀúÀÚ ¼³Á¤

		XSSFSheet sheet = workBook.createSheet("meta");
		int rowIndex = 0;
		XSSFRow row = sheet.createRow(rowIndex);
		XSSFCell cell;
		String[] args = new String[] { "ÀÌ¸§", "°úÁ¦", "ÆÄÀÏ °¹¼ö", "zipÁ¦Ãâ¿©ºÎ", "Á¦Ãâ ³¯Â¥", "´ñ±Û" };
		// Á¦Ãâ³¯Â¥¶û Á¦ÃâÇÊµå´Â ¾ø¾ÖÀÚ
		for (int i = 0; i < args.length; i++) {
			cell = row.createCell(i);
			cell.setCellValue(args[i]);
		} // ¸Ç À­ÁÙ µ¥ÀÌÅÍ ÀÔ·Â

		int num = 0;
		for (int i = 0; i < metaDatas.size(); i++) {
			MetaDataHW meta = metaDatas.get(i);
			row = sheet.createRow(i + 1);
			row.createCell(0).setCellValue(Integer.valueOf(meta.id));
			row.createCell(1).setCellValue(meta.name);
			row.createCell(2).setCellValue(meta.outputFilesCount);
			row.createCell(3).setCellValue(meta.isSubmitFileZiped);
			row.createCell(4).setCellValue(meta.submitDate);
			for (int j = 0; j < meta.comment.size(); j++) {
				row.createCell(5 + j).setCellValue(meta.comment.get(j));
			}
			num++;
			if (num % 10 == 0) {
				OJApi.printSwagWithStars(num + "¸í ÀÛ¼º ¿Ï·á", 50);
			}
		} // ÇĞ»ıº° µ¥ÀÌÅÍ ÀÔ·Â

		try {
			workBook.write(new FileOutputStream(excelFile));
			workBook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
