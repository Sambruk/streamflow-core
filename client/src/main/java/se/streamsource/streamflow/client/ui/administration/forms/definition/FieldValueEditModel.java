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

package se.streamsource.streamflow.client.ui.administration.forms.definition;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.structure.Module;
import org.restlet.data.Form;
import org.restlet.resource.ResourceException;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.streamflow.api.administration.form.FieldDefinitionValue;
import se.streamsource.streamflow.client.util.Refreshable;

/**
 * JAVADOC
 */
public class FieldValueEditModel
      implements Refreshable
{
   private FieldDefinitionValue value;

   @Uses
   private CommandQueryClient client;

   @Structure
   private Module module;

   public FieldDefinitionValue getFieldDefinition()
   {
      return value;
   }

   public void changeMandatory( boolean mandatory ) throws ResourceException
   {
      client.postCommand( "changemandatory", new Form("mandatory="+mandatory).getWebRepresentation() );
   }

   public void changeDescription( String newDescription ) throws ResourceException
   {
      client.postCommand( "changedescription", new Form("description="+newDescription).getWebRepresentation() );
   }

   public void changeNote( String newNote ) throws ResourceException
   {
      client.postCommand( "changenote", new Form("string="+newNote).getWebRepresentation() );
   }

   public void changeFieldId( String newId ) throws ResourceException
   {
      client.postCommand( "changefieldid", new Form("id="+newId).getWebRepresentation() );
   }

   public void changeWidth( Integer newWidth ) throws ResourceException
   {
      client.postCommand( "changewidth", new Form("width="+newWidth).getWebRepresentation() );
   }

   public void changeRows( Integer newRows ) throws ResourceException
   {
      client.postCommand( "changerows", new Form("rows="+newRows).getWebRepresentation() );
   }

   public void changeCols( Integer newColumns ) throws ResourceException
   {
      client.postCommand( "changecols", new Form("columns="+newColumns).getWebRepresentation() );
   }

   public void changeInteger( Boolean isInteger ) throws ResourceException
   {
      client.postCommand( "changeinteger", new Form("integer="+isInteger).getWebRepresentation() );
   }

   public void changeComment( String comment ) throws ResourceException
   {
      client.postCommand( "changecomment", new Form("comment="+comment).getWebRepresentation() );
   }

   public void changeHint( String hint ) throws ResourceException
   {
      client.postCommand( "changehint", new Form("hint="+hint).getWebRepresentation() );
   }

   public void changeRegularExpression( String regularExpression ) throws ResourceException
   {
      client.postCommand( "changeregularexpression", new Form("expression="+regularExpression).getWebRepresentation() );
   }

   public void changeOpenSelectionName( String name )
   {
      client.postCommand( "changeopenselectionname", new Form("name="+name).getWebRepresentation() );
   }

   public void refresh()
   {
      value = (FieldDefinitionValue) client.query( "field", FieldDefinitionValue.class ).buildWith().prototype();
   }

   public void remove() throws ResourceException
   {
      client.delete();

   }

   public SelectionElementsModel newSelectionElementsModel()
   {
      return module.objectBuilderFactory().newObjectBuilder(SelectionElementsModel.class).use(client).newInstance();
   }
}