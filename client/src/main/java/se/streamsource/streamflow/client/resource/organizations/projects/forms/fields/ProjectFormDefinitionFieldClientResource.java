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

package se.streamsource.streamflow.client.resource.organizations.projects.forms.fields;

import org.qi4j.api.injection.scope.Uses;
import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.resource.CommandQueryClientResource;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.resource.roles.BooleanDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * JAVADOC
 */
public class ProjectFormDefinitionFieldClientResource
      extends CommandQueryClientResource
{
   public ProjectFormDefinitionFieldClientResource( @Uses Context context, @Uses Reference reference )
   {
      super( context, reference );
   }

   public FieldDefinitionValue field() throws ResourceException
   {
      return query( "field", FieldDefinitionValue.class );
   }

   public void updateMandatory( BooleanDTO mandatory ) throws ResourceException
   {
      putCommand( "updateMandatory", mandatory );
   }

   public void changeDescription( StringDTO newDescription ) throws ResourceException
   {
      putCommand( "changeDescription", newDescription );
   }

   public void changeNote( StringDTO newNote ) throws ResourceException
   {
      putCommand( "changeNote", newNote );
   }

   public void moveField( IntegerDTO newIndex ) throws ResourceException
   {
      putCommand( "moveField", newIndex );
   }
}