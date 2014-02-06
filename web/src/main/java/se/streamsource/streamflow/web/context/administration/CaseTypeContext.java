/**
 *
 * Copyright 2009-2014 Jayway Products AB
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
package se.streamsource.streamflow.web.context.administration;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.query.Query;
import org.qi4j.api.query.QueryExpressions;
import org.qi4j.api.specification.Specification;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.dci.api.ServiceAvailable;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.web.application.knowledgebase.KnowledgebaseService;
import se.streamsource.streamflow.web.context.LinksBuilder;
import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Removable;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypeEntity;
import se.streamsource.streamflow.web.domain.interaction.gtd.Ownable;
import se.streamsource.streamflow.web.domain.interaction.gtd.Owner;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;

import static org.qi4j.api.query.QueryExpressions.*;
import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class CaseTypeContext
      implements DeleteContext
{
   @Structure
   Module module;

   @Service
   KnowledgebaseService knowledgebaseService;

   public LinksValue usages()
   {
      Query<SelectedCaseTypes> usageQuery = RoleMap.role( CaseTypes.class ).usages( RoleMap.role( CaseType.class ) );
      LinksBuilder builder = new LinksBuilder( module.valueBuilderFactory() );
      for (SelectedCaseTypes selectedCaseTypes : usageQuery)
      {
         if( !(((Removable.Data)((Ownable.Data)selectedCaseTypes).owner().get()).removed().get()) )
            builder.addDescribable( (Describable) selectedCaseTypes, ((Describable)((Ownable.Data)selectedCaseTypes).owner().get()).getDescription() );
      }

      return builder.newLinks();
   }

   public void delete()
   {
      CaseTypes caseTypes = RoleMap.role( CaseTypes.class );

      CaseType caseType = RoleMap.role( CaseType.class );

      caseTypes.removeCaseType( caseType );
   }

   public Iterable<CaseTypes> possiblemoveto()
   {
      final CaseTypes thisCaseTypes = role(CaseTypes.class);

      return Iterables.filter( new Specification<CaseTypes>()
      {
         public boolean satisfiedBy( CaseTypes item )
         {
            Owner owner = ((Ownable.Data) item).owner().get();

            return !item.equals( thisCaseTypes ) && !((Removable.Data) owner).removed().get();
         }
      }, module.queryBuilderFactory().newQueryBuilder( CaseTypes.class )
            .where( and(
                  eq( templateFor( Removable.Data.class ).removed(), false ),
                  QueryExpressions.isNotNull( templateFor( Ownable.Data.class ).owner() )
            ) )
            .newQuery( module.unitOfWorkFactory().currentUnitOfWork() ) );
   }

   public void move( EntityValue to )
   {
      CaseTypes toCaseTypes = module.unitOfWorkFactory().currentUnitOfWork().get( CaseTypes.class, to.entity().get() );
      CaseType caseType = RoleMap.role( CaseType.class );
      RoleMap.role( CaseTypes.class ).moveCaseType( caseType, toCaseTypes );
   }

   @ServiceAvailable(service = KnowledgebaseService.class, availability = true)
   public LinkValue knowledgeBase()
   {
      CaseTypeEntity caseType = RoleMap.role(CaseTypeEntity.class);
      ValueBuilder<LinkValue> builder = module.valueBuilderFactory().newValueBuilder(LinkValue.class);
      builder.prototype().id().set(caseType.identity().get());
      builder.prototype().text().set(caseType.getDescription());
      builder.prototype().rel().set("knowledgebase");
      builder.prototype().href().set(knowledgebaseService.createURL(caseType));
      return builder.newInstance();
   }
}
