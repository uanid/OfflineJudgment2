package kr.dpmc.offlinejudgment.main;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFPatternFormatting;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.StudentHW;
import kr.dpmc.offlinejudgment.TestData;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class ScoreCheck {

	private static Random random = new Random();

	/**
	 * ¸ŞÀÎ Ã¤Á¡ ¸Ş¼­µå
	 * 
	 * @param config ÆÄÀÏ
	 * @param classNumber ºĞ¹İ ³Ñ¹ö
	 * @throws Exception
	 */
	public static void OfflineJudgmentMain(YamlConfiguration config, int classNumber) throws Exception {
		List<TestData> testDatas = getTestDatas(config);
		OJApi.printSwagWithStars("Å×½ºÆ® µ¥ÀÌÅÍ ºÒ·¯¿À±â ¿Ï·á size=" + testDatas.size(), 50);
		// Å×½ºÆ® µ¥ÀÌÅÍ ¼³Á¤

		Map<String, StudentHW> studentMap = checkStudentHwScore(testDatas, config);
		OJApi.printSwagWithStars("Ã¤Á¡ ¿Ï·á", 50);
		// ÇĞ¹ø, ÇĞ»ıÁ¤º¸ µ¥ÀÌÅÍ

		writeToSimilirarityFiles(studentMap, config);
		OJApi.printSwagWithStars("À¯»çµµ °Ë»ç¿ë ÆÄÀÏ »ı¼º ¿Ï·á", 50);
		// À¯»çµµ °Ë»ç¿ë ÆÄÀÏ·Î ÄÚµå¸¸ ³»º¸³»±â
		
		addNotAssignmentStudent(studentMap, testDatas, classNumber, config);
		OJApi.printSwagWithStars("¹ÌÁ¦ÃâÀÚ µ¥ÀÌÅÍ È®º¸", 50);
		// ¹ÌÁ¦ÃâÀÚ È®º¸

		OJApi.printSwagWithStars("¿¢¼¿·Î ³»º¸³»±â ½ÃÀÛ", 50);
		writeToExcel(studentMap, testDatas, config);
		// writeToCSV(studentMap, testDatas, config);
		OJApi.printSwagWithStars("Ã¤Á¡ °á°ú ¿¢¼¿ ÆÄÀÏ·Î ÀúÀå ¿Ï·á", 50);
		// Ã¤Á¡°á°ú ÀúÀå
		
		copyToHWSumFolder(studentMap, config);
		OJApi.printSwagWithStars("ÄÚµåÆÄÀÏ¸ğÀ½Æú´õ¿Í »çÁøÆÄÀÏ¸ğÀ½Æú´õ·Î ³»º¸³»±â ¿Ï·á", 50);
	}

	/**
	 * ÄÚµå, »çÁø ¸ğÀ½ ÆÄÀÏ·Î º¹»ç
	 * 
	 * @param studentMap
	 * @param config Ã¤Á¡.ÄÚµåÆÄÀÏ¸ğÀ½Æú´õ, Ã¤Á¡.»çÁøÆÄÀÏ¸ğÀ½Æú´õ
	 * @throws Exception
	 */
	public static void copyToHWSumFolder(Map<String, StudentHW> studentMap, YamlConfiguration config) throws Exception {
		String folder = config.getString("Ã¤Á¡.ÄÚµåÆÄÀÏ¸ğÀ½Æú´õ");
		File fFolder = new File(folder);
		if (!fFolder.exists()) {
			fFolder.mkdirs();
		}
		for (StudentHW student : studentMap.values()) {
			if (student.hwfile == null || student.fileName.equals("null")) {
				continue;
			}
			File toCopy = new File(fFolder.getPath() + "/" + student.id + " " + student.name + ".py");
			OJApi.fileCopy(student.hwfile, toCopy);
		} // ÄÚµåÆÄÀÏ ¸ğÀ½

		folder = config.getString("Ã¤Á¡.»çÁøÆÄÀÏ¸ğÀ½Æú´õ");
		fFolder = new File(folder);
		if (!fFolder.exists()) {
			fFolder.mkdirs();
		}
		for (StudentHW student : studentMap.values()) {
			if (student.screenshotFiles == null || student.screenshotFiles.size() == 0) {
				continue;
			}
			int index = 1;
			for (File file : student.screenshotFiles) {
				String extension = OJApi.getFileExtension(file.getName());
				File toCopy = new File(fFolder.getPath() + "/" + student.id + " " + student.name + " " + index + "." + extension);
				OJApi.fileCopy(file, toCopy);
			}
		} // »çÁøÆÄÀÏ ¸ğÀ½

	}

	public static void addNotAssignmentStudent(Map<String, StudentHW> studentMap, List<TestData> testDatas, int classNumber, YamlConfiguration config) {
		if (classNumber > 0) {
			List<String[]> studentList = OJApi.getStudentList(config, classNumber);
			List<String> idList = new ArrayList<>(studentMap.keySet());

			for (int i = 0; i < studentList.size(); i++) {
				String[] args = studentList.get(i);
				String id = args[0];
				String name = args[1];

				if (!idList.contains(id)) {
					StudentHW student = new StudentHW(name, id);
					student.homeworkFileScore = 1;
					student.screenshotScore = 1;
					student.fileName = "null";
					student.testcaseScore = new int[testDatas.size()];
					for (int j = 0; j < student.testcaseScore.length; j++) {
						student.testcaseScore[j] = 9;
					}
					studentMap.put(id, student);
					idList.add(id);
				} // ÇĞ»ı ¸ñ·Ï¿¡ ¾ø´Ù¸é
			}

			Collections.sort(idList, OJApi.comparatorString);

			Map<String, StudentHW> map = new LinkedHashMap<>();
			for (String id : idList) {
				map.put(id, studentMap.get(id));
			}

			studentMap.clear();
			for (String id : map.keySet()) {
				studentMap.put(id, map.get(id));
			}
			// studentMap Á¤·Ä ¿Ï·á
		}
	}

	public static void writeToSimilirarityFiles(Map<String, StudentHW> studentMap, YamlConfiguration config) throws Exception {
		File similirarity = new File(config.getString("À¯»çµµ.Á¦ÃâÆú´õ"));
		if (!similirarity.exists()) {
			similirarity.mkdirs();
		} // À¯»çµµ °Ë»ç Æú´õ

		for (StudentHW student : studentMap.values()) {
			if (student.fileName == null || student.fileName.equals("null")) {
				OJApi.printSwagWithAccent(student.id + " " + student.name + "ÇĞ»ıÀº À¯»çµµ °Ë»çÆÄÀÏ »ı¼º ºÒ°¡");
				continue;
			}

			if (student.fileName.equals("")) {
				continue;
			} // ¹ÌÁ¦ÃâÀÚ ³Ñ±è

			File file = new File(similirarity.getPath() + "/" + student.id + ".py");
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedReader br = new BufferedReader(new FileReader(student.hwfile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			String s;
			while ((s = br.readLine()) != null) {
				s = OJApi.stringLineConvertToException(s, config.getStringList("À¯»çµµ.Á¦¿Ü¹®ÀÚ"), config.getBoolean("À¯»çµµ.ÁÖ¼®Á¦°Å"));

				if (config.getBoolean("À¯»çµµ.ÅÇÁ¦°Å")) {
					s = s.replace("\t", "");
				} // ÅÇ Á¦°ÅÇÏ°í trim

				s = s.trim();

				bw.write(s);

				if (!config.getBoolean("À¯»çµµ.ÁÙ³Ñ±èÁ¦°Å")) {
					bw.write('\n');
				} // ÁÙ³Ñ±è Á¦°Å

			}
			bw.close();
			br.close();

		}
	}

	public static Map<String, StudentHW> checkStudentHwScore(List<TestData> testDatas, YamlConfiguration config) throws Exception {
		Map<String, StudentHW> studentMap = new LinkedHashMap<>();
		List<String> checkExceptionChar = config.getStringList("Ã¤Á¡.Ã¤Á¡Á¦¿Ü¹®ÀÚ");
		List<String> hwRecognizeList = config.getStringList("Ã¤Á¡.°úÁ¦ÀÎ½Ä¹®ÀÚ");
		List<String> screenshotExtension = config.getStringList("Ã¤Á¡.»çÁøÀÎ½ÄÈ®ÀåÀÚ");
		List<String> sourcecodeCheck = config.getStringList("Ã¤Á¡.¼Ò½ºÆÄÀÏ°Ë»ç");

		File parentFolder = new File(config.getString("Ã¤Á¡.Á¦ÃâÆú´õ"));
		if (!parentFolder.exists()) {
			parentFolder.mkdir();
		} // Æú´õ ¾øÀ¸¸é ¸¸µé±â
		File instFolder = new File("Ãâ·Â");
		instFolder.mkdirs();
		int num = 0;
		for (File folder : parentFolder.listFiles()) {
			// ÇĞ»ıº° Á¦Ãâ Æú´õ = folder

			if (!folder.isDirectory()) {
				OJApi.printSwagWithBraket(folder.getName() + "Àº Æú´õ°¡ ¾Æ´Ô");
				continue;
			}
			// Æú´õ°¡ ¾Æ´Ò °æ¿ì

			String fname = folder.getName();
			if (!fname.matches("[0-9]+ [0-9°¡-ÆR]+")) {
				OJApi.printSwagWithBraket(fname + " Æú´õ¿¡¼­ ÇĞ»ı ÀÌ¸§°ú ÇĞ¹ø ÀÎ½Ä ºÒ°¡´É");
				continue;
			}
			// Á¤±Ô½Ä ÀÏÄ¡ÇÏÁö ¾ÊÀ» °æ¿ì

			String[] args = fname.split(" ");
			if (args.length != 2) {
				OJApi.printSwagWithBraket(Arrays.toString(args) + " Æú´õ ÀÌ¸§¿¡¼­ ÇĞ»ı°ú ÇĞ¹ø¿Ü¿¡ ´Ù¸¥ Á¤º¸°¡ ÀÖÀ½");
				continue;
			}
			// Á¤º¸°¡ ³Ê¹« ¸¹À» °æ¿ì

			String id = args[0];
			String name = args[1];
			StudentHW student = new StudentHW(name, id);
			studentMap.put(id, student);
			// ÇĞ»ı ÀÌ¸§°ú ÇĞ¹ø Á¤º¸ ºĞ¸®

			// »çÁø ÆÄÀÏ ÃßÃâ
			student.screenshotFiles = new LinkedList<>();
			for (File file : folder.listFiles()) {
				boolean isEndwith = false;
				String fileName = file.getName().toLowerCase();
				for (String extens : screenshotExtension) {
					if (fileName.endsWith(extens)) {
						isEndwith = true;
						break;
					}
				}
				if (isEndwith) {
					student.screenshotFiles.add(file);
				}
			}

			if (student.screenshotFiles.size() >= 1) {
				student.screenshotScore = 2;
				// »çÁø ÆÄÀÏ ÀÖÀ½
			} else {
				student.screenshotScore = 1;
				// »çÁø ÆÄÀÏ ¾øÀ½
			}

			// °úÁ¦ ÆÄÀÏÀ» Ã£¾Ò´Â°¡
			boolean isFindHW = false;
			for (File hwFile : folder.listFiles()) {
				isFindHW = false;
				if (hwFile.getName().endsWith(".py")) {
					isFindHW = true;
					StringBuilder sb = OJApi.getSourceCodeToStringBuilder(hwFile, config.getStringList("Ã¤Á¡.°úÁ¦ÀÎ½ÄÁ¦¿Ü¹®ÀÚ"), true);
					for (String s : hwRecognizeList) {
						if (sb.indexOf(s) == -1) {
							isFindHW = false;
							break;
						}
					} // °úÁ¦ ÆÄÀÏ Ã£±â for

					if (isFindHW) {
						student.fileName = hwFile.getName();
						student.hwfile = hwFile;
						break;
					} // ¿øÇÏ´Â ÆÄÀÏÀ» Ã£¾ÒÀ» °æ¿ì
				}
			}
			// °úÁ¦ ÆÄÀÏ Ã£±â

			num++;
			if (num % 10 == 0) {
				//OJApi.printSwagWithBraket(num + "¸í Ã¤Á¡ ¿Ï·á");
			} // Ãâ·Â¿ë ¸Ş¼¼Áö

			if (isFindHW) {
				student.testcaseScore = new int[testDatas.size()];
				for (int i = 0; i < testDatas.size(); i++) {
					student.testcaseScore[i] = 9;
				}
				student.homeworkFileScore = 3;
				//System.out.print(student.id + " " + student.name + "->");
				//System.out.println(student.hwfile);
				StringBuilder sb = OJApi.getSourceCodeToStringBuilder(student.hwfile, null, true);
				boolean hasSourceCodeString = true; // ¼Ò½ºÄÚµå¿¡ Æ¯Á¤ ¹®ÀÚ¿­ ÀÖ´ÂÁö ¾Ë·ÁÁÖ´Â º¯¼ö
				for (String str : sourcecodeCheck) {
					if (sb.indexOf(str) == -1) {
						hasSourceCodeString = false;
					}
				}
				if (hasSourceCodeString) {
					student.sourcecodeScore = 2;
				} else {
					student.sourcecodeScore = 1;
				}
				// °úÁ¦ ÆÄÀÏ Ã£À¸¸é
			} else {
				OJApi.printSwagWithAccent(id + " " + name + "Æú´õ¿¡¼­ °úÁ¦ ÆÄÀÏ Ã£Áö ¸øÇÔ");
				student.testcaseScore = new int[testDatas.size()];
				for (int i = 0; i < testDatas.size(); i++) {
					student.testcaseScore[i] = 9;
				}
				student.homeworkFileScore = 2;
				student.sourcecodeScore = 0;
				student.hwfile = null;
				student.fileName = "null";
				continue;
				// °úÁ¦ÆÄÀÏÀ» Ã£Áö ¸øÇØ¼­ ³Ñ¾î°¨
			}

			double score = 0;
			File originalFile = null;
			if (config.getBoolean("Ã¤Á¡.ÀÔ·ÂÇÁ·ÒÇÁÆ®¸Ş¼¼ÁöÁ¦°Å")) {
				originalFile = student.hwfile;

				String pathf = student.hwfile.getPath();
				String namef = student.hwfile.getName();
				pathf = pathf.replace(namef, "");
				pathf = pathf + File.separator + random.nextInt(1000000) + ".py";
				student.hwfile = new File(pathf);

				BufferedReader br = new BufferedReader(new FileReader(originalFile));
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(student.hwfile), "Cp949"));
				String s;
				while ((s = br.readLine()) != null) {
					s = OJApi.stringLineConvertToException(s, null, false);
					if (s.contains("input")) {
						int i1 = s.indexOf("input") + "input".length();
						boolean isOpenQuotes = false;
						int countOpenBracket = 0;
						int endIndex = -1;
						for (int i = i1; i < s.length(); i++) {
							char c = s.charAt(i);
							if (isOpenQuotes) {
								if (s.charAt(i - 1) != '\\' && (c == '\"' || c == '\'')) {
									// \"ÀÏ °æ¿ì ¹«½ÃÇÏ°í " ÀÏ¶§¸¸ ÀÛµ¿
									isOpenQuotes = false;
								}
							} else {
								if (c == '(') {
									countOpenBracket++;
									// ¿­¸² ±âÈ£½Ã 1Áõ°¡
								} else if (c == '\"' || c == '\'') {
									isOpenQuotes = true;
								} else if (c == ')') {
									// ´İÈû ±âÈ£½Ã 1°¨¼Ò
									countOpenBracket--;
									if (countOpenBracket == 0) {
										endIndex = i;
										break;
									}
								}
							}
						}

						if (endIndex == -1) {
							//System.out.println("¼Ò½º ºÒ·® " + student.id + " " + student.name);
							//System.out.println("s=[" + s + "]");
							bw.append(s).append('\n');
						} else {
							String s2 = s.substring(0, i1 + 1);
							String s3 = s.substring(endIndex);
							//System.out.println("¼Ò½º Á¤»ó " + student.id + " " + student.name);
							//System.out.println("s=[" + s2 + s3 + "]");
							bw.append(s2).append(s3).append('\n');
						}
					} else {
						bw.append(s).append('\n');
					}
				}
				br.close();
				bw.close();
			}
			
			FileWriter fw = new FileWriter(instFolder + "/" + student.id + " " + student.name + ".txt");
			for (int i = 0; i < testDatas.size(); i++) {
				TestData testData = testDatas.get(i);
				//¾Ë¸²Á¡
				List<String> list = new ArrayList<>();
				int code = ScoreCheck.check(student.hwfile.getPath(), testData, checkExceptionChar, list);
				fw.append("TestCase: " + (i+1)).append('\n');
				for(String s : list){
					fw.append(s).append('\n');
				}
				fw.append('\n');
				student.testcaseScore[i] = code;
				if (code == 7)
					score++;
			} // Å×½ºÆ® ÄÉÀÌ½ºº°·Î Ã¤Á¡ÇÏ´Â°Í
			fw.close();
			score /= testDatas.size();
			student.totalScore = score;

			if (config.getBoolean("Ã¤Á¡.ÀÔ·ÂÇÁ·ÒÇÁÆ®¸Ş¼¼ÁöÁ¦°Å")) {
				student.hwfile.delete();
				student.hwfile = originalFile;
			}

			// ÇĞ»ı ÇÑ¸í(ÆÄÀÏ ÇÏ³ª) ³¡
		}
		return studentMap;
	}

	@SuppressWarnings({ "unused", "deprecation" })
	public static List<TestData> getTestDatas(YamlConfiguration config) throws Exception {
		List<TestData> testDatas = new ArrayList<>();
		File testDataFolder = new File(config.getString("Ã¤Á¡.ÀÔÃâ·ÂÆú´õ"));
		if (!testDataFolder.exists()) {
			testDataFolder.mkdirs();
		} // Å×½ºÆ® µ¥ÀÌÅÍ °¡Á®¿À±â

		if (testDataFolder.listFiles().length == 0) {
			OJApi.printSwagWithAccent("Å×½ºÆ® µ¥ÀÌÅÍ°¡ ¾ø½À´Ï´Ù. »ó¼¼ ¼³¸íÀ» ÀĞ¾îÁÖ¼¼¿ä.");
			return null;
		} // Å×½ºÆ® µ¥ÀÌÅÍ ¾øÀ¸¹Ç·Î Á¾·á

		if (false) {
			Set<String> testDataNames = new LinkedHashSet<>();
			for (File file : testDataFolder.listFiles()) {
				String name = file.getName();
				if (name.endsWith(".in") || name.endsWith(".outlow") || name.endsWith(".outhigh")) {
					int i1 = name.lastIndexOf('.');
					testDataNames.add(name.substring(0, i1));
				} else {
					
					System.out.println(name + "Àº Á¤ÇØÁø ÆÄÀÏ È®ÀåÀÚ°¡ ¾Æ´Õ´Ï´Ù. (.in .outlow .outhigh)");
				}
			} // Å×½ºÆ® µ¥ÀÌÅÍ ¸ñ·Ïµé¸¸ °¡Á®¿À±â

			String parentPath = testDataFolder.getPath();
			for (String name : testDataNames) {
				File in = new File(parentPath + "/" + name + ".in");
				File outraw = new File(parentPath + "/" + name + ".outlow");
				File outhigh = new File(parentPath + "/" + name + ".outhigh");
				if (in.exists() && outraw.exists() && outhigh.exists()) {
					testDatas.add(new TestData(in, outraw, outhigh));
				} else {
					System.out.println(name + " Å×½ºÆ® µ¥ÀÌÅÍ´Â .in .outlow .outhigh È®ÀåÀÚ°¡ ÀüºÎ Á¸ÀçÇÏÁö ¾Ê½À´Ï´Ù.");
				}
			}
		} // ·¹°Å½Ã ÄÚµå (ºôµå6 ÀÌÇÏ)

		for (File file : testDataFolder.listFiles()) {
			if (file.getName().endsWith(".testcase")) {
				testDatas.add(new TestData(file));
			} else {
				OJApi.printSwagWithAccent(file.getName() +"Àº Á¤ÇØÁø ÆÄÀÏ È®ÀåÀÚ°¡ ¾Æ´Õ´Ï´Ù. (.case)");
			}

		} // test data·Îµå ºôµå7ÀÌ»ó

		return testDatas;
	}

	public static void writeToExcel(Map<String, StudentHW> map, List<TestData> testDatas, YamlConfiguration config) throws Exception {
		XSSFWorkbook workBook = new XSSFWorkbook();
		POIXMLProperties xmlProps = workBook.getProperties();
		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
		coreProps.setCreator("DPmc");
		// ÀúÀÚ ¼³Á¤

		XSSFSheet sheet = workBook.createSheet("°á°ú");
		
		XSSFRow row = sheet.createRow(0);
		String[] args = new String[] { "ÇĞ¹ø", "ÀÌ¸§", "ÆÄÀÏÀÌ¸§", "ÅëÇÕÁ¡¼ö", "ÆÄÀÏÁ¡¼ö", "»çÁøÁ¡¼ö", "¼Ò½ºÄÚµåÁ¡¼ö" };
		String[] args2 = new String[] { "°á°ú ¿ä¾à", "Á¡¼ö", "ÄÚ¸àÆ®", "ºñ°í" };

		int rowIndex = 1;
		int columnIndex = 0;
		columnIndex = setCellValueForObject(row, columnIndex, args);
		for (int i = 0; i < testDatas.size(); i++) {
			row.createCell(columnIndex).setCellValue("ÄÉÀÌ½º" + (i + 1));
			columnIndex++;
		}
		columnIndex = setCellValueForObject(row, columnIndex, args2);

		XSSFCellStyle style = workBook.createCellStyle();
		XSSFDataFormat format = workBook.createDataFormat();
		style.setDataFormat(format.getFormat("0.0\"%\""));
		// ÆÛ¼¾Æ®±âÈ£ ºÙÀÌ´Â ¼­½Ä
		// ÅëÇÕÁ¡¼ö ¼­½ÄÀÎµ¥ ¾ø¾Ú

		int num = 0;
		for (StudentHW std : map.values()) {
			row = sheet.createRow(rowIndex);
			rowIndex++;

			columnIndex = 0;
			columnIndex = setCellValueForObject(row, columnIndex, std.id);
			columnIndex = setCellValueForObject(row, columnIndex, std.name);
			columnIndex = setCellValueForObject(row, columnIndex, std.fileName);
			columnIndex = setCellValueForObject(row, columnIndex, getTotalScore(std));// ÅëÇÕÁ¡¼ö
			columnIndex = setCellValueForObject(row, columnIndex, std.homeworkFileScore);
			columnIndex = setCellValueForObject(row, columnIndex, std.screenshotScore);
			columnIndex = setCellValueForObject(row, columnIndex, std.sourcecodeScore);
			// row.getCell(columnIndex - 1).setCellStyle(style);
			// args1 µ¥ÀÌÅÍµé ÀÔ·Â

			columnIndex = setCellValueForObject(row, columnIndex, std.testcaseScore);
			// Å×½ºÆ® ÄÉÀÌ½º ÀÔ·Â

			boolean isPerfectCode = true;
			boolean isNotAssignment = false;
			boolean isCantFindPYFile = false;
			isNotAssignment = std.homeworkFileScore == 1;
			isCantFindPYFile = std.homeworkFileScore == 2;
			for (int i = 0; i < std.testcaseScore.length; i++) {
				int code = std.testcaseScore[i];
				if (code != 7)
					isPerfectCode = false;
			} // °á°ú ÆÇ´Ü ¿ä¾à

			String result;
			if (isNotAssignment) {
				result = "°úÁ¦ ¹ÌÁ¦Ãâ";
			} else if (isCantFindPYFile) {
				result = "ÄÚµåÆÄÀÏ ¸øÃ£À½";
			} else if (isPerfectCode) {
				result = "Á¤»ó Ã¤Á¡";
			} else {
				result = "Àç°Ë»ç ÇÊ¿ä";
			}
			columnIndex = setCellValueForObject(row, columnIndex, result);
			// °á°ú ¿ä¾à ÀÔ·Â

			num++;
			if (num % 10 == 0) {
				// System.out.println(" *** " + num + "¸í ¿¢¼¿·Î ÀÛ¼º ¿Ï·á ***");
			}
		}
		OJApi.printSwagWithStars("Ã¤Á¡ µ¥ÀÌÅÍ ÀÛ¼º ¿Ï·á", 50);

		{
			StringBuilder totalDes2 = new StringBuilder("322");
			for (int i = 0; i < testDatas.size(); i++) {
				totalDes2.append("7");
			}
			String[] totalscoreDescription = { "µÚÀÇ Á¡¼ö ÄÚµå¸¦ ÇÕÄ£ ¼¿", totalDes2.toString() + "ÀÌ ¿Ïº®ÇÑ Á¤´äÄÚµå" };// index 3
			String[] fileDescription = { "0: null", "1: °úÁ¦ ¹ÌÁ¦Ãâ", "2: ÄÚµåÆÄÀÏ ¸øÃ£À½", "3: ÄÚµåÆÄÀÏ Ã£À½" };// index 4
			String[] screenshotDescription = { "0: null", "1: »çÁøÆÄÀÏ ¸øÃ£À½", "2: »çÁøÆÄÀÏ Ã£À½" };// index 5
			String[] codeDescription = { "0: null", "1: ÄÚµå¿¡ Æ¯Á¤ ¹®ÀÚ¿­ ¾øÀ½", "2: ÄÚµå¿¡ Æ¯Á¤ ¹®ÀÚ¿­ ÀÖÀ½" };// index 6
			String[] testcaseDescription = { "low/high/equalÀ» ¼ø¼­´ë·Î ÇÑ ÀÚ¸®¾¿ ÇÒ´çÇÔ", "0: 000 (Àü¿ø ºÒÀÏÄ¡)", "1: 001", "2: 010", "3: 011", "4: 100", "5: 101", "6: 110", "7: 111 (Àü¿ø ÀÏÄ¡)", "8: ÆÄÀÌ½ã ¿À·ù ¹ß»ı", "9: null" }; // 7
			String[] colorDescription = {"ÁÖÈ²: 7(111)ÀÌ ¾Æ´Ñ °Íµé", "»¡°­: 9(ÆÄÀÏ¾øÀ½)", "ÃÊ·Ï: 100Á¡ÀÌ ¾Æ´Ñ °Íµé"};
			
			rowIndex += 5;
			//5Ä­ ³»·Á°¨
			
			for (int i = rowIndex; i < rowIndex + testcaseDescription.length; i++) {
				sheet.createRow(i);
			}
			// testcase¼³¸íÀÌ °¡Àå ±â´Ï±î ±× ±æÀÌ¸¦ »ç¿ëÇÔ

			sheet.setColumnWidth(2, (int)(sheet.getColumnWidth(2) * 2.5));
			sheet.setColumnWidth(3, sheet.getColumnWidth(3) * 2);
			sheet.setColumnWidth(2, (int)(sheet.getColumnWidth(4) * 1.5));
			sheet.setColumnWidth(2, (int)(sheet.getColumnWidth(5) * 1.5));
			sheet.setColumnWidth(2, (int)(sheet.getColumnWidth(6) * 1.5));
			
			int resultIndex = args.length + testDatas.size();
			sheet.setColumnWidth(resultIndex, (int)(sheet.getColumnWidth(resultIndex) * 1.75));
			//±æÀÌ ¼³Á¤
			
			
			setColumnValueForStringArray(sheet, rowIndex, 3, totalscoreDescription);
			setColumnValueForStringArray(sheet, rowIndex, 4, fileDescription);
			setColumnValueForStringArray(sheet, rowIndex, 5, screenshotDescription);
			setColumnValueForStringArray(sheet, rowIndex, 6, codeDescription);
			setColumnValueForStringArray(sheet, rowIndex, 7, testcaseDescription);
			setColumnValueForStringArray(sheet, rowIndex, 8, colorDescription);
		}

		XSSFSheetConditionalFormatting sheetcf = sheet.getSheetConditionalFormatting();
		// Á¶°ÇºÎ¼­½Ä »ı¼ºÀÚ
		
		{
			XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.EQUAL, "9");
			XSSFPatternFormatting patternFormat = rule.createPatternFormatting();
			patternFormat.setFillBackgroundColor(IndexedColors.RED.index);
			// Á¶°ÇºÎ ¼­½Ä »ı¼º

			CellRangeAddress[] arr = { new CellRangeAddress(1, map.size(), args.length, args.length + testDatas.size() - 1) };
			sheetcf.addConditionalFormatting(arr, rule);
			// Á¶°ÇºÎ ¼­½Ä ÀÔ·Â
		} // Å×½ºÆ® ÄÉÀÌ½º¿¡ »¡°­ »ö»ó ÀÔ·Â
		
		{
			XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.NOT_EQUAL, "7");
			XSSFPatternFormatting patternFormat = rule.createPatternFormatting();
			patternFormat.setFillBackgroundColor(IndexedColors.LIGHT_ORANGE.index);
			// Á¶°ÇºÎ ¼­½Ä »ı¼º

			CellRangeAddress[] arr = { new CellRangeAddress(1, map.size(), args.length, args.length + testDatas.size() - 1) };
			sheetcf.addConditionalFormatting(arr, rule);
			// System.out.println("ÁÖÈ²=" + arr[0].formatAsString());
			// Á¶°ÇºÎ ¼­½Ä ÀÔ·Â
		} // Å×½ºÆ® ÄÉÀÌ½º¿¡ ÁÖÈ² »ö»ó ÀÔ·Â

		{
			XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.NOT_EQUAL, "\"Á¤»ó Ã¤Á¡\"");
			XSSFPatternFormatting patternFormat = rule.createPatternFormatting();
			patternFormat.setFillBackgroundColor(new XSSFColor(new Color(146, 208, 80)));
			// Á¶°ÇºÎ ¼­½Ä »ı¼º

			CellRangeAddress[] arr = { new CellRangeAddress(1, map.size(), args.length + testDatas.size(), args.length + testDatas.size()) };
			sheetcf.addConditionalFormatting(arr, rule);
			// System.out.println("ÃÊ·Ï=" + arr[0].formatAsString());
			// Á¶°ÇºÎ ¼­½Ä ÀÔ·Â
		}
		OJApi.printSwagWithStars("Á¶°ÇºÎ ¼­½Ä ¼³Á¤ ¿Ï·á", 50);

		File excelFile = new File(config.getString("Ã¤Á¡.°á°úÆÄÀÏ"));
		if (excelFile.exists())
			excelFile.delete();
		workBook.write(new FileOutputStream(excelFile));
		workBook.close();
	}

	public static long getTotalScore(StudentHW std) {
		long l = 0;
		l *= 10;
		l += std.homeworkFileScore;

		l *= 10;
		l += std.screenshotScore;

		l *= 10;
		l += std.sourcecodeScore;

		for (int i = 0; i < std.testcaseScore.length; i++) {
			l *= 10;
			l += std.testcaseScore[i];
		}
		return l;
	}

	public static void setColumnValueForStringArray(XSSFSheet sheet, int rowIndex, int columnIndex, String[] arr) {
		for (int i = 0; i < arr.length; i++) {
			XSSFRow row = sheet.getRow(rowIndex + i);
			XSSFCell cell = row.createCell(columnIndex);
			cell.setCellValue(arr[i]);
		}
	}

	public static int setCellValueForObject(XSSFRow row, int columnIndex, Object obj) {
		if (obj instanceof int[]) {
			int[] arr = (int[]) obj;
			for (int i = 0; i < arr.length; i++) {
				row.createCell(columnIndex).setCellValue(arr[i]);
				columnIndex++;
			}
		} else if (obj instanceof String[]) {
			String[] arr = (String[]) obj;
			for (int i = 0; i < arr.length; i++) {
				row.createCell(columnIndex).setCellValue(arr[i]);
				columnIndex++;
			}
		} else if (obj instanceof Double) {
			row.createCell(columnIndex).setCellValue((double) obj);
			columnIndex++;
		} else if (obj instanceof String) {
			row.createCell(columnIndex).setCellValue((String) obj);
			columnIndex++;
		} else if (obj instanceof Integer) {
			row.createCell(columnIndex).setCellValue((int) obj);
			columnIndex++;
		} else if (obj instanceof Long) {
			row.createCell(columnIndex).setCellValue((long) obj);
			columnIndex++;
		}
		return columnIndex;
	}

	public static boolean IS_DEBUG = false;

	/**
	 * ÇĞ»ı 1¸í °úÃ¼ Ã¼Å©
	 * 
	 * @param filePath ÆÄÀÏ À§Ä¡
	 * @param testData Å×½ºÆ® µ¥ÀÌÅÍ ¸ğÀ½
	 * @param outputException Ã¤Á¡½Ã Á¦¿ÜÇÒ ¹®ÀÚµé
	 * @return Ã¤Á¡ÄÚµå
	 * @throws IOException
	 */
	public static int check(String filePath, TestData testData, List<String> outputException, List<String> output) throws IOException {
		Process pc = Runtime.getRuntime().exec("python \"" + filePath + "\"");

		OutputStream os = pc.getOutputStream();
		InputStream is = pc.getInputStream();
		InputStream es = pc.getErrorStream();

		for (int i = 0; i < testData.input.size(); i++) {
			os.write((testData.input.get(i) + "\n").getBytes());
			if (IS_DEBUG)
				System.out.println("[Std in] " + testData.input.get(i));
		}
		os.flush();
		os.close();
		// input µ¥ÀÌÅÍ ÀÔ·Â

		List<String> outLines = new ArrayList<>();
		List<String> errLines = new ArrayList<>();
		{
			String line;
			BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(is));
			while ((line = brCleanUp.readLine()) != null) {
				output.add(line);
				outLines.add(OJApi.stringLineConvertToException(line, outputException, false));// Å°¿öµå
																								// Á¦°Å
				if (IS_DEBUG)
					System.out.println("[Stdout] " + line);
			}
			brCleanUp.close();
			// output ÀÔ·Â¹Ş±â

			brCleanUp = new BufferedReader(new InputStreamReader(es));
			while ((line = brCleanUp.readLine()) != null) {
				errLines.add(line);
				if (IS_DEBUG)
					System.out.println("[Stderr] " + line);
			}
			brCleanUp.close();
			// ¿¡·¯ ¸Ş¼¼Áö ÀÔ·Â¹Ş±â
		}

		boolean isLowChecked = false;
		boolean isHighChecked = false;
		boolean isEqualChecked = false;

		isLowChecked = listContainsString(outLines, testData.outputLow);
		isHighChecked = listContainsString(outLines, testData.outputHigh);
		isEqualChecked = listEqualsString(outLines, testData.outputEqual);

		int code = 0;
		if (isLowChecked)
			code += 100;
		if (isHighChecked)
			code += 10;
		if (isEqualChecked)
			code += 1;
		// code - low/high/equal °¢°¢ 1ÀÚ¸®¾¿ ¿©ºÎ

		if (errLines.size() >= 1) {
			// python ¿À·ù
			return 8;
		} else if (code == 000) {
			return 0;
		} else if (code == 001) {
			return 1;
		} else if (code == 010) {
			return 2;
		} else if (code == 011) {
			return 3;
		} else if (code == 100) {
			return 4;
		} else if (code == 101) {
			return 5;
		} else if (code == 110) {
			return 6;
		} else if (code == 111) {
			return 7;
		} else {
			return 9;
		}
	}

	/**
	 * outLines¾È¿¡ testData°¡ ¼ø¼­´ë·Î ÀÖ´ÂÁö °Ë»ç
	 * 
	 * @param outLines °Ë»çµÉ ¹®ÀÚ ¸®½ºÆ®
	 * @param testData °Ë»çÇÒ ¹®ÀÚ ¸®½ºÆ®
	 * @return contains ¿©ºÎ
	 */
	public static boolean listContainsString(List<String> outLines, List<String> testData) {
		if (testData.size() == 0) {
			return true;
		}

		boolean isContains = false;
		int index = 0;
		for (String line : outLines) {
			String compareStr = testData.get(index);
			if (line.contains(compareStr)) {
				index++;
			}
			if (index == testData.size()) {
				isContains = true;
				break;
			}
		}
		return isContains;
	}

	/**
	 * outLines¾È¿¡ testData°¡ ¼ø¼­´ë·Î ÀÖ´ÂÁö °Ë»ç
	 * 
	 * @param outLines °Ë»çµÉ ¹®ÀÚ ¸®½ºÆ®
	 * @param testData °Ë»çÇÒ ¹®ÀÚ ¸®½ºÆ®
	 * @return equals ¿©ºÎ
	 */
	public static boolean listEqualsString(List<String> outLines, List<String> testData) {
		if (testData.size() == 0) {
			return true;
		}

		boolean isEqual = false;
		int index = 0;
		for (String line : outLines) {
			String compareStr = testData.get(index);
			if (line.equals(compareStr)) {
				index++;
			}
			if (index == testData.size()) {
				isEqual = true;
				break;
			}
		}
		return isEqual;
	}
}
