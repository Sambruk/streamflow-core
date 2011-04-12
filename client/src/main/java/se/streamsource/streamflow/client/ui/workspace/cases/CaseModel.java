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

package se.streamsource.streamflow.client.ui.workspace.cases;

import ca.odell.glazedlists.*;
import org.qi4j.api.io.*;
import org.restlet.representation.*;
import se.streamsource.dci.value.link.*;
import se.streamsource.streamflow.client.*;
import se.streamsource.streamflow.client.util.*;
import se.streamsource.streamflow.resource.caze.*;

import java.io.*;
import java.util.*;

/**
 * Model for the info and actions on a case.
 */
public class CaseModel
   extends ResourceModel<CaseDTO>
{
   private TransactionList<LinkValue> subcases = new TransactionList<LinkValue>( new BasicEventList<LinkValue>() );

   public void refresh()
   {
      super.refresh();

      CaseDTO caseDTO = getIndex();

      EventListSynch.synchronize( caseDTO.subcases().get().links().get(), subcases );
   }

   public TransactionList<LinkValue> getSubcases()
   {
      return subcases;
   }

   // Commands and queries
   public void createSubCase()
   {
      client.command( "createsubcase" );
   }

   public void open()
   {
      client.command( "open" );
   }

   public void assignToMe()
   {
      client.command( "assign" );
   }

   public void close()
   {
      client.command( "close" );
   }

   public void delete()
   {
      client.delete();
   }

   public EventList<TitledLinkValue> getPossibleSendTo()
   {
      BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

      LinksValue linksValue = client.query( "possiblesendto", LinksValue.class );
      list.addAll( (Collection) linksValue.links().get() );

      return list;
   }

   public void sendTo( LinkValue linkValue )
   {
      client.postLink( linkValue );
   }

   public void reopen()
   {
      client.command( "reopen" );
   }

   public void unassign()
   {
      client.command( "unassign" );
   }

   public void onHold()
   {
      client.command( "onhold" );
   }

   public void resume()
   {
      client.command( "resume" );
   }

   public EventList<TitledLinkValue> getPossibleResolutions()
   {
      BasicEventList<TitledLinkValue> list = new BasicEventList<TitledLinkValue>();

      LinksValue linksValue = client.query( "possibleresolutions", LinksValue.class );
      list.addAll( (Collection) linksValue.links().get() );

      return list;
   }

   public void resolve( LinkValue linkValue )
   {
      client.postLink( linkValue );
   }

   public File print( CaseOutputConfigValue config ) throws IOException
   {
      Representation representation = client.queryRepresentation( "exportpdf", config );

      String name = representation.getDisposition().getFilename();
      String[] fileNameParts = name.split( "\\." );
      File file = File.createTempFile( fileNameParts[0] + "_", "." + fileNameParts[1] );

      Inputs.byteBuffer( representation.getStream(), 1024 ).transferTo( Outputs.byteBuffer( file ) );

      return file;
   }
}