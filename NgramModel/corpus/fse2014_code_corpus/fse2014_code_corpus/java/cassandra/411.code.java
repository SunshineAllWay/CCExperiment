package org.apache.cassandra.utils;
import java.io.Closeable;
import java.util.Iterator;
public interface CloseableIterator<T> extends Iterator<T>, Closeable
{
}
