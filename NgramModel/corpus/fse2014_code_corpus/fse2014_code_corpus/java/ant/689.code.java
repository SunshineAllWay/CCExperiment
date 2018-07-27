package org.apache.tools.ant.types.resources;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.util.ConcatResourceInputStream;
import org.apache.tools.ant.util.LineTokenizer;
import org.apache.tools.ant.util.Tokenizer;
public class Tokens extends BaseResourceCollectionWrapper {
    private Tokenizer tokenizer;
    private String encoding;
    protected synchronized Collection getCollection() {
        ResourceCollection rc = getResourceCollection();
        if (rc.size() == 0) {
            return Collections.EMPTY_SET;
        }
        if (tokenizer == null) {
            tokenizer = new LineTokenizer();
        }
        ConcatResourceInputStream cat = new ConcatResourceInputStream(rc);
        cat.setManagingComponent(this);
        InputStreamReader rdr = null;
        if (encoding == null) {
            rdr = new InputStreamReader(cat);
        } else {
            try {
                rdr = new InputStreamReader(cat, encoding);
            } catch (UnsupportedEncodingException e) {
                throw new BuildException(e);
            }
        }
        ArrayList result = new ArrayList();
        try {
            for (String s = tokenizer.getToken(rdr); s != null; s = tokenizer.getToken(rdr)) {
                StringResource resource = new StringResource(s);
                resource.setProject(getProject());
                result.add(resource);
            }
        } catch (IOException e) {
            throw new BuildException("Error reading tokens", e);
        }
        return result;
    }
    public synchronized void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public synchronized void add(Tokenizer tokenizer) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (this.tokenizer != null) {
            throw new BuildException("Only one nested tokenizer allowed.");
        }
        this.tokenizer = tokenizer;
        setChecked(false);
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        super.dieOnCircularReference(stk, p);
        if (!isReference()) {
            if (tokenizer instanceof DataType) {
                pushAndInvokeCircularReferenceCheck((DataType) tokenizer, stk,
                                                    p);
            }
            setChecked(true);
        }
    }
}
