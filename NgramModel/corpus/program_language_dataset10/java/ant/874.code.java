package org.apache.tools.zip;
public interface UnixStat {
    int PERM_MASK =           07777;
    int LINK_FLAG =         0120000;
    int FILE_FLAG =         0100000;
    int DIR_FLAG =           040000;
    int DEFAULT_LINK_PERM =    0777;
    int DEFAULT_DIR_PERM =     0755;
    int DEFAULT_FILE_PERM =    0644;
}