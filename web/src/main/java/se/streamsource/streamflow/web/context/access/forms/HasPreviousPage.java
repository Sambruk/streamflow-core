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

import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;
import se.streamsource.streamflow.domain.form.FormSubmissionValue;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Check that an assignable is assigned or not
 */
@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Constraints(HasPreviousPage.Constraint.class)
public @interface HasPreviousPage
{
   public class Constraint
         implements org.qi4j.api.constraint.Constraint<HasPreviousPage, FormSubmissionValue>
   {
      public boolean isValid( HasPreviousPage page, FormSubmissionValue value )
      {
         return !(value.currentPage().get() == 0);
      }
   }
}