/*
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.streamflow.web.context.workspace.cases.form;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.util.DateFunctions;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import se.streamsource.dci.api.IndexContext;
import se.streamsource.dci.api.RoleMap;
import se.streamsource.streamflow.domain.form.AttachmentFieldSubmission;
import se.streamsource.streamflow.domain.form.AttachmentFieldValue;
import se.streamsource.streamflow.resource.caze.EffectiveFieldsDTO;
import se.streamsource.streamflow.resource.caze.FieldDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormDTO;
import se.streamsource.streamflow.resource.caze.SubmittedFormsListDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;
import se.streamsource.streamflow.util.Strings;
import se.streamsource.streamflow.web.domain.entity.form.SubmittedFormsQueries;
import se.streamsource.streamflow.web.domain.structure.attachment.AttachedFile;
import se.streamsource.streamflow.web.infrastructure.attachment.AttachmentStore;

import java.io.IOException;
import java.net.URISyntaxException;

import static se.streamsource.dci.api.RoleMap.*;

/**
 * JAVADOC
 */
public class CaseSubmittedFormContext
      implements IndexContext<SubmittedFormDTO>
{
   public SubmittedFormDTO index()
   {
      return RoleMap.role( SubmittedFormDTO.class );
   }

}
