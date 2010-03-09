/*
 * Copyright (c) 2010, Mads Enevoldsen. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.streamflow.web.context.access.forms;

import org.qi4j.api.mixin.Mixins;
import se.streamsource.dci.context.Context;
import se.streamsource.dci.context.ContextMixin;
import se.streamsource.dci.context.IndexContext;
import se.streamsource.dci.context.SubContexts;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;
import se.streamsource.streamflow.domain.form.SubmittedPageValue;
import se.streamsource.streamflow.infrastructure.application.LinksBuilder;

/**
 * JAVADOC
 */
@Mixins(FormSummaryContext.Mixin.class)
public interface FormSummaryContext
   extends Context, IndexContext<FormSubmissionValue>
{

   abstract class Mixin
      extends ContextMixin
      implements FormSummaryContext
   {
      public FormSubmissionValue index()
      {
         return context.role( FormSubmissionValue.class );
      }
   }
}