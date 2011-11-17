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

package se.streamsource.streamflow.client.ui.administration.casesettings;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.client.ResourceModel;

/**
 * JAVADOC
 */
public class FormOnCloseModel
      extends ResourceModel<LinkValue>
{
   @Structure
   Module module;

   public EventList<LinkValue> getPossibleForms()
   {
      BasicEventList<LinkValue> possibleLinks = new BasicEventList<LinkValue>();
      //create an empty link for update with null
      ValueBuilder<TitledLinkValue> builder = module.valueBuilderFactory().newValueBuilder( TitledLinkValue.class );
      builder.prototype().title().set( " " );
      builder.prototype().href().set( "update?entity=null" );
      builder.prototype().text().set( " " );
      builder.prototype().id().set( "NA" );
      possibleLinks.add( builder.newInstance() );
      // add real links to forms
      possibleLinks.addAll( client.query( "possibleforms", LinksValue.class ).links().get() );
      return possibleLinks;
   }

   public void changeFormOnClose( LinkValue selectedLink )
   {
      client.postLink( selectedLink );
   }
}
