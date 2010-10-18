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

package se.streamsource.streamflow.web.context.organizations;

import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.Query;
import se.streamsource.dci.api.Context;
import se.streamsource.dci.api.ContextMixin;
import se.streamsource.dci.api.DeleteContext;
import se.streamsource.dci.api.SubContext;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.structure.Describable;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;
import se.streamsource.streamflow.web.context.organizations.forms.FormsContext;
import se.streamsource.streamflow.web.context.organizations.forms.SelectedFormsContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelsContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelsContext;
import se.streamsource.streamflow.web.context.structure.resolutions.ResolutionsContext;
import se.streamsource.streamflow.web.context.structure.resolutions.SelectedResolutionsContext;
import se.streamsource.streamflow.web.domain.entity.casetype.CaseTypesQueries;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseType;
import se.streamsource.streamflow.web.domain.structure.casetype.CaseTypes;
import se.streamsource.streamflow.web.domain.structure.casetype.SelectedCaseTypes;

/**
 * JAVADOC
 */
@Mixins(CaseTypeContext.Mixin.class)
public interface CaseTypeContext
   extends DescribableContext, DeleteContext, Context
{
   LinksValue usages();

   @SubContext
   FormsContext forms();

   @SubContext
   SelectedFormsContext selectedforms();

   @SubContext
   public LabelsContext labels();

   @SubContext
   SelectedLabelsContext selectedlabels();

   @SubContext
   ResolutionsContext resolutions();

   @SubContext
   SelectedResolutionsContext selectedresolutions();

   LinksValue possiblemoveto();

   void move( EntityValue to);

   abstract class Mixin
      extends ContextMixin
      implements CaseTypeContext
   {
      public LinksValue usages()
      {
         Query<SelectedCaseTypes> usageQuery = roleMap.get( CaseTypes.class).usages( roleMap.get( CaseType.class) );
         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory()); // TODO What to use for path here?
         for (SelectedCaseTypes selectedCaseTypes : usageQuery)
         {
            builder.addDescribable( (Describable) selectedCaseTypes );
         }

         return builder.newLinks();
      }

      public void delete()
      {
         CaseTypes caseTypes = roleMap.get( CaseTypes.class);

         CaseType caseType = roleMap.get( CaseType.class);

         caseTypes.removeCaseType( caseType );
      }

      public FormsContext forms()
      {
         return subContext( FormsContext.class );
      }

      public LabelsContext labels()
      {
         return subContext(LabelsContext.class);
      }

      public SelectedFormsContext selectedforms()
      {
         return subContext( SelectedFormsContext.class );
      }

      public SelectedLabelsContext selectedlabels()
      {
         return subContext( SelectedLabelsContext.class );
      }

      public ResolutionsContext resolutions()
      {
         return subContext( ResolutionsContext.class );
      }

      public SelectedResolutionsContext selectedresolutions()
      {
         return subContext( SelectedResolutionsContext.class );
      }

      public LinksValue possiblemoveto()
      {
         LinksBuilder builder = new LinksBuilder(module.valueBuilderFactory());
         builder.command( "move" );
         roleMap.get( CaseTypesQueries.class).possibleMoveCaseTypeTo( builder );
         return builder.newLinks();
      }

      public void move( EntityValue to )
      {
         CaseTypes toCaseTypes = module.unitOfWorkFactory().currentUnitOfWork().get( CaseTypes.class, to.entity().get() );
         CaseType caseType = roleMap.get( CaseType.class);
         roleMap.get( CaseTypes.class ).moveCaseType( caseType, toCaseTypes );
      }
   }
}
