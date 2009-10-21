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

package se.streamsource.streamflow.web.domain.form;

import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(FormDefinitions.FormDefinitionsMixin.class)
public interface FormDefinitions
{
    FormDefinitionEntity createFormDefinition(String name);
    void removeFormDefinition(FormDefinition form);

    interface FormDefinitionsState
    {
        ManyAssociation<FormDefinitionEntity> formDefinitions();

        FormDefinitionEntity formDefinitionCreated( DomainEvent event, String id);
        void formDefinitionAdded( DomainEvent event, FormDefinitionEntity form);
        void formDefinitionRemoved( DomainEvent event, FormDefinition form);

        FormDefinitionEntity getFormByName(String name);
    }

    abstract class FormDefinitionsMixin
        implements FormDefinitions, FormDefinitionsState
    {
        @Service
        IdentityGenerator idGen;

        public FormDefinitionEntity createFormDefinition( String name )
        {
            FormDefinitionEntity form = formDefinitionCreated( DomainEvent.CREATE, idGen.generate(FormDefinitionEntity.class ));
            formDefinitionAdded( DomainEvent.CREATE, form);
            form.changeDescription( name );
            return form;
        }

        public FormDefinitionEntity getFormByName( String name )
        {
            return Describable.DescribableMixin.getDescribable( formDefinitions(), name );
        }
    }
}
