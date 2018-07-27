package org.apache.maven.model.building;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
interface ModelCacheTag<T>
{
    String getName();
    Class<T> getType();
    T intoCache( T data );
    T fromCache( T data );
    ModelCacheTag<ModelData> RAW = new ModelCacheTag<ModelData>()
    {
        public String getName()
        {
            return "raw";
        }
        public Class<ModelData> getType()
        {
            return ModelData.class;
        }
        public ModelData intoCache( ModelData data )
        {
            Model model = ( data.getModel() != null ) ? data.getModel().clone() : null;
            return new ModelData( model, data.getGroupId(), data.getArtifactId(), data.getVersion() );
        }
        public ModelData fromCache( ModelData data )
        {
            return intoCache( data );
        }
    };
    ModelCacheTag<DependencyManagement> IMPORT = new ModelCacheTag<DependencyManagement>()
    {
        public String getName()
        {
            return "import";
        }
        public Class<DependencyManagement> getType()
        {
            return DependencyManagement.class;
        }
        public DependencyManagement intoCache( DependencyManagement data )
        {
            return ( data != null ) ? data.clone() : null;
        };
        public DependencyManagement fromCache( DependencyManagement data )
        {
            return intoCache( data );
        };
    };
}
