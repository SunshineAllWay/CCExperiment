package org.apache.tools.ant.types.resources.selectors;
import org.apache.tools.ant.types.Resource;
public interface ResourceSelector {
    boolean isSelected(Resource r);
}
