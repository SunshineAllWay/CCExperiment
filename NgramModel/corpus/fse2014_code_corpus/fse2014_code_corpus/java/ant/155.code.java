package org.apache.tools.ant.property;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
public class ResolvePropertyMap implements GetProperty {
    private final Set seen = new HashSet();
    private final ParseProperties parseProperties;
    private final GetProperty master;
    private Map map;
    private String prefix;
    private boolean prefixValues = false;
    private boolean expandingLHS = true;
    public ResolvePropertyMap(Project project, GetProperty master, Collection expanders) {
        this.master = master;
        this.parseProperties = new ParseProperties(project, expanders, this);
    }
    public Object getProperty(String name) {
        if (seen.contains(name)) {
            throw new BuildException(
                "Property " + name + " was circularly " + "defined.");
        }
        try {
            String fullKey = name;
            if (prefix != null && (expandingLHS || prefixValues)) {
                fullKey = prefix + name;
            }
            Object masterValue = master.getProperty(fullKey);
            if (masterValue != null) {
                return masterValue;
            }
            seen.add(name);
            expandingLHS = false;
            return parseProperties.parseProperties((String) map.get(name));
        } finally {
            seen.remove(name);
        }
    }
    public void resolveAllProperties(Map map) {
        resolveAllProperties(map, null, false);
    }
    public void resolveAllProperties(Map map, String prefix) {
        resolveAllProperties(map, null, false);
    }
    public void resolveAllProperties(Map map, String prefix,
                                     boolean prefixValues) {
        this.map = map;
        this.prefix = prefix;
        this.prefixValues = prefixValues;
        for (Iterator i = map.keySet().iterator(); i.hasNext();) {
            expandingLHS = true;
            String key = (String) i.next();
            Object result = getProperty(key);
            String value = result == null ? "" : result.toString();
            map.put(key, value);
        }
    }
}
