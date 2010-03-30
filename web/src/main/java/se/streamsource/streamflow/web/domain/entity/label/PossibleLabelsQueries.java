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

package se.streamsource.streamflow.web.domain.entity.label;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.web.domain.structure.label.Label;
import se.streamsource.streamflow.web.domain.structure.label.SelectedLabels;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(PossibleLabelsQueries.Mixin.class)
public interface PossibleLabelsQueries
{
   List<Label> possibleLabels( ManyAssociation<Label> labels );

   class Mixin
      implements PossibleLabelsQueries
   {
      @This
      SelectedLabels.Data data;

      public List<Label> possibleLabels( ManyAssociation<Label> labels )
      {
         List<Label> possibleLabels = new ArrayList<Label>( );

         for (Label label : labels)
         {
            if (!data.selectedLabels().contains( label ))
            {
               possibleLabels.add( label );
            }
         }

         return possibleLabels;
      }

   }
}
