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

import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.entity.Identity;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.domain.form.PageDefinitionValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.web.domain.structure.form.Page;
import se.streamsource.streamflow.web.domain.structure.form.Pages;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.NotableContext;

/**
 * JAVADOC
 */
@Mixins(FormPageContext.Mixin.class)
public interface FormPageContext
   extends DescribableContext, NotableContext, DeleteInteraction, Interactions
{
   PageDefinitionValue page();
   void move( StringValue direction );

   @SubContext
   FormFieldsContext fields();
   
   abstract class Mixin
      extends InteractionsMixin
      implements FormPageContext
   {
      public PageDefinitionValue page()
      {
         Describable describable = context.get(Describable.class);
         Identity identity = context.get(Identity.class);

         ValueBuilder<PageDefinitionValue> builder = module.valueBuilderFactory().newValueBuilder( PageDefinitionValue.class );
         builder.prototype().description().set( describable.getDescription() );
         builder.prototype().page().set( EntityReference.parseEntityReference( identity.identity().get() ));
         return builder.newInstance();
      }

      public void move( StringValue direction )
      {
         Page page = context.get(Page.class);
         Pages.Data pagesData = context.get(Pages.Data.class);
         Pages pages = context.get(Pages.class);

         int index = pagesData.pages().toList().indexOf( page );
         if ( direction.string().get().equalsIgnoreCase( "up" ))
         {
            try
            {
               pages.movePage( page, index-1 );
            } catch(ConstraintViolationException e) {}
         } else
         {
            pages.movePage( page, index+1);
         }
      }

      public void delete()
      {
         Page pageEntity = context.get(Page.class);
         Pages form = context.get( Pages.class);

         form.removePage( pageEntity );
      }

      public FormFieldsContext fields()
      {
         return subContext( FormFieldsContext.class );
      }
   }
}
