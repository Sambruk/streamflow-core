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

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.entity.IdentityGenerator;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Forms.Mixin.class)
public interface Forms
{
    Form createForm();
    Form createFormFromTemplate( FormTemplate template);
    void removeForm( Form form );

    interface Data
    {
        ManyAssociation<Form> forms();

        FormEntity createdForm(DomainEvent event, String id, @Optional FormTemplate template);
        void removedForm( DomainEvent event, Form removedForm);

        FormEntity getFormByName(String name);
    }

    abstract class Mixin
        implements Forms, Data
    {
        @Service
        IdentityGenerator idGen;

        @Structure
        UnitOfWorkFactory uowf;

        public Form createForm()
        {
            FormEntity form = createdForm( DomainEvent.CREATE,  idGen.generate( FormEntity.class ), null);

            return form;
        }

        public Form createFormFromTemplate( FormTemplate template )
        {
            FormEntity form = createdForm( DomainEvent.CREATE,  idGen.generate( FormEntity.class ), template);

            form.synchronizeWithTemplate();

            return form;
        }

        public void removeForm( Form form )
        {
            if (!forms().contains( form ))
                return;

            removedForm( DomainEvent.CREATE, form );
        }

        public FormEntity createdForm( DomainEvent event, String id, FormTemplate template )
        {
            EntityBuilder<FormEntity> builder = uowf.currentUnitOfWork().newEntityBuilder( FormEntity.class, id );
            builder.instance().template().set( template );
            FormEntity form = builder.newInstance();
            forms().add( form );
            return form;
        }

        public void projectFormDefinitionAdded( DomainEvent event, Form addedForm )
        {
            forms().add( addedForm );
        }

        public void removedForm( DomainEvent event, Form removedForm )
        {
            forms().remove( removedForm );
        }

        public FormEntity getFormByName( String name )
        {
            return (FormEntity) Describable.Mixin.getDescribable( forms(), name );
        }
    }
}
