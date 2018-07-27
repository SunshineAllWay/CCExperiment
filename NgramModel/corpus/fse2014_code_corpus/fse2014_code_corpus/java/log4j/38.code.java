package examples;
import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
public class SortAlgo {
  final static String className = SortAlgo.class.getName();
  final static Logger LOG = Logger.getLogger(className);
  final static Logger OUTER = Logger.getLogger(className + ".OUTER");
  final static Logger INNER = Logger.getLogger(className + ".INNER");
  final static Logger DUMP = Logger.getLogger(className + ".DUMP");
  final static Logger SWAP = Logger.getLogger(className + ".SWAP");
  int[] intArray;
  SortAlgo(int[] intArray) {
    this.intArray = intArray;
  }
  void bubbleSort() {
    LOG.info( "Entered the sort method.");
    for(int i = intArray.length -1; i >= 0  ; i--) {
      NDC.push("i=" + i);
      OUTER.debug("in outer loop.");
      for(int j = 0; j < i; j++) {
	NDC.push("j=" + j);
	INNER.debug( "in inner loop.");
         if(intArray[j] > intArray[j+1])
	   swap(j, j+1);
	NDC.pop();
      }
      NDC.pop();
    }
  }  
  void dump() {    
    if(! (this.intArray instanceof int[])) {
      DUMP.error("Tried to dump an uninitialized array.");
      return;
    }
    DUMP.info("Dump of integer array:");
    for(int i = 0; i < this.intArray.length; i++) {
      DUMP.info("Element [" + i + "]=" + this.intArray[i]);
    }    
  }
  void swap(int l, int r) {
    SWAP.debug( "Swapping intArray["+l+"]=" + intArray[l] +
	                     " and intArray["+r+"]=" + intArray[r]);
    int temp = this.intArray[l];
    this.intArray[l] = this.intArray[r];
    this.intArray[r] = temp;
  }
}
