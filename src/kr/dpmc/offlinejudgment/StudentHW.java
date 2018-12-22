package kr.dpmc.offlinejudgment;

import java.io.File;
import java.util.List;

/**
 * 학생 점수를 저장하는 객체
 */
public class StudentHW {

	/**
	 * 이름
	 */
	public String name = null;
	
	/**
	 * 학번
	 */
	public String id = null;
	
	/**
	 * 파일 이름
	 */
	public String fileName = null;
	
	/**
	 * 파일 객체
	 */
	public File hwfile = null;
	
	/**
	 * 스크린샷 파일들
	 */
	public List<File> screenshotFiles = null;
	
	/**
	 * 스크린샷 점수코드
	 */
	public int screenshotScore = 0;
	
	/**
	 * 과제파일유무 점수코드
	 */
	public int homeworkFileScore = 0;
	
	/**
	 * 소스파일 점수코드
	 */
	public int sourcecodeScore = 0;
	
	/**
	 * 테스트케이스 통합점수
	 */
	public double totalScore = 0;
	
	/**
	 * 테스트 케이스별 점수코드
	 */
	public int[] testcaseScore;

	public StudentHW(String name, String id) {
		this.name = name;
		this.id = id;
	}
	
	//일반학생:
	//	hwfile = null일 수 있음
	// 	returnCode = null일 수 있음
	//  fileName = "null"일 수 있음
	
	//학번-이름-파일이름-파일점수-사진점수-소스코드점수-테스트케이스통합점수-테스트케이스들... -결과요약-점수-코멘트
	//파일점수: 파일 제출여부
	//사진점수: 사진파일 존재여부
	//소스코드점수: 소스코드에 특정 문자열 여부
}
