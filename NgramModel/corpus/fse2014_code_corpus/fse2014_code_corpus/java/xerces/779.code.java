package dom.util;
import java.io.PrintWriter;
import java.io.StringWriter;
public class Assertion {
    public static boolean verify(boolean result) {
	return verify(result, null);
    }
    public static boolean verify(boolean result, String error) {
	if (!result) {
	    System.err.print("Assertion failed: ");
	    if (error != null) {
		System.err.print(error);
	    }
	    System.err.println();
	    System.err.println(getSourceLocation());
	}
	return result;
    }
    public static boolean equals(String s1, String s2) {
        boolean result = ((s1 != null && s1.equals(s2))
			  || (s1 == null && s2 == null));
	if (!result) {
	    verify(result);
	    System.err.println("  was: equals(" + s1 + ", \"" + s2 + "\")");
	}
	return result;
    }
    public static String getSourceLocation() {
	RuntimeException ex = new RuntimeException("assertion failed");
	StringWriter writer = new StringWriter();
	PrintWriter printer = new PrintWriter(writer);
	ex.printStackTrace(printer);
	String buf = writer.toString();
	int index = buf.lastIndexOf("dom.util.Assertion.");
	index = buf.indexOf('\n', index);
	return buf.substring(index + 1);
    }
}
