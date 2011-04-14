/**
 *
 * Copyright 2009-2011 Streamsource AB
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

import org.qi4j.api.value.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.form.CreateFieldDTO;
import se.streamsource.streamflow.client.util.LinkValueListModel;
import se.streamsource.streamflow.api.administration.form.FieldTypes;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

/**
 * JAVADOC
 */
public class FormElementsModel
      extends LinkValueListModel
{
   public FormElementsModel()
   {
      super( "formelements" );
   }

   public void addField( LinkValue pageItem, String name, FieldTypes fieldType )
   {
      ValueBuilder<CreateFieldDTO> builder = module.valueBuilderFactory().newValueBuilder( CreateFieldDTO.class );
      builder.prototype().name().set( name );
      builder.prototype().fieldType().set( fieldType );

      client.getClient( pageItem ).postCommand( "create", builder.newInstance() );
   }

   public void addPage( String pageName )
   {
      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
      builder.prototype().string().set( pageName );

      client.postCommand( "create", builder.newInstance() );
   }

   public void move( LinkValue item, String direction )
   {
      ValueBuilder<StringValue> builder = module.valueBuilderFactory().newValueBuilder( StringValue.class );
      builder.prototype().string().set( direction );

      client.getClient( item ).putCommand( "move",  builder.newInstance() );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.onEntities( client.getReference().getLastSegment() ), transactions ))
      {
         refresh();
      }
   }
}