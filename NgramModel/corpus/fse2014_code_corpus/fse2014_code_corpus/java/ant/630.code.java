package org.apache.tools.ant.types.mappers;
import java.io.StringReader;
import java.io.Reader;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.UnsupportedAttributeException;
import org.apache.tools.ant.filters.util.ChainReaderHelper;
import org.apache.tools.ant.types.FilterChain;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
public class FilterMapper extends FilterChain implements FileNameMapper {
    private static final int BUFFER_SIZE = 8192;
    public void setFrom(String from) {
        throw new UnsupportedAttributeException(
            "filtermapper doesn't support the \"from\" attribute.", "from");
    }
    public void setTo(String to) {
        throw new UnsupportedAttributeException(
            "filtermapper doesn't support the \"to\" attribute.", "to");
    }
    public String[] mapFileName(String sourceFileName) {
        try {
            Reader stringReader = new StringReader(sourceFileName);
            ChainReaderHelper helper = new ChainReaderHelper();
            helper.setBufferSize(BUFFER_SIZE);
            helper.setPrimaryReader(stringReader);
            helper.setProject(getProject());
            Vector filterChains = new Vector();
            filterChains.add(this);
            helper.setFilterChains(filterChains);
            String result = FileUtils.safeReadFully(helper.getAssembledReader());
            if (result.length() == 0) {
                return null;
            } else {
                return new String[] {result};
            }
        } catch (BuildException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
    }
}
