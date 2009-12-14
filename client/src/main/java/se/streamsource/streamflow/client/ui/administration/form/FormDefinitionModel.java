/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.administration.form;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.resource.organizations.forms.FormDefinitionClientResource;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.infrastructure.application.ListItemValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.EventListener;

import javax.swing.AbstractListModel;
import java.util.List;

/**
 * JAVADOC
 */
public class FormDefinitionModel
      extends AbstractListModel
      implements EventListener
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   private FormDefinitionClientResource form;

   private List<ListItemValue> list;

   public int getSize()
   {
      return list == null ? 0 : list.size();
   }

   public Object getElementAt( int index )
   {
      return list == null ? null : list.get( index );
   }

   public void createField( String description, EntityReference valueType )
   {
      try
      {
         ValueBuilder<ListItemValue> builder = vbf.newValueBuilder( ListItemValue.class );
         builder.prototype().description().set( description );
         builder.prototype().entity().set( valueType );
         form.createField( builder.newInstance() );

      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_create_field, e );
      }
   }

   public void removeForm( int idx )
   {
      try
      {
         form.field( idx ).deleteCommand();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_remove_field, e );
      }
   }

   public void refresh()
   {
/*
        try
        {
            list = form.field().items().get();
            fireContentsChanged(this, 0, list.size());
        } catch (ResourceException e)
        {
            throw new OperationException(AdministrationResources.could_not_refresh, e);
        }
*/
   }

   public void notifyEvent( DomainEvent event )
   {

   }

   public FormDefinitionModel getFormFieldModel( int index )
   {
      return null;
   }
}