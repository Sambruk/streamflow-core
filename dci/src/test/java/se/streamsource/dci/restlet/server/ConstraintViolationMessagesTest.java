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
package se.streamsource.dci.restlet.server;

import org.junit.Test;
import org.qi4j.api.constraint.ConstraintDeclaration;
import org.qi4j.api.constraint.ConstraintViolation;
import org.qi4j.api.constraint.Constraints;
import org.qi4j.library.constraints.MaxLengthConstraint;
import org.qi4j.library.constraints.annotation.MaxLength;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Locale;

/**
 * TODO
 */
public class ConstraintViolationMessagesTest
{
   @MaxLength(50)
   public String foo;

   @Range(min=10, max=30)
   public int bar;

   @Range(min=10, max=30, message="Wrong range")
   public int bar2;

   @Test
   public void testMessages() throws NoSuchFieldException, IllegalAccessException
   {
      {
         ConstraintViolation violation = new ConstraintViolation("foo", getClass().getField("foo").getAnnotation(MaxLength.class), 70);
         String message = new ConstraintViolationMessages().getMessage(violation, Locale.getDefault());
         System.out.println(message);
      }
      {
         ConstraintViolation violation = new ConstraintViolation("bar", getClass().getField("bar").getAnnotation(Range.class), 70);
         String message = new ConstraintViolationMessages().getMessage(violation, Locale.getDefault());
         System.out.println(message);
      }
      {
         ConstraintViolation violation = new ConstraintViolation("bar2", getClass().getField("bar2").getAnnotation(Range.class), 70);
         String message = new ConstraintViolationMessages().getMessage(violation, Locale.getDefault());
         System.out.println(message);
      }
   }

   @ConstraintDeclaration
   @Retention( RetentionPolicy.RUNTIME )
   @Constraints( MaxLengthConstraint.class )
   public @interface Range
   {
       String message() default "{Range.message}";
       int min();
       int max();
   }
}
