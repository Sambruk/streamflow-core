package se.streamsource.streamflow.web.domain.structure.project;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.domain.Describable;

/**
 * 
 */
@Mixins(ProjectsQueries.Mixin.class)
public interface ProjectsQueries
{
   Project getProjectByName( String name ) throws IllegalArgumentException;
   
   class Mixin
      implements ProjectsQueries
   {
      @This
      Projects.Data data;
      
      public Project getProjectByName( String name ) throws IllegalArgumentException
      {
         for( Describable describable : data.projects().toList() )
         {
            if( name.equals( describable.getDescription() ) )
               return (Project)describable;
         }
         throw new IllegalArgumentException( name );
      }
   }
}
