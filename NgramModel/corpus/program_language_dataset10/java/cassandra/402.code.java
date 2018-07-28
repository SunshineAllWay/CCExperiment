package org.apache.cassandra.tools;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.commons.cli.*;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.ExpiringColumn;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.io.sstable.*;
import static org.apache.cassandra.utils.ByteBufferUtil.bytesToHex;
import static org.apache.cassandra.utils.ByteBufferUtil.hexToBytes;
public class SSTableExport
{
    private static int INPUT_FILE_BUFFER_SIZE = 8 * 1024 * 1024;
    private static final String KEY_OPTION = "k";
    private static final String EXCLUDEKEY_OPTION = "x";
    private static final String ENUMERATEKEYS_OPTION = "e";
    private static Options options;
    private static CommandLine cmd;
    static
    {
        options = new Options();
        Option optKey = new Option(KEY_OPTION, true, "Row key");
        optKey.setArgs(500);
        options.addOption(optKey);
        Option excludeKey = new Option(EXCLUDEKEY_OPTION, true, "Excluded row key");
        excludeKey.setArgs(500);
        options.addOption(excludeKey);
        Option optEnumerate = new Option(ENUMERATEKEYS_OPTION, false, "enumerate keys only");
        options.addOption(optEnumerate);
    }
    private static String quote(String val)
    {
        return String.format("\"%s\"", val);
    }
    private static String asKey(String val)
    {
        return String.format("%s: ", quote(val));
    }
    private static void serializeColumns(PrintStream outs, Collection<IColumn> cols, AbstractType comp)
    {
        outs.print("[");
        Iterator<IColumn> iter = cols.iterator();
        while (iter.hasNext())
        {
            outs.print("[");
            IColumn column = iter.next();
            outs.print(quote(bytesToHex(column.name())));
            outs.print(", ");
            outs.print(quote(bytesToHex(column.value())));
            outs.print(", ");
            outs.print(column.timestamp());
            outs.print(", ");
            outs.print(column.isMarkedForDelete());
            if (column instanceof ExpiringColumn)
            {
                outs.print(", ");
                outs.print(((ExpiringColumn) column).getTimeToLive());
                outs.print(", ");
                outs.print(column.getLocalDeletionTime());
            }
            outs.print("]");
            if (iter.hasNext())
                outs.print(", ");
        }
        outs.print("]");
    }
    private static void serializeRow(PrintStream outs, SSTableIdentityIterator row) throws IOException
    {
        ColumnFamily cf = row.getColumnFamilyWithColumns();
        AbstractType comparator = cf.getComparator();
        outs.print(asKey(bytesToHex(row.getKey().key)));
        if (cf.isSuper())
        {
            outs.print("{ ");
            Iterator<IColumn> iter = cf.getSortedColumns().iterator();
            while (iter.hasNext())
            {
                IColumn column = iter.next();
                outs.print(asKey(bytesToHex(column.name())));
                outs.print("{");
                outs.print(asKey("deletedAt"));
                outs.print(column.getMarkedForDeleteAt());
                outs.print(", ");
                outs.print(asKey("subColumns"));
                serializeColumns(outs, column.getSubColumns(), comparator);
                outs.print("}");
                if (iter.hasNext())
                    outs.print(", ");
            }
            outs.print("}");
        }
        else
        {
            serializeColumns(outs, cf.getSortedColumns(), comparator);
        }
    }
    public static void enumeratekeys(String ssTableFile, PrintStream outs)
    throws IOException
    {
        Descriptor desc = Descriptor.fromFilename(ssTableFile);
        KeyIterator iter = new KeyIterator(desc);
        DecoratedKey lastKey = null;
        while (iter.hasNext())
        {
            DecoratedKey key = iter.next();
            if (lastKey != null && lastKey.compareTo(key) > 0 )
                throw new IOException("Key out of order! " + lastKey + " > " + key);
            lastKey = key;
            outs.println(bytesToHex(key.key));
        }
        iter.close();
        outs.flush();
    }
    public static void export(String ssTableFile, PrintStream outs, String[] keys, String[] excludes)
    throws IOException
    {
        SSTableReader reader = SSTableReader.open(Descriptor.fromFilename(ssTableFile));
        SSTableScanner scanner = reader.getDirectScanner(INPUT_FILE_BUFFER_SIZE);
        IPartitioner<?> partitioner = DatabaseDescriptor.getPartitioner();    
        Set<String> excludeSet = new HashSet<String>();
        int i = 0;
        if (excludes != null)
            excludeSet = new HashSet<String>(Arrays.asList(excludes));
        outs.println("{");
        DecoratedKey lastKey = null;
        for (String key : keys)
        {
            if (excludeSet.contains(key))
                continue;
            DecoratedKey<?> dk = partitioner.decorateKey(hexToBytes(key));
            if (lastKey != null && lastKey.compareTo(dk) > 0 )
                throw new IOException("Key out of order! " + lastKey + " > " + dk);
            lastKey = dk;
            scanner.seekTo(dk);
            i++;
            if (scanner.hasNext())
            {
                SSTableIdentityIterator row = (SSTableIdentityIterator) scanner.next();
                try
                {
                    serializeRow(outs, row);
                    if (i != 1)
                        outs.println(",");
                }
                catch (IOException ioexc)
                {
                    System.err.println("WARNING: Corrupt row " + key + " (skipping).");
                    continue;
                }
                catch (OutOfMemoryError oom)
                {
                    System.err.println("ERROR: Out of memory deserializing row " + key);
                    continue;
                }
            }
        }
        outs.println("\n}");
        outs.flush();
    }
    static void export(SSTableReader reader, PrintStream outs, String[] excludes) throws IOException
    {
        SSTableScanner scanner = reader.getDirectScanner(INPUT_FILE_BUFFER_SIZE);
        Set<String> excludeSet = new HashSet<String>();
        if (excludes != null)
            excludeSet = new HashSet<String>(Arrays.asList(excludes));
        outs.println("{");
        SSTableIdentityIterator row;
        boolean elementWritten = false;
        while (scanner.hasNext())
        {
            row = (SSTableIdentityIterator) scanner.next();
            if (excludeSet.contains(bytesToHex(row.getKey().key)))
                continue;
            else if (elementWritten)
                outs.println(",");
            try
            {
                serializeRow(outs, row);
                if (!elementWritten)
                    elementWritten = true;
            }
            catch (IOException ioexcep)
            {
                System.err.println("WARNING: Corrupt row " + bytesToHex(row.getKey().key) + " (skipping).");
                continue;
            }
            catch (OutOfMemoryError oom)
            {
                System.err.println("ERROR: Out of memory deserializing row " + bytesToHex(row.getKey().key));
                continue;
            }
        }
        outs.printf("%n}%n");
        outs.flush();
    }
    public static void export(String ssTableFile, PrintStream outs, String[] excludes) throws IOException
    {
        SSTableReader reader = SSTableReader.open(Descriptor.fromFilename(ssTableFile));
        export(reader, outs, excludes);
    }
    public static void export(String ssTableFile, String[] excludes) throws IOException
    {
        export(ssTableFile, System.out, excludes);
    }
    public static void main(String[] args) throws IOException, ConfigurationException
    {
        String usage = String.format("Usage: %s <sstable> [-k key [-k key [...]] -x key [-x key [...]]]%n", SSTableExport.class.getName());
        CommandLineParser parser = new PosixParser();
        try
        {
            cmd = parser.parse(options, args);
        }
        catch (ParseException e1)
        {
            System.err.println(e1.getMessage());
            System.err.println(usage);
            System.exit(1);
        }
        if (cmd.getArgs().length != 1)
        {
            System.err.println("You must supply exactly one sstable");
            System.err.println(usage);
            System.exit(1);
        }
        String[] keys = cmd.getOptionValues(KEY_OPTION);
        String[] excludes = cmd.getOptionValues(EXCLUDEKEY_OPTION);
        String ssTableFileName = new File(cmd.getArgs()[0]).getAbsolutePath();
        DatabaseDescriptor.loadSchemas();
        if (DatabaseDescriptor.getNonSystemTables().size() < 1)
        {
            String msg = "no non-system tables are defined";
            System.err.println(msg);
            throw new ConfigurationException(msg);
        }
        if (cmd.hasOption(ENUMERATEKEYS_OPTION))
        {
            enumeratekeys(ssTableFileName, System.out);
        }
        else
        {
            if ((keys != null) && (keys.length > 0))
                export(ssTableFileName, System.out, keys, excludes);
            else
                export(ssTableFileName, excludes);
        }
        System.exit(0);
    }
}
