package se.streamsource.streamflow.web.domain.structure.casetype;

import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.web.domain.Describable;

/**
 * 
 */
@Mixins(SelectedCaseTypesQueries.Mixin.class)
public interface SelectedCaseTypesQueries
{
   CaseType getCaseTypeByName( String name ) throws IllegalArgumentException;
   
   class Mixin
      implements SelectedCaseTypesQueries
   {
      @This
      SelectedCaseTypes.Data data;
      
      public CaseType getCaseTypeByName( String name ) throws IllegalArgumentException
      {
         for( Describable caseType : data.selectedCaseTypes().toList() )
         {
            if( name.equals( caseType.getDescription() ) )
               return (CaseType)caseType;
         }
         throw new IllegalArgumentException( name );
      }
   }
}
