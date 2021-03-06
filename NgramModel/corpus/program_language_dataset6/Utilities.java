package edu.ucdavis.cacheca;

import java.util.ArrayList;

//@author Originally written in C++ by Zhaopeng; converted to Java by Christine

/**
 * utility operations
 */
public final class Utilities {

	/**
	 * split string by separator
	 * @param line: input string
	 * @param separator: separator
	 * @return the list of segments in the string separated by separator
	 */
	static public ArrayList<String> split(String line, String separator)
	{
	    ArrayList<String> items = new ArrayList<String>();

	    int start, end;

	    int sep_size = separator.length();

	    start = 0;
	    //See: http://en.wikipedia.org/wiki/Comparison_of_programming_languages_(string_functions)
	    end = line.indexOf(separator);

	    while (end != -1) //See: http://en.wikipedia.org/wiki/Comparison_of_programming_languages_(string_functions) >> returns this if not found
	    {
	        items.add(line.substring(start, end));

	        start = end + sep_size;
	        end = line.indexOf(separator, start);
	    }

	    items.add(line.substring(start, line.length()));
	    
	    return items;
	}

	//count words in the string
	static public int countWords(String str)
	{		
		if(str.equals("") || str == null){
			return 0;
		}
		int numberSpaces = 0;
		for(char c : str.toCharArray()){
			if(c == ' '){
				numberSpaces++;
			}
		}
		
	    return ++numberSpaces;
	}

	//get the first n words in the string
	static public String getFirstNWords(String str, int n)
	{
	    if (n == 0)
	        return "";

		int pos = str.indexOf(" ");
		int count = 0;

		//indexOf returns -1 when not found
		while (++count < n && pos != -1)
			pos = str.indexOf(" ", pos+1);

		if (count < n)
			return str;
		else
			return str.substring(0, pos);
	}

	//get the last n words in the string
	static  public String getLastNWords(String str, int n)
	{
	    if (n == 0)
	        return "";

		int pos = str.lastIndexOf(" ");
		int count = 0;

		//indexOf returns -1 when not found
		while (++count < n && pos != -1)
			pos = str.lastIndexOf(" ", pos-1);

		if (count < n)
			return str;
		else
			return str.substring(pos+1, str.length());
	}

}
