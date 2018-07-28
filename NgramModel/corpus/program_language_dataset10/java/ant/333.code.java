package org.apache.tools.ant.taskdefs.cvslib;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import org.apache.tools.ant.taskdefs.AbstractCvsTask;
import org.apache.tools.ant.util.CollectionUtils;
class ChangeLogParser {
    private static final int GET_FILE = 1;
    private static final int GET_DATE = 2;
    private static final int GET_COMMENT = 3;
    private static final int GET_REVISION = 4;
    private static final int GET_PREVIOUS_REV = 5;
    private static final SimpleDateFormat INPUT_DATE
        = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat CVS1129_INPUT_DATE =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US);
    static {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        INPUT_DATE.setTimeZone(utc);
        CVS1129_INPUT_DATE.setTimeZone(utc);
    }
    private String file;
    private String date;
    private String author;
    private String comment;
    private String revision;
    private String previousRevision;
    private int status = GET_FILE;
    private final Hashtable entries = new Hashtable();
    private final boolean remote;
    private final String[] moduleNames;
    private final int[] moduleNameLengths;
    public ChangeLogParser() {
        this(false, "", CollectionUtils.EMPTY_LIST);
    }
    public ChangeLogParser(boolean remote, String packageName, List modules) {
        this.remote = remote;
        ArrayList names = new ArrayList();
        if (packageName != null) {
            for (StringTokenizer tok = new StringTokenizer(packageName);
                 tok.hasMoreTokens(); ) {
                names.add(tok.nextToken());
            }
        }
        for (Iterator iter = modules.iterator(); iter.hasNext(); ) {
            AbstractCvsTask.Module m = (AbstractCvsTask.Module) iter.next();
            names.add(m.getName());
        }
        moduleNames = (String[]) names.toArray(new String[names.size()]);
        moduleNameLengths = new int[moduleNames.length];
        for (int i = 0; i < moduleNames.length; i++) {
            moduleNameLengths[i] = moduleNames[i].length();
        }
    }
    public CVSEntry[] getEntrySetAsArray() {
        final CVSEntry[] array = new CVSEntry[ entries.size() ];
        int i = 0;
        for (Enumeration e = entries.elements(); e.hasMoreElements();) {
            array[i++] = (CVSEntry) e.nextElement();
        }
        return array;
    }
    public void stdout(final String line) {
        switch(status) {
            case GET_FILE:
                reset();
                processFile(line);
                break;
            case GET_REVISION:
                processRevision(line);
                break;
            case GET_DATE:
                processDate(line);
                break;
            case GET_COMMENT:
                processComment(line);
                break;
            case GET_PREVIOUS_REV:
                processGetPreviousRevision(line);
                break;
            default:
                break;
        }
    }
    private void processComment(final String line) {
        final String lineSeparator = System.getProperty("line.separator");
        if (line.equals(
                "=============================================================================")) {
            final int end
                = comment.length() - lineSeparator.length(); 
            comment = comment.substring(0, end);
            saveEntry();
            status = GET_FILE;
        } else if (line.equals("----------------------------")) {
            final int end
                = comment.length() - lineSeparator.length(); 
            comment = comment.substring(0, end);
            status = GET_PREVIOUS_REV;
        } else {
            comment += line + lineSeparator;
        }
    }
    private void processFile(final String line) {
        if (!remote && line.startsWith("Working file:")) {
            file = line.substring(14, line.length());
            status = GET_REVISION;
        } else if (remote && line.startsWith("RCS file:")) {
            int startOfFileName = 0;
            for (int i = 0; i < moduleNames.length; i++) {
                int index = line.indexOf(moduleNames[i]);
                if (index >= 0) {
                    startOfFileName = index + moduleNameLengths[i] + 1;
                    break;
                }
            }
            int endOfFileName = line.indexOf(",v");
            if (endOfFileName == -1) {
                file = line.substring(startOfFileName);
            } else {
                file = line.substring(startOfFileName, endOfFileName);
            }
            status = GET_REVISION;
        }
    }
    private void processRevision(final String line) {
        if (line.startsWith("revision")) {
            revision = line.substring(9);
            status = GET_DATE;
        } else if (line.startsWith("======")) {
            status = GET_FILE;
        }
    }
    private void processDate(final String line) {
        if (line.startsWith("date:")) {
            int endOfDateIndex = line.indexOf(';');
            date = line.substring("date: ".length(), endOfDateIndex);
            int startOfAuthorIndex = line.indexOf("author: ", endOfDateIndex + 1);
            int endOfAuthorIndex = line.indexOf(';', startOfAuthorIndex + 1);
            author = line.substring("author: ".length() + startOfAuthorIndex, endOfAuthorIndex);
            status = GET_COMMENT;
            comment = "";
        }
    }
    private void processGetPreviousRevision(final String line) {
        if (!line.startsWith("revision ")) {
            throw new IllegalStateException("Unexpected line from CVS: "
                + line);
        }
        previousRevision = line.substring("revision ".length());
        saveEntry();
        revision = previousRevision;
        status = GET_DATE;
    }
    private void saveEntry() {
        final String entryKey = date + author + comment;
        CVSEntry entry;
        if (!entries.containsKey(entryKey)) {
            Date dateObject = parseDate(date);
            entry = new CVSEntry(dateObject, author, comment);
            entries.put(entryKey, entry);
        } else {
            entry = (CVSEntry) entries.get(entryKey);
        }
        entry.addFile(file, revision, previousRevision);
    }
    private Date parseDate(final String date) {
        try {
            return INPUT_DATE.parse(date);
        } catch (ParseException e) {
            try {
                return CVS1129_INPUT_DATE.parse(date);
            } catch (ParseException e2) {
                throw new IllegalStateException("Invalid date format: " + date);
            }
        }
    }
    public void reset() {
        this.file = null;
        this.date = null;
        this.author = null;
        this.comment = null;
        this.revision = null;
        this.previousRevision = null;
    }
}
