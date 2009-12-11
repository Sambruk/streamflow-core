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

import org.qi4j.api.common.Optional;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.mixin.Mixins;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * JAVADOC
 */
@Mixins(Labelable.Mixin.class)
public interface Labelable
{
    void addLabel(Label label);
    void removeLabel(Label label);

    void retainLabels(@Optional SelectedLabels currentSelection, @Optional SelectedLabels nextSelection);

    interface Data
    {
        ManyAssociation<LabelEntity> labels();

        void addedLabel(DomainEvent event, Label label);
        void removedLabel(DomainEvent event, Label label);
    }

    abstract class Mixin
            implements Labelable, Data
    {
        @This
        Data state;

        public void addLabel(Label label)
        {
            addedLabel(DomainEvent.CREATE, label);
        }

        public void removeLabel(Label label)
        {
            if (state.labels().contains((LabelEntity) label))
            {
                removedLabel(DomainEvent.CREATE, label);
            }
        }

        public void retainLabels( SelectedLabels currentSelection, SelectedLabels nextSelection )
        {
            List<Label> removedLabels = new ArrayList<Label>( );
            if (nextSelection == null)
            {
                for (LabelEntity labelEntity : labels())
                {
                    if (currentSelection.hasLabel( labelEntity ))
                        removedLabels.add( labelEntity );
                }
            } else if (currentSelection == null)
            {
                // Do nothing
            } else
            {
                for (LabelEntity labelEntity : labels())
                {
                    if (currentSelection.hasLabel( labelEntity ) && !nextSelection.hasLabel( labelEntity ))
                        removedLabels.add( labelEntity );
                }
            }

            // Remove any labels that should not be retained
            for (Label removedLabel : removedLabels)
            {
                removeLabel(removedLabel);
            }
        }

        public void addedLabel(DomainEvent event, Label label)
        {
            for (int i = 0; i < state.labels().count(); i++)
            {
                if (state.labels().get(i).getDescription().compareTo(label.getDescription()) > 0)
                {
                    state.labels().add(i, (LabelEntity) label);
                    return;
                }
            }

            state.labels().add((LabelEntity) label);
        }

        public void removedLabel(DomainEvent event, Label label)
        {
            state.labels().remove((LabelEntity) label);
        }
    }
}
