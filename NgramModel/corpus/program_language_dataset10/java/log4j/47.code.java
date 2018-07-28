package examples.lf5.UsingLogMonitorAdapter;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.util.LogMonitorAdapter;
public class UsingLogMonitorAdapter {
  private static LogMonitorAdapter _adapter;
  static {
    _adapter = LogMonitorAdapter.newInstance(LogMonitorAdapter.LOG4J_LOG_LEVELS);
  }
  public static void main(String[] args) {
    UsingLogMonitorAdapter test = new UsingLogMonitorAdapter();
    test.doMyBidding();
  }
  public void doMyBidding() {
    String logger = this.getClass().getName();
    _adapter.log(logger, "Doh this is a debugging");
    _adapter.log(logger, LogLevel.INFO, "Hmmm fobidden doughnut");
    _adapter.log(logger, LogLevel.WARN, "Danger Danger Will Robinson",
        new RuntimeException("DANGER"), "32");
    _adapter.log(logger, LogLevel.ERROR, "Exit stage right->");
    _adapter.log(logger, LogLevel.FATAL, "What's up Doc?",
        new NullPointerException("Unfortunate exception"));
  }
}
