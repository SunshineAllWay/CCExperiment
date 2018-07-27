package org.apache.lucene.swing.models;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractListModel;
public class BaseListModel extends AbstractListModel {
    private List<Object> data = new ArrayList<Object>();
    public BaseListModel(Iterator<?> iterator) {
        while (iterator.hasNext()) {
            data.add(iterator.next());
        }
    }
    public int getSize() {
        return data.size();
    }
    public Object getElementAt(int index) {
        return data.get(index);
    }
    public void addRow(Object toAdd) {
        data.add(toAdd);
        fireContentsChanged(this, 0, getSize());
    }
    public void removeRow(Object toRemove) {
        data.remove(toRemove);
        fireContentsChanged(this, 0, getSize());
    }
}
