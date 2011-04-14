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

package se.streamsource.streamflow.client.ui.administration.forms.definition;

import org.qi4j.api.injection.scope.*;
import org.qi4j.api.value.*;
import org.restlet.resource.*;
import se.streamsource.dci.restlet.client.*;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.api.administration.form.PageDefinitionValue;
import se.streamsource.streamflow.client.util.Refreshable;

/**
 * JAVADOC
 */
public class PageEditModel
      implements Refreshable
{
   @Uses
   private CommandQueryClient client;

   @Structure
   private ValueBuilderFactory vbf;

   private PageDefinitionValue page;

   public void refresh()
   {
      page = (PageDefinitionValue) client.query( "index", PageDefinitionValue.class ).buildWith().prototype();
   }

   public void changeDescription( String pageName ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( pageName );

      client.putCommand( "changedescription", builder.newInstance() );
   }

   public PageDefinitionValue getPageDefinition()
   {
      return page;
   }

   public void move( String direction ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( direction );


      client.postCommand( "move", builder.newInstance() );
   }

   public void remove() throws ResourceException
   {
      client.delete();
   }
}