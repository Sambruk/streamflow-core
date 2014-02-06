/**
 *
 * Copyright 2009-2014 Jayway Products AB
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

import org.qi4j.api.injection.scope.Uses;
import org.qi4j.api.property.Property;
import se.streamsource.streamflow.client.util.CommandTask;

import java.util.Observable;
import java.util.Observer;

public class FieldValueObserver
      implements Observer
{
   @Uses
   private FieldValueEditModel model;

   public void update( Observable observable, Object arg )
   {
      final Property property = (Property) arg;
      if (property.qualifiedName().name().equals( "mandatory" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeMandatory( (Boolean) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "description" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeDescription( (String) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "note" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeNote( (String) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "fieldId" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeFieldId( (String) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "width" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeWidth( Integer.parseInt( (String) property.get() ) );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "rows" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeRows( Integer.parseInt( (String) property.get() ) );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "cols" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeCols( Integer.parseInt( (String) property.get() ) );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "comment" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeComment( (String) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "integer" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeInteger( (Boolean) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "hint" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeHint( (String) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "regularExpression" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeRegularExpression( (String) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "openSelectionName" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeOpenSelectionName( (String) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "datatype" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeDatatype( (String) property.get() );
            }
         }.execute();
      }else if (property.qualifiedName().name().equals( "point" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changePoint( (Boolean) property.get() );
            }
         }.execute();
      }else if (property.qualifiedName().name().equals( "polyline" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changePolyline( (Boolean) property.get() );
            }
         }.execute();
      }else if (property.qualifiedName().name().equals( "polygon" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changePolygon( (Boolean) property.get() );
            }
         }.execute();
      } else if (property.qualifiedName().name().equals( "statistical" ))
      {
         new CommandTask()
         {
            @Override
            public void command()
               throws Exception
            {
               model.changeStatistical( (Boolean) property.get() );
            }
         }.execute();
      } 
   }

}
