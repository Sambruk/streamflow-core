/**
 *
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

package se.streamsource.streamflow.client.ui.workspace.table;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.UniqueList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.client.util.LinkComparator;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.resource.user.profile.PerspectiveValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

public class PerspectiveModel extends Observable implements Refreshable
{
   @Uses
   CommandQueryClient client;

   @Structure
   ValueBuilderFactory vbf;

   BasicEventList<LinkValue> possibleLabels = new BasicEventList<LinkValue>();
   List<String> selectedLabels = new ArrayList<String>();

   List<String> selectedStatuses = new ArrayList<String>( Arrays.asList( OPEN.name() ) );

   GroupBy groupBy = GroupBy.none;
   SortBy sortBy = SortBy.none;
   SortOrder sortOrder = SortOrder.asc;

   public void refresh()
   {
      LinksValue listValue = client.query( "possiblelabels",
            LinksValue.class );
      possibleLabels.addAll( listValue.links().get() );
   }

   public EventList<LinkValue> getPossibleLabels()
   {
      return new UniqueList<LinkValue>( possibleLabels, new LinkComparator() );
   }

   public List<String> getSelectedLabels()
   {
      return selectedLabels;
   }

   public List<String> getSelectedStatuses()
   {
      return selectedStatuses;
   }

   public void setSelectedLabels( List<String> selectedLabels )
   {
      this.selectedLabels.clear();
      this.selectedLabels.addAll( selectedLabels );
   }

   public void setSelectedStatuses( List<String> selectedStatuses )
   {
      this.selectedStatuses.clear();
      this.selectedStatuses.addAll( selectedStatuses );
   }

   @Override
   public void notifyObservers()
   {
      setChanged();
      super.notifyObservers();
   }

   public PerspectiveValue getPerspective( String name, String query )
   {
      ValueBuilder<PerspectiveValue> builder = vbf.newValueBuilder( PerspectiveValue.class );
      builder.prototype().query().set( query );
      builder.prototype().name().set( name );
      builder.prototype().labels().set( getSelectedLabels() );
      builder.prototype().statuses().set( getSelectedStatuses() );
      builder.prototype().sortBy().set( getSortBy().name() );
      builder.prototype().sortOrder().set( getSortOrder().name() );
      builder.prototype().groupBy().set( getGroupBy().name() );

      return builder.newInstance();
   }

   public void savePerspective( String name, String query )
   {
      client.getClient( "../perspectives/" ).postCommand( "createperspective", getPerspective( name, query ) );
   }

   public GroupBy getGroupBy()
   {
      return groupBy;
   }

   public void setGroupBy( GroupBy groupBy )
   {
      this.groupBy = groupBy;
   }

   public SortBy getSortBy()
   {
      return sortBy;
   }

   public void setSortBy( SortBy sortBy )
   {
      this.sortBy = sortBy;
   }

   public SortOrder getSortOrder()
   {
      return sortOrder;
   }

   public void setSortOrder( SortOrder sortOrder )
   {
      this.sortOrder = sortOrder;
   }
}
