package org.apache.maven.model.path;
import java.io.File;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelBuildingRequest;
public interface ModelPathTranslator
{
    void alignToBaseDirectory( Model model, File basedir, ModelBuildingRequest request );
}
