import javax.xml.namespace.*;
import javax.xml.xpath.*;
import java.util.Iterator;
public class XPathResolver
{
    private static final String EXPR = "ex:addFunc(2, 3) + $xyz";
    public static class MyNamespaceContext implements NamespaceContext
    {
        public String getNamespaceURI(String prefix)
        {
            if (prefix == null)
              throw new IllegalArgumentException("The prefix cannot be null.");
            if (prefix.equals("ex"))
                return "http://ex.com";
            else
                return null;
        }
        public String getPrefix(String namespace)
        {
            if (namespace == null)
              throw new IllegalArgumentException("The namespace uri cannot be null.");
            if (namespace.equals("http://ex.com"))
              return "ex";
            else
              return null;
        }
        public Iterator getPrefixes(String namespace)
        {
            return null;
        }
    }
    public static class MyFunctionResolver implements XPathFunctionResolver
    {
    	public XPathFunction resolveFunction(QName fname, int arity)
    	{
    	  if (fname == null)
    	    throw new NullPointerException("The function name cannot be null.");
    	  if (fname.equals(new QName("http://ex.com", "addFunc", "ex")))
    	    return new XPathFunction() {
    	      public Object evaluate(java.util.List args) {
    	        if (args.size() == 2) {
    	          Double arg1 = (Double)args.get(0);
    	          Double arg2 = (Double)args.get(1);
    	          return new Double(arg1.doubleValue() + arg2.doubleValue());
    	        }
    	        else
    	          return null;
    	      }
    	    };
    	  else
    	    return null;
    	}
    }
    public static class MyVariableResolver implements XPathVariableResolver
    {
      public Object resolveVariable(QName varName)
      {
        if (varName == null)
          throw new NullPointerException("The variable name cannot be null.");
        if (varName.equals(new QName("", "xyz")))
          return new Double(4.0);
        else
          return null;        	
      }
    }
    public static void main(String[] args)
    {    	        
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext(new MyNamespaceContext());
        xpath.setXPathFunctionResolver(new MyFunctionResolver());
        xpath.setXPathVariableResolver(new MyVariableResolver());
        Object result = null;
        try {
          result = xpath.evaluate(EXPR, (Object)null, XPathConstants.NUMBER);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("The evaluation result: " + result);
    }
}
