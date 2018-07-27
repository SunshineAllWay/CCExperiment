package org.apache.tools.ant.util;
import java.util.Iterator;
public class FirstMatchMapper extends ContainerMapper {
    public String[] mapFileName(String sourceFileName) {
        for (Iterator iter = getMappers().iterator(); iter.hasNext();) {
            FileNameMapper mapper = (FileNameMapper) iter.next();
            if (mapper != null) {
                String[] mapped = mapper.mapFileName(sourceFileName);
                if (mapped != null) {
                    return mapped;
                }
            }
        }
        return null;
    }
}
