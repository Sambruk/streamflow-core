/**
 *
 * Copyright 2009-2012 Streamsource AB
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
package se.streamsource.streamflow.client.domain.individual;

import org.junit.Test;
import org.qi4j.api.constraint.ConstraintViolationException;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.test.AbstractQi4jTest;

/**
 * Test of AccountSettingsValue
 */
public class AccountSettingsValueTest
      extends AbstractQi4jTest
{
   public void assemble( ModuleAssembly module ) throws AssemblyException
   {
      module.values( AccountSettingsValue.class );
      module.objects( getClass() );
   }

   @Test(expected = ConstraintViolationException.class)
   public void testDefaultValues()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.newInstance();
   }


   @Test(expected = ConstraintViolationException.class)
   public void testServerConstraintViolation()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.prototype().server().set( "###Illegal###" );
      settings.newInstance();

   }

   @Test
   public void testServer()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.prototype().server().set( "http://ec2-79-125-55-249.eu-west-1.compute.amazonaws.com:8040/streamflow" );
      settings.prototype().userName().set( "noinvisiblecharsallowed" );
      settings.prototype().password().set( "noinvisiblecharsallowed" );
      settings.newInstance();
   }


   @Test
   public void testUserName()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.prototype().userName().set( "mads.enevoldsen" );
   }

   @Test(expected = ConstraintViolationException.class)
   public void testUserNameSwedishCharConstraintViolation()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.prototype().userName().set( "börjebärnstråm" );
   }

   @Test(expected = ConstraintViolationException.class)
   public void testUserNameWhiteSpaceCharConstraintViolation()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.prototype().userName().set( "mads enevoldsen" );
   }

   @Test
   public void testPassword()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.prototype().password().set( "secret1!?(){}[]%&" );
   }


   @Test(expected = ConstraintViolationException.class)
   public void testPasswordShortConstraintViolation()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.prototype().password().set( "a" );
   }

   @Test(expected = ConstraintViolationException.class)
   public void testPasswordLongConstraintViolation()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.prototype().password().set( "thisislongerthanthirtycharacters" );
   }

   @Test(expected = ConstraintViolationException.class)
   public void testPasswordWhitspaceConstraintViolation()
   {
      objectBuilderFactory.newObjectBuilder( AccountSettingsValueTest.class ).injectTo( this );

      ValueBuilder<AccountSettingsValue> settings = valueBuilderFactory.newValueBuilder( AccountSettingsValue.class );
      settings.prototype().password().set( "my Secret" );
   }

}