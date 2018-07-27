package org.apache.maven.plugin.descriptor;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.plugin.lifecycle.Lifecycle;
import org.apache.maven.plugin.lifecycle.LifecycleConfiguration;
import org.apache.maven.plugin.lifecycle.io.xpp3.LifecycleMappingsXpp3Reader;
import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class PluginDescriptor
    extends ComponentSetDescriptor
{
    private String groupId;
    private String artifactId;
    private String version;
    private String goalPrefix;
    private String source;
    private boolean inheritedByDefault = true;
    private Artifact pluginArtifact;
    private List artifacts;
    private Map lifecycleMappings;
    private ClassRealm classRealm;
    private Map artifactMap;
    private Set introducedDependencyArtifacts;
    private String name;
    private String description;
    public List getMojos()
    {
        return getComponents();
    }
    public void addMojo( MojoDescriptor mojoDescriptor )
        throws DuplicateMojoDescriptorException
    {
        MojoDescriptor existing = null;
        List mojos = getComponents();
        if ( mojos != null && mojos.contains( mojoDescriptor ) )
        {
            int indexOf = mojos.indexOf( mojoDescriptor );
            existing = (MojoDescriptor) mojos.get( indexOf );
        }
        if ( existing != null )
        {
            throw new DuplicateMojoDescriptorException( getGoalPrefix(), mojoDescriptor.getGoal(), existing
                .getImplementation(), mojoDescriptor.getImplementation() );
        }
        else
        {
            addComponentDescriptor( mojoDescriptor );
        }
    }
    public String getGroupId()
    {
        return groupId;
    }
    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }
    public String getArtifactId()
    {
        return artifactId;
    }
    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }
    public static String constructPluginKey( String groupId, String artifactId, String version )
    {
        return groupId + ":" + artifactId + ":" + version;
    }
    private String lookupKey;
    public String getPluginLookupKey()
    {
        if ( lookupKey == null )
        {
            lookupKey = ( groupId + ":" + artifactId ).intern();
        }
        return lookupKey;
    }
    private String id;
    public String getId()
    {
        if ( id == null )
        {
            id = constructPluginKey( groupId, artifactId, version ).intern();
        }
        return id;
    }
    public static String getDefaultPluginArtifactId( String id )
    {
        return "maven-" + id + "-plugin";
    }
    public static String getDefaultPluginGroupId()
    {
        return "org.apache.maven.plugins";
    }
    public static String getGoalPrefixFromArtifactId( String artifactId )
    {
        if ( "maven-plugin-plugin".equals( artifactId ) )
        {
            return "plugin";
        }
        else
        {
            return artifactId.replaceAll( "-?maven-?", "" ).replaceAll( "-?plugin-?", "" );
        }
    }
    public String getGoalPrefix()
    {
        return goalPrefix;
    }
    public void setGoalPrefix( String goalPrefix )
    {
        this.goalPrefix = goalPrefix;
    }
    public void setVersion( String version )
    {
        this.version = version;
    }
    public String getVersion()
    {
        return version;
    }
    public void setSource( String source )
    {
        this.source = source;
    }
    public String getSource()
    {
        return source;
    }
    public boolean isInheritedByDefault()
    {
        return inheritedByDefault;
    }
    public void setInheritedByDefault( boolean inheritedByDefault )
    {
        this.inheritedByDefault = inheritedByDefault;
    }
    public List getArtifacts()
    {
        return artifacts;
    }
    public void setArtifacts( List artifacts )
    {
        this.artifacts = artifacts;
        artifactMap = null;
    }
    public Map getArtifactMap()
    {
        if ( artifactMap == null )
        {
            artifactMap = ArtifactUtils.artifactMapByVersionlessId( getArtifacts() );
        }
        return artifactMap;
    }
    public boolean equals( Object object )
    {
        if ( this == object )
        {
            return true;
        }
        return getId().equals( ( (PluginDescriptor) object ).getId() );
    }
    public int hashCode()
    {
        return 10 + getId().hashCode();
    }
    public MojoDescriptor getMojo( String goal )
    {
        if ( getMojos() == null )
        {
            return null; 
        }
        MojoDescriptor mojoDescriptor = null;
        for ( Iterator i = getMojos().iterator(); i.hasNext() && mojoDescriptor == null; )
        {
            MojoDescriptor desc = (MojoDescriptor) i.next();
            if ( goal.equals( desc.getGoal() ) )
            {
                mojoDescriptor = desc;
            }
        }
        return mojoDescriptor;
    }
    public Lifecycle getLifecycleMapping( String lifecycle )
        throws IOException, XmlPullParserException
    {
        if ( lifecycleMappings == null )
        {
            LifecycleMappingsXpp3Reader reader = new LifecycleMappingsXpp3Reader();
            InputStreamReader r = null;
            LifecycleConfiguration config;
            try
            {
                InputStream resourceAsStream = classRealm.getResourceAsStream( "/META-INF/maven/lifecycle.xml" );
                if ( resourceAsStream == null )
                {
                    throw new FileNotFoundException( "Unable to find /META-INF/maven/lifecycle.xml in the plugin" );
                }
                r = new InputStreamReader( resourceAsStream );
                config = reader.read( r, true );
            }
            finally
            {
                IOUtil.close( r );
            }
            Map map = new HashMap();
            for ( Iterator i = config.getLifecycles().iterator(); i.hasNext(); )
            {
                Lifecycle l = (Lifecycle) i.next();
                map.put( l.getId(), l );
            }
            lifecycleMappings = map;
        }
        return (Lifecycle) lifecycleMappings.get( lifecycle );
    }
    public void setClassRealm( ClassRealm classRealm )
    {
        this.classRealm = classRealm;
    }
    public ClassRealm getClassRealm()
    {
        return classRealm;
    }
    public void setIntroducedDependencyArtifacts( Set introducedDependencyArtifacts )
    {
        this.introducedDependencyArtifacts = introducedDependencyArtifacts;
    }
    public Set getIntroducedDependencyArtifacts()
    {
        return introducedDependencyArtifacts != null ? introducedDependencyArtifacts : Collections.EMPTY_SET;
    }
    public void setName( String name )
    {
        this.name = name;
    }
    public String getName()
    {
        return name;
    }
    public void setDescription( String description )
    {
        this.description = description;
    }
    public String getDescription()
    {
        return description;
    }
    public Artifact getPluginArtifact()
    {
        return pluginArtifact;
    }
    public void setPluginArtifact( Artifact pluginArtifact )
    {
        this.pluginArtifact = pluginArtifact;
    }
}
