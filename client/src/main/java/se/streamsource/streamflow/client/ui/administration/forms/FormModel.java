/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.ui.administration.forms;

import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.administration.form.FormValue;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormPagesModel;
import se.streamsource.streamflow.client.ui.administration.forms.definition.FormSignaturesModel;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionListener;

import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.matches;
import static se.streamsource.streamflow.infrastructure.event.domain.source.helper.Events.onEntities;

/**
 * JAVADOC
 */
public class FormModel
   extends ResourceModel<FormValue>
      implements Refreshable, TransactionListener

{
   public FormModel()
   {
      relationModelMapping("forminfo", FormModel.class);
      relationModelMapping("pages", FormPagesModel.class);
      relationModelMapping("signatures", FormSignaturesModel.class);
   }

   public void changeDescription( StringValue description ) throws ResourceException
   {
      client.putCommand( "changedescription", description );
   }

   public void changeNote( StringValue note ) throws ResourceException
   {
      client.putCommand( "changenote", note );
   }

   public void changeFormId( String id )
   {
      Form form = new Form();
      form.set("id", id);
      client.putCommand( "changeformid", form );
   }

   public void notifyTransactions( Iterable<TransactionDomainEvents> transactions )
   {
      // Refresh if either the owner of the list has changed, or if any of the entities in the list has changed
      if (matches( onEntities( client.getReference().getLastSegment() ), transactions ))
         refresh();
   }
}