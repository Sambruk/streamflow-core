/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;

import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.streamflow.api.administration.form.CreateFieldDTO;
import se.streamsource.streamflow.api.administration.form.CreateFieldGroupDTO;
import se.streamsource.streamflow.api.administration.form.FieldTypes;
import se.streamsource.streamflow.client.util.LinkValueListModel;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events;

/**
 * JAVADOC
 */
public class FormPagesModel
      extends LinkValueListModel
{
   public FormPagesModel()
   {
      relationModelMapping("page", PageEditModel.class);
      relationModelMapping("field", FieldValueEditModel.class);
   }

   public void addField( LinkValue pageItem, String name, LinkValue addLink )
   {
      if ("createfield".equals(addLink.rel().get())) {
         ValueBuilder<CreateFieldDTO> builder = module.valueBuilderFactory().newValueBuilder( CreateFieldDTO.class );
         builder.prototype().name().set( name );
         builder.prototype().fieldType().set( FieldTypes.valueOf( addLink.id().get()));

         client.getClient( pageItem ).postCommand( addLink.href().get(), builder.newInstance() );
         
      } else if ("createfieldgroup".equals( addLink.rel().get() )) {
         ValueBuilder<CreateFieldGroupDTO> builder = module.valueBuilderFactory().newValueBuilder( CreateFieldGroupDTO.class );
         builder.prototype().name().set( name );
         builder.prototype().fieldGroup().set( addLink.id().get());

         client.getClient( pageItem ).postCommand( addLink.href().get(), builder.newInstance() );
      }
      
   }

   public void addPage( String pageName )
   {
      Form form = new Form();
      form.set("name", pageName);
      client.postCommand( "create", form );
   }

   public void move( LinkValue item, String direction )
   {
      Form form = new Form();
      form.set("direction", direction);

      client.getClient( item ).putCommand( "move",  form.getWebRepresentation() );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      if (Events.matches( Events.onEntities( client.getReference().getLastSegment() ), transactions ))
      {
         refresh();
      }
   }
}