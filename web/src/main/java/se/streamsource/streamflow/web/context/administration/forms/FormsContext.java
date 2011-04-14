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

package se.streamsource.streamflow.web.context.administration.forms;

import org.qi4j.api.constraint.*;
import org.qi4j.api.injection.scope.*;
import org.qi4j.api.mixin.*;
import org.qi4j.api.structure.*;
import org.qi4j.library.constraints.annotation.*;
import se.streamsource.dci.api.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.dci.value.StringValueMaxLength;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.structure.form.Form;
import se.streamsource.streamflow.web.domain.structure.form.Forms;

/**
 * JAVADOC
 */
@Mixins(FormsContext.Mixin.class)
@Constraints(StringValueMaxLength.class)
public interface FormsContext
      extends IndexContext<LinksValue>, Context
{
   void createform( @MaxLength(50) StringValue formName );

   abstract class Mixin
         implements FormsContext
   {
      @Structure
      Module module;

      public LinksValue index()
      {
         Forms.Data forms = RoleMap.role( Forms.Data.class );

         return new LinksBuilder( module.valueBuilderFactory() ).rel( "form" ).addDescribables( forms.forms() ).newLinks();
      }

      public void createform( StringValue formName )
      {
         Forms forms = RoleMap.role( Forms.class );
         Form form = forms.createForm();
         form.changeDescription( formName.string().get() );
      }
   }
}
