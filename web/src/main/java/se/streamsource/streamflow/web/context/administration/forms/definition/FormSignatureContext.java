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

package se.streamsource.streamflow.web.context.administration.forms.definition;

import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.UpdateContext;
import se.streamsource.streamflow.api.administration.form.RequiredSignatureValue;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;

import java.io.*;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class FormSignatureContext
      implements DeleteContext, UpdateContext<RequiredSignatureValue>, IndexContext<RequiredSignatureValue>
{
   public RequiredSignatureValue index()
   {
      return role( RequiredSignatures.Data.class ).requiredSignatures().get().get( role( Integer.class ) );
   }

   public void update( RequiredSignatureValue newValue )
   {
      role( RequiredSignatures.class ).updateRequiredSignature( role( Integer.class ), newValue );
   }

   public void delete() throws ResourceException, IOException
   {
      role( RequiredSignatures.class ).removeRequiredSignature( role( Integer.class ) );
   }
}
