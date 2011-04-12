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

package se.streamsource.streamflow.web.resource.organizations.forms;

import org.restlet.resource.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.restlet.server.*;
import se.streamsource.dci.restlet.server.api.*;
import se.streamsource.streamflow.web.context.administration.forms.definition.*;
import se.streamsource.streamflow.web.context.structure.*;
import se.streamsource.streamflow.web.domain.structure.form.*;

/**
 * JAVADOC
 */
public class FormPageResource
   extends CommandQueryResource
   implements SubResources
{
   public FormPageResource()
   {
      super( FormPageContext.class, DescribableContext.class, NotableContext.class );
   }

   public void resource( String segment ) throws ResourceException
   {
      Field field = findManyAssociation( RoleMap.role(Fields.Data.class).fields(), segment );
      RoleMap.current().set(((FieldValueDefinition.Data)field).fieldValue().get());
      subResource(FormFieldResource.class );
   }
}
