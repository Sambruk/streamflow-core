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

package se.streamsource.streamflow.web.resource.organizations;

import static org.qi4j.api.query.QueryExpressions.templateFor;

import static org.qi4j.api.query.QueryExpressions.eq;
import org.qi4j.api.constraint.Name;
import org.qi4j.api.mixin.Mixins;

import se.streamsource.streamflow.domain.roles.Describable;
import se.streamsource.streamflow.resource.roles.StringDTO;

/**
 * Mapped to /{entity}/?operation=describe
 */
@Mixins(DescribableResource.DescribableMixin.class)
public interface DescribableResource
{
    void changedescription(StringDTO stringValue, @Name("entity") Describable describable);

    class DescribableMixin
        implements DescribableResource
    {
        public void changedescription(StringDTO stringValue, Describable describable)
        {
            describable.changeDescription(stringValue.string().get());
        }
    }
}