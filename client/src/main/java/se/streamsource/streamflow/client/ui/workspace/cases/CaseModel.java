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

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.io.Inputs;
import org.qi4j.api.io.Outputs;
import org.restlet.representation.Representation;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.link.TitledLinkValue;
import se.streamsource.streamflow.api.workspace.cases.CaseDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseOutputConfigDTO;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.util.EventListSynch;

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

   public File print( CaseOutputConfigDTO config ) throws IOException
   {
      Representation representation = client.queryRepresentation( "exportpdf", config );

      String name = representation.getDisposition().getFilename();
      String[] fileNameParts = name.split( "\\." );
      File file = File.createTempFile( fileNameParts[0] + "_", "." + fileNameParts[1] );

      Inputs.byteBuffer( representation.getStream(), 1024 ).transferTo( Outputs.byteBuffer( file ) );

      return file;
   }
}