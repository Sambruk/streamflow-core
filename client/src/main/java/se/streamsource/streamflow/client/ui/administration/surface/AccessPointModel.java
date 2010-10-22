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

package se.streamsource.streamflow.client.ui.administration.surface;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.LinkValue;
import se.streamsource.dci.value.LinksValue;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.caze.CaseLabelsModel;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.infrastructure.application.AccessPointValue;

import java.util.Observable;


public class AccessPointModel extends Observable
      implements Refreshable
{
   @Structure
   ValueBuilderFactory vbf;

   @Uses
   CaseLabelsModel caseLabelsModel;

   CommandQueryClient client;

   private AccessPointValue accessPoint;

   public AccessPointModel( @Uses CommandQueryClient client )
   {
      this.client = client;
   }

   public void refresh() throws OperationException
   {
      AccessPointValue updatedValue = client.query( "index", AccessPointValue.class );
      accessPoint = (AccessPointValue) updatedValue.buildWith().prototype();
// TODO      caseLabelsModel.setLabels( accessPoint.labels().get() );

      setChanged();
      notifyObservers();
   }

   public void setProject( String id )
   {
      client.postCommand( "setproject", getStringValue( id ) );
   }

   public void setCaseType( String id )
   {
      client.postCommand( "setcasetype", getStringValue( id ) );
   }

   private StringValue getStringValue( String id )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( id );
      return builder.newInstance();
   }

   public Object getPossibleProjects()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         LinksValue listValue = client.query( "possibleprojects",
               LinksValue.class );
         list.addAll( listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public EventList<LinkValue> getPossibleCaseTypes()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         LinksValue listValue = client.query( "possiblecasetypes",
               LinksValue.class );
         list.addAll( listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public EventList<LinkValue> getPossibleLabels()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         LinksValue listValue = client.query( "possiblelabels",
               LinksValue.class );
         list.addAll( listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public AccessPointValue getAccessPointValue()
   {
      return accessPoint;
   }

   public CaseLabelsModel labelsModel()
   {
//      caseLabelsModel.setLabels( accessPoint.labels().get() );
      return caseLabelsModel;
   }

   public EventList<LinkValue> getPossibleForms()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         LinksValue listValue = client.query( "possibleforms",
               LinksValue.class );
         list.addAll( listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public void setForm( String id )
   {
      client.postCommand( "setform", getStringValue( id ) );
   }

   public EventList<LinkValue> getPossibleTemplates()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         LinksValue listValue = client.getSubClient( "template" ).query( "possibletemplates",
               getStringValue( "pdf" ), LinksValue.class );
         list.addAll( listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public void setTemplate( String id )
   {
      ValueBuilder<EntityValue> builder = vbf.newValueBuilder( EntityValue.class );
      builder.prototype().entity().set( id );
      client.getSubClient( "template" ).postCommand( "settemplate", builder.newInstance() );
   }

   public void removeTemplate()
   {
      ValueBuilder<EntityValue> builder = vbf.newValueBuilder( EntityValue.class );
      client.getSubClient( "template" ).postCommand( "settemplate", builder.newInstance() );
   }
}
