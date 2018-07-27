package org.apache.maven.project.inheritance;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
@Deprecated
public interface ModelInheritanceAssembler
{
    String ROLE = ModelInheritanceAssembler.class.getName();
    void assembleModelInheritance( Model child, Model parent, String childPathAdjustment );
    void assembleModelInheritance( Model child, Model parent );
    void assembleBuildInheritance( Build childBuild, Build parentBuild, boolean handleAsInheriance );
    void copyModel( Model dest, Model source );
}
