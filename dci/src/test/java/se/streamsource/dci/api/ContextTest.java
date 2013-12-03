/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
package se.streamsource.dci.api;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * JAVADOC
 */
public class ContextTest
{
   RoleMap roleMap = new RoleMap();

   @Test
   public void whenRoleSetThenGetWorks()
   {
      Foo foo = new Foo();

      roleMap.set( foo );

      assertThat( roleMap.get( Foo.class ), equalTo( foo ));
      assertThat( roleMap.get( Object.class ), equalTo( (Object) foo ));
   }

   @Test(expected=IllegalArgumentException.class)
   public void whenRoleNotSetThenGetFails()
   {
      roleMap.get( Bar.class );
   }

   @Test
   public void whenRoleSetInParentThenGetWorks()
   {
      RoleMap childRoleMap = new RoleMap( roleMap );

      Foo foo = new Foo();

      roleMap.set( foo );

      assertThat( childRoleMap.get( Foo.class ), equalTo( foo ));
   }

   @Test
   public void whenRoleSetInParentAndChildThenGetReturnsChild()
   {
      RoleMap childRoleMap = new RoleMap( roleMap );

      Foo foo = new Foo();
      Foo childFoo = new Foo();

      roleMap.set( foo );
      childRoleMap.set( childFoo );

      assertThat( childRoleMap.get( Foo.class ), equalTo( childFoo ));
   }

   @Test
   public void whenRoleSetInParentAndChildThenGetAllReturnsBoth()
   {
      RoleMap childRoleMap = new RoleMap( roleMap );

      Foo foo = new Foo();
      Foo childFoo = new Foo();

      roleMap.set( foo );
      childRoleMap.set( childFoo );

      assertThat( childRoleMap.getAll( Foo.class ), equalTo( Arrays.asList(childFoo, foo )));
   }

   public static class Foo
   {

   }

   public static class Bar
      extends Foo
   {

   }
}
