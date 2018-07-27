package org.apache.maven.classrealm;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.sonatype.aether.artifact.Artifact;
public interface ClassRealmManager
{
    ClassRealm getCoreRealm();
    ClassRealm getMavenApiRealm();
    ClassRealm createProjectRealm( Model model, List<Artifact> artifacts );
    ClassRealm createExtensionRealm( Plugin extension, List<Artifact> artifacts );
    ClassRealm createPluginRealm( Plugin plugin, ClassLoader parent, List<String> parentImports,
                                  Map<String, ClassLoader> foreignImports, List<Artifact> artifacts );
}
