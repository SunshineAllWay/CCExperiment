package examples.lf5.UsingLogMonitorAdapter;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.util.LogMonitorAdapter;
public class CustomizedLogLevels {
    public final static LogLevel LEVEL_ONE = new LogLevel("LEVEL 1", 1);
    public final static LogLevel LEVEL_TWO = new LogLevel("LEVEL 2", 2);
    public final static LogLevel LEVEL_THREE = new LogLevel("LEVEL 3", 3);
    public final static LogLevel LEVEL_FOUR = new LogLevel("LEVEL 4", 4);
    public final static LogLevel DEFAULT = new LogLevel("DEFAULT", 0);
    private static LogMonitorAdapter _adapter;
    static {
        _adapter = LogMonitorAdapter.newInstance(new LogLevel[]{DEFAULT, LEVEL_ONE,
                                                                LEVEL_TWO, LEVEL_THREE, LEVEL_FOUR, LogLevel.FATAL});
    }
    public static void main(String[] args) {
        CustomizedLogLevels test = new CustomizedLogLevels();
        test.doMyBidding();
    }
    public void doMyBidding() {
        _adapter.setSevereLevel(LEVEL_ONE);
        String levels = this.getClass().getName();
        _adapter.log(levels, "Using the customized LogLevels");
        _adapter.log(levels, LEVEL_FOUR, "This is a test");
        _adapter.log(levels, LEVEL_THREE, "Hmmm fobidden doughnut");
        _adapter.log(levels, LEVEL_ONE, "Danger Danger Will Robinson",
                new RuntimeException("DANGER"), "32");
        _adapter.log(levels, LEVEL_TWO, "Exit stage right->");
        _adapter.log(levels, LEVEL_FOUR, "What's up Doc?",
                new NullPointerException("Unfortunate exception"));
    }
}
