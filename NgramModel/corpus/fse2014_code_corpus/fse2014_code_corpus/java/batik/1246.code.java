package org.apache.batik.svggen.font.table;
import java.io.IOException;
import java.io.RandomAccessFile;
public class GsubTable implements Table, LookupSubtableFactory {
    private ScriptList scriptList;
    private FeatureList featureList;
    private LookupList lookupList;
    protected GsubTable(DirectoryEntry de, RandomAccessFile raf) throws IOException {
        raf.seek(de.getOffset());
             raf.readInt();
        int scriptListOffset  = raf.readUnsignedShort();
        int featureListOffset = raf.readUnsignedShort();
        int lookupListOffset  = raf.readUnsignedShort();
        scriptList = new ScriptList(raf, de.getOffset() + scriptListOffset);
        featureList = new FeatureList(raf, de.getOffset() + featureListOffset);
        lookupList = new LookupList(raf, de.getOffset() + lookupListOffset, this);
    }
    public LookupSubtable read(int type, RandomAccessFile raf, int offset)
    throws IOException {
        LookupSubtable s = null;
        switch (type) {
        case 1:
            s = SingleSubst.read(raf, offset);
            break;
        case 2:
            break;
        case 3:
            break;
        case 4:
            s = LigatureSubst.read(raf, offset);
            break;
        case 5:
            break;
        case 6:
            break;
        }
        return s;
    }
    public int getType() {
        return GSUB;
    }
    public ScriptList getScriptList() {
        return scriptList;
    }
    public FeatureList getFeatureList() {
        return featureList;
    }
    public LookupList getLookupList() {
        return lookupList;
    }
    public String toString() {
        return "GSUB";
    }
}
