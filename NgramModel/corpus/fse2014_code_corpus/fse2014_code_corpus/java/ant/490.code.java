package org.apache.tools.ant.taskdefs.optional.junit;
public class OutErrSummaryJUnitResultFormatter
    extends SummaryJUnitResultFormatter {
    public OutErrSummaryJUnitResultFormatter() {
        super();
        setWithOutAndErr(true);
    }
}
