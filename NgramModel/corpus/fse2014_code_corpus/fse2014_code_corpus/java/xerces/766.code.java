package dom.range;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.xerces.dom.DocumentImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.ranges.Range;
public class TestCompare extends TestCase 
{
    public TestCompare(String name) {
            super(name);
    }
    private Range[] buildRanges()
    {
        DocumentImpl doc=new org.apache.xerces.dom.DocumentImpl();
        Element body = doc.createElement("BODY");
        doc.appendChild(body);
        Element h1 = doc.createElement("H1");
        body.appendChild(h1);
        Text title = doc.createTextNode("Title");
        h1.appendChild(title);
        Element p = doc.createElement("P");
        body.appendChild(p);
        Text blah = doc.createTextNode("Blah xyz.");
        p.appendChild(blah);
        Range[] ranges = new Range[4];
        ranges[0] = doc.createRange();
        ranges[0].setStart( title, 2 );
        ranges[0].setEnd( blah, 2 );
        ranges[1] = doc.createRange();
        ranges[1].setStart( body, 1 );
        ranges[1].setEnd( body, 2 );
        ranges[2] = doc.createRange();
        ranges[2].setStart( p, 0 );
        ranges[2].setEnd( p, 1 );
        ranges[3] = doc.createRange();
        ranges[3].setStart( blah, 0 );
        ranges[3].setEnd( blah, 9 );
        return ranges;
    }
    private final int[][] results_START_TO_START = 
    {
        { 0, -1, -1, -1 },  
        { 1, 0, -1, -1 },   
        { 1, 1, 0, -1 },    
        { 1, 1, 1, 0 },     
    };
    private final int[][] results_START_TO_END = 
    {
        { 1, 1, 1, 1 },  
        { 1, 1, 1, 1 },  
        { 1, 1, 1, 1 },  
        { 1, 1, 1, 1 },  
    };
    private final int[][] results_END_TO_START = 
    {
        { -1, -1, -1, -1 },    
        { -1, -1, -1, -1 },    
        { -1, -1, -1, -1 },    
        { -1, -1, -1, -1 },    
    };
    private final int[][] results_END_TO_END = 
    {
        { 0, -1, -1, -1 },       
        { 1, 0, 1, 1 },          
        { 1, -1, 0, 1 },         
        { 1, -1, -1, 0 },        
    };
    private void doTestCompare( short how, int[][] results )
    {
        Range[] ranges = buildRanges();
        for( int i=0; i<ranges.length; ++i )
        {
            for( int j=0; j<ranges.length; ++j )
            {
                int result = ranges[i].compareBoundaryPoints( how, ranges[j] );
                assertTrue( "Compare returned the wrong value i="+i+" j="+j + " result="+result, result == results[i][j] );
            }
        }
    }
    public void testCompareStartToStart()
    {
        doTestCompare( Range.START_TO_START, results_START_TO_START );
    }
    public void testCompareStartToEnd()
    {
        doTestCompare( Range.START_TO_END, results_START_TO_END );
    }
    public void testCompareEndToStart()
    {
        doTestCompare( Range.END_TO_START, results_END_TO_START );
    }
    public void testCompareEndToEnd()
    {
        doTestCompare( Range.END_TO_END, results_END_TO_END );
    }
    public static junit.framework.Test suite() {
        return new TestSuite( TestCompare.class );
    }
    public static void main (String[] args) {
            junit.textui.TestRunner.run (suite());
    }
}
