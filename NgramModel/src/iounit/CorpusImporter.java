package iounit;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

import tokenunit.Tokensequence;
import tokenunit.Tokenstream;

/**
 * @author HHeart
 * CorpusImporter: import the corpus from external files
 */

public class CorpusImporter {
	static public String rootdir;
	public ArrayList<File> trainingDataFileList;
	public ArrayList<File> testingDataFileList;
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
		String testingDataSrcDir = "";
		if (datatype == 0) {
			trainingDataSrcDir = properties.getProperty("TRAINING_NL_DATAFILEDIR");
			testingDataSrcDir = properties.getProperty("TESTING_NL_DATAFILEDIR");
		} else {
			//for programming language
			trainingDataSrcDir = properties.getProperty("TRAINING_PL_DATAFILEDIR");
			testingDataSrcDir = properties.getProperty("TESTING_PL_DATAFILEDIR");
		}
		
		File trainingDataDir = new File(trainingDataSrcDir);
		File testingDataDir = new File(testingDataSrcDir);
		trainingDataFileList = new ArrayList<>();
		testingDataFileList = new ArrayList<>();
		searchForFileListInDirectory(trainingDataDir, trainingDataFileList);
		searchForFileListInDirectory(testingDataDir, testingDataFileList);
	}

	public ArrayList<File> searchForFileListInDirectory(File file, ArrayList<File> fileList) {
		if (!file.isDirectory()) {
			String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
			if (datatype == 1) {
				if (suffix.equals("java") || suffix.equals("c") || suffix.equals("py")) {
					fileList.add(file);
					return fileList;
				}
			} else {
				if (suffix.equals("txt")) {
					fileList.add(file);
					return fileList;
				}
			}
		}

		File[] files = file.listFiles();
		if (files == null) {
			return fileList;
		}

		for (int i = 0; i < files.length; i++) {
			searchForFileListInDirectory(files[i], fileList);
		}

		return fileList;
	}

	//import Dictionary of Token Sequence from single file
	public ArrayList<String> importCorpusFromSingleFile(File pfile) {
		Tokenstream corpustream = new Tokenstream(datatype, pfile);
		return corpustream.getWholeStream();
	}

	/**
	 * Import the list of elements of token used for training
	 * @return the list of elements of token used for training
	 */
	public ArrayList<String> importTrainingCorpusFromBase() {
		//collect names of files in trainingDataSrcDir and store in trainingDataList
		int fileNum = trainingDataFileList.size();
		ArrayList<String> tokenList = new ArrayList<>();
		for (int i = 0; i < fileNum; i++) {
			tokenList.addAll(importCorpusFromSingleFile(trainingDataFileList.get(i)));
		}
		return tokenList;
	}

	public ArrayList<String> importTestingCorpusFromBase() {
		int fileNum = testingDataFileList.size();
		ArrayList<String> tokenList = new ArrayList<>();
		for (int i = 0; i < fileNum; i++) {
			tokenList.addAll(importCorpusFromSingleFile(testingDataFileList.get(i)));
		}
		return tokenList;
	}
}