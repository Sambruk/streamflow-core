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
package se.streamsource.streamflow.web.context.administration;

import static se.streamsource.dci.api.RoleMap.role;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypesQueries;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;

/**
 * JAVADOC
 */
public class SelectedCaseTypesContext
      implements IndexContext<LinksValue>
{
   @Structure
   Module module;

   public LinksValue index()
   {
      SelectedCaseTypes.Data caseTypes = role( SelectedCaseTypes.Data.class );

      return new LinksBuilder( module.valueBuilderFactory() ).rel( "casetype" ).addDescribables( caseTypes.selectedCaseTypes() ).newLinks();
   }

   public LinksValue possiblecasetypes()
   {
      final SelectedCaseTypes.Data selectedLabels = role( SelectedCaseTypes.Data.class );
      CaseTypesQueries caseTypes = role( CaseTypesQueries.class );
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() ).command( "addcasetype" );
      caseTypes.caseTypes( builder, new Specification<CaseType>()
      {
         public boolean satisfiedBy( CaseType instance )
         {
            return !selectedLabels.selectedCaseTypes().contains( instance );
         }
      } );
      return builder.newLinks();
   }

   public void addcasetype( EntityValue caseTypeDTO )
   {
      UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();

      SelectedCaseTypes caseTypes = role( SelectedCaseTypes.class );
      CaseType caseType = uow.get( CaseType.class, caseTypeDTO.entity().get() );

      caseTypes.addSelectedCaseType( caseType );
   }
}
