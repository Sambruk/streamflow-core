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

package se.streamsource.streamflow.client.ui.administration.casetypes.forms;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.dci.restlet.client.CommandQueryClient;
import se.streamsource.dci.value.StringValue;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.infrastructure.ui.Refreshable;
import se.streamsource.streamflow.client.ui.administration.AdministrationResources;
import se.streamsource.streamflow.domain.form.FieldDefinitionValue;
import se.streamsource.streamflow.infrastructure.event.DomainEvent;
import se.streamsource.streamflow.infrastructure.event.source.EventVisitor;
import se.streamsource.streamflow.infrastructure.event.source.helper.EventVisitorFilter;
import se.streamsource.streamflow.resource.roles.BooleanDTO;
import se.streamsource.streamflow.resource.roles.IntegerDTO;

/**
 * JAVADOC
 */
public class FieldValueEditModel
      implements Refreshable, EventVisitor
{
   final Logger logger = LoggerFactory.getLogger( "administration" );
   private FieldDefinitionValue value;
   private CommandQueryClient client;
   private ValueBuilderFactory vbf;

   private EventVisitorFilter eventFilter;

   public FieldValueEditModel( @Uses CommandQueryClient client, @Structure ValueBuilderFactory vbf )
   {
      this.client = client;
      this.vbf = vbf;
      refresh();
      eventFilter = new EventVisitorFilter( this, "changedNote" );
   }

   public FieldDefinitionValue getFieldDefinition()
   {
      return value;
   }

   public void changeMandatory( boolean mandatory ) throws ResourceException
   {
      ValueBuilder<BooleanDTO> builder = vbf.newValueBuilder( BooleanDTO.class );
      builder.prototype().bool().set( mandatory );
      client.postCommand( "changemandatory", builder.newInstance() );
   }

   public void changeDescription( String newDescription ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newDescription );
      client.postCommand( "changedescription", builder.newInstance() );
   }

   public void changeNote( String newNote ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newNote );
      client.postCommand( "changenote", builder.newInstance() );
   }

   public void changeFieldId( String newId ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( newId );
      client.postCommand( "changefieldid", builder.newInstance() );
   }

   public void changeWidth( Integer newWidth ) throws ResourceException
   {
      ValueBuilder<IntegerDTO> builder = vbf.newValueBuilder( IntegerDTO.class );
      builder.prototype().integer().set( newWidth );
      client.postCommand( "changewidth", builder.newInstance() );
   }

   public void changeRows( Integer newWidth ) throws ResourceException
   {
      ValueBuilder<IntegerDTO> builder = vbf.newValueBuilder( IntegerDTO.class );
      builder.prototype().integer().set( newWidth );
      client.postCommand( "changerows", builder.newInstance() );
   }

   public void changeCols( Integer newWidth ) throws ResourceException
   {
      ValueBuilder<IntegerDTO> builder = vbf.newValueBuilder( IntegerDTO.class );
      builder.prototype().integer().set( newWidth );
      client.postCommand( "changecols", builder.newInstance() );
   }


   public void changeMultiple( Boolean multiple ) throws ResourceException
   {
      ValueBuilder<BooleanDTO> builder = vbf.newValueBuilder( BooleanDTO.class );
      builder.prototype().bool().set( multiple );
      client.postCommand( "changemultiple", builder.newInstance() );
   }

   public void changeInteger( Boolean multiple ) throws ResourceException
   {
      ValueBuilder<BooleanDTO> builder = vbf.newValueBuilder( BooleanDTO.class );
      builder.prototype().bool().set( multiple );
      client.postCommand( "changeinteger", builder.newInstance() );
   }

   public void changeComment( String comment ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( comment );
      client.postCommand( "changecomment", builder.newInstance() );
   }

   public void move( String direction ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( direction );
      client.postCommand( "move", builder.newInstance() );
   }

   public void changeHint( String hint ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( hint );
      client.postCommand( "changehint", builder.newInstance() );
   }

   public void changeRegularExpression( String regularExpression ) throws ResourceException
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( regularExpression );
      client.postCommand( "changeregularexpression", builder.newInstance() );
   }

   public void changeOpenSelectionName( String name )
   {
      ValueBuilder<StringValue> builder = vbf.newValueBuilder( StringValue.class );
      builder.prototype().string().set( name );
      client.postCommand( "changeopenselectionname", builder.newInstance() );
   }

   public void refresh() throws OperationException
   {
      try
      {
         value = (FieldDefinitionValue) client.query( "field", FieldDefinitionValue.class ).buildWith().prototype();
      } catch (ResourceException e)
      {
         throw new OperationException( AdministrationResources.could_not_get_field, e );
      }
   }

   public boolean visit( DomainEvent event )
   {
      if (value.field().get().identity().equals( event.entity().get() ))
      {
         logger.info( "Refresh the field values" );
         refresh();
      }
      return false;
   }

   public CommandQueryClient getClient()
   {
      return client;
   }
   public void remove() throws ResourceException
   {
      client.delete();

   }
}