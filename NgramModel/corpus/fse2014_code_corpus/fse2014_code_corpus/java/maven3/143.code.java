package org.apache.maven.repository.legacy.resolver.conflict;
public interface ConflictResolverFactory
{
    String ROLE = ConflictResolverFactory.class.getName();
    ConflictResolver getConflictResolver( String type )
        throws ConflictResolverNotFoundException;
}
