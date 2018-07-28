package org.apache.cassandra.utils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sun.jna.LastErrorException;
import com.sun.jna.Native;
public final class CLibrary
{
    private static Logger logger = LoggerFactory.getLogger(CLibrary.class);
    private static final int MCL_CURRENT = 1;
    private static final int MCL_FUTURE = 2;
    private static final int ENOMEM = 12;
    private static final int F_GETFL   = 3;  
    private static final int F_SETFL   = 4;  
    private static final int F_NOCACHE = 48; 
    private static final int O_DIRECT  = 040000; 
    private static final int POSIX_FADV_NORMAL     = 0; 
    private static final int POSIX_FADV_RANDOM     = 1; 
    private static final int POSIX_FADV_SEQUENTIAL = 2; 
    private static final int POSIX_FADV_WILLNEED   = 3; 
    private static final int POSIX_FADV_DONTNEED   = 4; 
    private static final int POSIX_FADV_NOREUSE    = 5; 
    static
    {
        try
        {
            Native.register("c");
        }
        catch (NoClassDefFoundError e)
        {
            logger.info("JNA not found. Native methods will be disabled.");
        }
        catch (UnsatisfiedLinkError e)
        {
            logger.info("Unable to link C library. Native methods will be disabled.");
        }
        catch (NoSuchMethodError e)
        {
            logger.warn("Obsolete version of JNA present; unable to register C library. Upgrade to JNA 3.2.7 or later");
        }
    }
    private static native int mlockall(int flags) throws LastErrorException;
    private static native int munlockall() throws LastErrorException;
    private static native int link(String from, String to) throws LastErrorException;
    public static native int fcntl(int fd, int command, long flags) throws LastErrorException;
    public static native int posix_fadvise(int fd, int offset, int len, int flag) throws LastErrorException;
    private static int errno(RuntimeException e)
    {
        assert e instanceof LastErrorException;
        try
        {
            return ((LastErrorException) e).getErrorCode();
        }
        catch (NoSuchMethodError x)
        {
            logger.warn("Obsolete version of JNA present; unable to read errno. Upgrade to JNA 3.2.7 or later");
            return 0;
        }
    }
    private CLibrary() {}
    public static void tryMlockall()
    {
        try
        {
            int result = mlockall(MCL_CURRENT);
            assert result == 0; 
            logger.info("JNA mlockall successful");
        }
        catch (UnsatisfiedLinkError e)
        {
        }
        catch (RuntimeException e)
        {
            if (!(e instanceof LastErrorException))
                throw e;
            if (errno(e) == ENOMEM && System.getProperty("os.name").toLowerCase().contains("linux"))
            {
                logger.warn("Unable to lock JVM memory (ENOMEM)."
                             + " This can result in part of the JVM being swapped out, especially with mmapped I/O enabled."
                             + " Increase RLIMIT_MEMLOCK or run Cassandra as root.");
            }
            else if (!System.getProperty("os.name").toLowerCase().contains("mac"))
            {
                logger.warn("Unknown mlockall error " + errno(e));
            }
        }
    }
    public static void createHardLink(File sourceFile, File destinationFile) throws IOException
    {
        try
        {
            int result = link(sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath());
            assert result == 0; 
        }
        catch (UnsatisfiedLinkError e)
        {
            createHardLinkWithExec(sourceFile, destinationFile);
        }
        catch (RuntimeException e)
        {
            if (!(e instanceof LastErrorException))
                throw e;
            throw new IOException(String.format("Unable to create hard link from %s to %s (errno %d)",
                                                sourceFile, destinationFile, errno(e)));
        }
    }
    private static void createHardLinkWithExec(File sourceFile, File destinationFile) throws IOException
    {
        String osname = System.getProperty("os.name");
        ProcessBuilder pb;
        if (osname.startsWith("Windows"))
        {
            float osversion = Float.parseFloat(System.getProperty("os.version"));
            if (osversion >= 6.0f)
            {
                pb = new ProcessBuilder("cmd", "/c", "mklink", "/H", destinationFile.getAbsolutePath(), sourceFile.getAbsolutePath());
            }
            else
            {
                pb = new ProcessBuilder("fsutil", "hardlink", "create", destinationFile.getAbsolutePath(), sourceFile.getAbsolutePath());
            }
        }
        else
        {
            pb = new ProcessBuilder("ln", sourceFile.getAbsolutePath(), destinationFile.getAbsolutePath());
            pb.redirectErrorStream(true);
        }
        Process p = pb.start();
        try
        {
            p.waitFor();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
    public static void trySkipCache(int fd, int offset, int len)
    {
        if (fd < 0)
            return;
        try
        {
            if (System.getProperty("os.name").toLowerCase().contains("linux"))
            {
                posix_fadvise(fd, offset, len, POSIX_FADV_DONTNEED);
            }
            else if (System.getProperty("os.name").toLowerCase().contains("mac"))
            {
                tryFcntl(fd, F_NOCACHE, 1);
            }
        }
        catch (UnsatisfiedLinkError e)
        {
        }
    }
    public static int tryFcntl(int fd, int command, int flags)
    {
        int result = -1;
        try
        {
            result = CLibrary.fcntl(fd, command, flags);
            assert result >= 0; 
        }
        catch (RuntimeException e)
        {
            if (!(e instanceof LastErrorException))
                throw e;
            logger.warn(String.format("fcntl(%d, %d, %d) failed, errno (%d).",
                                      fd, command, flags, CLibrary.errno(e)));
        }
        return result;
    }
    public static int getfd(FileDescriptor descriptor)
    {
        Field field = FBUtilities.getProtectedField(descriptor.getClass(), "fd");
        if (field == null)
            return -1;
        try
        {
            return field.getInt(descriptor);
        }
        catch (Exception e)
        {
            logger.warn("unable to read fd field from FileDescriptor");
        }
        return -1;
    }
}
