package org.apache.solr.client.solrj.response;
import java.util.Date;
import org.apache.solr.common.util.NamedList;
public class CoreAdminResponse extends SolrResponseBase
{ 
  @SuppressWarnings("unchecked")
  public NamedList<NamedList<Object>> getCoreStatus()
  {
    return (NamedList<NamedList<Object>>) getResponse().get( "status" );
  }
  public NamedList<Object> getCoreStatus( String core )
  {
    return getCoreStatus().get( core );
  }
  public Date getStartTime( String core )
  {
    NamedList<Object> v = getCoreStatus( core );
    if( v == null ) {
      return null;
    }
    return (Date) v.get( "startTime" );
  }
  public Long getUptime( String core )
  {
    NamedList<Object> v = getCoreStatus( core );
    if( v == null ) {
      return null;
    }
    return (Long) v.get( "uptime" );
  }
}