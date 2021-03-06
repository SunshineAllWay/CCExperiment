package org.apache.lucene.search.highlight;
public class SpanGradientFormatter
	extends GradientFormatter
{
	public SpanGradientFormatter(float maxScore, String minForegroundColor,
            String maxForegroundColor, String minBackgroundColor,
            String maxBackgroundColor)
    {
		super( maxScore, minForegroundColor,
			   maxForegroundColor, minBackgroundColor,
			   maxBackgroundColor);
	}
	@Override
	public String highlightTerm(String originalText, TokenGroup tokenGroup)
    {
        if (tokenGroup.getTotalScore() == 0)
            return originalText;
        float score = tokenGroup.getTotalScore();
        if (score == 0)
        {
            return originalText;
        }
        StringBuilder sb = new StringBuilder( originalText.length() + EXTRA);
		sb.append("<span style=\""); 
		if (highlightForeground) 
		{
			sb.append("color: "); 
			sb.append(getForegroundColorString(score)); 
			sb.append("; "); 
		}
		if (highlightBackground)
		{
			sb.append("background: ");
			sb.append(getBackgroundColorString(score));
			sb.append("; ");
		}
		sb.append("\">");
		sb.append(originalText);
		sb.append("</span>");
        return sb.toString();
    }
	private static final String TEMPLATE = "<span style=\"background: #EEEEEE; color: #000000;\">...</span>";
	private static final int EXTRA = TEMPLATE.length();	
}
