package kr.dpmc.offlinejudgment.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.poi.hssf.record.CFRuleBase.ComparisonOperator;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
import org.apache.poi.xssf.usermodel.XSSFPatternFormatting;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheetConditionalFormatting;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class OfflineJudgment {

	// C 채점 기능 추가해야 함
	// C 채점 input에 file io 생성 추가
	// C 채점 output 
	

	public static void main3(String[] args) throws Exception {
		File file = new File("test.xlsx");
		XSSFWorkbook workBook = new XSSFWorkbook(file);
		XSSFSheet sheet = workBook.getSheetAt(0);
		XSSFSheetConditionalFormatting sheetcf = sheet.getSheetConditionalFormatting();
		XSSFConditionalFormattingRule rule = sheetcf.createConditionalFormattingRule(ComparisonOperator.BETWEEN, "1", "G3");
		XSSFPatternFormatting format = rule.createPatternFormatting();
		format.setFillBackgroundColor(IndexedColors.SKY_BLUE.index);

		CellRangeAddress[] arr = { CellRangeAddress.valueOf("A1:F5") };

		sheetcf.addConditionalFormatting(arr, rule);
		workBook.write(new FileOutputStream(new File("test2.xlsx")));
		workBook.close();
		System.out.println("완료");
	}

	public static void main2(String[] args) throws Exception {
		File file = new File("test.txt");
		FileOutputStream fos = new FileOutputStream(file);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "Cp949");
		BufferedWriter bw = new BufferedWriter(osw);
		bw.write("aaa\n");
		bw.write("하하하\n");
		bw.close();
		System.out.println("끝");
	}
	

	public static void main(String[] args) throws Exception {
		YamlConfiguration config = new YamlConfiguration("config.yaml");
		Scanner sc = new Scanner(System.in);

		while (true) {
			try {
				if (mainLoop(sc, config)) {
					break;
				}
			} catch (Exception e) {
				System.out.println(" === 오류 발생 ===");
				e.printStackTrace();
			}
		}
		sc.close();
	}

	public static boolean mainLoop(Scanner sc, YamlConfiguration config) throws Exception {
		if (config.getFile().exists()){
			config.reloadYaml();
		}
		ConfigInit(config);
		System.out.println("Build: " + config.getInt("OJ.build"));
		// config 설정

		System.out.println("숫자 명령을 입력해주세요");
		System.out.println("1. 파일이름정규화-> 폴더 : " + config.getString("정규화.다운로드폴더"));
		System.out.println("2. OJ시스템 채점하기-> 폴더 : " + config.getString("채점.제출폴더"));
		System.out.println("3. 유사도검사-> 폴더 : " + config.getString("유사도.제출폴더"));
		System.out.println("4. 파일 지우기-> 폴더 : " + config.getString("정규화.다운로드폴더") + ", " + config.getString("채점.제출폴더") + ", " + config.getString("채점.코드파일모음폴더") + ", " + config.getString("채점.사진파일모음폴더") + ", " + config.getString("유사도.제출폴더"));
		System.out.println("5. 블랙보드 제출용 파일 만들기-> 파일 : " + config.getString("업로드.업로드파일"));
		System.out.println("6. 종료");
		System.out.print("명령: ");

		int cmd = sc.nextInt();
		if (cmd == 1) {
			System.out.println("1-2. 분반을 입력해주세요 (없으면 0 또는 -1)");
			System.out.print("입력: ");
			int classNumber = sc.nextInt();
			HomeworkNormalization.Normalization(config, classNumber);
		} else if (cmd == 2) {
			System.out.println("2-2. 분반을 입력해주세요 (없으면 0 또는 -1)");
			System.out.print("입력: ");
			int classNumber = sc.nextInt();
			ScoreCheck.OfflineJudgmentMain(config, classNumber);
		} else if (cmd == 3) {
			SimilirarityCheck.SimiliraritySummary(config);
		} else if (cmd == 4) {
			System.out.println("결과 엑셀 파일도 같이 삭제할까요? (0 또는 1)");
			System.out.print("입력: ");
			int result = sc.nextInt();
			System.out.println("진짜로 파일들을 삭제할까요? (0 또는 1)");
			System.out.print("입력: ");
			int isDelete = sc.nextInt();
			if (isDelete == 1) {
				DeleteFile.DeleteProjectFile(config, result);
			} else {
				OJApi.printSwagWithBraket("모든 파일을 삭제하지 않고 종료합니다.");
			}
		} else if (cmd == 5) {
			UploadToBlackboard.writeUploadFile(config);
			// 업로드 파일 합치기
		} else if (cmd == 6) {
			OJApi.printSwagWithBraket("OJ를 종료합니다.");
			return true;
		}

		if (1 <= cmd && cmd <= 6) {
			OJApi.printSwagWithBraket("명령수행 완료");
		} else {
			OJApi.printSwagWithBraket("잘못된 명령입니다");
		}

		System.out.println("***********************************************************");
		return false;
	}

	public static void ConfigInit(YamlConfiguration config) throws Exception {
		int value = config.getInt("OJ.build");
		if (value <= 7) {
			for (int i = 0; i < 11; i++) {
				System.out.println();
			}
			OJApi.printSwag("config 파일이 오래된 버전입니다.", 10, " ===", "===");
			OJApi.printSwag("config를 새 내용으로 덮어씌웁니다.", 10, " ===", "===");
			OJApi.printSwag("config 내용 누락을 주의하세요.", 10, " ===", "===");
			for (int i = 0; i < 11; i++) {
				System.out.println();
			}

			config.set("OJ.build", 8);

			ArrayList<String> list;

			config.set("채점.제출폴더", "hw");
			config.set("채점.입출력폴더", "테스트케이스");
			config.set("채점.코드파일모음폴더", "코드파일모음");
			config.set("채점.사진파일모음폴더", "사진파일모음");
			config.set("채점.결과파일", "채점결과.xlsx");
			config.set("채점.입력프롬프트메세지제거", true);

			list = new ArrayList<>();
			list.add(".jpg");
			list.add(".jpeg");
			list.add(".png");
			list.add(".bmp");
			list.add(".hwp");
			list.add(".docx");
			config.set("채점.사진인식확장자", list);

			list = new ArrayList<>();
			list.add(" ");
			config.set("채점.채점제외문자", list);

			list = new ArrayList<>();
			list.add("input");
			list.add("\"");
			config.set("채점.소스파일검사", list);

			list = new ArrayList<>();
			list.add("동");
			list.add("호");
			config.set("채점.과제인식문자", list);

			list = new ArrayList<>();
			list.add(" ");
			list.add("*");
			config.set("채점.과제인식제외문자", list);
			// 채점 데이터

			config.set("업로드.점수파일", "채점결과.xlsx");
			config.set("업로드.업로드파일", "업로드.xls");
			config.set("업로드.업로드종합파일", "업로드종합.xls");

			config.set("유사도.제출폴더", "CodeDistance");
			config.set("유사도.결과파일", "유사도결과.xlsx");
			config.set("유사도.주석제거", false);
			config.set("유사도.탭제거", true);
			config.set("유사도.줄넘김제거", false);

			list = new ArrayList<>();
			list.add(" ");
			list.add("**");
			config.set("유사도.제외문자", list);
			// 유사도 데이터

			config.set("정규화.다운로드폴더", "다운로드");
			// config.set("정규화.다운로드정리폴더", "다운로드정리");
			config.set("정규화.결과파일", "제출요약.xlsx");
			config.set("정규화.출석부폴더", "분반별출석부");

			// 정규화 데이터
			config.saveYaml();
		}
	}

}
