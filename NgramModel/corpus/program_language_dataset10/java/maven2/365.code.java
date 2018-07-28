package org.apache.maven.script.beanshell;
import bsh.EvalError;
import bsh.Interpreter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.component.factory.bsh.BshComponent;
public class BeanshellMojoAdapter
    extends AbstractMojo
    implements BshComponent
{
    private Mojo mojo;
    private Interpreter interpreter;
    public BeanshellMojoAdapter( Mojo mojo, Interpreter interpreter )
    {
        this.mojo = mojo;
        this.interpreter = interpreter;
    }
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            interpreter.set( "logger", getLog() );
        }
        catch ( EvalError evalError )
        {
            throw new MojoExecutionException( "Unable to establish mojo", evalError );
        }
        mojo.execute();
    }
    public Interpreter getInterpreter()
    {
        return interpreter;
    }
}
