/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.client.util;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.link.TitledLinkValue;

/**
 * Management of owned entities
 */
public class DefinitionListModel
   extends LinkValueListModel
{
   private String changeDescription;
   private final String create;

   public DefinitionListModel( String create )
   {
      this("changedescription", create);
   }

   public DefinitionListModel( String changedescription, String create )
   {
      this.changeDescription = changedescription;
      this.create = create;
   }

   public void changeDescription( LinkValue link, String newName )
   {
      try
      {
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
         builder.prototype().string().set( newName );

         client.getSubClient( link.id().get() ).putCommand( changeDescription, builder.newInstance() );
      } catch (ResourceException e)
      {
         handleException( e );
      }
   }

   public void create( String name )
   {
      Form form = new Form();
      form.set("name", name);

      try
      {
         client.postCommand( create, form );
      } catch (ResourceException e)
      {
         handleException( e );
      }
   }


   public EventList<TitledLinkValue> usages(LinkValue link)
   {
      LinksValue usages = client.getClient( link ).query( "usages", LinksValue.class );
      EventList<TitledLinkValue> eventList = new BasicEventList<TitledLinkValue>();
      EventListSynch.synchronize( usages.links().get(), eventList );

      return eventList;
   }
}