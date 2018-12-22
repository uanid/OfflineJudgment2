package kr.dpmc.offlinejudgment.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIXMLProperties;
import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
import org.apache.poi.ss.usermodel.CellStyle;
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

import info.debatty.java.stringsimilarity.JaroWinkler;
import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class SimilirarityCheck {

	public static JaroWinkler jaro = new JaroWinkler();

	@SuppressWarnings("deprecation")
	public static void SimiliraritySummary(YamlConfiguration config) throws Exception {
		File parentFolder = new File(config.getString("유사도.제출폴더"));
		Map<String, StringBuilder> map = new LinkedHashMap<>();
		// id, 소스내용

		List<String> exceptionlist = new ArrayList<String>();
		exceptionlist.add(" ");
		
		for (File file : parentFolder.listFiles()) {
			map.put(file.getName().replace(".txt", ""), OJApi.getSourceCodeToStringBuilder(file, exceptionlist, true));
		}
		//1.c를 .py로 교체해야함

		Map<String, List<Double>> distanceMap = new LinkedHashMap<>();

		int num = 0;
		List<String> studentList = new LinkedList<>(map.keySet());
		studentList.sort(String.CASE_INSENSITIVE_ORDER);
		
		for (String id : studentList) {
			StringBuilder sb1 = map.get(id);
			List<Double> list = new LinkedList<>();
			for (String idp : studentList) {
				StringBuilder sb2 = map.get(idp);
				if (id.equals(idp)) {
					list.add(1.0);
				} else {
					double d = getDistance(sb1, sb2);
					list.add(d);
					// System.out.println(id + "??" + idp + "= " + d);
				}
			}
			distanceMap.put(id, list);
			num++;
			if (num % 10 == 0) {
				OJApi.printSwagWithStars(num + "명 비교 완료", 30);
			}
		}
		OJApi.printSwagWithStars("전체 학생 점수 구하기 완료 O(n^2)", 50);
		// 코드 유사도 검사


		OJApi.printSwagWithStars("엑셀로 내보내기 시작", 50);
		
		File excelFile = new File(config.getString("유사도.결과파일"));
		if (excelFile.exists())
			excelFile.delete();

		XSSFWorkbook workBook = new XSSFWorkbook();
		POIXMLProperties xmlProps = workBook.getProperties();
		POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties();
		coreProps.setCreator("DPmc");
		//저자 설정
		
		XSSFSheet sheet = workBook.createSheet("유사도");

		XSSFCellStyle style = workBook.createCellStyle();
		XSSFDataFormat format = workBook.createDataFormat();
		style.setDataFormat(format.getFormat("0.00\"%\""));

		XSSFCellStyle style2 = workBook.createCellStyle();
		XSSFColor color = new XSSFColor(new java.awt.Color(255, 255, 204));
		style2.setFillForegroundColor(color);// 베이지색
		// style2.setFillBackgroundColor(color);
		style2.setFillPattern(CellStyle.SOLID_FOREGROUND);

		XSSFRow row = sheet.createRow(0);
		XSSFCell cell;
		cell = row.createCell(0);
		cell.setCellValue(studentList.size() + "명");
		cell.setCellStyle(style2);

		for (int i = 0; i < studentList.size(); i++) {
			cell = row.createCell(i + 1);
			cell.setCellValue(studentList.get(i));
			cell.setCellStyle(style2);
		}
		for (int i = 0; i < studentList.size(); i++) {
			cell = sheet.createRow(i + 1).createCell(0);
			cell.setCellValue(studentList.get(i));
			cell.setCellStyle(style2);
		} // 맨 윗줄, 맨 왼쪽줄 학번 입력

		num = 0;
		for (int i = 0; i < studentList.size(); i++) {
			row = sheet.getRow(i + 1);
			List<Double> distances = distanceMap.get(studentList.get(i));
			for (int j = 0; j < distances.size(); j++) {
				cell = row.createCell(j + 1);
				cell.setCellStyle(style);
				cell.setCellValue(distances.get(j) * 100);
			} // row 1줄 입력

			num++;
			if (num % 10 == 0) {
				OJApi.printSwagWithStars(num + "명 작성 완료", 50);
			}
		}
		// 입력 완료

		int studentSize = studentList.size();
		CellRangeAddress range = new CellRangeAddress(1, 1, studentSize + 3, studentSize + 3);
		// 조건부 서식 값
		// System.out.println(range.formatAsString("유사도", true));
		sheet.getRow(0).createCell(studentSize + 3).setCellValue("조건부 서식 계수");
		if (sheet.getRow(1) == null) {
			sheet.createRow(1);
		}
		sheet.getRow(1).createCell(studentSize + 3).setCellValue(80);
		
		XSSFSheetConditionalFormatting sheetcf = sheet.getSheetConditionalFormatting();
		XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.BETWEEN, range.formatAsString("유사도", true), "100");
		XSSFPatternFormatting patternFormat = rule.createPatternFormatting();
		patternFormat.setFillBackgroundColor(IndexedColors.SKY_BLUE.index);
		// 조건부 서식 생성

		CellRangeAddress[] arr = { new CellRangeAddress(1, studentSize, 1, studentSize) };
		sheetcf.addConditionalFormatting(arr, rule);
		// 조건부 서식 입력

		workBook.write(new FileOutputStream(excelFile));
		workBook.close();
		OJApi.printSwagWithStars("유사도 요약 결과 파일 생성", 50); 
	}

	public static void writeToSCV(Map<String, StringBuilder> map, Map<String, List<Double>> distanceMap, YamlConfiguration config) throws Exception {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(config.getString("유사도.결과파일"))));
		bw.write(" ");
		for (String id : map.keySet()) {
			bw.write("," + id);
		}
		bw.write("\n");
		// 맨 첫줄

		for (String id : map.keySet()) {
			bw.write(id);
			for (Double d : distanceMap.get(id)) {
				bw.write(", " + Double.toString(d));
			}
			bw.write("\n");
		}
		bw.close();
		System.out.println("CSV파일로 내보내기 완료");
	}

	public static double getDistance(StringBuilder sb1, StringBuilder sb2) {
		return 1 - jaro.distance(sb1.toString(), sb2.toString());
	}

}
