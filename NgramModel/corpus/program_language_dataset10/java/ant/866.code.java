package org.apache.tools.zip;
import java.util.zip.ZipException;
public interface CentralDirectoryParsingZipExtraField extends ZipExtraField {
    void parseFromCentralDirectoryData(byte[] data, int offset, int length)
        throws ZipException;
}
