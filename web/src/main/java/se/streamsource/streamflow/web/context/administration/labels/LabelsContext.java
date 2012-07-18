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
package se.streamsource.streamflow.web.context.administration.labels;

import org.qi4j.api.constraint.Name;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.structure.Module;
import org.qi4j.library.constraints.annotation.MaxLength;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.CreateContext;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.Labels;

import static se.streamsource.dci.api.RoleMap.role;

/**
 * JAVADOC
 */
@Mixins(LabelsContext.Mixin.class)
public interface LabelsContext
      extends Context, IndexContext<Iterable<Label>>, CreateContext<String, Label>
{
   Label create( @MaxLength(50) @Name("name") String name );

   abstract class Mixin
         implements LabelsContext
   {
      @Structure
      Module module;

      public Iterable<Label> index()
      {
         return role( Labels.Data.class ).labels();
      }

      public Label create( String name )
      {
         Labels labels = role( Labels.class );

         return labels.createLabel( name );
      }
   }
}
