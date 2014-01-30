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
package se.streamsource.streamflow.client.ui.administration.forms.definition;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TransactionList;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.structure.Module;
import org.qi4j.api.value.ValueBuilder;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.value.EntityValue;
import se.streamsource.dci.value.link.LinkValue;
import se.streamsource.dci.value.link.LinksValue;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionAdminValue;
import se.streamsource.streamflow.client.ResourceModel;
import se.streamsource.streamflow.client.util.EventListSynch;
import se.streamsource.streamflow.client.util.Refreshable;

/**
 * JAVADOC
 */
public class FieldValueEditModel
   extends ResourceModel<FieldDefinitionAdminValue>
      implements Refreshable
{
   public static final String DATATYPE_NONE = "none";

   private EventList<LinkValue> possibleDatatypes = new TransactionList<LinkValue>( new BasicEventList<LinkValue>());
   private final LinkValue noneLink;

   public FieldValueEditModel(@Structure Module module)
   {
      this.module = module;
      ValueBuilder<LinkValue> valueBuilder = module.valueBuilderFactory().newValueBuilder( LinkValue.class );
      valueBuilder.prototype().id().set( DATATYPE_NONE );
      valueBuilder.prototype().href().set( "none/");
      valueBuilder.prototype().text().set( "<ingen>");
      noneLink = valueBuilder.newInstance();
      
   }

   public FieldDefinitionAdminValue getFieldDefinition()
   {
      return getIndex().<FieldDefinitionAdminValue>buildWith().prototype();
   }
   
   public EventList<LinkValue> getPossibleDatatypes()
   {
      return possibleDatatypes;
   }

   public void changeMandatory( boolean mandatory ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "mandatory", ""+mandatory );
      client.postCommand( "changemandatory", form.getWebRepresentation() );
   }

   public void changeDescription( String newDescription ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "description", newDescription );
      client.postCommand( "changedescription", form.getWebRepresentation() );
   }

   public void changeNote( String newNote ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "string", newNote );
      client.postCommand( "changenote", form.getWebRepresentation() );
   }

   public void changeFieldId( String newId ) throws ResourceException
   {
      Form form = new Form(  );
      form.set( "id", newId );
      client.postCommand( "changefieldid", form.getWebRepresentation() );
   }

   public void changeWidth( Integer newWidth ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "width", newWidth.toString() );
      client.postCommand( "changewidth", form.getWebRepresentation() );
   }

   public void changeRows( Integer newRows ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "rows", newRows.toString() );
      client.postCommand( "changerows", form.getWebRepresentation() );
   }

   public void changeCols( Integer newColumns ) throws ResourceException
   {
      Form form = new Form(  );
      form.set( "columns", newColumns.toString() );
      client.postCommand( "changecols", form.getWebRepresentation() );
   }

   public void changeInteger( Boolean isInteger ) throws ResourceException
   {
      Form form = new Form(  );
      form.set( "integer", isInteger.toString() );
      client.postCommand( "changeinteger", form.getWebRepresentation() );
   }

   public void changeComment( String comment ) throws ResourceException
   {
      Form form = new Form(  );
      form.set( "comment", comment );
      client.postCommand( "changecomment", form.getWebRepresentation() );
   }

   public void changeHint( String hint ) throws ResourceException
   {
      Form form = new Form(  );
      form.set( "hint", hint );

      client.postCommand( "changehint", form.getWebRepresentation());
   }

   public void changeRegularExpression( String regularExpression ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "expression", regularExpression );
      client.postCommand( "changeregularexpression", form.getWebRepresentation() );
   }

   public void changeOpenSelectionName( String name )
   {
      Form form = new Form( );
      form.set( "name", name );
      client.postCommand( "changeopenselectionname", form.getWebRepresentation() );
   }

   public void changeDatatype( String id ) throws ResourceException
   {
      if (id != null)
      {
         Form form = new Form( );
         form.set( "entity", id );
         client.postCommand( "changedatatype", form.getWebRepresentation() );
      } else
      {
         client.postCommand( "changedatatype", module.valueBuilderFactory().newValue(EntityValue.class) );
      }
   }

   public void changeStatistical( boolean statistical ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "statistical", ""+statistical );
      client.postCommand( "changestatistical", form.getWebRepresentation() );
   }
   
   public LinkValue getSelectedDatatype()
   {
      if (getIndex().datatype().get() != null)
      {
         for (LinkValue linkValue : possibleDatatypes)
         {
            if (linkValue.id().get().equals( getIndex().datatype().get().id().get() ))
            {
               return linkValue;
            }
         }
      } 
      return noneLink;  
   }

   public void changeRuleFieldId( LinkValue fieldId )
   {
      if( fieldId != null && !fieldId.id().get().equals( getIndex().rule().get().field().get() ) )
      {
         Form form = new Form( );
         form.set( "fieldid", fieldId.id().get() );
         client.putCommand( "changerulefieldid", form.getWebRepresentation() );
      }
   }

   public void changeRuleCondition( LinkValue condition )
   {
      if( condition != null && !condition.text().get().equals( getIndex().rule().get().condition().get().name() ))
      {
         Form form = new Form();
         form.set( "condition", condition.text().get() );
         client.putCommand( "changerulecondition", form.getWebRepresentation() );
      }
   }

   public void changeRuleVisibleWhen( boolean visibleWhen )
   {
      if( visibleWhen != getIndex().rule().get().visibleWhen().get() )
      {
         Form form = new Form();
         form.set( "visiblewhen", "" + visibleWhen );
         client.putCommand( "changerulevisiblewhen", form.getWebRepresentation() );
      }
   }

   public void changePoint( boolean point ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "point", ""+point );
      client.postCommand( "changepoint", form.getWebRepresentation() );
   }

   public void changePolyline( boolean polyline ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "polyline", ""+polyline );
      client.postCommand( "changepolyline", form.getWebRepresentation() );
   }

   public void changePolygon( boolean polygon ) throws ResourceException
   {
      Form form = new Form( );
      form.set( "polygon", ""+polygon );
      client.postCommand( "changepolygon", form.getWebRepresentation() );
   }

   @Override
   public void refresh()
   {
      super.refresh();

      possibleDatatypes.clear();
      
      possibleDatatypes.add( noneLink );
      possibleDatatypes.addAll( client.query("possibledatatypes", LinksValue.class).links().get() );
   }

   public void remove() throws ResourceException
   {
      client.delete();

   }

   public SelectionElementsModel newSelectionElementsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(SelectionElementsModel.class).use(client).newInstance();
   }

   public EventList<LinkValue> possibleRuleFields()
   {
      return EventListSynch.synchronize( client.query( "possiblerulefields", LinksValue.class ).links().get(), new BasicEventList<LinkValue>() );
   }

   public EventList<LinkValue> possibleRuleConditions()
   {
      return EventListSynch.synchronize( client.query( "possibleruleconditions", LinksValue.class ).links().get(), new BasicEventList<LinkValue>() );
   }

   public VisibilityRuleValuesModel newVisibilityRuleValuesModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(VisibilityRuleValuesModel.class).use(client).newInstance();
   }
}