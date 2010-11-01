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

package se.streamsource.streamflow.client.ui.workspace.cases.actions;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.TitledLinkValue;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.domain.interaction.gtd.Actions;
import se.streamsource.streamflow.resource.caze.CaseVisitorConfigValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * JAVADOC
 */
public class CaseActionsModel
   implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   private CommandQueryClient client;

   private TransactionList<String> actionList = new TransactionList<String>(new BasicEventList<String>( ));

   public void refresh()
   {
      // Get action list
      Actions actions = client.query( "actions", Actions.class );
      EventListSynch.synchronize( actions.actions().get(), actionList );
   }

   public EventList<String> getActionList()
   {
      return actionList;
   }

   public EventList<TitledLinkValue> getPossibleProjects()
   {
      BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

      LinksValue linksValue = client.query( "possiblesendto", LinksValue.class );
      list.addAll( (Collection) linksValue.links().get() );

      return list;
   }

   public EventList<TitledLinkValue> getPossibleResolutions()
   {
      BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

      LinksValue linksValue = client.query( "possibleresolutions", LinksValue.class );
      list.addAll( (Collection) linksValue.links().get() );

      return list;
   }

   // Actions
   public void open()
   {
      client.postCommand( "open" );
   }

   public void assignToMe()
   {
      client.postCommand( "assign" );
   }

   public void close()
   {
      client.postCommand( "close" );
   }

   public void delete()
   {
      client.delete();
   }


   public void sendTo( LinkValue linkValue)
   {
      client.postLink( linkValue );
   }

   public void reopen()
   {
      client.postCommand( "reopen" );
   }

   public void unassign()
   {
      client.postCommand( "unassign" );
   }

   public void onHold()
   {
      client.postCommand( "onhold" );
   }

   public void resume()
   {
      client.postCommand( "resume" );
   }

   public void resolve( LinkValue linkValue )
   {
      client.postLink( linkValue );
   }

   public InputStream print( CaseVisitorConfigValue config ) throws IOException
   {
      return client.queryStream( "exportpdf", config );
   }
}
