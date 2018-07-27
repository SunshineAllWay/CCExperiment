package org.apache.xerces.impl.xs.util;
import java.util.ArrayList;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.XSModelImpl;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xs.XSModel;
public class XSGrammarPool extends XMLGrammarPoolImpl {
    public XSModel toXSModel() {
    	return toXSModel(Constants.SCHEMA_VERSION_1_0);
    }
    public XSModel toXSModel(short schemaVersion) {
        ArrayList list = new ArrayList();
        for (int i = 0; i < fGrammars.length; i++) {
            for (Entry entry = fGrammars[i] ; entry != null ; entry = entry.next) {
                if (entry.desc.getGrammarType().equals(XMLGrammarDescription.XML_SCHEMA)) {
                    list.add(entry.grammar);
                }
            }
        }
        int size = list.size();
        if (size == 0) {
            return toXSModel(new SchemaGrammar[0], schemaVersion);
        }
        SchemaGrammar[] gs = (SchemaGrammar[])list.toArray(new SchemaGrammar[size]);
        return toXSModel(gs, schemaVersion);
    }
    protected XSModel toXSModel(SchemaGrammar[] grammars, short schemaVersion) {
        return new XSModelImpl(grammars, schemaVersion);
    }
} 
