package org.apache.cassandra.io.util;
import java.io.*;
public abstract class AbstractDataInput extends InputStream implements DataInput
{
    protected abstract void seekInternal(int position);
    protected abstract int getPosition();
    public final boolean readBoolean() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp != 0;
    }
    public final byte readByte() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return (byte) temp;
    }
    public final char readChar() throws IOException {
        byte[] buffer = new byte[2];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return (char) (((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff));
    }
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    public void readFully(byte[] buffer) throws IOException
    {
        readFully(buffer, 0, buffer.length);
    }
    public void readFully(byte[] buffer, int offset, int count) throws IOException
    {
        if (buffer == null) {
            throw new NullPointerException();
        }
        if (offset < 0 || offset > buffer.length || count < 0
                || count > buffer.length - offset) {
            throw new IndexOutOfBoundsException();
        }
        while (count > 0) {
            int result = read(buffer, offset, count);
            if (result < 0) {
                throw new EOFException();
            }
            offset += result;
            count -= result;
        }
    }
    public final int readInt() throws IOException {
        byte[] buffer = new byte[4];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return ((buffer[0] & 0xff) << 24) + ((buffer[1] & 0xff) << 16)
                + ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff);
    }
    public final String readLine() throws IOException {
        StringBuilder line = new StringBuilder(80); 
        boolean foundTerminator = false;
        int unreadPosition = 0;
        while (true) {
            int nextByte = read();
            switch (nextByte) {
                case -1:
                    return line.length() != 0 ? line.toString() : null;
                case (byte) '\r':
                    if (foundTerminator) {
                        seekInternal(unreadPosition);
                        return line.toString();
                    }
                    foundTerminator = true;
                    unreadPosition = getPosition();
                    break;
                case (byte) '\n':
                    return line.toString();
                default:
                    if (foundTerminator) {
                        seekInternal(unreadPosition);
                        return line.toString();
                    }
                    line.append((char) nextByte);
            }
        }
    }
    public final long readLong() throws IOException {
        byte[] buffer = new byte[8];
        int n = read(buffer, 0, buffer.length);
        if (n != buffer.length) {
            throw new EOFException("expected 8 bytes; read " + n + " at final position " + getPosition());
        }
        return ((long) (((buffer[0] & 0xff) << 24) + ((buffer[1] & 0xff) << 16)
                + ((buffer[2] & 0xff) << 8) + (buffer[3] & 0xff)) << 32)
                + ((long) (buffer[4] & 0xff) << 24)
                + ((buffer[5] & 0xff) << 16)
                + ((buffer[6] & 0xff) << 8)
                + (buffer[7] & 0xff);
    }
    public final short readShort() throws IOException {
        byte[] buffer = new byte[2];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return (short) (((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff));
    }
    public final int readUnsignedByte() throws IOException {
        int temp = this.read();
        if (temp < 0) {
            throw new EOFException();
        }
        return temp;
    }
    public final int readUnsignedShort() throws IOException {
        byte[] buffer = new byte[2];
        if (read(buffer, 0, buffer.length) != buffer.length) {
            throw new EOFException();
        }
        return ((buffer[0] & 0xff) << 8) + (buffer[1] & 0xff);
    }
    public final String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }
}
