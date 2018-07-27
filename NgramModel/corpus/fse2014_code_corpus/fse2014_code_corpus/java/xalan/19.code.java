package servlet;
public class ApplyXSLTException extends Exception {
    private String myMessage = "";
    private int  myHttpStatusCode = javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR; 
    private Exception myException = null;
    public ApplyXSLTException() 
    { 
        super(); 
    }
    public ApplyXSLTException(String s) 
    { 
        super(); 
	myMessage = s;
    }
    public ApplyXSLTException(int hsc) 
    {
	super();
	myHttpStatusCode = hsc;
    }
    public ApplyXSLTException(String s, int hsc)
    {
	super();
	myHttpStatusCode = hsc;
    }
    public ApplyXSLTException(Exception e)
    {
	super();
	myMessage = e.getMessage();
	myException = e;
    }
    public ApplyXSLTException (String s, Exception e)
    {
	super();
	myMessage = s;
	myException = e;
    }
    public ApplyXSLTException(Exception e, int hsc)
    {
	super();
	myMessage = e.getMessage();
	myException = e;
	myHttpStatusCode = hsc;
    }
    public ApplyXSLTException(String s, Exception e, int hsc)
    {
	super();
	myMessage = s;
	myException = e;
	myHttpStatusCode = hsc;
    }
    public String getMessage()
    {
	return myMessage;
    }
    public void appendMessage(String s)
    {
	myMessage += s;
    }
    public Exception getException()
    {
	return myException;
    }
    public int getStatusCode()
    {
	return myHttpStatusCode;
    }
}
