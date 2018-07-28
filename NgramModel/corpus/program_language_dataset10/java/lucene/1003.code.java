package org.apache.lucene.search.highlight;
public class SimpleHTMLEncoder implements Encoder
{
	public SimpleHTMLEncoder()
	{
	}
	public String encodeText(String originalText)
	{
		return htmlEncode(originalText);
	}
	public final static String htmlEncode(String plainText) 
	{
		if (plainText == null || plainText.length() == 0)
		{
			return "";
		}
		StringBuilder result = new StringBuilder(plainText.length());
		for (int index=0; index<plainText.length(); index++) 
		{
			char ch = plainText.charAt(index);
			switch (ch) 
			{
			case '"':
				result.append("&quot;");
				break;
			case '&':
				result.append("&amp;");
				break;
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			default:
				   if (ch < 128) 
				   {
			           result.append(ch);
			       } 
				   else 
			       {
			           result.append("&#").append((int)ch).append(";");
			       }
			}
		}
		return result.toString();
	}
}