package org.apache.cassandra.io.sstable;
import java.io.File;
import java.util.StringTokenizer;
import com.google.common.base.Objects;
import org.apache.cassandra.utils.Pair;
public class Descriptor
{
    public static final String LEGACY_VERSION = "a";
    public static final String CURRENT_VERSION = "f";
    public final File directory;
    public final String version;
    public final String ksname;
    public final String cfname;
    public final int generation;
    public final boolean temporary;
    private final int hashCode;
    public final boolean hasStringsInBloomFilter;
    public final boolean hasIntRowSize;
    public final boolean hasEncodedKeys;
    public final boolean isLatestVersion;
    public final boolean usesOldBloomFilter;
    public Descriptor(File directory, String ksname, String cfname, int generation, boolean temp)
    {
        this(CURRENT_VERSION, directory, ksname, cfname, generation, temp);
    }
    public Descriptor(String version, File directory, String ksname, String cfname, int generation, boolean temp)
    {
        assert version != null && directory != null && ksname != null && cfname != null;
        this.version = version;
        this.directory = directory;
        this.ksname = ksname;
        this.cfname = cfname;
        this.generation = generation;
        temporary = temp;
        hashCode = Objects.hashCode(directory, generation, ksname, cfname);
        hasStringsInBloomFilter = version.compareTo("c") < 0;
        hasIntRowSize = version.compareTo("d") < 0;
        hasEncodedKeys = version.compareTo("e") < 0;
        isLatestVersion = version.compareTo(CURRENT_VERSION) == 0;
        usesOldBloomFilter = version.compareTo("f") < 0;
    }
    public String filenameFor(Component component)
    {
        return filenameFor(component.name());
    }
    private String baseFilename()
    {
        StringBuilder buff = new StringBuilder();
        buff.append(directory).append(File.separatorChar);
        buff.append(cfname).append("-");
        if (temporary)
            buff.append(SSTable.TEMPFILE_MARKER).append("-");
        if (!LEGACY_VERSION.equals(version))
            buff.append(version).append("-");
        buff.append(generation);
        return buff.toString();
    }
    public String filenameFor(String suffix)
    {
        return baseFilename() + "-" + suffix;
    }
    public static Descriptor fromFilename(String filename)
    {
        int separatorPos = filename.lastIndexOf(File.separatorChar);
        assert separatorPos != -1 : "Filename must include parent directory.";
        File directory = new File(filename.substring(0, separatorPos));
        String name = filename.substring(separatorPos+1, filename.length());
        return fromFilename(directory, name).left;
    }
    public static Pair<Descriptor,String> fromFilename(File directory, String name)
    {
        String ksname = directory.getName();
        StringTokenizer st = new StringTokenizer(name, "-");
        String nexttok = null;
        String cfname = st.nextToken();
        nexttok = st.nextToken();
        boolean temporary = false;
        if (nexttok.equals(SSTable.TEMPFILE_MARKER))
        {
            temporary = true;
            nexttok = st.nextToken();
        }
        String version = LEGACY_VERSION;
        if (versionValidate(nexttok))
        {
            version = nexttok;
            nexttok = st.nextToken();
        }
        int generation = Integer.parseInt(nexttok);
        String component = st.nextToken();
        return new Pair<Descriptor,String>(new Descriptor(version, directory, ksname, cfname, generation, temporary), component);
    }
    public Descriptor asTemporary(boolean temporary)
    {
        return new Descriptor(version, directory, ksname, cfname, generation, temporary);
    }
    static boolean versionValidate(String ver)
    {
        if (ver.length() < 1) return false;
        for (char ch : ver.toCharArray())
            if (!Character.isLetter(ch) || !Character.isLowerCase(ch))
                return false;
        return true;
    }
    public boolean isFromTheFuture()
    {
        return version.compareTo(CURRENT_VERSION) > 0;
    }
    @Override
    public String toString()
    {
        return baseFilename();
    }
    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof Descriptor))
            return false;
        Descriptor that = (Descriptor)o;
        return that.directory.equals(this.directory) && that.generation == this.generation && that.ksname.equals(this.ksname) && that.cfname.equals(this.cfname);
    }
    @Override
    public int hashCode()
    {
        return hashCode;
    }
}
