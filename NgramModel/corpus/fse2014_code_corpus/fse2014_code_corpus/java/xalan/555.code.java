package org.apache.xalan.xsltc.util;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
public class JavaCupRedirect {
    private final static String ERRMSG = 
		 "You must supply a filename with the -stdin option.";
    public static void main (String args[]) {
		 boolean systemExitOK = true;
		 InputStream input = null;
		 final int argc = args.length;
		 String[] new_args = new String[argc - 2];
		 int new_argc = 0;
		 for (int i = 0; i < argc; i++) {
		     if (args[i].equals("-stdin")) {
		 		 if ((++i >= argc) || (args[i].startsWith("-"))) {
		 		     System.err.println(ERRMSG);
                     throw new RuntimeException(ERRMSG);
		 		 }
		 		 try {
		 		     input = new FileInputStream(args[i]);
		 		 }
		 		 catch (FileNotFoundException e) {
		 		     System.err.println("Could not open file "+args[i]);
                     throw new RuntimeException(e.getMessage());
		 		 }
		 		 catch (SecurityException e) {
		 		     System.err.println("No permission to file "+args[i]);
                     throw new RuntimeException(e.getMessage());
		 		 }
		     }
		     else {
		 		 if (new_argc == new_args.length) {
		 		     System.err.println("Missing -stdin option!");
                     throw new RuntimeException();
		 		 }
		 		 new_args[new_argc++] = args[i];
		     }
		 }
		 System.setIn(input);
		 try {
		     java_cup.Main.main(new_args);
		 }
		 catch (Exception e) {
		     System.err.println("Error running JavaCUP:");
		     e.printStackTrace();
		 }
    }
}
