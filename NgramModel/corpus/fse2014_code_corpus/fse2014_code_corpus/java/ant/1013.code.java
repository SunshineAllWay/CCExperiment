package org.apache.tools.ant.taskdefs;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Date;
import java.text.SimpleDateFormat;
import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Location;
public class TStampTest extends TestCase {
    protected Tstamp tstamp;
    protected Project project;
    protected Location location;
    public TStampTest(String s) {
        super(s);
    }
    protected void setUp() throws Exception {
        location = new Location("test.xml");
        project = new Project();
        tstamp = new Tstamp();
        tstamp.setLocation(location);
        tstamp.setProject(project);
    }
    public void testTimeZone() throws Exception {
        Tstamp.CustomFormat format = tstamp.createFormat();
        format.setProperty("today");
        format.setPattern("HH:mm:ss z");
        format.setTimezone("GMT");
        Date date = Calendar.getInstance().getTime();
        format.execute(project, date, location);
        String today = project.getProperty("today");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss z");
        sdf.setTimeZone( TimeZone.getTimeZone("GMT") );
        String expected = sdf.format(date);
        assertEquals(expected, today);
    }
    public void testWriteOrder() throws Exception {
        Tstamp.CustomFormat format = tstamp.createFormat();
        format.setProperty("TODAY");
        format.setPattern("HH:mm:ss z");
        format.setTimezone("GMT");
        Date date = Calendar.getInstance().getTime();
        format.execute(project, date, location);
        String today = project.getProperty("TODAY");
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss z");
        sdf.setTimeZone( TimeZone.getTimeZone("GMT") );
        String expected = sdf.format(date);
        assertEquals(expected, today);
    }
    public void testPrefix() throws Exception {
        tstamp.setPrefix("prefix");
        tstamp.execute();
        String prop= project.getProperty("prefix.DSTAMP");
        assertNotNull(prop);
    }
    public void testFormatPrefix() throws Exception {
	Tstamp.CustomFormat format = tstamp.createFormat();
        format.setProperty("format");
        format.setPattern("HH:mm:ss z");
        format.setTimezone("GMT");
        tstamp.setPrefix("prefix");
        tstamp.execute();
        String prop= project.getProperty("prefix.format");
        assertNotNull(prop);
    }
}
