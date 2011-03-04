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

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.OPEN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

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
import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.UniqueList;

public class PerspectiveModel extends Observable implements Refreshable
{
   @Uses
   CommandQueryClient client;
   
   @Structure
   ValueBuilderFactory vbf;
   
   BasicEventList<LinkValue> possibleLabels = new BasicEventList<LinkValue>();
   BasicEventList<LinkValue> possibleCaseTypes = new BasicEventList<LinkValue>();
   BasicEventList<LinkValue> possibleAssignees = new BasicEventList<LinkValue>();
   BasicEventList<LinkValue> possibleProjects = new BasicEventList<LinkValue>();
   BasicEventList<LinkValue> possibleCreatedBy = new BasicEventList<LinkValue>();

   List<String> selectedStatuses = new ArrayList<String>(Arrays.asList(OPEN.name()));
   List<String> selectedCaseTypes = new ArrayList<String>();
   List<String> selectedLabels = new ArrayList<String>();
   List<String> selectedAssignees = new ArrayList<String>();
   List<String> selectedProjects = new ArrayList<String>();
   List<String> selectedCreatedBy = new ArrayList<String>();
   
   GroupBy groupBy = GroupBy.none;
   SortBy sortBy = SortBy.none;
   SortOrder sortOrder = SortOrder.asc;
   
   String createdOn = "";
   
   public void refresh()
   {
      LinksValue labels = client.query( "possiblelabels",
            LinksValue.class );
      possibleLabels.clear();
      possibleLabels.addAll(labels.links().get());

      LinksValue caseTypes = client.query( "possiblecasetypes",
            LinksValue.class );
      possibleCaseTypes.clear();
      possibleCaseTypes.addAll(caseTypes.links().get());

      LinksValue assignees = client.query( "possibleassignees",
            LinksValue.class );
      possibleAssignees.clear();
      possibleAssignees.addAll(assignees.links().get());

      LinksValue projects = client.query( "possibleprojects",
            LinksValue.class );
      possibleProjects.clear();
      possibleProjects.addAll(projects.links().get());

      LinksValue createdby = client.query( "possiblecreatedby",
            LinksValue.class );
      possibleCreatedBy.clear();
      possibleCreatedBy.addAll(createdby.links().get());
   }

   public EventList<LinkValue> getPossibleLabels()
   {
      return new UniqueList<LinkValue>(possibleLabels, new LinkComparator());
   }

   public BasicEventList<LinkValue> getPossibleCaseTypes()
   {
      return possibleCaseTypes;
   }

   public BasicEventList<LinkValue> getPossibleAssignees()
   {
      return possibleAssignees;
   }
   
   public BasicEventList<LinkValue> getPossibleProjects()
   {
      return possibleProjects;
   }
   
   public BasicEventList<LinkValue> getPossibleCreatedBy()
   {
      return possibleCreatedBy;
   }
   
   public List<String> getSelectedStatuses()
   {
      return selectedStatuses;
   }

   public List<String> getSelectedCaseTypes()
   {
      return selectedCaseTypes;
   }

   public List<String> getSelectedLabels()
   {
      return selectedLabels;
   }

   public List<String> getSelectedAssignees()
   {
      return selectedAssignees;
   }
   
   public List<String> getSelectedProjects()
   {
      return selectedProjects;
   }
   
   public List<String> getSelectedCreatedBy()
   {
      return selectedCreatedBy;
   }

   public void setSelectedStatuses(List<String> selectedStatuses)
   {
      this.selectedStatuses.clear();
      this.selectedStatuses.addAll(selectedStatuses);
   }
   
   public void setSelectedCaseTypes(List<String> selectedCaseTypes)
   {
      this.selectedCaseTypes.clear();
      this.selectedCaseTypes.addAll(selectedCaseTypes);
   }
   
   public void setSelectedLabels(List<String> selectedLabels)
   {
      this.selectedLabels.clear();
      this.selectedLabels.addAll(selectedLabels);
   }
   
   public void setSelectedAssigness(List<String> selectedAssigness)
   {
      this.selectedAssignees.clear();
      this.selectedAssignees.addAll(selectedAssigness);
   }
   
   public void setSelectedProjects(List<String> selectedProjects)
   {
      this.selectedProjects.clear();
      this.selectedProjects.addAll(selectedProjects);
   }
   
   public void setSelectedCreatedBy(List<String> selectedCreatedBy)
   {
      this.selectedCreatedBy.clear();
      this.selectedCreatedBy.addAll(selectedCreatedBy);
   }
   
   
   public GroupBy getGroupBy()
   {
      return groupBy;
   }

   public void setGroupBy(GroupBy groupBy)
   {
      this.groupBy = groupBy;
   }

   public SortBy getSortBy()
   {
      return sortBy;
   }

   public void setSortBy(SortBy sortBy)
   {
      this.sortBy = sortBy;
   }

   public SortOrder getSortOrder()
   {
      return sortOrder;
   }

   public void setSortOrder(SortOrder sortOrder)
   {
      this.sortOrder = sortOrder;
   }
   
   public PerspectiveValue createPerspective(String name)
   {
      ValueBuilder<PerspectiveValue> builder = vbf.newValueBuilder(PerspectiveValue.class);
      builder.prototype().query().set("Test");
      builder.prototype().name().set(name);
      builder.prototype().statuses().set(getSelectedStatuses());
      builder.prototype().labels().set(getSelectedLabels());
      builder.prototype().caseTypes().set(getSelectedCaseTypes());
      builder.prototype().assignees().set(getSelectedAssignees());
      builder.prototype().projects().set(getSelectedProjects());
      builder.prototype().createdBy().set(getSelectedCreatedBy());
      builder.prototype().sortBy().set(getSortBy().name());
      builder.prototype().sortOrder().set(getSortOrder().name());
      builder.prototype().groupBy().set(getGroupBy().name());
      
      return builder.newInstance();
   }
   
   public void savePerspective(String name)
   {
      client.postCommand("createperspective", createPerspective(name));
   }

   @Override
   public void notifyObservers()
   {
      setChanged();
      super.notifyObservers();
   }

   public String getCreatedOn()
   {
      return createdOn;
   }

   public void setCreatedOn(String createdOn)
   {
      this.createdOn = createdOn;
   }
}
