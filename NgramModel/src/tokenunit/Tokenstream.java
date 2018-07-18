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
 */

public class Tokenstream {
	public int type;
	public File tokenSourceFile;  //sourcefile containing tokens with type K
	private ArrayList<String> wholeStream;

	public Tokenstream(int ptype, File ptokenSourceFile) {
		this.type = ptype;
		this.tokenSourceFile = ptokenSourceFile;
		importTokenStreamFromFile();
	}

	private boolean tokenFiltering(char ch) {
		if (type == 0) {
			if (ch != '\r' && ch != '\t' && ch != '(' && ch != ')' && ch != ' ' && ch != '\n' && ch != '，' && ch != ',' && ch != '.' && ch != '。' && ch != '！' && ch != '!') {
				return true;
			}
		} else {
			if (ch != '\r' && ch != '\t' && ch != ' ' && ch != '.' && ch != '(' && ch != ')' && ch != '{' && ch != '}' && ch != ';' && ch != '[' && ch != ']' && ch != '\n') {
				return true;
			}
		}
		return false;
	}

	//import token stream from sourcefile
	private void importTokenStreamFromFile() {
		ArrayList<Character> strStream = new ArrayList<>();
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
		ArrayList<Character> strStream = new ArrayList<>();

		try {
			reader = new InputStreamReader(new FileInputStream(tokenSourceFile), "UTF-8");
			int tempchar;
			while ((tempchar = reader.read()) != -1) {
				if (type == 0) {
					if (tokenFiltering((char) tempchar)) {
						strStream.add(new Character((char) tempchar));
					}
				} else {
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
		wholeStream = new ArrayList<>();
		int len = strStream.size();

		//K: Character(default), String, etc.
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < len; i++) {
			char ch = strStream.get(i).charValue();
			if (ch != '\r' && ch != '\t' && ch != ' ' && ch != '.' && ch != '(' && ch != ')' && ch != '{' && ch != '}' && ch != ';' && ch != '[' && ch != ']' && ch != '\n') {
				sb.append(ch);
				if (type == 0) {
						wholeStream.add(sb.toString());
						sb = new StringBuilder();
				}
			} else {
				wholeStream.add(sb.toString());
				sb = new StringBuilder();
			}
		}
	}

	public ArrayList<String> getWholeStream() {
		return wholeStream;
	}
}