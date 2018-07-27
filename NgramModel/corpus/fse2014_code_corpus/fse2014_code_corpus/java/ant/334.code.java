package org.apache.tools.ant.taskdefs.cvslib;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.AbstractCvsTask;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.FileUtils;
public class ChangeLogTask extends AbstractCvsTask {
    private File usersFile;
    private Vector cvsUsers = new Vector();
    private File inputDir;
    private File destFile;
    private Date startDate;
    private Date endDate;
    private boolean remote = false;
    private String startTag;
    private String endTag;
    private final Vector filesets = new Vector();
    public void setDir(final File inputDir) {
        this.inputDir = inputDir;
    }
    public void setDestfile(final File destFile) {
        this.destFile = destFile;
    }
    public void setUsersfile(final File usersFile) {
        this.usersFile = usersFile;
    }
    public void addUser(final CvsUser user) {
        cvsUsers.addElement(user);
    }
    public void setStart(final Date start) {
        this.startDate = start;
    }
    public void setEnd(final Date endDate) {
        this.endDate = endDate;
    }
    public void setDaysinpast(final int days) {
        final long time = System.currentTimeMillis()
             - (long) days * 24 * 60 * 60 * 1000;
        setStart(new Date(time));
    }
    public void setRemote(final boolean remote) {
        this.remote = remote;
    }
    public void setStartTag(final String start) {
        this.startTag = start;
    }
    public void setEndTag(final String end) {
        this.endTag = end;
    }
    public void addFileset(final FileSet fileSet) {
        filesets.addElement(fileSet);
    }
    public void execute() throws BuildException {
        File savedDir = inputDir; 
        try {
            validate();
            final Properties userList = new Properties();
            loadUserlist(userList);
            for (int i = 0, size = cvsUsers.size(); i < size; i++) {
                final CvsUser user = (CvsUser) cvsUsers.get(i);
                user.validate();
                userList.put(user.getUserID(), user.getDisplayname());
            }
            if (!remote) {
                setCommand("log");
                if (getTag() != null) {
                    CvsVersion myCvsVersion = new CvsVersion();
                    myCvsVersion.setProject(getProject());
                    myCvsVersion.setTaskName("cvsversion");
                    myCvsVersion.setCvsRoot(getCvsRoot());
                    myCvsVersion.setCvsRsh(getCvsRsh());
                    myCvsVersion.setPassfile(getPassFile());
                    myCvsVersion.setDest(inputDir);
                    myCvsVersion.execute();
                    if (myCvsVersion.supportsCvsLogWithSOption()) {
                        addCommandArgument("-S");
                    }
                }
            } else {
                setCommand("");
                addCommandArgument("rlog");
                addCommandArgument("-S");
                addCommandArgument("-N");
            }
            if (null != startTag || null != endTag) {
                String startValue = startTag == null ? "" : startTag;
                String endValue = endTag == null ? "" : endTag;
                addCommandArgument("-r" + startValue + "::" + endValue);
            } else if (null != startDate) {
                final SimpleDateFormat outputDate =
                    new SimpleDateFormat("yyyy-MM-dd");
                final String dateRange = ">=" + outputDate.format(startDate);
                addCommandArgument("-d");
                addCommandArgument(dateRange);
            }
            if (!filesets.isEmpty()) {
                final Enumeration e = filesets.elements();
                while (e.hasMoreElements()) {
                    final FileSet fileSet = (FileSet) e.nextElement();
                    final DirectoryScanner scanner =
                        fileSet.getDirectoryScanner(getProject());
                    final String[] files = scanner.getIncludedFiles();
                    for (int i = 0; i < files.length; i++) {
                        addCommandArgument(files[i]);
                    }
                }
            }
            final ChangeLogParser parser = new ChangeLogParser(remote,
                                                               getPackage(),
                                                               getModules());
            final RedirectingStreamHandler handler =
                new RedirectingStreamHandler(parser);
            log(getCommand(), Project.MSG_VERBOSE);
            setDest(inputDir);
            setExecuteStreamHandler(handler);
            try {
                super.execute();
            } finally {
                final String errors = handler.getErrors();
                if (null != errors) {
                    log(errors, Project.MSG_ERR);
                }
            }
            final CVSEntry[] entrySet = parser.getEntrySetAsArray();
            final CVSEntry[] filteredEntrySet = filterEntrySet(entrySet);
            replaceAuthorIdWithName(userList, filteredEntrySet);
            writeChangeLog(filteredEntrySet);
        } finally {
            inputDir = savedDir;
        }
    }
    private void validate()
         throws BuildException {
        if (null == inputDir) {
            inputDir = getProject().getBaseDir();
        }
        if (null == destFile) {
            final String message = "Destfile must be set.";
            throw new BuildException(message);
        }
        if (!inputDir.exists()) {
            final String message = "Cannot find base dir "
                 + inputDir.getAbsolutePath();
            throw new BuildException(message);
        }
        if (null != usersFile && !usersFile.exists()) {
            final String message = "Cannot find user lookup list "
                 + usersFile.getAbsolutePath();
            throw new BuildException(message);
        }
        if ((null != startTag || null != endTag)
            && (null != startDate || null != endDate)) {
            final String message = "Specify either a tag or date range,"
                + " not both";
            throw new BuildException(message);
        }
    }
    private void loadUserlist(final Properties userList)
         throws BuildException {
        if (null != usersFile) {
            try {
                userList.load(new FileInputStream(usersFile));
            } catch (final IOException ioe) {
                throw new BuildException(ioe.toString(), ioe);
            }
        }
    }
    private CVSEntry[] filterEntrySet(final CVSEntry[] entrySet) {
        final Vector results = new Vector();
        for (int i = 0; i < entrySet.length; i++) {
            final CVSEntry cvsEntry = entrySet[i];
            final Date date = cvsEntry.getDate();
            if (null == date) {
                continue;
            }
            if (null != startDate && startDate.after(date)) {
                continue;
            }
            if (null != endDate && endDate.before(date)) {
                continue;
            }
            results.addElement(cvsEntry);
        }
        final CVSEntry[] resultArray = new CVSEntry[results.size()];
        results.copyInto(resultArray);
        return resultArray;
    }
    private void replaceAuthorIdWithName(final Properties userList,
                                         final CVSEntry[] entrySet) {
        for (int i = 0; i < entrySet.length; i++) {
            final CVSEntry entry = entrySet[ i ];
            if (userList.containsKey(entry.getAuthor())) {
                entry.setAuthor(userList.getProperty(entry.getAuthor()));
            }
        }
    }
    private void writeChangeLog(final CVSEntry[] entrySet)
         throws BuildException {
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(destFile);
            final PrintWriter writer =
                new PrintWriter(new OutputStreamWriter(output, "UTF-8"));
            final ChangeLogWriter serializer = new ChangeLogWriter();
            serializer.printChangeLog(writer, entrySet);
            if (writer.checkError()) {
                throw new IOException("Encountered an error writing changelog");
            }
        } catch (final UnsupportedEncodingException uee) {
            getProject().log(uee.toString(), Project.MSG_ERR);
        } catch (final IOException ioe) {
            throw new BuildException(ioe.toString(), ioe);
        } finally {
            FileUtils.close(output);
        }
    }
}
