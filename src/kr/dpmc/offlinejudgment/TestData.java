package kr.dpmc.offlinejudgment;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class TestData {

	public List<String> input = new LinkedList<>();
	public List<String> outputLow = new LinkedList<>();
	public List<String> outputHigh = new LinkedList<>();
	public List<String> outputEqual = new LinkedList<>();

	@Deprecated
	public TestData(File in, File outRaw, File outHigh) {
		input = OJApi.getSourceCodeToStringBuilder(in);
		outputLow = OJApi.getSourceCodeToStringBuilder(outRaw);
		outputHigh = OJApi.getSourceCodeToStringBuilder(outHigh);
	}//레거시 코드

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public TestData(File testData) {
		List<String> list = OJApi.getSourceCodeToStringBuilder(testData);
		List[] listArray = { input, outputLow, outputHigh, outputEqual};
		int i = 0;
		int arrayIndex = 0;
		while (i < list.size()) {
			String s = list.get(i);
			String sLower = s.toLowerCase();
			boolean isContinue = false;
			if (sLower.equals("input:")) {
				arrayIndex = 0;
				isContinue = true;
			} else if (sLower.equals("outputlow:") || sLower.equals("low:")) {
				arrayIndex = 1;
				isContinue = true;
			} else if (sLower.equals("outputhigh:") || sLower.equals("high:")) {
				arrayIndex = 2;
				isContinue = true;
			} else if (sLower.equals("outputequal:") || sLower.equals("equal:")) {
				arrayIndex = 3;
				isContinue = true;
			}

			if (isContinue) {
				i++;
				continue;
			}

			i++;
			if (!s.equals("")) {
				listArray[arrayIndex].add(s);
			}
		}
	}
}
