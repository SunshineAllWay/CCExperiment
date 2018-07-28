package org.apache.maven.model.building;
public interface ModelBuilder
{
    ModelBuildingResult build( ModelBuildingRequest request )
        throws ModelBuildingException;
    ModelBuildingResult build( ModelBuildingRequest request, ModelBuildingResult result )
        throws ModelBuildingException;
}
