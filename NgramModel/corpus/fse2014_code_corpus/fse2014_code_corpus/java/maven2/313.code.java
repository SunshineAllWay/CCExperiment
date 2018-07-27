package org.apache.maven.project.path;
import org.apache.maven.model.Model;
import java.io.File;
public interface PathTranslator
{
    String ROLE = PathTranslator.class.getName();
    void alignToBaseDirectory( Model model, File basedir );
    String alignToBaseDirectory( String path, File basedir );
    void unalignFromBaseDirectory( Model model, File basedir );
    String unalignFromBaseDirectory( String directory, File basedir );
}
