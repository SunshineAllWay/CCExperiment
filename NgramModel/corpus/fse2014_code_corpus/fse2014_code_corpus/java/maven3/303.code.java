package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;
public interface ResolutionListener
{
    String ROLE = ResolutionListener.class.getName();
    int TEST_ARTIFACT = 1;
    int PROCESS_CHILDREN = 2;
    int FINISH_PROCESSING_CHILDREN = 3;
    int INCLUDE_ARTIFACT = 4;
    int OMIT_FOR_NEARER = 5;
    int UPDATE_SCOPE = 6;
    @Deprecated
    int MANAGE_ARTIFACT = 7;
    int OMIT_FOR_CYCLE = 8;
    int UPDATE_SCOPE_CURRENT_POM = 9;
    int SELECT_VERSION_FROM_RANGE = 10;
    int RESTRICT_RANGE = 11;
    int MANAGE_ARTIFACT_VERSION = 12;
    int MANAGE_ARTIFACT_SCOPE = 13;
    int MANAGE_ARTIFACT_SYSTEM_PATH = 14;
    void testArtifact( Artifact node );
    void startProcessChildren( Artifact artifact );
    void endProcessChildren( Artifact artifact );
    void includeArtifact( Artifact artifact );
    void omitForNearer( Artifact omitted,
                        Artifact kept );
    void updateScope( Artifact artifact,
                      String scope );
    @Deprecated
    void manageArtifact( Artifact artifact,
                         Artifact replacement );
    void omitForCycle( Artifact artifact );
    void updateScopeCurrentPom( Artifact artifact,
                                String ignoredScope );
    void selectVersionFromRange( Artifact artifact );
    void restrictRange( Artifact artifact,
                        Artifact replacement,
                        VersionRange newRange );
}
