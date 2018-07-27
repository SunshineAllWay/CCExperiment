package org.apache.maven.model.profile.activation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.model.Activation;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.profile.ProfileActivationContext;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = ProfileActivator.class, hint = "jdk-version" )
public class JdkVersionProfileActivator
    implements ProfileActivator
{
    public boolean isActive( Profile profile, ProfileActivationContext context, ModelProblemCollector problems )
    {
        boolean active = false;
        Activation activation = profile.getActivation();
        if ( activation != null )
        {
            String jdk = activation.getJdk();
            if ( jdk != null )
            {
                String version = context.getSystemProperties().get( "java.version" );
                if ( version == null || version.length() <= 0 )
                {
                    problems.add( Severity.ERROR, "Failed to determine Java version for profile " + profile.getId(),
                                  activation.getLocation( "jdk" ), null );
                    return false;
                }
                if ( jdk.startsWith( "!" ) )
                {
                    active = !version.startsWith( jdk.substring( 1 ) );
                }
                else if ( isRange( jdk ) )
                {
                    active = isInRange( version, getRange( jdk ) );
                }
                else
                {
                    active = version.startsWith( jdk );
                }
            }
        }
        return active;
    }
    private static boolean isInRange( String value, List<RangeValue> range )
    {
        int leftRelation = getRelationOrder( value, range.get( 0 ), true );
        if ( leftRelation == 0 )
        {
            return true;
        }
        if ( leftRelation < 0 )
        {
            return false;
        }
        return getRelationOrder( value, range.get( 1 ), false ) <= 0;
    }
    private static int getRelationOrder( String value, RangeValue rangeValue, boolean isLeft )
    {
        if ( rangeValue.value.length() <= 0 )
        {
            return isLeft ? 1 : -1;
        }
        value = value.replaceAll( "[^0-9\\.\\-\\_]", "" );
        List<String> valueTokens = new ArrayList<String>( Arrays.asList( value.split( "[\\.\\-\\_]" ) ) );
        List<String> rangeValueTokens = new ArrayList<String>( Arrays.asList( rangeValue.value.split( "\\." ) ) );
        addZeroTokens( valueTokens, 3 );
        addZeroTokens( rangeValueTokens, 3 );
        for ( int i = 0; i < 3; i++ )
        {
            int x = Integer.parseInt( valueTokens.get( i ) );
            int y = Integer.parseInt( rangeValueTokens.get( i ) );
            if ( x < y )
            {
                return -1;
            }
            else if ( x > y )
            {
                return 1;
            }
        }
        if ( !rangeValue.closed )
        {
            return isLeft ? -1 : 1;
        }
        return 0;
    }
    private static void addZeroTokens( List<String> tokens, int max )
    {
        while ( tokens.size() < max )
        {
            tokens.add( "0" );
        }
    }
    private static boolean isRange( String value )
    {
        return value.startsWith( "[" ) || value.startsWith( "(" );
    }
    private static List<RangeValue> getRange( String range )
    {
        List<RangeValue> ranges = new ArrayList<RangeValue>();
        for ( String token : range.split( "," ) )
        {
            if ( token.startsWith( "[" ) )
            {
                ranges.add( new RangeValue( token.replace( "[", "" ), true ) );
            }
            else if ( token.startsWith( "(" ) )
            {
                ranges.add( new RangeValue( token.replace( "(", "" ), false ) );
            }
            else if ( token.endsWith( "]" ) )
            {
                ranges.add( new RangeValue( token.replace( "]", "" ), true ) );
            }
            else if ( token.endsWith( ")" ) )
            {
                ranges.add( new RangeValue( token.replace( ")", "" ), false ) );
            }
            else if ( token.length() <= 0 )
            {
                ranges.add( new RangeValue( "", false ) );
            }
        }
        if ( ranges.size() < 2 )
        {
            ranges.add( new RangeValue( "99999999", false ) );
        }
        return ranges;
    }
    private static class RangeValue
    {
        private String value;
        private boolean closed;
        RangeValue( String value, boolean closed )
        {
            this.value = value.trim();
            this.closed = closed;
        }
        public String toString()
        {
            return value;
        }
    }
}
