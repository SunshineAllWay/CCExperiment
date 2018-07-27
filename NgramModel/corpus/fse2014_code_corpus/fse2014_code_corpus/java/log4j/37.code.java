package examples;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;
public class Sort {
  static Logger logger = Logger.getLogger(Sort.class.getName());
  public static void main(String[] args) {
    if(args.length != 2) {
      usage("Incorrect number of parameters.");
    }
    int arraySize = -1;
    try {
      arraySize = Integer.valueOf(args[1]).intValue();
      if(arraySize <= 0) 
	usage("Negative array size.");
    }
    catch(java.lang.NumberFormatException e) {
      usage("Could not number format ["+args[1]+"].");
    }
    PropertyConfigurator.configure(args[0]);
    int[] intArray = new int[arraySize];
    logger.info("Populating an array of " + arraySize + " elements in" +
	     " reverse order.");
    for(int i = arraySize -1 ; i >= 0; i--) {
      intArray[i] = arraySize - i - 1;
    }
    SortAlgo sa1 = new SortAlgo(intArray);
    sa1.bubbleSort();
    sa1.dump();
    SortAlgo sa2 = new SortAlgo(null);
    logger.info("The next log statement should be an error message.");
    sa2.dump();  
    logger.info("Exiting main method.");    
  }
  static
  void usage(String errMsg) {
    System.err.println(errMsg);
    System.err.println("\nUsage: java org.apache.examples.Sort " +
		       "configFile ARRAY_SIZE\n"+
      "where  configFile is a configuration file\n"+
      "      ARRAY_SIZE is a positive integer.\n");
    System.exit(1);
  }
}
