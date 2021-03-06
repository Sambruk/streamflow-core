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
package se.streamsource.streamflow.web.context.surface.customers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.qi4j.api.entity.EntityReference;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.workspace.cases.caselog.CaseLogEntryDTO;
import se.streamsource.streamflow.surface.api.ClosedCaseDTO;
import se.streamsource.streamflow.util.Translator;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.context.workspace.cases.conversation.MessagesContext;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.entity.caze.CaseEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLog;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLogEntryValue;
import se.streamsource.streamflow.web.domain.structure.caselog.CaseLoggable;

/**
 * Context for closed case
 */
public class ClosedCaseContext
        implements IndexContext<ClosedCaseDTO>
{
   @Structure
   Module module;

   public ClosedCaseDTO index()
   {
      ValueBuilderFactory vbf = module.valueBuilderFactory();
      ValueBuilder<ClosedCaseDTO> builder = vbf.newValueBuilder(ClosedCaseDTO.class);
      CaseEntity aCase = RoleMap.role(CaseEntity.class);
      builder.prototype().description().set(aCase.description().get());
      builder.prototype().creationDate().set(aCase.createdOn().get());
      builder.prototype().closeDate().set(aCase.closedOn().get());

      if (aCase.resolution().get() != null)
         builder.prototype().resolution().set(aCase.resolution().get().getDescription());

      builder.prototype().caseId().set(aCase.caseId().get());

      Owner owner = aCase.owner().get();
      builder.prototype().project().set(((Describable) owner).getDescription());

      return builder.newInstance();
   }
   

   public LinksValue caselog()
   {
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      ValueBuilder<CaseLogEntryDTO> valueBuilder = module.valueBuilderFactory().newValueBuilder( CaseLogEntryDTO.class );

      CaseLoggable.Data caseLog = RoleMap.role( CaseLoggable.Data.class );

      ResourceBundle bundle = ResourceBundle.getBundle( MessagesContext.class.getName(), RoleMap.role( Locale.class ) );
      Map<String, String> translations = new HashMap<String, String>();
      for (String key : bundle.keySet())
      {
         translations.put( key, bundle.getString( key ) );
      }

      for (CaseLogEntryValue entry : ((CaseLog.Data) caseLog.caselog().get()).entries().get())
      {
         if (entry.availableOnMypages().get())
         {
            UnitOfWork uow = module.unitOfWorkFactory().currentUnitOfWork();
            Describable user = uow.get( Describable.class, entry.createdBy().get().identity() );
            valueBuilder.prototype().creationDate().set( entry.createdOn().get() );
            valueBuilder.prototype().creator().set( user.getDescription() );
            String translatedMessage = Translator.translate( entry.message().get(), translations ).replace( "\n", "<br/>" );
            valueBuilder.prototype().message().set( translatedMessage );
            String id = "";
            if (entry.entity().get() != null)
            {
               id = EntityReference.getEntityReference( entry.entity().get() ).identity();
            }
            valueBuilder.prototype().href().set( id );
            valueBuilder.prototype().id().set( id );
            valueBuilder.prototype().myPagesVisibility().set( entry.availableOnMypages().get() );
            valueBuilder.prototype().caseLogType().set( entry.entryType().get());

            valueBuilder.prototype().text().set( translatedMessage );

            builder.addLink( valueBuilder.newInstance() );
         }
      }
      return builder.newLinks();
   }
}
