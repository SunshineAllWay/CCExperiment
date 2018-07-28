package org.apache.tools.ant.util;
import java.util.Iterator;
import java.util.LinkedHashSet;
public class CompositeMapper extends ContainerMapper {
    public String[] mapFileName(String sourceFileName) {
        LinkedHashSet results = new LinkedHashSet();
        FileNameMapper mapper = null;
        for (Iterator mIter = getMappers().iterator(); mIter.hasNext();) {
            mapper = (FileNameMapper) (mIter.next());
            if (mapper != null) {
                String[] mapped = mapper.mapFileName(sourceFileName);
                if (mapped != null) {
                    for (int i = 0; i < mapped.length; i++) {
                        results.add(mapped[i]);
                    }
                }
            }
        }
        return (results.size() == 0) ? null
            : (String[]) results.toArray(new String[results.size()]);
    }
}
