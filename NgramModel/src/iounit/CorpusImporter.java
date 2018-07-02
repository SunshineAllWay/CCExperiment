package iounit;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import tokenunit.Tokensequence;
import tokenunit.Tokenstream;

/**
 * @author HHeart
 * CorpusImporter: import the corpus from external files
 * @param <K>: type of token
 */

public class CorpusImporter<K> {
	static public String rootdir;
	public ArrayList<File> trainingDataFileList;
	public int datatype; //0: natural language data; 1: programming language data
	
	public CorpusImporter(int type) {
		String dataInputPath = "config\\corpuspath.properties";
		datatype = type;

		Properties properties = new Properties();  
		try {  
		    InputStream in = new BufferedInputStream(new FileInputStream(dataInputPath));  //error-prone
		    properties.load(in);  
		} catch (Exception e) {  
		    e.printStackTrace();  
		}
		
		String trainingDataSrcDir = "";
		if (datatype == 0) {
			trainingDataSrcDir = properties.getProperty("TRAINING_NL_DATAFILEDIR");
		} else {
			//for programming language
		}
		
		File DataSrcDir = new File(trainingDataSrcDir);
		trainingDataFileList = new ArrayList<>(Arrays.asList(DataSrcDir.listFiles()));
	}
	
	//import Dictionary of Token Sequence from single file
	public ArrayList<K> importCorpusFromSingleFile(File pfile) {
		Tokenstream<K> corpustream = new Tokenstream<>(pfile);
		return corpustream.getWholeStream();
	}

	/**
	 * Import the list of elements of token used for training
	 * @return the list of elements of token used for training
	 */
	public ArrayList<K> importTrainingCorpusFromBase(double ratio) {
		//collect names of files in trainingDataSrcDir and store in trainingDataList
		int fileNum = (int)(trainingDataFileList.size() * ratio);
		ArrayList<K> tokenList = new ArrayList<>();
		for (int i = 0; i < fileNum; i++) {
			tokenList.addAll(importCorpusFromSingleFile(trainingDataFileList.get(i)));
		}
		return tokenList;
	}

	/**
	 * Import the list of elements of token used for testing
	 * @return the list of elements of token used for testing
	 */
	public ArrayList<K> imporTestingCorpusFromBase(double ratio) {
		//collect names of files in trainingDataSrcDir and store in trainingDataList
		int fileNum = trainingDataFileList.size();
		int startIndex = (int)(fileNum * ratio);
		ArrayList<K> tokenList = new ArrayList<>();
		for (int i = startIndex; i < fileNum; i++) {
			tokenList.addAll(importCorpusFromSingleFile(trainingDataFileList.get(i)));
		}
		return tokenList;
	}


}