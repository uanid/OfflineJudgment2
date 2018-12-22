package kr.dpmc.offlinejudgment.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class UploadToBlackboard {

	public static class Student {
		public String name;
		public String id;
		public double score;
		public String comment;

		public Student() {

		}

		public Student(String name, String id, double score, String comment) {
			this.name = name;
			this.id = id;
			this.score = score;
			this.comment = comment;
		}
	}

	public static boolean isDoublePositive(String string) {
		return string.matches("([0-9]+|[0-9]+[.][0-9]+)");
	}

	public static void writeUploadFile(YamlConfiguration config) throws Exception {
		File scoreFile = new File(config.getString("업로드.점수파일"));
		File uploadFile = new File(config.getString("업로드.업로드파일"));
		File uploadSumFile = new File(config.getString("업로드.업로드종합파일"));

		if (!scoreFile.exists()) {
			OJApi.printSwagWithAccent("점수 파일을 찾을 수 없습니다.");
			return;
		}
		if (!uploadFile.exists()) {
			OJApi.printSwagWithAccent("업로드 파일을 찾을 수 없습니다.");
			return;
		}

		Map<String, Student> stdMap = new HashMap<>();
		boolean isGetScore = getScoreData(scoreFile, stdMap);
		if (!isGetScore) {
			OJApi.printSwagWithAccent("채점 데이터를 불러올 수 없습니다.");
			return;
		}
		OJApi.printSwagWithAccent("채점 데이터를 불러왔습니다.");
		
		Map<String, String[]> csvMap = new HashMap<>();
		String title = getCSVData(uploadFile, csvMap);
		// 첫번째 줄 반환

		OJApi.printSwagWithAccent("업로드 데이터를 불러왔습니다.");
		
		summarizeToUploadData(stdMap, csvMap);
		OJApi.printSwagWithAccent("업로드 데이터와 채점 데이터 통합 완료");
		
		writeToCSV(csvMap, title, uploadSumFile);
		OJApi.printSwagWithAccent("업로드 종합 파일로 작성 완료");
	}

	public static void writeToCSV(Map<String, String[]> csvMap, String title, File uploadFile) throws Exception {
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(uploadFile), Charset.forName("UTF-16").newEncoder()));
		bw.append(title).append('\n');
		for (String[] args : csvMap.values()) {
			for (int i = 0; i < args.length; i++) {
				bw.append('\"').append(args[i]).append('\"');
				if (i + 1 != args.length) {
					bw.append('\t');
				}
			}
			bw.append('\n');
		}
		bw.close();
	}

	public static void summarizeToUploadData(Map<String, Student> stdMap, Map<String, String[]> csvMap) {
		for (String id : csvMap.keySet()) {
			Student std = stdMap.get(id);
			String[] args = csvMap.get(id);
			if (args[4].equals("채점 필요")) {
				args[4] = String.valueOf(std.score);
				args[7] = std.comment;
			}
		}
	}

	public static String getCSVData(File uploadFile, Map<String, String[]> csvMap) throws Exception {
		List<String> lines = OJApi.getSourceCodeToStringBuilder_UTF16(uploadFile);
		// 이름, id, 학과, 접속일자, 과제명(점수), 교수메모, 메모포맷, 피드백, 피드백포맷
		// 9개 또는 4개?
		for (int i = 1; i < lines.size(); i++) {
			String[] args = lines.get(i).split("\t");
			if (args.length < 2) {
				OJApi.printSwag("올바르지 않은 내용(인자 2개 미만)", 50, " ↓↓↓", "↓↓↓");
				System.out.println(lines.get(i));
			} else {
				if (args.length == 9) {
					// 전체 다 있는 경우
					for (int j = 0; j < args.length; j++) {
						if (args[j].startsWith("\""))
							args[j] = args[j].substring(1, args[j].length() - 1);
					}
					// 큰따옴표 제거
					csvMap.put(args[1], args);
				} else if (args.length >= 4) {
					// 학번 이름만 있는 경우
					for (int j = 0; j < args.length; j++) {
						if (args[j].startsWith("\""))
							args[j] = args[j].substring(1, args[j].length() - 1);
					}
					// 큰따옴표 제거
					String[] args2 = new String[9];
					for (int j = 0; j < 4; j++) {
						args2[j] = args[j];

					}
					for (int j = 4; j < args2.length; j++) {
						args2[j] = "";
					}
					csvMap.put(args[1], args2);
				} else {
					OJApi.printSwag("올바르지 않은 내용(인자 4개 미만)", 50, " ↓↓↓", "↓↓↓");
					System.out.println(lines.get(i));
				}
			}
		}
		return lines.get(0);
	}

	public static boolean getScoreData(File scoreFile, Map<String, Student> stdmap) throws Exception {
		XSSFWorkbook workBook = new XSSFWorkbook(scoreFile);
		XSSFSheet sheet = workBook.getSheetAt(0);

		XSSFRow row = sheet.getRow(0);
		if (row == null) {
			workBook.close();
			return false;
		}

		int scoreIndex = -1;
		int commentIndex = -1;
		int nameIndex = -1;
		int idIndex = -1;

		int rowEndNum = row.getLastCellNum();
		DataFormatter formatter = new DataFormatter();

		for (int i = row.getFirstCellNum(); i < rowEndNum; i++) {
			XSSFCell cell = row.getCell(i);
			if (cell != null) {
				String value = formatter.formatCellValue(cell);
				if (value.equals("학번")) {
					idIndex = i;
				} else if (value.equals("이름")) {
					nameIndex = i;
				} else if (value.equals("점수")) {
					scoreIndex = i;
				} else if (value.equals("코멘트")) {
					commentIndex = i;
				}
			}
		}

		if (idIndex == -1 || commentIndex == -1 || nameIndex == -1 || scoreIndex == -1) {
			workBook.close();
			return false;
		}

		try {
			int columnEndNum = sheet.getPhysicalNumberOfRows();
			for (int i = 1; i < columnEndNum; i++) {
				row = sheet.getRow(i);
				String name = formatter.formatCellValue(row.getCell(nameIndex));
				String id = formatter.formatCellValue(row.getCell(idIndex));
				String score = formatter.formatCellValue(row.getCell(scoreIndex));
				String comment = formatter.formatCellValue(row.getCell(commentIndex));

				if (isDoublePositive(score)) {
					Student std = new Student(name, id, Double.valueOf(score), comment);
					// System.out.println("name=" + name + ", id=" + id + ", score="
					// + score + ", comment=#" + comment + "#");
					stdmap.put(id, std);
				} else {
					OJApi.printSwagWithAccent(name + " " + id + "학생 점수 실수가 아님");
				}
			}
		} catch (Exception e) {
			//오류가 나는곳까지 정보 읽어오면 됨
		}
		
		workBook.close();

		return true;
	}

}
