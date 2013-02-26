/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
import ca.odell.glazedlists.TransactionList;
import ca.odell.glazedlists.UniqueList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.dci.value.table.CellValue;
import se.streamsource.dci.value.table.ColumnValue;
import se.streamsource.dci.value.table.RowValue;
import se.streamsource.dci.value.table.TableQuery;
import se.streamsource.dci.value.table.TableValue;
import se.streamsource.streamflow.api.administration.priority.PriorityValue;
import se.streamsource.streamflow.api.workspace.PerspectiveDTO;
import se.streamsource.streamflow.api.workspace.cases.CaseStates;
import se.streamsource.streamflow.client.ui.workspace.cases.CaseTableValue;
import se.streamsource.streamflow.client.util.LinkComparator;
import se.streamsource.streamflow.client.util.Refreshable;
import se.streamsource.streamflow.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Observable;

import static se.streamsource.streamflow.client.ui.workspace.WorkspaceResources.*;

/**
 * Base class for all models that list cases
 */
public class CasesTableModel extends Observable
      implements Refreshable
{
   @Structure
   protected Module module;

   @Uses
   protected CommandQueryClient client;

   BasicEventList<LinkValue> possibleLabels = new BasicEventList<LinkValue>();
   BasicEventList<LinkValue> possibleCaseTypes = new BasicEventList<LinkValue>();
   BasicEventList<LinkValue> possibleAssignees = new BasicEventList<LinkValue>();
   BasicEventList<LinkValue> possibleProjects = new BasicEventList<LinkValue>();
   BasicEventList<LinkValue> possibleCreatedBy = new BasicEventList<LinkValue>();

   List<String> selectedStatuses = new ArrayList<String>( Arrays.asList( OPEN.name(), ON_HOLD.name(), CLOSED.name() ));
   List<String> selectedCaseTypeIds = new ArrayList<String>();
   List<String> selectedLabelIds = new ArrayList<String>();
   List<String> selectedAssigneeIds = new ArrayList<String>();
   List<String> selectedProjectIds = new ArrayList<String>();
   List<String> selectedCreatedByIds = new ArrayList<String>();
   List<Integer> invisibleColumns = new ArrayList<Integer>();

   GroupBy groupBy = GroupBy.none;
   SortBy sortBy = SortBy.none;
   SortOrder sortOrder = SortOrder.asc;

   private PerspectivePeriodModel createdOnModel;
   private PerspectivePeriodModel dueOnModel;
   
   public CasesTableModel(@Structure Module module  )
   {
      this.module = module;
      createdOnModel = module.objectBuilderFactory().newObjectBuilder(PerspectivePeriodModel.class).use( Period.none ).newInstance();
      dueOnModel = module.objectBuilderFactory().newObjectBuilder(PerspectivePeriodModel.class).use( Period.none ).newInstance();
   }

   protected EventList<CaseTableValue> eventList = new TransactionList<CaseTableValue>(new BasicEventList<CaseTableValue>());

   public EventList<CaseTableValue> getEventList()
   {
      return eventList;
   }

   public void search( String text )
   {
      refresh();
   }

   public void refresh()
   {
      ValueBuilder<TableQuery> builder = module.valueBuilderFactory().newValueBuilder(TableQuery.class);
      String queryString = "select *";
      String whereClause = addWhereClauseFromFilter();
      String sorting = addSortingFromFilter();

      if( !Strings.empty( whereClause ) )
         queryString += " where " + whereClause;

      if( !Strings.empty( sorting ))
         queryString += sorting;

      builder.prototype().tq().set( queryString );
      TableQuery query = builder.newInstance();


      TableValue table = client.query( "cases", TableValue.class, query);
      List<CaseTableValue> caseTableValues = caseTableValues( table );

      eventList.getReadWriteLock().writeLock().lock();
      try
      {
         if (eventList instanceof TransactionList)
            ((TransactionList) eventList).beginEvent();

            eventList.clear();
            eventList.addAll( caseTableValues );

         if (eventList instanceof TransactionList)
            ((TransactionList) eventList).commitEvent();
      } finally
      {
         eventList.getReadWriteLock().writeLock().unlock();
      }

      setChanged();
      notifyObservers();
   }

   protected List<CaseTableValue> caseTableValues( TableValue table )
   {
      List<CaseTableValue> caseTableValues = new ArrayList<CaseTableValue>(  );
      for(RowValue row : table.rows().get())
      {
         ValueBuilder<CaseTableValue> caseBuilder = module.valueBuilderFactory().newValueBuilder(CaseTableValue.class);
         CaseTableValue prototype = caseBuilder.prototype();
         List<CellValue> cells = row.c().get();
         for (int i = 0; i < table.cols().get().size(); i++)
         {
            ColumnValue columnValue = table.cols().get().get( i );
            CellValue cell = cells.get( i );
            if (columnValue.id().get().equals("assigned"))
               prototype.assignedTo().set( cell.f().get() );
            else if (columnValue.id().get().equals("caseid"))
               prototype.caseId().set( cell.f().get() );
            else if (columnValue.id().get().equals("casetype"))
               prototype.caseType().set(cell.f().get());
            else if (columnValue.id().get().equals("creator"))
               prototype.createdBy().set(cell.f().get());
            else if (columnValue.id().get().equals("created"))
               prototype.creationDate().set((Date) cell.v().get());
            else if (columnValue.id().get().equals("due"))
               prototype.dueOn().set((Date) cell.v().get());
            else if (columnValue.id().get().equals("description"))
               prototype.description().set(cell.f().get());
            else if (columnValue.id().get().equals("hasattachments"))
               prototype.hasAttachments().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("hascontacts"))
               prototype.hasContacts().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("hasconversations"))
               prototype.hasConversations().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("hasunreadconversation"))
               prototype.hasUnreadConversation().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("hassubmittedforms"))
               prototype.hasSubmittedForms().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("hasunreadform"))
               prototype.hasUnreadForm().set((Boolean) cell.v().get());
            else if (columnValue.id().get().equals("labels"))
            {
               String json = cell.v().get().toString();
               prototype.labels().set(module.valueBuilderFactory().newValueFromJSON(LinksValue.class, json));
            }
            else if (columnValue.id().get().equals("owner"))
               prototype.owner().set(cell.f().get());
            else if (columnValue.id().get().equals("parent") && cell.v().get() != null)
               prototype.parentCase().set(module.valueBuilderFactory().newValueFromJSON(LinkValue.class, cell.v().get().toString()));
            else if (columnValue.id().get().equals("resolution"))
               prototype.resolution().set(cell.f().get());
            else if (columnValue.id().get().equals("status"))
               prototype.status().set( CaseStates.valueOf( cell.v().get().toString() ));
            else if (columnValue.id().get().equals("subcases"))
               prototype.subcases().set( module.valueBuilderFactory().newValueFromJSON(LinksValue.class, cell.v().get().toString()) );
            else if (columnValue.id().get().equals( "href" ))
               prototype.href().set( cell.v().get().toString() );
            else if( columnValue.id().get().equals( "removed" ))
               prototype.removed().set( (Boolean)cell.v().get() );
            else if( columnValue.id().get().equals( "priority" ) && cell.v().get() != null )
               prototype.priority().set( module.valueBuilderFactory().newValueFromJSON(PriorityValue.class, cell.v().get().toString()) );
            else if (columnValue.id().get().equals("unread"))
               prototype.unread().set((Boolean) cell.v().get());
         }
         caseTableValues.add(caseBuilder.newInstance());
      }
      return caseTableValues;
   }

   public EventList<LinkValue> getPossibleLabels()
   {
      LinksValue labels = client.query( "possiblelabels",
            LinksValue.class );
      possibleLabels.clear();
      possibleLabels.addAll(labels.links().get());
      return new UniqueList<LinkValue>( possibleLabels, new LinkComparator() );
   }

   public BasicEventList<LinkValue> getPossibleCaseTypes()
   {
      LinksValue caseTypes = client.query( "possiblecasetypes",
            LinksValue.class );
      possibleCaseTypes.clear();
      possibleCaseTypes.addAll(caseTypes.links().get());
      return possibleCaseTypes;
   }

   public BasicEventList<LinkValue> getPossibleAssignees()
   {
      LinksValue assignees = client.query( "possibleassignees",
            LinksValue.class );
      possibleAssignees.clear();
      possibleAssignees.addAll(assignees.links().get());
      return possibleAssignees;
   }

   public BasicEventList<LinkValue> getPossibleProjects()
   {
      LinksValue projects = client.query( "possibleprojects",
            LinksValue.class );
      possibleProjects.clear();
      possibleProjects.addAll((Collection)projects.links().get());
      return possibleProjects;
   }

   public BasicEventList<LinkValue> getPossibleCreatedBy()
   {
      LinksValue createdby = client.query( "possiblecreatedby",
            LinksValue.class );
      possibleCreatedBy.clear();
      possibleCreatedBy.addAll(createdby.links().get());
      return possibleCreatedBy;
   }

   public List<String> getSelectedStatuses()
   {
      return selectedStatuses;
   }

   public List<String> getSelectedCaseTypes()
   {
      return selectedDescriptions( selectedCaseTypeIds, possibleCaseTypes );
   }

   public List<String> getSelectedCaseTypeIds()
   {
      return selectedCaseTypeIds;
   }

   public List<String> getSelectedLabels()
   {
      return selectedDescriptions( selectedLabelIds, possibleLabels );
   }

   public List<String> getSelectedLabelIds()
   {
      return selectedLabelIds;
   }

   public List<String> getSelectedAssignees()
   {
      return selectedDescriptions( selectedAssigneeIds, possibleAssignees );
   }

   public List<String> getSelectedAssigneeIds()
   {
      return selectedAssigneeIds;
   }

   public List<String> getSelectedProjects()
   {
      return selectedDescriptions( selectedProjectIds, possibleProjects );
   }

   public List<String> getSelectedProjectIds()
   {
      return selectedProjectIds;
   }

   public List<String> getSelectedCreatedBy()
   {
      return selectedDescriptions( selectedCreatedByIds, possibleCreatedBy );
   }

   public List<String> getSelectedCreatedByIds()
   {
      return selectedCreatedByIds;
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

   public PerspectivePeriodModel getCreatedOnModel()
   {
      return createdOnModel;
   }

   public PerspectivePeriodModel getDueOnModel()
   {
      return dueOnModel;
   }

   public PerspectiveDTO getPerspective(String name, String query)
   {
      ValueBuilder<PerspectiveDTO> builder = module.valueBuilderFactory().newValueBuilder(PerspectiveDTO.class);
      builder.prototype().query().set( query );
      builder.prototype().name().set( name );
      builder.prototype().labels().set( selectedLabelIds );
      builder.prototype().statuses().set( selectedStatuses );
      builder.prototype().sortBy().set( sortBy.name() );
      builder.prototype().sortOrder().set( sortOrder.name() );
      builder.prototype().groupBy().set( groupBy.name() );
      builder.prototype().assignees().set( selectedAssigneeIds );
      builder.prototype().caseTypes().set( selectedCaseTypeIds );
      builder.prototype().createdBy().set( selectedCreatedByIds );
      builder.prototype().projects().set( selectedProjectIds );
      builder.prototype().createdOnPeriod().set( createdOnModel.getPeriod().name() );
      builder.prototype().createdOn().set( createdOnModel.getDate() );
      builder.prototype().dueOnPeriod().set( dueOnModel.getPeriod().name() );
      builder.prototype().dueOn().set( dueOnModel.getDate() );
      //TODO Can refernce be made relative
      builder.prototype().context().set( client.getReference().toString() );
      builder.prototype().invisibleColumns().set( invisibleColumns );

      return builder.newInstance();
   }

   public void clearFilter()
   {
      selectedStatuses = new ArrayList<String>();
      selectedCaseTypeIds = new ArrayList<String>();
      selectedLabelIds = new ArrayList<String>();
      selectedAssigneeIds = new ArrayList<String>();
      selectedProjectIds = new ArrayList<String>();
      selectedCreatedByIds = new ArrayList<String>();
      invisibleColumns = new ArrayList<Integer>();

      groupBy = GroupBy.none;
      sortBy = SortBy.none;
      sortOrder = SortOrder.asc;

      createdOnModel.setDate( null );
      createdOnModel.setPeriod( Period.none );

      dueOnModel.setDate( null );
      dueOnModel.setPeriod( Period.none );
   }

   public List<LinkValue> possibleFilterLinks()
   {
      return client.query().queries().get();
   }

   public void setFilter(PerspectiveDTO perspectiveDTO)
   {
      perspectiveDTO = module.valueBuilderFactory().newValueBuilder(PerspectiveDTO.class).withPrototype(perspectiveDTO).prototype();
      selectedStatuses = perspectiveDTO.statuses().get();
      selectedCaseTypeIds = perspectiveDTO.caseTypes().get();
      selectedLabelIds = perspectiveDTO.labels().get();
      selectedAssigneeIds = perspectiveDTO.assignees().get();
      selectedProjectIds = perspectiveDTO.projects().get();
      selectedCreatedByIds = perspectiveDTO.createdBy().get();
      sortBy = SortBy.valueOf( perspectiveDTO.sortBy().get() );
      sortOrder = SortOrder.valueOf( perspectiveDTO.sortOrder().get() );
      groupBy = GroupBy.valueOf( perspectiveDTO.groupBy().get() );
      createdOnModel.setPeriod( Period.valueOf( perspectiveDTO.createdOnPeriod().get() ) );
      createdOnModel.setDate( perspectiveDTO.createdOn().get() );
      dueOnModel.setPeriod( Period.valueOf( perspectiveDTO.dueOnPeriod().get() ) );
      dueOnModel.setDate( perspectiveDTO.dueOn().get() );
      invisibleColumns = perspectiveDTO.invisibleColumns().get();
   }

   protected String addSortingFromFilter()
   {
       String sort = "";
      if (sortBy != SortBy.none)
      {
         sort = " order by " + sortBy.name() + " " + sortOrder.name();
      }
      return sort;
   }

   protected String addWhereClauseFromFilter()
   {
      String filter = "";

      if (!selectedStatuses.isEmpty())
      {
         filter += " status:";
         String comma = "";
         for ( String status : selectedStatuses )
         {
            filter += comma + status;
            comma = ",";
         }
      }

      if (!selectedCaseTypeIds.isEmpty())
      {
         filter += " caseType:\"";
         String comma = "";
         for (String caseType : selectedCaseTypeIds)
         {
            filter += comma + caseType;
            comma = ",";
         }
         filter +=  "\"";
      }

      if (!selectedLabelIds.isEmpty())
      {
         filter += " label:\"";
         String comma = "";
         for (String label : selectedLabelIds)
         {
            filter += comma + label;
            comma = ",";
         }
         filter +=  "\"";
      }

      if (!selectedAssigneeIds.isEmpty())
      {
         filter += " assignedTo:\"";
         String comma = "";
         for (String assignee : selectedAssigneeIds)
         {
            filter += comma + assignee;
            comma = ",";
         }
         filter +=  "\"";
      }

      if (!selectedProjectIds.isEmpty())
      {
         filter += " project:\"";
         String comma = "";
         for (String project : selectedProjectIds)
         {
            filter += comma + project;
            comma = ",";
         }
         filter +=  "\"";
      }

      if (!selectedCreatedByIds.isEmpty())
      {
         filter += " createdBy:\"";
         String comma = "";
         for (String createdBy : selectedCreatedByIds)
         {
            filter += comma + createdBy;
            comma = ",";
         }
         filter +=  "\"";
      }

      if ( !Period.none.equals( createdOnModel.getPeriod() ) )
      {
         filter += " createdOn:" + createdOnModel.getSearchValue( "yyyyMMdd", "-" );
      }

      if( !Period.none.equals( dueOnModel.getPeriod() ))
      {
         filter += " dueOn:" + dueOnModel.getSearchValue( "yyyyMMdd", "-" );
      }
      return filter;
   }

   private List<String> selectedDescriptions( List<String> selected, List<LinkValue> baseList )
   {
      List<String> descriptions = new ArrayList<String>();
      for(String id : selected )
      {
         for( LinkValue link : baseList )
         {
            if( link.id().get().equals( id ))
               descriptions.add( link.text().get() );
         }
      }
      return descriptions;
   }

   public List<Integer> getInvisibleColumns()
   {
      return invisibleColumns;
   }

   public void addInvisibleColumn( Integer index )
   {
      if( !invisibleColumns.contains( index ) )
      {
         invisibleColumns.add( index  );
      }
   }

   public void removeInvisibleColumn( Integer index )
   {
      invisibleColumns.remove( index );
   }

   public void createCase()
   {
      client.postCommand( "createcase" );
   }

   public boolean isCreateCaseEnabled()
   {
      return client.getReference().getLastSegment().equals("assignments") || client.getReference().getLastSegment().equals("drafts");
   }

   public boolean containsCaseWithPriority()
   {

      for(CaseTableValue value : getEventList() )
      {
         if( value.priority().get() != null )
         {
            return true;
         }
      }
      return false;
   }
}