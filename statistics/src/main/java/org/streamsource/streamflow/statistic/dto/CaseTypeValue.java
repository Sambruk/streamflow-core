package org.streamsource.streamflow.statistic.dto;

/**
 * Created by IntelliJ IDEA.
 * User: arvidhuss
 * Date: 2/23/12
 * Time: 4:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class CaseTypeValue
   implements Comparable<CaseTypeValue>
{
   private String id;
   private String name;
   
   public CaseTypeValue( String id, String name )
   {
      this.id = id;
      this.name = name;
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public int compareTo( CaseTypeValue o )
   {
      return this.name.compareTo( o.getName() );
   }
}
