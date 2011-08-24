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

package se.streamsource.streamflow.web.rest.resource.organizations.forms;

import se.streamsource.dci.restlet.server.CommandQueryResource;
import se.streamsource.dci.restlet.server.api.SubResource;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.administration.forms.FormContext;
import se.streamsource.streamflow.web.context.administration.forms.definition.FormInfoContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.NotableContext;

/**
 * JAVADOC
 */
public class FormResource
      extends CommandQueryResource
{
   public FormResource()
   {
      super( FormContext.class);
   }

   public LinksValue possiblemoveto() throws Throwable
   {
      LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
      builder.command( "move" );

      builder.addDescribables( (Iterable) context(FormContext.class).possiblemoveto() );

      return builder.newLinks();
   }

   @SubResource
   public void forminfo( )
   {
      subResourceContexts( FormInfoContext.class, DescribableContext.class, NotableContext.class );
   }

   @SubResource
   public void pages()
   {
      subResource(FormPagesResource.class );
   }

   @SubResource
   public void signatures()
   {
      subResource(FormSignaturesResource.class );
   }
}
