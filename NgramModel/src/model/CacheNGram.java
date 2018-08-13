package model;

import iounit.MethodContextProcessor;
import parseunit.ASTGenerator;
import parseunit.MyMethodNode;
import tokenunit.Tokensequence;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author HHeart
 */

public class CacheNGram extends BasicNGram{
	public ArrayList<File> cacheFileList;
	public File curFile;

	public ArrayList<MyMethodNode> nodeList;
	public MyMethodNode curNode;

	public HashMap<MyMethodNode, ArrayList<String>> nodeToContextMap;
	public HashMap<MyMethodNode, Double> nodeToSimMap;


	public CacheNGram(int ngramN, int type, ArrayList<File> cacheList, File pCurFile) {
		super(ngramN, type);
        this.cacheFileList = cacheList;
        this.curFile = pCurFile;
        nodeList = new ArrayList<>();
        curNode = null;


	}

	/**
	 * Another training way
	 */
	public void preAction() {
		ASTGenerator gen = new ASTGenerator(curFile);
		ArrayList<MyMethodNode> ls = gen.methodNodeList;
		if (ls.size() == 0) {
			return;
		}

		curNode = ls.get(ls.size() - 1);
		nodeList.addAll(ls);

		for (int i = 0; i < cacheFileList.size(); i++) {
			MethodContextProcessor processor = new MethodContextProcessor(cacheFileList.get(i), curNode);
		    nodeList.addAll(processor.nodeList);
		    nodeToContextMap.putAll(processor.nodeToContextMap);
		    nodeToSimMap.putAll(processor.nodeToSimMap);
		}

		//training
		for (int i = 0; i < nodeList.size(); i++) {
			ArrayList<String> context = nodeToContextMap.get(nodeList.get(i));
			double sim = nodeToSimMap.get(nodeList.get(i)).doubleValue();
			trainingInSingleRound(context, sim);
		}
	}

	private void trainingInSingleRound(ArrayList<String> stringList, double sim) {
		int len = stringList.size();

		for (int i = 0; i < len - n; i++) {
			ArrayList<String> ls = new ArrayList<>();
			for (int j = i; j < i + n; j++) {
				ls.add(stringList.get(j));
			}
			Tokensequence tmpTokenInitSeq = new Tokensequence(ls);
			String lastTokenElem = stringList.get(i + n);
			HashMap<String, Double> tokenCntMap;

			if (seqCntModel.containsKey(tmpTokenInitSeq)) {
				tokenCntMap = seqCntModel.get(tmpTokenInitSeq);
				if (tokenCntMap.containsKey(lastTokenElem)) {
					double cnt = tokenCntMap.get(lastTokenElem).doubleValue();
					cnt += sim * 100;
					tokenCntMap.put(lastTokenElem, cnt);
				} else {
					tokenCntMap.put(lastTokenElem, sim * 100);
				}
			} else {
				tokenCntMap = new HashMap<>();
				tokenCntMap.put(lastTokenElem, sim * 100);
				seqCntModel.put(tmpTokenInitSeq, tokenCntMap);
				seqNum++;
			}
		}
	}
}