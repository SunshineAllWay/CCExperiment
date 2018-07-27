package org.apache.log4j.lf5.viewer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class LogTableColumn implements java.io.Serializable {
  private static final long serialVersionUID = -4275827753626456547L;
  public final static LogTableColumn DATE = new LogTableColumn("Date");
  public final static LogTableColumn THREAD = new LogTableColumn("Thread");
  public final static LogTableColumn MESSAGE_NUM = new LogTableColumn("Message #");
  public final static LogTableColumn LEVEL = new LogTableColumn("Level");
  public final static LogTableColumn NDC = new LogTableColumn("NDC");
  public final static LogTableColumn CATEGORY = new LogTableColumn("Category");
  public final static LogTableColumn MESSAGE = new LogTableColumn("Message");
  public final static LogTableColumn LOCATION = new LogTableColumn("Location");
  public final static LogTableColumn THROWN = new LogTableColumn("Thrown");
  protected String _label;
  private static LogTableColumn[] _log4JColumns;
  private static Map _logTableColumnMap;
  static {
    _log4JColumns = new LogTableColumn[]{DATE, THREAD, MESSAGE_NUM, LEVEL, NDC, CATEGORY,
                                         MESSAGE, LOCATION, THROWN};
    _logTableColumnMap = new HashMap();
    for (int i = 0; i < _log4JColumns.length; i++) {
      _logTableColumnMap.put(_log4JColumns[i].getLabel(), _log4JColumns[i]);
    }
  }
  public LogTableColumn(String label) {
    _label = label;
  }
  public String getLabel() {
    return _label;
  }
  public static LogTableColumn valueOf(String column)
      throws LogTableColumnFormatException {
    LogTableColumn tableColumn = null;
    if (column != null) {
      column = column.trim();
      tableColumn = (LogTableColumn) _logTableColumnMap.get(column);
    }
    if (tableColumn == null) {
      StringBuffer buf = new StringBuffer();
      buf.append("Error while trying to parse (" + column + ") into");
      buf.append(" a LogTableColumn.");
      throw new LogTableColumnFormatException(buf.toString());
    }
    return tableColumn;
  }
  public boolean equals(Object o) {
    boolean equals = false;
    if (o instanceof LogTableColumn) {
      if (this.getLabel() ==
          ((LogTableColumn) o).getLabel()) {
        equals = true;
      }
    }
    return equals;
  }
  public int hashCode() {
    return _label.hashCode();
  }
  public String toString() {
    return _label;
  }
  public static List getLogTableColumns() {
    return Arrays.asList(_log4JColumns);
  }
  public static LogTableColumn[] getLogTableColumnArray() {
    return _log4JColumns;
  }
}
