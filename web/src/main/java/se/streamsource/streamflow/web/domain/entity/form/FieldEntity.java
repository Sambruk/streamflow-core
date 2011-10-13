/**
 *
 * Copyright 2009-2011 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.domain.entity.form;

import se.streamsource.streamflow.web.domain.Describable;
import se.streamsource.streamflow.web.domain.Notable;
import se.streamsource.streamflow.web.domain.entity.DomainEntity;
import se.streamsource.streamflow.web.domain.structure.form.Field;
import se.streamsource.streamflow.web.domain.structure.form.FieldId;
import se.streamsource.streamflow.web.domain.structure.form.Datatype;
import se.streamsource.streamflow.web.domain.structure.form.FieldValueDefinition;
import se.streamsource.streamflow.web.domain.structure.form.Mandatory;

/**
 * JAVADOC
 */
public interface FieldEntity
      extends
      Field,
      Describable.Data,
      FieldId.Data,
      Datatype.Data,
      Notable.Data,
      FieldValueDefinition.Data,
      Mandatory.Data,
      DomainEntity
{
}
