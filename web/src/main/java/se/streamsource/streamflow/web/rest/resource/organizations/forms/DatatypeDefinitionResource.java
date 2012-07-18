/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.web.rest.resource.organizations.forms;

import org.restlet.data.Form;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.streamflow.web.context.administration.forms.definition.DatatypeDefinitionContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.NotableContext;
import se.streamsource.streamflow.web.domain.structure.form.DatatypeDefinition;

/**
 * JAVADOC
 */
public class DatatypeDefinitionResource
   extends CommandQueryResource
{
   public DatatypeDefinitionResource()
   {
      super( DatatypeDefinitionContext.class, DescribableContext.class, NotableContext.class );
   }
   
   public Form index(){
      DatatypeDefinition datatypeDefinition = context(DatatypeDefinitionContext.class).index();
      Form form = new Form();
      form.set("description", datatypeDefinition.getDescription());
      form.set("note", datatypeDefinition.getNote());
      return form;
      
   }

}