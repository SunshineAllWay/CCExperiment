package org.apache.tools.ant.types;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.filters.ClassConstants;
import org.apache.tools.ant.filters.EscapeUnicode;
import org.apache.tools.ant.filters.ExpandProperties;
import org.apache.tools.ant.filters.HeadFilter;
import org.apache.tools.ant.filters.LineContains;
import org.apache.tools.ant.filters.LineContainsRegExp;
import org.apache.tools.ant.filters.PrefixLines;
import org.apache.tools.ant.filters.ReplaceTokens;
import org.apache.tools.ant.filters.StripJavaComments;
import org.apache.tools.ant.filters.StripLineBreaks;
import org.apache.tools.ant.filters.StripLineComments;
import org.apache.tools.ant.filters.SuffixLines;
import org.apache.tools.ant.filters.TabsToSpaces;
import org.apache.tools.ant.filters.TailFilter;
import org.apache.tools.ant.filters.TokenFilter;
public class FilterChain extends DataType
    implements Cloneable {
    private Vector filterReaders = new Vector();
    public void addFilterReader(final AntFilterReader filterReader) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(filterReader);
    }
    public Vector getFilterReaders() {
        if (isReference()) {
            return ((FilterChain) getCheckedRef()).getFilterReaders();
        }
        dieOnCircularReference();
        return filterReaders;
    }
    public void addClassConstants(final ClassConstants classConstants) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(classConstants);
    }
    public void addExpandProperties(final ExpandProperties expandProperties) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(expandProperties);
    }
    public void addHeadFilter(final HeadFilter headFilter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(headFilter);
    }
    public void addLineContains(final LineContains lineContains) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(lineContains);
    }
    public void addLineContainsRegExp(final LineContainsRegExp
                                                lineContainsRegExp) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(lineContainsRegExp);
    }
    public void addPrefixLines(final PrefixLines prefixLines) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(prefixLines);
    }
    public void addSuffixLines(final SuffixLines suffixLines) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(suffixLines);
    }
    public void addReplaceTokens(final ReplaceTokens replaceTokens) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(replaceTokens);
    }
    public void addStripJavaComments(final StripJavaComments
                                                stripJavaComments) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(stripJavaComments);
    }
    public void addStripLineBreaks(final StripLineBreaks
                                                stripLineBreaks) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(stripLineBreaks);
    }
    public void addStripLineComments(final StripLineComments
                                                stripLineComments) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(stripLineComments);
    }
    public void addTabsToSpaces(final TabsToSpaces tabsToSpaces) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(tabsToSpaces);
    }
    public void addTailFilter(final TailFilter tailFilter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(tailFilter);
    }
    public void addEscapeUnicode(final EscapeUnicode escapeUnicode) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(escapeUnicode);
    }
    public void addTokenFilter(final TokenFilter tokenFilter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(tokenFilter);
    }
    public void addDeleteCharacters(TokenFilter.DeleteCharacters filter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(filter);
    }
    public void addContainsRegex(TokenFilter.ContainsRegex filter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(filter);
    }
    public void addReplaceRegex(TokenFilter.ReplaceRegex filter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(filter);
    }
    public void addTrim(TokenFilter.Trim filter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(filter);
    }
    public void addReplaceString(
        TokenFilter.ReplaceString filter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(filter);
    }
    public void addIgnoreBlank(
        TokenFilter.IgnoreBlank filter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(filter);
    }
    public void setRefid(Reference r) throws BuildException {
        if (!filterReaders.isEmpty()) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    public void add(ChainableReader filter) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        setChecked(false);
        filterReaders.addElement(filter);
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            for (Iterator i = filterReaders.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (o instanceof DataType) {
                    pushAndInvokeCircularReferenceCheck((DataType) o, stk, p);
                }
            }
            setChecked(true);
        }
    }
}
