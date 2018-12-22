package kr.dpmc.offlinejudgment;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class MetaDataHW {

	public static class File2 {
		public File original;
		// 원본파일

		public File newer;
		// 블랙보드에서 변형된 파일
	}

	private static String[] divideStr = { "이름:", "과제:", "제출 날짜:", "현재 성적:", "제출 필드:", "댓글:", "파일:" };

	public String name;
	public String id;
	public String homework;
	public String submitDate;
	public String scoreNow;
	public String submitField;
	public int outputFilesCount;
	public boolean isSubmitFileZiped;
	public List<String> comment;
	public List<File2> files;

	public MetaDataHW(String id, String name) {
		this.id = id;
		this.name = name;
		this.homework = "";
		this.submitDate = "미제출";
		this.scoreNow = "";
		this.submitField = "";
		this.outputFilesCount = 0;
		this.isSubmitFileZiped = false;
		this.comment = new LinkedList<>();
		this.files = null;
	}
	
	public String toString(){
		return "name=" + name + ", id=" + id + ", homework=" + homework + ", scoreNow=" + scoreNow + ", comment=" + comment.toString();
	}

	public MetaDataHW(File metaFile) {
		List<String> list = OJApi.getSourceCodeToStringBuilder_UTF8(metaFile);
		StringBuilder sb = new StringBuilder();
		for (String line : list) {
			sb.append(line).append('\n');
		}

		int[] iArr = new int[divideStr.length];
		for (int i = 0; i < divideStr.length; i++) {
			iArr[i] = sb.indexOf(divideStr[i]);
		}

		List<String> datas = new LinkedList<>();
		for (int i = 0; i < iArr.length - 1; i++) {
			int start = iArr[i] + divideStr[i].length();
			int end = iArr[i + 1] - 1;
			// System.out.println("data=[" + sb.substring(start, end).trim() +
			// "]");
			datas.add(sb.substring(start, end).trim());
		}
		// 댓글: 까지 추출
		// 파일은 따로 추출하도록

		{
			int endIndex = iArr.length - 1;
			int start = iArr[endIndex] + divideStr[endIndex].length();
			datas.add(sb.substring(start).replace("\t", "").trim());
		} // 파일 추출

		{
			String line = datas.get(0);
			int i0 = line.indexOf('.');
			int i1 = line.indexOf('(');
			int i2 = line.indexOf(')');
			if (i0 == -1) {
				this.name = line.substring(0, i1);
				this.id = line.substring(i1 + 1, i2);
			} else {
				this.name = line.substring(i0 + 1, i1);
				this.id = line.substring(i1 + 1, i2);
			}
		} // 이름 학번으로 저장

		this.homework = datas.get(1);
		this.submitDate = datas.get(2);
		this.scoreNow = datas.get(3);
		this.submitField = datas.get(4);
		// 기타 데이터 저장

		{
			List<String> comments = new LinkedList<>();
			String[] args = datas.get(5).split("\n");
			for (String arg : args) {
				if (arg.trim().length() != 0)
					comments.add(arg);
			}
			this.comment = comments;
		}
		// 코멘트 저장

		{
			String[] args = datas.get(6).split("\n");
			List<String> files = new LinkedList<>();
			// System.out.println(id + ":" + name + "=" +
			// Arrays.toString(args));
			for (String arg : args) {
				if (arg.trim().length() != 0) {
					if (arg.startsWith("원래 파일명:")) {
						files.add(arg.substring("원래 파일명:".length() + 1).trim());
					} else if (arg.startsWith("파일명:")) {
						files.add(arg.substring("파일명:".length() + 1).trim());
					} else {
						System.out.println(arg + "오류 발생");
					}
				}
			}
			// System.out.println(id + ":" + name + "=" + files);

			if (files.size() % 2 != 0) {
				System.out.println(this.id + " " + this.name + " 메타 데이터 올바르지 않음");
				this.files = null;
				return;
			}

			this.files = new LinkedList<>();
			File parent = metaFile.getParentFile();
			for (int i = 0; i < files.size() / 2; i++) {
				File2 f2 = new File2();
				f2.original = new File(parent, files.get(i * 2));
				f2.newer = new File(parent, files.get(i * 2 + 1));
				this.files.add(f2);
				// System.out.println(id + ":" + name + "=" +
				// f2.newer.exists());
			}
		}
		this.outputFilesCount = 0;
		this.isSubmitFileZiped = false;

	}

}
