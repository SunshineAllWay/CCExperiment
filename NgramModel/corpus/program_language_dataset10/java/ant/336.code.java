package org.apache.tools.ant.taskdefs.cvslib;
import java.util.Date;
import java.util.Vector;
public class CVSEntry {
    private Date date;
    private String author;
    private final String comment;
    private final Vector files = new Vector();
    public CVSEntry(final Date date, final String author, final String comment) {
        this.date = date;
        this.author = author;
        this.comment = comment;
    }
    public void addFile(final String file, final String revision) {
        files.addElement(new RCSFile(file, revision));
    }
    public void addFile(final String file, final String revision, final String previousRevision) {
        files.addElement(new RCSFile(file, revision, previousRevision));
    }
    public Date getDate() {
        return date;
    }
    public void setAuthor(final String author) {
        this.author = author;
    }
    public String getAuthor() {
        return author;
    }
    public String getComment() {
        return comment;
    }
    public Vector getFiles() {
        return files;
    }
    public String toString() {
        return getAuthor() + "\n" + getDate() + "\n" + getFiles() + "\n"
            + getComment();
    }
}