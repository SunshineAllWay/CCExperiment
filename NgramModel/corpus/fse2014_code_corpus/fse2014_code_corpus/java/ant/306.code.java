package org.apache.tools.ant.taskdefs.condition;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.Available;
import org.apache.tools.ant.taskdefs.Checksum;
import org.apache.tools.ant.taskdefs.UpToDate;
public abstract class ConditionBase extends ProjectComponent {
    private String taskName = "condition";
    private Vector conditions = new Vector();
    protected ConditionBase() {
        taskName = "component";
    }
    protected ConditionBase(String taskName) {
        this.taskName = taskName;
    }
    protected int countConditions() {
        return conditions.size();
    }
    protected final Enumeration getConditions() {
        return conditions.elements();
    }
    public void setTaskName(String name) {
        this.taskName = name;
    }
    public String getTaskName() {
        return taskName;
    }
    public void addAvailable(Available a) {
        conditions.addElement(a);
    }
    public void addChecksum(Checksum c) {
        conditions.addElement(c);
    }
    public void addUptodate(UpToDate u) {
        conditions.addElement(u);
    }
    public void addNot(Not n) {
        conditions.addElement(n);
    }
    public void addAnd(And a) {
        conditions.addElement(a);
    }
    public void addOr(Or o) {
        conditions.addElement(o);
    }
    public void addEquals(Equals e) {
        conditions.addElement(e);
    }
    public void addOs(Os o) {
        conditions.addElement(o);
    }
    public void addIsSet(IsSet i) {
        conditions.addElement(i);
    }
    public void addHttp(Http h) {
        conditions.addElement(h);
    }
    public void addSocket(Socket s) {
        conditions.addElement(s);
    }
    public void addFilesMatch(FilesMatch test) {
        conditions.addElement(test);
    }
    public void addContains(Contains test) {
        conditions.addElement(test);
    }
    public void addIsTrue(IsTrue test) {
        conditions.addElement(test);
    }
    public void addIsFalse(IsFalse test) {
        conditions.addElement(test);
    }
    public void addIsReference(IsReference i) {
        conditions.addElement(i);
    }
    public void addIsFileSelected(IsFileSelected test) {
        conditions.addElement(test);
    }
    public void add(Condition c) {
        conditions.addElement(c);
    }
}
