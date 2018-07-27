package servlet;
import java.net.MalformedURLException;
import javax.servlet.*;
import javax.servlet.http.*;
public class ApplyXSLTProperties {
    private final String DEFAULT_URL;
    private final String DEFAULT_xslURL;
    private final boolean DEFAULT_debug;
    private final boolean DEFAULT_noCW;
    public ApplyXSLTProperties() 
    {
	DEFAULT_URL = null;
	DEFAULT_xslURL = null;
	DEFAULT_debug = false;
	DEFAULT_noCW = false;
    }
    public ApplyXSLTProperties(ServletConfig config)
    {
	String xm = config.getInitParameter("URL"),
	       xu = config.getInitParameter("xslURL"),
	       db = config.getInitParameter("debug"),
	       cw = config.getInitParameter("noConflictWarnings");
	if (xm != null) DEFAULT_URL = xm;
	else DEFAULT_URL = null;
	if (xu != null) DEFAULT_xslURL = xu;
	else DEFAULT_xslURL = null;
	if (db != null) DEFAULT_debug = new Boolean(db).booleanValue();
	else DEFAULT_debug = false;
	if (cw != null) DEFAULT_noCW = new Boolean(cw).booleanValue();
	else DEFAULT_noCW = false;
    }
    public String getRequestParmString(HttpServletRequest request, String param)
    {
	if (request != null) { 
	    String[] paramVals = request.getParameterValues(param); 
	    if (paramVals != null) 
		return paramVals[0];
	}
	return null;
    }
    public String getXMLurl(HttpServletRequest request)
    throws MalformedURLException
    {
	String temp = getRequestParmString(request, "URL");
	if (temp != null)
	    return temp;
	return DEFAULT_URL;
    }     
    public String getXSLurl(HttpServletRequest request)
    throws MalformedURLException
    {  
	String temp = getRequestParmString(request, "xslURL");
	if (temp != null)
	    return temp;
	return DEFAULT_xslURL;
    }
    public boolean isDebug(HttpServletRequest request)
    {
	String temp = getRequestParmString(request, "debug");
	if (temp != null)
	    return new Boolean(temp).booleanValue();
	return DEFAULT_debug;
    }
    boolean isNoCW(HttpServletRequest request)
    {
	String temp = getRequestParmString(request, "noConflictWarnings");
	if (temp != null)
	    return new Boolean(temp).booleanValue();
	return DEFAULT_noCW;
    }    
}
