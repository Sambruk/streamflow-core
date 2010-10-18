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
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.domain.form.FieldValueDTO;

public class FormSubmissionWizardPageModel
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CommandQueryClient client;

   public void updateField( EntityReference reference, String value ) throws ResourceException
   {
      ValueBuilder<FieldValueDTO> builder = vbf.newValueBuilder( FieldValueDTO.class );
      builder.prototype().field().set( reference );
      builder.prototype().value().set( value );

      client.putCommand( "updatefield", builder.newInstance() );
   }
}