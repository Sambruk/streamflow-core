/**
 *
 * Copyright 2009-2014 Jayway Products AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.domain.structure.external;

import org.joda.time.DateTime;
import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.util.Iterables;
import se.streamsource.streamflow.api.external.ContentValue;
import se.streamsource.streamflow.api.external.LogValue;
import se.streamsource.streamflow.infrastructure.event.domain.DomainEvent;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.structure.created.CreatedOn;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks a case residing in another system.
 */
@Mixins( ShadowCase.Mixin.class )
public interface ShadowCase
   extends
      Describable,
      CreatedOn,
      Removable
{

   void updateContent( List<ContentValue> newcontent );

   void updateLog( LogValue newlog );

   interface Data
   {
      Property<String> systemName();

      Property<String> externalId();

      Property<String> contactId();

      Property<DateTime> creationDate();

      @UseDefaults @Optional
      Property<List<ContentValue>> content();

      @UseDefaults @Optional
      Property<List<LogValue>> log();

      void updatedContent( @Optional DomainEvent event, @Optional List<ContentValue> newcontent );

      void updatedLog( @Optional DomainEvent event, LogValue newLog );
   }

   abstract class Mixin
      implements ShadowCase, Data
   {
      @This
      Data data;

      public void updateContent( List<ContentValue> newcontent )
      {
         // if new content not empty or null try to match keys
         // if a key matches and the value differs - update the value
         // if key does not match add the whole Content value to the list
         if( newcontent != null && !newcontent.isEmpty() )
         {
            updatedContent( null, newcontent );
         }

      }

      public void updatedContent( DomainEvent event, final List<ContentValue> newcontent )
      {
         List<ContentValue> current = data.content().get();
         Iterable<ContentValue> toUpdate = Iterables.filter( new Specification<ContentValue>()
         {
            public boolean satisfiedBy( final ContentValue existingValue )
            {
               return Iterables.matchesAny( new Specification<ContentValue>()
               {
                  public boolean satisfiedBy( ContentValue newValue )
                  {
                     boolean same = existingValue.key().get().equals( newValue.key().get() );
                     if (same)
                     {
                        if (!existingValue.value().get().equals( newValue.value().get() ))
                        {
                           existingValue.value().set( newValue.value().get() );
                        }
                        return true;
                     }
                     return false;
                  }
               }, newcontent );
            }
         }, current );


         Iterable<ContentValue> toAdd = Iterables.filter( new Specification<ContentValue>()
         {
            public boolean satisfiedBy( final ContentValue existingValue )
            {
               return !Iterables.matchesAny( new Specification<ContentValue>()
               {
                  public boolean satisfiedBy( ContentValue newValue )
                  {
                     return existingValue.key().get().equals( newValue.key().get() );
                  }
               }, newcontent );
            }
         }, current );
         List<ContentValue> completeList = new ArrayList<ContentValue>(  );
         Iterables.addAll( completeList, toUpdate );
         Iterables.addAll( completeList, toAdd );

         data.content().set( completeList );

      }

      public void updateLog( LogValue log )
      {
         if( log != null )
         {
            updatedLog( null, log );
         }
      }

      public void updatedLog( DomainEvent event, LogValue log )
      {
         List<LogValue> logEntries = data.log().get();

         logEntries.add( log );
         data.log().set( logEntries );
      }
   }
}
