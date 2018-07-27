package org.apache.maven.model.building;
interface ModelBuildingEventCatapult
{
    void fire( ModelBuildingListener listener, ModelBuildingEvent event );
    final ModelBuildingEventCatapult BUILD_EXTENSIONS_ASSEMBLED = new ModelBuildingEventCatapult()
    {
        public void fire( ModelBuildingListener listener, ModelBuildingEvent event )
        {
            listener.buildExtensionsAssembled( event );
        }
    };
}
