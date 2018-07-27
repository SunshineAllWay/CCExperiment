package org.apache.lucene.swing.models;
import java.util.ArrayList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.swing.models.ListSearcher.CountingCollector;
import org.apache.lucene.util.Version;
public class TableSearcher extends AbstractTableModel {
    protected TableModel tableModel;
    private TableModelListener tableModelListener;
    private ArrayList<Integer> rowToModelIndex = new ArrayList<Integer>();
    private RAMDirectory directory;
    private Analyzer analyzer;
    private static final String ROW_NUMBER = "ROW_NUMBER";
    private String searchString = null;
    public TableSearcher(TableModel tableModel) {
        analyzer = new WhitespaceAnalyzer(Version.LUCENE_CURRENT);
        tableModelListener = new TableModelHandler();
        setTableModel(tableModel);
        tableModel.addTableModelListener(tableModelListener);
        clearSearchingState();
    }
    public TableModel getTableModel() {
        return tableModel;
    }
    public void setTableModel(TableModel tableModel) {
        if (this.tableModel != null) {
            this.tableModel.removeTableModelListener(tableModelListener);
        }
        this.tableModel = tableModel;
        if (this.tableModel != null) {
            this.tableModel.addTableModelListener(tableModelListener);
        }
        reindex();
        fireTableStructureChanged();
    }
    private void reindex() {
        try {
            directory = new RAMDirectory();
            IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
                Version.LUCENE_CURRENT, analyzer));
            for (int row=0; row < tableModel.getRowCount(); row++){
                Document document = new Document();
                document.add(new Field(ROW_NUMBER, "" + row, Field.Store.YES, Field.Index.ANALYZED));
                for (int column=0; column < tableModel.getColumnCount(); column++){
                    String columnName = tableModel.getColumnName(column);
                    String columnValue = String.valueOf(tableModel.getValueAt(row, column)).toLowerCase();
                    document.add(new Field(columnName, columnValue, Field.Store.YES, Field.Index.ANALYZED));
                }
                writer.addDocument(document);
            }
            writer.optimize();
            writer.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public Analyzer getAnalyzer() {
        return analyzer;
    }
    public void setAnalyzer(Analyzer analyzer) {
        this.analyzer = analyzer;
        reindex();
        if (isSearching()){
            search(searchString);
        }
    }
    public void search(String searchString){
        if (searchString == null || searchString.equals("")){
            clearSearchingState();
            fireTableDataChanged();
            return;
        }
        try {
            this.searchString = searchString;
            IndexSearcher is = new IndexSearcher(directory, true);
            String[] fields = new String[tableModel.getColumnCount()];
            for (int t=0; t<tableModel.getColumnCount(); t++){
                fields[t]=tableModel.getColumnName(t);
            }
            MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_CURRENT, fields, analyzer);
            Query query = parser.parse(searchString);
            resetSearchResults(is, query);
        } catch (Exception e){
            e.printStackTrace();
        }
        fireTableStructureChanged();
    }
    private void resetSearchResults(IndexSearcher searcher, Query query) {
        try {
            rowToModelIndex.clear();
            CountingCollector countingCollector = new CountingCollector();
            searcher.search(query, countingCollector);
            ScoreDoc[] hits = searcher.search(query, countingCollector.numHits).scoreDocs;
            for (int t=0; t<hits.length; t++){
                Document document = searcher.doc(hits[t].doc);
                Fieldable field = document.getField(ROW_NUMBER);
                rowToModelIndex.add(Integer.valueOf(field.stringValue()));
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    private int getModelRow(int row){
        return rowToModelIndex.get(row);
    }
    private void clearSearchingState(){
        searchString = null;
        rowToModelIndex.clear();
        for (int t=0; t<tableModel.getRowCount(); t++){
            rowToModelIndex.add(t);
        }
    }
    public int getRowCount() {
        return (tableModel == null) ? 0 : rowToModelIndex.size();
    }
    public int getColumnCount() {
        return (tableModel == null) ? 0 : tableModel.getColumnCount();
    }
    @Override
    public String getColumnName(int column) {
        return tableModel.getColumnName(column);
    }
    @Override
    public Class<?> getColumnClass(int column) {
        return tableModel.getColumnClass(column);
    }
    @Override
    public boolean isCellEditable(int row, int column) {
        return tableModel.isCellEditable(getModelRow(row), column);
    }
    public Object getValueAt(int row, int column) {
        return tableModel.getValueAt(getModelRow(row), column);
    }
    @Override
    public void setValueAt(Object aValue, int row, int column) {
        tableModel.setValueAt(aValue, getModelRow(row), column);
    }
    private boolean isSearching() {
        return searchString != null;
    }
    private class TableModelHandler implements TableModelListener {
        public void tableChanged(TableModelEvent e) {
            if (!isSearching()) {
                clearSearchingState();
                reindex();
                fireTableChanged(e);
                return;
            }
            reindex();
            search(searchString);
            fireTableDataChanged();
            return;
        }
    }
}
