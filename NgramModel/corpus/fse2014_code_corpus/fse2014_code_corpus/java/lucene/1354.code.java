package org.apache.lucene.swing.models;
import junit.framework.TestCase;
public class TestUpdatingTable extends TestCase {
    private BaseTableModel baseTableModel;
    private TableSearcher tableSearcher;
    RestaurantInfo infoToAdd1, infoToAdd2;
    @Override
    protected void setUp() throws Exception {
        baseTableModel = new BaseTableModel(DataStore.getRestaurants());
        tableSearcher = new TableSearcher(baseTableModel);
        infoToAdd1 = new RestaurantInfo();
        infoToAdd1.setName("Pino's");
        infoToAdd1.setType("Italian");
        infoToAdd2 = new RestaurantInfo();
        infoToAdd2.setName("Pino's");
        infoToAdd2.setType("Italian");
    }
    public void testAddWithoutSearch(){
        assertEquals(baseTableModel.getRowCount(), tableSearcher.getRowCount());
        int count = tableSearcher.getRowCount();
        baseTableModel.addRow(infoToAdd1);
        count++;
        assertEquals(count, tableSearcher.getRowCount());
    }
    public void testRemoveWithoutSearch(){
        assertEquals(baseTableModel.getRowCount(), tableSearcher.getRowCount());
        int count = tableSearcher.getRowCount();
        baseTableModel.addRow(infoToAdd1);
        baseTableModel.removeRow(infoToAdd1);
        assertEquals(count, tableSearcher.getRowCount());
    }
    public void testAddWithSearch(){
        assertEquals(baseTableModel.getRowCount(), tableSearcher.getRowCount());
        tableSearcher.search("pino's");
        int count = tableSearcher.getRowCount();
        baseTableModel.addRow(infoToAdd2);
        count++;
        assertEquals(count, tableSearcher.getRowCount());
    }
    public void testRemoveWithSearch(){
        assertEquals(baseTableModel.getRowCount(), tableSearcher.getRowCount());
        baseTableModel.addRow(infoToAdd1);
        tableSearcher.search("pino's");
        int count = tableSearcher.getRowCount();
        baseTableModel.removeRow(infoToAdd1);
        count--;
        assertEquals(count, tableSearcher.getRowCount());
    }
}
