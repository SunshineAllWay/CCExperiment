package org.apache.maven.project.validation;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
@Component( role = ModelValidator.class )
@Deprecated
public class DefaultModelValidator
    implements ModelValidator
{
    @Requirement
    private org.apache.maven.model.validation.ModelValidator modelValidator;
    public ModelValidationResult validate( Model model )
    {
        ModelValidationResult result = new ModelValidationResult();
        ModelBuildingRequest request =
            new DefaultModelBuildingRequest().setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MAVEN_2_0 );
        SimpleModelProblemCollector problems = new SimpleModelProblemCollector( result );
        modelValidator.validateEffectiveModel( model, request, problems );
        return result;
    }
    private static class SimpleModelProblemCollector
        implements ModelProblemCollector
    {
        ModelValidationResult result;
        public SimpleModelProblemCollector( ModelValidationResult result )
        {
            this.result = result;
        }
        public void add( Severity severity, String message, InputLocation location, Exception cause )
        {
            if ( !ModelProblem.Severity.WARNING.equals( severity ) )
            {
                result.addMessage( message );
            }
        }
    }
}
