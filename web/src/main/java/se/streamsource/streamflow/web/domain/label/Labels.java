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
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * JAVADOC
 */
public interface Labels
{
    Label newLabel();

    Iterable<Label> labels();

    interface LabelsState
    {
        ManyAssociation<Label> labels();
    }

    class LabelsMixin
        implements Labels
    {
        @Structure
        UnitOfWorkFactory uowf;

        @This
        LabelsState state;

        public Label newLabel()
        {
            UnitOfWork uow = uowf.currentUnitOfWork();
            Label label = uow.newEntity(Label.class);
            state.labels().add(state.labels().count(), label);
            return label;
        }

        public Iterable<Label> labels()
        {
            return state.labels();
        }
    }
}
