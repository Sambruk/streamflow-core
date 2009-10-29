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

package se.streamsource.streamflow.web.domain.organization;

import se.streamsource.streamflow.web.domain.form.FieldDefinitions;
import se.streamsource.streamflow.web.domain.form.FormDefinitions;
import se.streamsource.streamflow.web.domain.form.ValueDefinitions;
import se.streamsource.streamflow.web.domain.project.IdGenerator;
import se.streamsource.streamflow.web.domain.role.Roles;

/**
 * JAVADOC
 */
public interface Organization
        extends OrganizationalUnitRefactoring,
        Roles,
        FormDefinitions,
        FieldDefinitions,
        ValueDefinitions,
        IdGenerator
{
}
