package org.apache.solr.handler.component;
import java.util.ArrayList;
import java.util.List;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.AbstractSolrTestCase;
public class SearchHandlerTest extends AbstractSolrTestCase 
{
  @Override public String getSchemaFile() { return "schema.xml"; }
  @Override public String getSolrConfigFile() { return "solrconfig.xml"; }
  @SuppressWarnings("unchecked")
  public void testInitalization()
  {
    SolrCore core = h.getCore();
    List<String> names0 = new ArrayList<String>();
    names0.add( MoreLikeThisComponent.COMPONENT_NAME );
    NamedList args = new NamedList();
    args.add( SearchHandler.INIT_COMPONENTS, names0 );
    SearchHandler handler = new SearchHandler();
    handler.init( args );
    handler.inform( core );
    assertEquals( 1, handler.getComponents().size() );
    assertEquals( core.getSearchComponent( MoreLikeThisComponent.COMPONENT_NAME ), 
        handler.getComponents().get( 0 ) );
    names0 = new ArrayList<String>();
    names0.add( FacetComponent.COMPONENT_NAME );
    names0.add( DebugComponent.COMPONENT_NAME );
    names0.add( MoreLikeThisComponent.COMPONENT_NAME );
    args = new NamedList();
    args.add( SearchHandler.INIT_COMPONENTS, names0 );
    handler = new SearchHandler();
    handler.init( args );
    handler.inform( core );
    assertEquals( 3, handler.getComponents().size() );
    assertEquals( core.getSearchComponent( FacetComponent.COMPONENT_NAME ),
        handler.getComponents().get( 0 ) );
    assertEquals( core.getSearchComponent( DebugComponent.COMPONENT_NAME ),
        handler.getComponents().get( 1 ) );
    assertEquals( core.getSearchComponent( MoreLikeThisComponent.COMPONENT_NAME ), 
        handler.getComponents().get( 2 ) );
    names0 = new ArrayList<String>();
    names0.add( MoreLikeThisComponent.COMPONENT_NAME );
    List<String> names1 = new ArrayList<String>();
    names1.add( FacetComponent.COMPONENT_NAME );
    args = new NamedList();
    args.add( SearchHandler.INIT_FIRST_COMPONENTS, names0 );
    args.add( SearchHandler.INIT_LAST_COMPONENTS, names1 );
    handler = new SearchHandler();
    handler.init( args );
    handler.inform( core );
    List<SearchComponent> comps = handler.getComponents();
    assertEquals( 2+handler.getDefaultComponents().size(), comps.size() );
    assertEquals( core.getSearchComponent( MoreLikeThisComponent.COMPONENT_NAME ), comps.get( 0 ) );
    assertEquals( core.getSearchComponent( FacetComponent.COMPONENT_NAME ), comps.get( comps.size()-2 ) );
    assertEquals( core.getSearchComponent( DebugComponent.COMPONENT_NAME ), comps.get( comps.size()-1 ) );
  }
}
