package tokenunit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author HHeart
 * @param <K>: type of element in stream
 */

public class Tokenstream<K> {
	public File tokenSourceFile;  //sourcefile containing tokens with type K
	private ArrayList<K> wholeStream;

	public Tokenstream(File ptokenSourceFile) {
		this.tokenSourceFile = ptokenSourceFile;
		importTokenStreamFromFile();
	}

	//import token stream from sourcefile
	private void importTokenStreamFromFile() {
		ArrayList<Character> strStream = new ArrayList<Character>();
		try {
			strStream = importCharStreamFromFile();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//The type of token K can be Character, String and other types
		convertCharStreamToTokenStream(strStream);
	}

	//import character stream from sourcefile
	private ArrayList<Character> importCharStreamFromFile() {
		Reader reader = null;
		ArrayList<Character> strStream = new ArrayList<Character>();

		try {
			reader = new InputStreamReader(new FileInputStream(tokenSourceFile));
			int tempchar;
			while ((tempchar = reader.read()) != -1) {
				if (((char) tempchar) != '\r') {
					strStream.add(new Character((char) tempchar));
				}
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strStream;
	}

	private void convertCharStreamToTokenStream(ArrayList<Character> strStream) {
		wholeStream = new ArrayList<K>();
		int len = strStream.size();

		//K: Character(default), String, etc.
		for (int i = 0; i < len; i++) {
			wholeStream.add((K)strStream.get(i));
		}
	}

	public ArrayList<K> getWholeStream() {
		return wholeStream;
	}
}