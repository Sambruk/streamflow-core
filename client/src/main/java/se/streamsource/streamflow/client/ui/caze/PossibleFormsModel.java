/**
 *
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.client.ui.caze;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.object.ObjectBuilderFactory;
import org.qi4j.api.value.ValueBuilderFactory;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.infrastructure.ui.EventListSynch;
import se.streamsource.streamflow.client.infrastructure.ui.WeakModelMap;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;
import se.streamsource.streamflow.resource.roles.EntityReferenceDTO;
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import java.util.List;

public class PossibleFormsModel
   implements EventListener
{
   @Uses
   CommandQueryClient client;

   @Structure
   ObjectBuilderFactory obf;

   @Structure
   ValueBuilderFactory vbf;

   BasicEventList<LinkValue> forms = new BasicEventList<LinkValue>( );

   WeakModelMap<String, FormSubmissionModel> formSubmitModels = new WeakModelMap<String, FormSubmissionModel>()
   {
      @Override
      protected FormSubmissionModel newModel(String key)
      {
         try
         {
            ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
            builder.prototype().entity().set( EntityReference.parseEntityReference( key ));

            client.postCommand( "createformsubmission", builder.newInstance() );
            EntityReferenceDTO formSubmission = client.query( "formsubmission", builder.newInstance(), EntityReferenceDTO.class );
            return obf.newObjectBuilder( FormSubmissionModel.class )
                  .use( client.getSubClient( formSubmission.entity().get().identity() ) ).newInstance();
         } catch (ResourceException e)
         {
            throw new OperationException(WorkspaceResources.could_not_get_form, e);
         }
      }
   };

   public void setForms( List<LinkValue> forms )
   {
      EventListSynch.synchronize( forms, this.forms );
   }

   public EventList<LinkValue> getForms()
   {
      return forms;
   }

   public FormSubmissionModel getFormSubmitModel(String key)
   {
      return formSubmitModels.get(key);
   }

   public void submit( EntityReference form )
   {
      ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
      builder.prototype().entity().set( form );
      try
      {
         client.postCommand( "submit", builder.newInstance() );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_submit_form, e);
      }
   }

   public void discard( EntityReference form )
   {
      ValueBuilder<EntityReferenceDTO> builder = vbf.newValueBuilder( EntityReferenceDTO.class );
      builder.prototype().entity().set( form );
      try
      {
         client.postCommand( "discard", builder.newInstance() );
         formSubmitModels.remove( form.identity() );
      } catch (ResourceException e)
      {
         throw new OperationException(WorkspaceResources.could_not_discard_form_submission, e);
      }
   }

   public void notifyEvent( DomainEvent event )
   {
      for (FormSubmissionModel formSubmitModel : formSubmitModels)
      {
         formSubmitModel.notifyEvent( event );
      }
   }
}
