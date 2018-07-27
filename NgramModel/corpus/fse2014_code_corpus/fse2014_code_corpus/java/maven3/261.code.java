package org.apache.maven;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
public class DuplicateProjectException
    extends MavenExecutionException
{
    private Map<String, List<File>> collisions;
    public DuplicateProjectException( String message, Map<String, List<File>> collisions )
    {
        super( message, (File) null );
        this.collisions = ( collisions != null ) ? collisions : new LinkedHashMap<String, List<File>>();
    }
    public Map<String, List<File>> getCollisions()
    {
        return collisions;
    }
}
