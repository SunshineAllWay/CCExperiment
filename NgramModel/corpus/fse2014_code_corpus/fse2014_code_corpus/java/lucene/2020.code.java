package org.apache.lucene.store;
import java.io.IOException;
import org.apache.lucene.store.SimpleFSDirectory.SimpleFSIndexInput;
public class _TestHelper {
    public static boolean isSimpleFSIndexInput(IndexInput is) {
        return is instanceof SimpleFSIndexInput;
    }
    public static boolean isSimpleFSIndexInputClone(IndexInput is) {
        if (isSimpleFSIndexInput(is)) {
            return ((SimpleFSIndexInput) is).isClone;
        } else {
            return false;
        }
    }
    public static boolean isSimpleFSIndexInputOpen(IndexInput is)
    throws IOException
    {
        if (isSimpleFSIndexInput(is)) {
            SimpleFSIndexInput fis = (SimpleFSIndexInput) is;
            return fis.isFDValid();
        } else {
            return false;
        }
    }
}
