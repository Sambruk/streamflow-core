/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.project;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.form.FormDefinition;

/**
 * JAVADOC
 */
@Mixins(ProjectFormDefinitions.ProjectFormDefinitionsMixin.class)
public interface ProjectFormDefinitions
{
    void addFormDefinition( FormDefinition formDefinition);
    void removeFormDefinition( FormDefinition formDefinition);

    interface ProjectFormDefinitionsState
    {
        ManyAssociation<FormDefinition> formDefinitions();

        void formDefinitionAdded( DomainEvent event, FormDefinition addedForm);
        void formDefinitionRemoved( DomainEvent event, FormDefinition removedForm);
    }

    abstract class ProjectFormDefinitionsMixin
        implements ProjectFormDefinitions, ProjectFormDefinitionsState
    {
        public void addFormDefinition( FormDefinition formDefinition )
        {
            if (formDefinitions().contains( formDefinition ))
                return;

            formDefinitionAdded( DomainEvent.CREATE, formDefinition );
        }

        public void removeFormDefinition( FormDefinition formDefinition )
        {
            if (!formDefinitions().contains( formDefinition ))
                return;

            formDefinitionRemoved( DomainEvent.CREATE, formDefinition );
        }

        public void formDefinitionAdded( DomainEvent event, FormDefinition addedForm )
        {
            formDefinitions().add( addedForm );
        }

        public void formDefinitionRemoved( DomainEvent event, FormDefinition removedForm )
        {
            formDefinitions().remove( removedForm );
        }
    }
}
