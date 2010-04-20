/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.api.Interactions;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.infrastructure.application.ListValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.web.domain.entity.form.PageQueries;
import se.streamsource.streamflow.web.domain.structure.form.Page;
import se.streamsource.streamflow.web.domain.structure.form.Pages;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.api.SubContexts;

/**
 * JAVADOC
 */
@Mixins(FormPagesContext.Mixin.class)
public interface FormPagesContext
   extends SubContexts<FormPageContext>, Interactions
{
   public LinksValue pages();
   public ListValue pagessummary();
   public void add( StringValue name );

   abstract class Mixin
      extends InteractionsMixin
      implements FormPagesContext
   {
      public LinksValue pages()
      {
         Pages.Data pages = context.get(Pages.Data.class);

         return new LinksBuilder( module.valueBuilderFactory() ).rel("page").addDescribables( pages.pages() ).newLinks();
      }

      public ListValue pagessummary()
      {
         PageQueries pageQueries = context.get(PageQueries.class);

         return pageQueries.getPagesSummary();
      }

      public void add( StringValue name )
      {
         Pages pages = context.get(Pages.class);;

         pages.createPage( name.string().get() );
      }

      public FormPageContext context( String id )
      {
         Page page = module.unitOfWorkFactory().currentUnitOfWork().get( Page.class, id );

         if (!context.get( Pages.Data.class ).pages().contains( page ))
            throw new IllegalArgumentException("Page is not a member of this form");

         context.set(page);
         return subContext( FormPageContext.class );
      }
   }
}
