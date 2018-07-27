package org.apache.cassandra.hadoop.streaming;
import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.cassandra.hadoop.avro.Mutation;
import org.apache.cassandra.hadoop.avro.StreamingMutation;
import org.apache.hadoop.streaming.PipeMapRed;
import org.apache.hadoop.streaming.io.OutputReader;
public class AvroOutputReader extends OutputReader<ByteBuffer, List<Mutation>>
{
    private BinaryDecoder decoder;
    private SpecificDatumReader<StreamingMutation> reader;
    private final StreamingMutation entry = new StreamingMutation();
    private final ArrayList<Mutation> mutations = new ArrayList<Mutation>(1);
    @Override
    public void initialize(PipeMapRed pmr) throws IOException
    {
        super.initialize(pmr);
        InputStream in;
        if (pmr.getClientInput() instanceof InputStream)
            in = (InputStream)pmr.getClientInput();
        else
            in = new FromDataInputStream(pmr.getClientInput());
        decoder = DecoderFactory.defaultFactory().createBinaryDecoder(in, null);
        reader = new SpecificDatumReader<StreamingMutation>(StreamingMutation.SCHEMA$);
    }
    @Override
    public boolean readKeyValue() throws IOException
    {
        try
        {
            reader.read(entry, decoder);
        }
        catch (EOFException e)
        {
            return false;
        }
        mutations.clear();
        mutations.add(entry.mutation);
        return true;
    }
    @Override
    public ByteBuffer getCurrentKey() throws IOException
    {
        return entry.key;
    }
    @Override
    public List<Mutation> getCurrentValue() throws IOException
    {
        return mutations;
    }
    @Override
    public String getLastOutput()
    {
        return entry.toString();
    }
    private static final class FromDataInputStream extends InputStream
    {
        private final DataInput in;
        public FromDataInputStream(DataInput in)
        {
            this.in = in;
        }
        @Override
        public boolean markSupported()
        {
            return false;
        }
        @Override
        public int read() throws IOException
        {
            try
            {
                return in.readUnsignedByte();
            }
            catch (EOFException e)
            {
                return -1;
            }
        }
        @Override
        public long skip(long n) throws IOException
        {
            long skipped = 0;
            while (n > 0)
            {
                int skip = (int)Math.min(Integer.MAX_VALUE, n);
                skipped += in.skipBytes(skip);
                n -= skip;
            }
            return skipped;
        }
    }
}
