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
import se.streamsource.dci.api.InteractionsMixin;
import se.streamsource.streamflow.web.context.organizations.forms.FormsContext;
import se.streamsource.streamflow.web.context.structure.labels.LabelsContext;
import se.streamsource.streamflow.web.context.structure.labels.SelectedLabelsContext;
import se.streamsource.streamflow.web.domain.structure.organization.Projects;
import se.streamsource.streamflow.web.domain.structure.project.Project;
import se.streamsource.dci.api.Interactions;
import se.streamsource.dci.api.DeleteInteraction;
import se.streamsource.dci.api.SubContext;
import se.streamsource.streamflow.web.context.structure.DescribableContext;

/**
 * JAVADOC
 */
@Mixins(ProjectContext.Mixin.class)
public interface ProjectContext
   extends DeleteInteraction,
      DescribableContext,
      Interactions
{
   @SubContext
   MembersContext members();

   @SubContext
   FormsContext forms();

   @SubContext
   CaseTypesContext casetypes();

   @SubContext
   public LabelsContext labels();

   @SubContext
   SelectedLabelsContext selectedlabels();

   @SubContext
   SelectedCaseTypesContext selectedcasetypes();

   abstract class Mixin
      extends InteractionsMixin
      implements ProjectContext
   {
      public void delete()
      {
         Projects projects = context.get(Projects.class);
         Project project = context.get(Project.class);

         projects.removeProject( project );
      }

      public MembersContext members()
      {
         return subContext( MembersContext.class );
      }

      public FormsContext forms()
      {
         return subContext( FormsContext.class );
      }

      public CaseTypesContext casetypes()
      {
         return subContext( CaseTypesContext.class );
      }

      public LabelsContext labels()
      {
         return subContext(LabelsContext.class);
      }

      public SelectedLabelsContext selectedlabels()
      {
         return subContext( SelectedLabelsContext.class );
      }

      public SelectedCaseTypesContext selectedcasetypes()
      {
         return subContext( SelectedCaseTypesContext.class );
      }
   }
}
