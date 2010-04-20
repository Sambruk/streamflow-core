/**
 *
 * Copyright (c) 2009 Streamsource AB
 * All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.streamsource.dci.api;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import se.streamsource.dci.api.Context;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * JAVADOC
 */
public class ContextTest
{
   Context context = new Context();

   @Test
   public void whenRoleSetThenGetWorks()
   {
      Foo foo = new Foo();

      context.set( foo );

      assertThat(context.get( Foo.class ), equalTo( foo ));
      assertThat(context.get( Object.class ), equalTo( (Object) foo ));
   }

   @Test(expected=IllegalArgumentException.class)
   public void whenRoleNotSetThenGetFails()
   {
      context.get( Bar.class );
   }

   @Test
   public void whenRoleSetInParentThenGetWorks()
   {
      Context childContext = new Context(context);

      Foo foo = new Foo();

      context.set( foo );

      assertThat(childContext.get( Foo.class ), equalTo( foo ));
   }

   @Test
   public void whenRoleSetInParentAndChildThenGetReturnsChild()
   {
      Context childContext = new Context(context);

      Foo foo = new Foo();
      Foo childFoo = new Foo();

      context.set( foo );
      childContext.set( childFoo );

      assertThat(childContext.get( Foo.class ), equalTo( childFoo ));
   }

   @Test
   public void whenRoleSetInParentAndChildThenGetAllReturnsBoth()
   {
      Context childContext = new Context(context);

      Foo foo = new Foo();
      Foo childFoo = new Foo();

      context.set( foo );
      childContext.set( childFoo );

      assertThat(childContext.getAll( Foo.class ), equalTo( Arrays.asList(childFoo, foo )));
   }

   public static class Foo
   {

   }

   public static class Bar
      extends Foo
   {

   }
}
