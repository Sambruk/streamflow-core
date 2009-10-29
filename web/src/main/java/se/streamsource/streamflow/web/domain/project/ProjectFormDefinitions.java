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
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.web.domain.form.FormDefinition;
import se.streamsource.streamflow.web.domain.form.FormDefinitionEntity;

/**
 * JAVADOC
 */
@Mixins(ProjectFormDefinitions.Mixin.class)
public interface ProjectFormDefinitions
{
    void addFormDefinition( FormDefinition formDefinition);
    void removeFormDefinition( FormDefinition formDefinition);

    interface Data
    {
        ManyAssociation<FormDefinition> formDefinitions();

        void projectFormDefinitionAdded( DomainEvent event, FormDefinition addedForm);
        void projectFormDefinitionRemoved( DomainEvent event, FormDefinition removedForm);

        FormDefinitionEntity getFormDefinitionByName(String name);
    }

    abstract class Mixin
        implements ProjectFormDefinitions, Data
    {
        public void addFormDefinition( FormDefinition formDefinition )
        {
            if (formDefinitions().contains( formDefinition ))
                return;

            projectFormDefinitionAdded( DomainEvent.CREATE, formDefinition );
        }

        public void removeFormDefinition( FormDefinition formDefinition )
        {
            if (!formDefinitions().contains( formDefinition ))
                return;

            projectFormDefinitionRemoved( DomainEvent.CREATE, formDefinition );
        }

        public void projectFormDefinitionAdded( DomainEvent event, FormDefinition addedForm )
        {
            formDefinitions().add( addedForm );
        }

        public void projectFormDefinitionRemoved( DomainEvent event, FormDefinition removedForm )
        {
            formDefinitions().remove( removedForm );
        }

        public FormDefinitionEntity getFormDefinitionByName( String name )
        {
            return (FormDefinitionEntity) Describable.Mixin.getDescribable( formDefinitions(), name );
        }
    }
}
