package org.apache.lucene.swing.models;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.TableModel;
import junit.framework.TestCase;
public class TestBasicTable extends TestCase {
    private TableModel baseTableModel;
    private TableSearcher tableSearcher;
    private List<RestaurantInfo> list;
    @Override
    protected void setUp() throws Exception {
        list = new ArrayList<RestaurantInfo>();
        list.add(DataStore.canolis);
        list.add(DataStore.chris);
        baseTableModel = new BaseTableModel(list.iterator());
        tableSearcher = new TableSearcher(baseTableModel);
    }
    public void testColumns(){
        assertEquals(baseTableModel.getColumnCount(), tableSearcher.getColumnCount());
        assertEquals(baseTableModel.getColumnName(0), tableSearcher.getColumnName(0));
        assertNotSame(baseTableModel.getColumnName(0), tableSearcher.getColumnName(1));
        assertEquals(baseTableModel.getColumnClass(0), tableSearcher.getColumnClass(0));
    }
    public void testRows(){
        assertEquals(list.size(), tableSearcher.getRowCount());
    }
    public void testValueAt(){
        assertEquals(baseTableModel.getValueAt(0,0), tableSearcher.getValueAt(0,0));
        assertEquals(baseTableModel.getValueAt(0,3), tableSearcher.getValueAt(0,3));
    }
}
