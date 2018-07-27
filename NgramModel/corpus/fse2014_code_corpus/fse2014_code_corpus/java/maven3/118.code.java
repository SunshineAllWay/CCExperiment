package org.apache.maven.project.validation;
import org.apache.maven.model.Model;
@Deprecated
public interface ModelValidator
{
    String ROLE = ModelValidator.class.getName();
    ModelValidationResult validate( Model model );
}
