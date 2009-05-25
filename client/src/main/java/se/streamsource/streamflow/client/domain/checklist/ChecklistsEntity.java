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

package se.streamsource.streamflow.client.domain.checklist;

import org.qi4j.api.composite.CompositeBuilderFactory;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;

/**
 * JAVADOC
 */
@Mixins(ChecklistsEntity.ChecklistsMixin.class)
public interface ChecklistsEntity
        extends Checklists, EntityComposite
{
    class ChecklistsMixin
            implements Checklists
    {
        @Structure
        CompositeBuilderFactory cbf;

        public void applyChecklist(ChecklistValue checklist, Object contextNode) throws Exception
        {
            for (ChecklistItemValue checklistItemValue : checklist.items().get())
            {
                applyChecklistItem(checklistItemValue, contextNode);
            }
        }

        private void applyChecklistItem(ChecklistItemValue checklistItemValue, Object contextNode) throws Exception
        {
            if (checklistItemValue.include().get())
            {
/*
                Class<? extends InteractionComposite> interactionClass = checklistItemValue.interaction().get().interactionClass().get();
                Callable callable = cbf.newCompositeBuilder(interactionClass).use(contextNode, checklistItemValue.interaction().get().context().get()).newInstance();

                Object childNode = callable.call();
                List<ChecklistItemValue> list = checklistItemValue.children().get();
                for (ChecklistItemValue itemValue : list)
                {
                    applyChecklistItem(itemValue, childNode);
                }
*/
            }
        }
    }
}
