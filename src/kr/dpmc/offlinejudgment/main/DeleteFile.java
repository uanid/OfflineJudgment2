package kr.dpmc.offlinejudgment.main;

import java.io.File;

import kr.dpmc.offlinejudgment.OJApi;
import kr.dpmc.offlinejudgment.YamlConfiguration;

public class DeleteFile {

	public static void DeleteProjectFile(YamlConfiguration config, int isDeleteResultFile) throws Exception {
		File normalOriginal = new File(config.getString("정규화.다운로드폴더"));
		File check = new File(config.getString("채점.제출폴더"));
		File codeSummary = new File(config.getString("채점.코드파일모음폴더"));
		File screenshotSummary = new File(config.getString("채점.사진파일모음폴더"));
		File similarity = new File(config.getString("유사도.제출폴더"));

		deleteSubFiles(normalOriginal);
		normalOriginal.mkdirs();
		OJApi.printSwagWithStars("다운로드폴더 삭제 완료", 50);

		deleteSubFiles(check);
		check.mkdirs();
		OJApi.printSwagWithStars("과제폴더 삭제 완료", 50);
		
		deleteSubFiles(codeSummary);
		codeSummary.mkdirs();
		OJApi.printSwagWithStars("코드파일모음폴더 삭제 완료", 50);
		
		deleteSubFiles(screenshotSummary);
		screenshotSummary.mkdirs();
		OJApi.printSwagWithStars("사진파일모음폴더 삭제 완료", 50);

		deleteSubFiles(similarity);
		similarity.mkdirs();
		OJApi.printSwagWithStars("유사도폴더 삭제 완료", 50);

		if (isDeleteResultFile == 1) {
			File parent = new File(".");
			for (File file : parent.listFiles()) {
				if (file.isFile() && (file.getName().endsWith(".xlsx") || file.getName().endsWith(".xls"))) {
					file.delete();
				}
			}
			OJApi.printSwagWithStars("결과엑섹파일 삭제 완료", 50);
		}
	}

	public static void deleteSubFiles(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File sub : file.listFiles()) {
					deleteSubFiles(sub);
				}
				file.delete();
			} else {
				file.delete();
			}
		}
	}
}
