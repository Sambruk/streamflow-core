/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.web.context.administration.surface.accesspoints;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Function;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;

import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.*;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.organization.AccessPointEntity;
import se.streamsource.streamflow.web.domain.structure.form.*;

/**
 *
 */
public class ReplacementSelectionFieldsContext
    implements IndexContext<LinksValue>
{
    @Structure
    Module module;

    public LinksValue index() {
        LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
        SelectedForms.Data forms = RoleMap.role( SelectedForms.Data.class );
        Form form = forms.selectedForms().get(0);
        if( form != null )
        {
            for( Page page : ((Pages.Data)form).pages() )
            {
               builder.addLink( page.getDescription(), page.toString(), "page", page.toString(),null );

               for( Field field : ((Fields.Data)page).fields() )
               {
                   if( ((FieldValueDefinition.Data)field).fieldValue().get() instanceof SelectionFieldValue )
                   {
                       builder.addLink( field.getDescription(), field.toString(), "selectionfieldvalue", field.toString() + "/", null, page.getDescription() );
                   } else
                   {
                       builder.addLink( field.getDescription(), field.toString(), "none", field.toString(), null, page.getDescription() );
                   }
               }
            }
        }
        return builder.newLinks();
    }
}
