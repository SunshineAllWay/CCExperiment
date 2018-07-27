package org.apache.solr.client.solrj.response;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.solr.client.solrj.util.ClientUtils;
 public class FacetField implements Serializable
 {
   public static class Count implements Serializable 
   {
     private String _name = null;
     private long _count = 0;
     private FacetField _ff = null;
     public Count( FacetField ff, String n, long c )
     {
       _name = n;
       _count = c;
       _ff = ff;
     }
     public String getName() {
       return _name;
     }
     public void setName( String n )
     {
       _name = n;
     }
     public long getCount() {
       return _count;
     }
     public void setCount( long c )
     {
       _count = c;
     }
     public FacetField getFacetField() {
       return _ff;
     }
     @Override
     public String toString()
     {
       return _name+" ("+_count+")";
     }
     public String getAsFilterQuery() {
       if (_ff.getName().equals("facet_queries")) {
         return _name;
       }
       return 
          ClientUtils.escapeQueryChars( _ff._name ) + ":" + 
          ClientUtils.escapeQueryChars( _name );
     }
   }
   private String      _name   = null;
   private List<Count> _values = null;
   private String _gap = null;
   private Date _end = null;
   public FacetField( final String n )
   {
     _name = n;
   }
   public FacetField(String name, String gap, Date end) {
     _name = name;
     _gap = gap;
     _end = end;
   }
   public String getGap()   {
     return _gap;
   }
   public Date getEnd() {
     return _end;
   }
   public void add( String name, long cnt )
   {
     if( _values == null ) {
       _values = new ArrayList<Count>( 30 );
     }
     _values.add( new Count( this, name, cnt ) );
   }
   public void insert( String name, long cnt )
   {
     if( _values == null ) {
       _values = new ArrayList<Count>( 30 );
     }
     _values.add( 0, new Count( this, name, cnt ) );
   }
   public String getName() {
     return _name;
   }
   public List<Count> getValues() {
     return _values;
   }
   public int getValueCount()
   {
     return _values == null ? 0 : _values.size();
   }
   public FacetField getLimitingFields(long max) 
   {
     FacetField ff = new FacetField( _name );
     if( _values != null ) {
       ff._values = new ArrayList<Count>( _values.size() );
       for( Count c : _values ) {
         if( c._count < max ) { 
           ff._values.add( c );
         }
       }
     }
     return ff;
   }
   @Override
   public String toString()
   {
     return _name + ":" + _values;
   }
 }
