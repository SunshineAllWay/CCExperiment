package org.apache.maven.classrealm;
import java.util.List;
import java.util.Map;
class DefaultClassRealmRequest
    implements ClassRealmRequest
{
    private final RealmType type;
    private final ClassLoader parent;
    private final List<String> parentImports;
    private final Map<String, ClassLoader> foreignImports;
    private final List<ClassRealmConstituent> constituents;
    public DefaultClassRealmRequest( RealmType type, ClassLoader parent, List<String> parentImports,
                                     Map<String, ClassLoader> foreignImports, List<ClassRealmConstituent> constituents )
    {
        this.type = type;
        this.parent = parent;
        this.parentImports = parentImports;
        this.foreignImports = foreignImports;
        this.constituents = constituents;
    }
    public RealmType getType()
    {
        return type;
    }
    public ClassLoader getParent()
    {
        return parent;
    }
    public List<String> getImports()
    {
        return getParentImports();
    }
    public List<String> getParentImports()
    {
        return parentImports;
    }
    public Map<String, ClassLoader> getForeignImports()
    {
        return foreignImports;
    }
    public List<ClassRealmConstituent> getConstituents()
    {
        return constituents;
    }
}
