/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.dci.test.interactions.file;

import org.qi4j.api.constraint.Constraint;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.Constraints;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JAVADOC
 */
@ConstraintDeclaration
@Retention(RetentionPolicy.RUNTIME)
@Constraints(RequiresFile.ConstraintImpl.class)
public @interface RequiresFile
{
   class ConstraintImpl
      implements Constraint<RequiresFile, File>
   {
      public boolean isValid( RequiresFile requiresRoles, File file )
      {
         return file.isFile();
      }
   }
}
