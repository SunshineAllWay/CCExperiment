package org.apache.xalan.transformer;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.w3c.dom.Element;
class NumeratorFormatter
{
  protected Element m_xslNumberElement;
  NumberFormatStringTokenizer m_formatTokenizer;
  Locale m_locale;
  java.text.NumberFormat m_formatter;
  TransformerImpl m_processor;
  private final static DecimalToRoman m_romanConvertTable[] = {
    new DecimalToRoman(1000, "M", 900, "CM"),
    new DecimalToRoman(500, "D", 400, "CD"),
    new DecimalToRoman(100L, "C", 90L, "XC"),
    new DecimalToRoman(50L, "L", 40L, "XL"),
    new DecimalToRoman(10L, "X", 9L, "IX"),
    new DecimalToRoman(5L, "V", 4L, "IV"),
    new DecimalToRoman(1L, "I", 1L, "I") };
  private final static char[] m_alphaCountTable = { 'Z',  
                                                    'A', 'B', 'C', 'D', 'E',
                                                    'F', 'G', 'H', 'I', 'J',
                                                    'K', 'L', 'M', 'N', 'O',
                                                    'P', 'Q', 'R', 'S', 'T',
                                                    'U', 'V', 'W', 'X', 'Y' };
  NumeratorFormatter(Element xslNumberElement, TransformerImpl processor)
  {
    m_xslNumberElement = xslNumberElement;
    m_processor = processor;
  }  
  protected String int2alphaCount(int val, char[] table)
  {
    int radix = table.length;
    char buf[] = new char[100];
    int charPos = buf.length - 1;  
    int lookupIndex = 1;  
    int correction = 0;
    do
    {
      correction =
        ((lookupIndex == 0) || (correction != 0 && lookupIndex == radix - 1))
        ? (radix - 1) : 0;
      lookupIndex = (val + correction) % radix;
      val = (val / radix);
      if (lookupIndex == 0 && val == 0)
        break;
      buf[charPos--] = table[lookupIndex];
    }
    while (val > 0);
    return new String(buf, charPos + 1, (buf.length - charPos - 1));
  }
  String long2roman(long val, boolean prefixesAreOK)
  {
    if (val <= 0)
    {
      return "#E(" + val + ")";
    }
    String roman = "";
    int place = 0;
    if (val <= 3999L)
    {
      do
      {
        while (val >= m_romanConvertTable[place].m_postValue)
        {
          roman += m_romanConvertTable[place].m_postLetter;
          val -= m_romanConvertTable[place].m_postValue;
        }
        if (prefixesAreOK)
        {
          if (val >= m_romanConvertTable[place].m_preValue)
          {
            roman += m_romanConvertTable[place].m_preLetter;
            val -= m_romanConvertTable[place].m_preValue;
          }
        }
        place++;
      }
      while (val > 0);
    }
    else
    {
      roman = "#error";
    }
    return roman;
  }  
  class NumberFormatStringTokenizer
  {
    private int currentPosition;
    private int maxPosition;
    private String str;
    NumberFormatStringTokenizer(String str)
    {
      this.str = str;
      maxPosition = str.length();
    }
    void reset()
    {
      currentPosition = 0;
    }
    String nextToken()
    {
      if (currentPosition >= maxPosition)
      {
        throw new NoSuchElementException();
      }
      int start = currentPosition;
      while ((currentPosition < maxPosition)
             && Character.isLetterOrDigit(str.charAt(currentPosition)))
      {
        currentPosition++;
      }
      if ((start == currentPosition)
              && (!Character.isLetterOrDigit(str.charAt(currentPosition))))
      {
        currentPosition++;
      }
      return str.substring(start, currentPosition);
    }
    boolean hasMoreTokens()
    {
      return (currentPosition >= maxPosition) ? false : true;
    }
    int countTokens()
    {
      int count = 0;
      int currpos = currentPosition;
      while (currpos < maxPosition)
      {
        int start = currpos;
        while ((currpos < maxPosition)
               && Character.isLetterOrDigit(str.charAt(currpos)))
        {
          currpos++;
        }
        if ((start == currpos)
                && (Character.isLetterOrDigit(str.charAt(currpos)) == false))
        {
          currpos++;
        }
        count++;
      }
      return count;
    }
  }  
}
