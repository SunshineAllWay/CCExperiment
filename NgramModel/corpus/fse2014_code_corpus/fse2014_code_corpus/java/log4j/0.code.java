import org.apache.log4j.helpers.CyclicBuffer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Layout;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.JScrollPane;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Container;
public class AppenderTable extends JTable {
  static Logger logger = Logger.getLogger(AppenderTable.class);
  static public void main(String[] args) {
    if(args.length != 2) {
      System.err.println(
      "Usage: java AppenderTable bufferSize runLength\n"
      +"  where bufferSize is the size of the cyclic buffer in the TableModel\n"
      +"  and runLength is the total number of elements to add to the table in\n"
      +"  this test run.");
      return;
    }
    JFrame frame = new JFrame("JTableAppennder test");
    Container container = frame.getContentPane();
    AppenderTable tableAppender = new AppenderTable();
    int bufferSize = Integer.parseInt(args[0]);
    AppenderTableModel model = new AppenderTableModel(bufferSize);
    tableAppender.setModel(model);
    int runLength = Integer.parseInt(args[1]);
    JScrollPane sp = new JScrollPane(tableAppender);
    sp.setPreferredSize(new Dimension(250, 80));
    container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
    container.add(sp);
    JButton button = new JButton("ADD");
    container.add(button);
    button.addActionListener(new JTableAddAction(tableAppender));
    frame.setSize(new Dimension(500,300));
    frame.setVisible(true);
    long before = System.currentTimeMillis();
    int i = 0;
    while(i++ < runLength) {      
      LoggingEvent event = new LoggingEvent("x", logger, Level.ERROR, 
					    "Message "+i, null);
      tableAppender.doAppend(event);
    }
    long after = System.currentTimeMillis();
    long totalTime = (after-before);
    System.out.println("Total time :"+totalTime+ " milliseconds for "+
		       "runLength insertions.");
    System.out.println("Average time per insertion :"
		       +(totalTime*1000/runLength)+ " micro-seconds.");
  }
  public
  AppenderTable() {
    this.setDefaultRenderer(Object.class, new Renderer());
  }
  public
  void doAppend(LoggingEvent event) {
    ((AppenderTableModel)getModel()).insert(event);
  }
  class Renderer extends JTextArea implements TableCellRenderer {
    PatternLayout layout;
    public
    Renderer() {
      layout = new PatternLayout("%r %p %c [%t] -  %m");
    }
    public Component getTableCellRendererComponent(JTable table,
						   Object value,
						   boolean isSelected,
						   boolean hasFocus,
						   int row,
						   int column) {
      if(value instanceof LoggingEvent) {
	LoggingEvent event = (LoggingEvent) value;
	String str = layout.format(event);
	setText(str);
      } else {
	setText(value.toString());
      }
      return this;
    }
  }
}
class AppenderTableModel extends AbstractTableModel {
  CyclicBuffer cb;
  AppenderTableModel(int size) {
    cb = new CyclicBuffer(size);
  }
  public
  void insert(LoggingEvent event) {
    cb.add(event);
    fireTableDataChanged();
  }
  public 
  int getColumnCount() { 
    return 1; 
  }
  public int getRowCount() { 
    return cb.length();
  }
  public 
  Object getValueAt(int row, int col) {
    return cb.get(row);
  }
}
class JTableAddAction implements ActionListener {
  AppenderTable appenderTable;
  Logger dummy = Logger.getLogger("x");
  int counter = 0;
  public
  JTableAddAction(AppenderTable appenderTable) {
    this.appenderTable = appenderTable;
  }
  public
  void actionPerformed(ActionEvent e) {
    counter++;
    LoggingEvent event = new LoggingEvent("x", dummy, Level.DEBUG, 
					  "Message "+counter, null);    
    appenderTable.doAppend(event);
  }
}
