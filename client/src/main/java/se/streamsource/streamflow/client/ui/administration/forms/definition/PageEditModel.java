/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.PageDefinitionValue;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

/**
 * JAVADOC
 */
public class PageEditModel
   extends ResourceModel<PageDefinitionValue>
      implements Refreshable
{
   @Uses
   private CommandQueryClient client;

   @Structure
   private Module module;


   public void changeDescription( String pageName ) throws ResourceException
   {
      if( !pageName.equals( getIndex().description().get() ) )
      {
         ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder(StringValue.class);
         builder.prototype().string().set( pageName );

         client.putCommand( "changedescription", builder.newInstance() );
      }
   }

   public void move( String direction ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder(StringValue.class);
      builder.prototype().string().set( direction );


      client.postCommand( "move", builder.newInstance() );
   }

   public void remove() throws ResourceException
   {
      client.delete();
   }

   public void changeRuleFieldId( LinkValue fieldId )
   {
      if( fieldId != null && !fieldId.id().get().equals( getIndex().rule().get().field().get() ) )
      {
         Form form = new Form( );
         form.set( "fieldid", fieldId.id().get() );
         client.putCommand( "changerulefieldid", form.getWebRepresentation() );
      }
   }

   public void changeRuleCondition( LinkValue condition )
   {
      if( condition != null && !condition.text().get().equals( getIndex().rule().get().condition().get().name() ))
      {
         Form form = new Form();
         form.set( "condition", condition.text().get() );
         client.putCommand( "changerulecondition", form.getWebRepresentation() );
      }
   }

   public void changeRuleVisibleWhen( boolean visibleWhen )
   {
      if( visibleWhen != getIndex().rule().get().visibleWhen().get() )
      {
         Form form = new Form();
         form.set( "visiblewhen", "" + visibleWhen );
         client.putCommand( "changerulevisiblewhen", form.getWebRepresentation() );
      }
   }

   public EventList<LinkValue> possibleRuleFields()
   {
     return EventListSynch.synchronize( client.query( "possiblerulefields", LinksValue.class ).links().get(), new BasicEventList<LinkValue>() );
   }

   public EventList<LinkValue> possibleRuleConditions()
   {
      return EventListSynch.synchronize( client.query( "possibleruleconditions", LinksValue.class ).links().get() , new BasicEventList<LinkValue>() );
   }

   public VisibilityRuleValuesModel newVisibilityRuleValuesModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(VisibilityRuleValuesModel.class).use(client).newInstance();
   }
}