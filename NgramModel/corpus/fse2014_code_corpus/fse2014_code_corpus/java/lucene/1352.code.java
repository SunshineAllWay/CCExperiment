package org.apache.lucene.swing.models;
import junit.framework.TestCase;
import javax.swing.table.TableModel;
public class TestSearchingTable extends TestCase {
    private TableModel baseTableModel;
    private TableSearcher tableSearcher;
    @Override
    protected void setUp() throws Exception {
        baseTableModel = new BaseTableModel(DataStore.getRestaurants());
        tableSearcher = new TableSearcher(baseTableModel);
    }
    public void testSearch(){
        assertEquals(baseTableModel.getRowCount(), tableSearcher.getRowCount());
        tableSearcher.search("pino's");
        assertEquals(1, tableSearcher.getRowCount());
        tableSearcher.search(null);
        assertEquals(baseTableModel.getRowCount(), tableSearcher.getRowCount());
    }
}
