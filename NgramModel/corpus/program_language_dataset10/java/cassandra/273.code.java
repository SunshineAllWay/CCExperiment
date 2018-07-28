package org.apache.cassandra.io;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.ipc.ByteBufferInputStream;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.avro.util.Utf8;
import org.apache.cassandra.io.util.OutputBuffer;
import org.apache.cassandra.utils.ByteBufferUtil;
public final class SerDeUtils
{
    private final static DecoderFactory DIRECT_DECODERS = new DecoderFactory().configureDirectDecoder(true);
    public static byte[] copy(ByteBuffer buff)
    {
        byte[] bytes = new byte[buff.remaining()];
        buff.get(bytes);
        buff.rewind();
        return bytes;
    }
    public static <T extends SpecificRecord> T deserialize(Schema writer, ByteBuffer bytes, T ob) throws IOException
    {
        BinaryDecoder dec = DIRECT_DECODERS.createBinaryDecoder(ByteBufferUtil.getArray(bytes), null);
        SpecificDatumReader<T> reader = new SpecificDatumReader<T>(writer);
        reader.setExpected(ob.getSchema());
        return reader.read(ob, dec);
    }
    public static <T extends SpecificRecord> ByteBuffer serialize(T o) throws IOException
    {
        OutputBuffer buff = new OutputBuffer();
        BinaryEncoder enc = new BinaryEncoder(buff);
        SpecificDatumWriter<T> writer = new SpecificDatumWriter<T>(o.getSchema());
        writer.write(o, enc);
        enc.flush();
        return ByteBuffer.wrap(buff.asByteArray());
    }
    public static <T extends SpecificRecord> T deserializeWithSchema(ByteBuffer bytes, T ob) throws IOException
    {
        BinaryDecoder dec = DIRECT_DECODERS.createBinaryDecoder(ByteBufferUtil.getArray(bytes), null);
        Schema writer = Schema.parse(dec.readString(new Utf8()).toString());
        SpecificDatumReader<T> reader = new SpecificDatumReader<T>(writer);
        reader.setExpected(ob.getSchema());
        return reader.read(ob, dec);
    }
    public static <T extends SpecificRecord> ByteBuffer serializeWithSchema(T o) throws IOException
    {
        OutputBuffer buff = new OutputBuffer();
        BinaryEncoder enc = new BinaryEncoder(buff);
        enc.writeString(new Utf8(o.getSchema().toString()));
        SpecificDatumWriter<T> writer = new SpecificDatumWriter<T>(o.getSchema());
        writer.write(o, enc);
        enc.flush();
        return ByteBuffer.wrap(buff.asByteArray());
    }
    public static DataInputStream createDataInputStream(ByteBuffer buff)
    {
        ByteBufferInputStream bbis = new ByteBufferInputStream(Collections.singletonList(buff));
        return new DataInputStream(bbis);
    }
    public static <T> GenericArray<T> createArray(int size, Schema schema)
    {
        return new GenericData.Array<T>(size, Schema.createArray(schema));
    }
}
