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

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import org.restlet.resource.ResourceException;
import se.streamsource.streamflow.client.OperationException;
import se.streamsource.streamflow.client.ui.caze.CaseResources;

import java.util.Observable;
import java.util.Observer;

public class FieldValueObserver
      implements Observer
{
   @Uses
   private FieldValueEditModel model;

   public void update( Observable observable, Object arg )
   {
      Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "mandatory" ))
      {
         try
         {
            model.changeMandatory( (Boolean) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_mandatory, e );
         }
      } else if (property.qualifiedName().name().equals( "description" ))
      {
         try
         {
            model.changeDescription( (String) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_name, e );
         }
      } else if (property.qualifiedName().name().equals( "note" ))
      {
         try
         {
            model.changeNote( (String) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_note, e );
         }
      } else if (property.qualifiedName().name().equals( "fieldId" ))
      {
         try
         {
            model.changeFieldId( (String) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_update_field, e );
         }
      } else if (property.qualifiedName().name().equals( "width" ))
      {
         try
         {
            model.changeWidth( Integer.parseInt( (String) property.get() ) );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_width, e );
         }
      } else if (property.qualifiedName().name().equals( "rows" ))
      {
         try
         {
            model.changeRows( Integer.parseInt( (String) property.get() ) );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_rows, e );
         }
      } else if (property.qualifiedName().name().equals( "cols" ))
      {
         try
         {
            model.changeCols( Integer.parseInt( (String) property.get() ) );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_rows, e );
         }
      } else if (property.qualifiedName().name().equals( "multiple" ))
      {
         try
         {
            model.changeMultiple( (Boolean) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_multiple, e );
         }
      } else if (property.qualifiedName().name().equals( "comment" ))
      {
         try
         {
            model.changeComment( (String) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_comment, e );
         }
      } else if (property.qualifiedName().name().equals( "integer" ))
      {
         try
         {
            model.changeInteger( (Boolean) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_comment, e );
         }
      } else if (property.qualifiedName().name().equals( "hint" ))
      {
         try
         {
            model.changeHint( (String) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_hint, e );
         }
      } else if (property.qualifiedName().name().equals( "regularExpression" ))
      {
         try
         {
            model.changeRegularExpression( (String) property.get() );
         } catch (ResourceException e)
         {
            throw new OperationException( CaseResources.could_not_change_regularexpression, e );
         }
      }

   }

}
