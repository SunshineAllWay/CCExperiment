package org.apache.lucene.swing.models;
import javax.swing.ListModel;
import junit.framework.TestCase;
public class TestSearchingList extends TestCase {
    private ListModel baseListModel;
    private ListSearcher listSearcher;
    @Override
    protected void setUp() throws Exception {
        baseListModel = new BaseListModel(DataStore.getRestaurants());
        listSearcher = new ListSearcher(baseListModel);
    }
    public void testSearch(){
        assertEquals(baseListModel.getSize(), listSearcher.getSize());
        listSearcher.search("pino's");
        assertEquals(1, listSearcher.getSize());
        listSearcher.search(null);
        assertEquals(baseListModel.getSize(), listSearcher.getSize());
    }
}
