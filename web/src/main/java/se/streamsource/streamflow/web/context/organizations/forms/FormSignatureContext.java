/*
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

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.mixin.Mixins;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.UpdateContext;
import se.streamsource.streamflow.domain.form.RequiredSignatureValue;
import se.streamsource.streamflow.web.domain.structure.form.RequiredSignatures;

import java.io.IOException;

/**
 * JAVADOC
 */
@Mixins(FormSignatureContext.Mixin.class)
public interface FormSignatureContext
   extends DeleteContext, UpdateContext<RequiredSignatureValue>, Context
{
   abstract class Mixin
         extends ContextMixin
      implements FormSignatureContext
   {
      public void update( RequiredSignatureValue newValue)
      {
         roleMap.get( RequiredSignatures.class ).updateRequiredSignature( roleMap.get(Integer.class), newValue );
      }

      public void delete() throws ResourceException, IOException
      {
         roleMap.get( RequiredSignatures.class ).removeRequiredSignature( roleMap.get(Integer.class) );
      }
   }
}
