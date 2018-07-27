package org.apache.maven.reporting;
import org.codehaus.doxia.sink.Sink;
import java.io.File;
import java.util.Locale;
public interface MavenReport
{
    String ROLE = MavenReport.class.getName();
    String CATEGORY_PROJECT_INFORMATION = "Project Info";
    String CATEGORY_PROJECT_REPORTS = "Project Reports";
    void generate( Sink sink, Locale locale )
        throws MavenReportException;
    String getOutputName();
    String getCategoryName();
    String getName( Locale locale );
    String getDescription( Locale locale );
    void setReportOutputDirectory( File outputDirectory );
    File getReportOutputDirectory();
    boolean isExternalReport();
    boolean canGenerateReport();
}
