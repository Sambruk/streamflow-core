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

package se.streamsource.streamflow.web.domain.label;

import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.unitofwork.UnitOfWork;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

/**
 * JAVADOC
 */
@Mixins(Labels.Mixin.class)
public interface Labels
{
    Label createLabel();
    void removeLabel(Label label);
    Iterable<Label> getLabels();

    interface Data
    {
        ManyAssociation<Label> labels();

        Label createdLabel(DomainEvent event);
        void removedLabel(DomainEvent event, Label label);
    }

    abstract class Mixin
            implements Labels, Data
    {
        @Structure
        UnitOfWorkFactory uowf;

        @This
        Data state;

        public Label createLabel()
        {
            return createdLabel(DomainEvent.CREATE);
        }

        public void removeLabel(Label label)
        {
            if (state.labels().contains(label))
            {
                removedLabel(DomainEvent.CREATE, label);
                label.removeEntity();
            }
        }

        public Iterable<Label> getLabels()
        {
            return state.labels();
        }


        public Label createdLabel(DomainEvent event)
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Label label = uow.newEntity(Label.class);
            state.labels().add(state.labels().count(), label);
            return label;
        }

        public void removedLabel(DomainEvent event, Label label)
        {
            state.labels().remove(label);
        }
    }
}
