package org.apache.maven.classrealm;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
public interface ClassRealmManagerDelegate
{
    void setupRealm( ClassRealm classRealm, ClassRealmRequest request );
}
