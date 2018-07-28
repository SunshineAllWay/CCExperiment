package org.apache.maven.classrealm;
import java.util.List;
import java.util.Map;
public interface ClassRealmRequest
{
    enum RealmType
    {
        Core,
        Project,
        Extension,
        Plugin,
    }
    RealmType getType();
    ClassLoader getParent();
    @Deprecated
    List<String> getImports();
    List<String> getParentImports();
    Map<String, ClassLoader> getForeignImports();
    List<ClassRealmConstituent> getConstituents();
}
