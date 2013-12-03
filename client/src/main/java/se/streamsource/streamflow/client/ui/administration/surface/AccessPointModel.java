/**
 *
 * Copyright 2009-2013 Jayway Products AB
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
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.ResourceValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.surface.AccessPointDTO;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;
import se.streamsource.streamflow.client.ui.workspace.cases.general.CaseLabelsModel;
import se.streamsource.streamflow.client.util.Refreshable;

import java.util.Observable;


public class AccessPointModel extends Observable
      implements Refreshable
{
   CaseLabelsModel labelsModel;

   @Structure
   Module module;

   @Uses
   private CommandQueryClient client;

   private AccessPointDTO accessPoint;
   private ResourceValue resourceValue;

   public AccessPointModel(@Uses CommandQueryClient client, @Structure Module module)
   {
      this.client = client;
      labelsModel = module.objectBuilderFactory().newObjectBuilder(CaseLabelsModel.class).use(client.getSubClient( "labels" )).newInstance();
   }

   public AccessPointDTO getAccessPointValue()
   {
      return accessPoint;
   }

   public void refresh() throws OperationException
   {
      resourceValue = client.query();
      accessPoint = (AccessPointDTO) resourceValue.index().get().buildWith().prototype();

      setChanged();
      notifyObservers( resourceValue );
   }

   public Object getPossibleProjects()
   {
      BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

      LinksValue listValue = client.query( "possibleprojects",
            LinksValue.class );
      list.addAll( listValue.links().get() );

      return list;
   }

   public void changeProject(LinkValue link)
   {
      client.postLink( link );
   }

   public EventList<LinkValue> getPossibleCaseTypes()
   {
      BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

      LinksValue listValue = client.query( "possiblecasetypes",
            LinksValue.class );
      list.addAll( listValue.links().get() );

      return list;
   }

   public void changeCaseType(LinkValue link)
   {
      client.postLink( link );
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

   public void changeForm(LinkValue link)
   {
      client.postLink( link );
   }

   public EventList<LinkValue> getPossibleLabels()
   {
      BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

      LinksValue listValue = client.query( "possiblelabels",
            LinksValue.class );
      list.addAll( listValue.links().get() );

      return list;
   }

   public CaseLabelsModel getLabelsModel()
   {
      return labelsModel;
   }

   public EventList<LinkValue> getPossibleTemplates()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         Form form = new Form();
         form.add( "filteron", "pdf" );

         LinksValue listValue = client.query( "possibleformtemplates",
               LinksValue.class, form );
         list.addAll( listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public void setTemplate( LinkValue link)
   {
      if( link != null )
      {
         client.postLink( link );
      } else
      {
         ValueBuilder<EntityValue> builder = module.valueBuilderFactory().newValueBuilder(EntityValue.class);
         client.postCommand( "setformtemplate", builder.newInstance() );
      }
   }

   public void changeMailSelectionMessage( String message )
   {
      Form form = new Form( );
      form.set( "mailmessage", message );
      client.postCommand( "changemailselectionmessage", form );
   }

   public void setSignActive1( boolean selected )
   {
      if( !accessPoint.primarysign().get().active().get().equals( selected ) )
      {
         Form form = new Form( );
         form.set( "active", "" + selected );

         client.postCommand( "updateprimarysignactive", form );
      }
   }

   public void setSignName1( String text )
   {
      if( !text.equals( accessPoint.primarysign().get().name().get() ))
      {
         Form form = new Form( );
         form.set( "name", text );

         client.postCommand( "updateprimarysign", form );
      }
   }

   public void setSignDescription1( String text )
   {
      if( !text.equals( accessPoint.primarysign().get().name().get() ))
      {
         Form form = new Form( );
         form.set( "description", text );

         client.postCommand( "updateprimarysign", form );
      }
   }

   public void setSignActive2( boolean selected )
   {
      if( !accessPoint.secondarysign().get().active().get().equals( selected ) )
      {
         Form form = new Form( );
         form.set( "active", "" + selected );

         client.postCommand( "updatesecondarysignactive", form );
      }
   }

   public void setSignName2( String text )
   {
      if( !text.equals( accessPoint.secondarysign().get().name().get() ))
      {
         Form form = new Form( );
         form.set( "name", text );

         client.postCommand( "updatesecondarysign", form );
      }
   }

   public void setSignDescription2( String text )
   {
      if( !text.equals( accessPoint.secondarysign().get().name().get() ))
      {
         Form form = new Form( );
         form.set( "description", text );

         client.postCommand( "updatesecondarysign", form );
      }
   }

   public void changeSecondForm( LinkValue selectedLink )
   {
      Form form = new Form();
      form.set( "formid", selectedLink.id().get() );
      form.set( "formdescription", selectedLink.text().get() );

      client.postCommand( "updatesecondarysign", form );
   }

   public void setSecondMandatory( boolean selected )
   {
      if( !accessPoint.secondarysign().get().mandatory().get().equals( selected ) )
      {
         Form form = new Form( );
         form.set( "mandatory", "" + selected );

         client.postCommand( "updatesecondarysign", form );
      }
   }

   public void setQuestion( String text )
   {
      if( !text.equals( accessPoint.secondarysign().get().question().get() ))
      {
         Form form = new Form( );
         form.set( "question", text );

         client.postCommand( "updatesecondarysign", form );
      }
   }

   public EventList<LinkValue> getPossibleSecondForms()
   {
      try
      {
         BasicEventList<LinkValue> list = new BasicEventList<LinkValue>();

         LinksValue listValue = client.query( "possiblesecondforms",
               LinksValue.class );
         list.addAll( listValue.links().get() );

         return list;
      } catch (ResourceException e)
      {
         throw new OperationException( WorkspaceResources.could_not_refresh,
               e );
      }
   }

   public void changeSubject(String text)
   {
      Form form = new Form();
      form.set("subject", text);
      client.postCommand("changesubject", form.getWebRepresentation());
   }

   public void updateTemplate(String key, String template)
   {
      Form form = new Form();
      form.set("key", key);
      form.set("template", template);
      client.postCommand("changetemplate", form.getWebRepresentation());
   }
}
