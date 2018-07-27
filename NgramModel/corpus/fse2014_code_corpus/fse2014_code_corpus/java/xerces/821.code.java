package org.apache.xerces.util;
import java.io.OutputStream;
import java.io.PrintWriter;
public class ArrayFillingCodeGenerator {
    public static void generateByteArray(String arrayName, 
                                         byte[] array, 
                                         OutputStream out) {
        PrintWriter writer = new PrintWriter(out);
        int cursor = 0;
        int i;
        byte last = 0;
        for (i = 0; i < array.length; ++i) {
            if (last == array[i]) {
                continue;
            }
            if (i - cursor > 1 && last != (byte) 0) {
                writer.print("Arrays.fill(" + arrayName + ", " + cursor + ", " + i + ", (byte) " + last + " );");
                writer.println(" // Fill " + (i - cursor) + " of value (byte) " + last);
                writer.flush();
            }
            else if (i - cursor == 1 && array[cursor] != (byte) 0) {
                writer.println(arrayName + "[" + cursor + "] = " + array[cursor] + ";");
                writer.flush();
            }
            last = array[i];
            cursor = i;
        }
        if (i - cursor > 1 && last != (byte) 0) {
            writer.print("Arrays.fill(" + arrayName + ", " + cursor + ", " + i + ", (byte) " + last + " );");
            writer.println(" // Fill " + (i - cursor) + " of value (byte) " + last);
            writer.flush();
        }
        else if (i - cursor == 1 && array[cursor] != (byte) 0) {
            writer.println(arrayName + "[" + cursor + "] = " + array[cursor] + ";");
            writer.flush();
        }
        writer.flush();
        writer.close();
    }
}
