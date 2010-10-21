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

package se.streamsource.streamflow.web.context.organizations.forms;

import org.qi4j.api.constraint.Constraints;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.Fields;
import se.streamsource.streamflow.web.domain.structure.form.Page;
import se.streamsource.streamflow.web.domain.structure.form.Pages;

/**
 * JAVADOC
 */
@Mixins(FormPagesContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface FormPagesContext
      extends IndexContext<LinksValue>, Context
{
   public void create( @MaxLength(50) StringValue name );

   LinksValue formelements();

   abstract class Mixin
         implements FormPagesContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         Pages.Data pages = RoleMap.role( Pages.Data.class );
         for (Page page : pages.pages())
         {
            linksBuilder.path( null );
            linksBuilder.rel( "page" );
            linksBuilder.addDescribable( page );
            Fields.Data fields = (Fields.Data) page;
            linksBuilder.path( page.toString() );
            linksBuilder.rel( "field" );
            for (Field field : fields.fields())
            {
               linksBuilder.addDescribable( field );
            }
         }

         return linksBuilder.newLinks();
      }

      public void create( StringValue name )
      {
         Pages pages = RoleMap.role( Pages.class );

         pages.createPage( name.string().get() );
      }

      public LinksValue formelements()
      {
         LinksBuilder linksBuilder = new LinksBuilder( module.valueBuilderFactory() );

         Pages.Data pages = RoleMap.role( Pages.Data.class );
         for (Page page : pages.pages())
         {
            linksBuilder.path( null );
            linksBuilder.rel( "page" );
            linksBuilder.addDescribable( page );
            Fields.Data fields = (Fields.Data) page;
            linksBuilder.path( page.toString() );
            linksBuilder.rel( "field" );
            for (Field field : fields.fields())
            {
               linksBuilder.addDescribable( field );
            }
         }

         return linksBuilder.newLinks();
      }
   }
}
