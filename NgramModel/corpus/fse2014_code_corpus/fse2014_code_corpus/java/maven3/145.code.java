package org.apache.maven.repository.legacy.resolver.conflict;
import org.codehaus.plexus.component.annotations.Component;
@Deprecated
@Component( role = ConflictResolver.class )
public class DefaultConflictResolver
    extends NearestConflictResolver
{
}
