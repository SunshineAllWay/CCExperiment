package org.apache.maven.toolchain.java;
import java.io.File;
import org.apache.maven.toolchain.DefaultToolchain;
import org.apache.maven.toolchain.model.ToolchainModel;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
@Component( role = JavaToolChain.class )
public class DefaultJavaToolChain
    extends DefaultToolchain
    implements JavaToolChain
{
    private String javaHome;
    public static final String KEY_JAVAHOME = "jdkHome"; 
    public DefaultJavaToolChain( ToolchainModel model, Logger logger )
    {
        super( model, "jdk", logger );
    }
    public String getJavaHome()
    {
        return javaHome;
    }
    public void setJavaHome( String javaHome )
    {
        this.javaHome = javaHome;
    }
    public String toString()
    {
        return "JDK[" + getJavaHome() + "]";
    }
    public String findTool( String toolName )
    {
        File toRet = findTool( toolName, new File( FileUtils.normalize( getJavaHome() ) ) );
        if ( toRet != null )
        {
            return toRet.getAbsolutePath();
        }
        return null;
    }
    private static File findTool( String toolName, File installFolder )
    {
        File bin = new File( installFolder, "bin" ); 
        if ( bin.exists() )
        {
            File tool = new File( bin, toolName + ( Os.isFamily( "windows" ) ? ".exe" : "" ) ); 
            if ( tool.exists() )
            {
                return tool;
            }
        }
        return null;
   }
}