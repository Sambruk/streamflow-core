/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.client.ui.task;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.ApplicationContext;
import org.qi4j.api.injection.scope.Service;
import se.streamsource.streamflow.client.infrastructure.ui.BindingFormBuilder;
import se.streamsource.streamflow.client.infrastructure.ui.StateBinder;

import javax.swing.JPanel;

/**
 * JAVADOC
 */
public class TaskView
      extends JPanel
{
   private StateBinder taskBinder;

//    @Service
//    UserInboxTaskDetailModel model;

   public TaskView( @Service ApplicationContext appContext )
   {
      FormLayout layout = new FormLayout(
            "right:max(40dlu;p), 4dlu, 200dlu, 7dlu, " // 1st major column
                  + "right:max(40dlu;p), 4dlu, 80dlu",        // 2nd major column
            "" );                                      // add rows dynamically
      DefaultFormBuilder builder = new DefaultFormBuilder( layout, this );
      builder.setDefaultDialogBorder();

      taskBinder = new StateBinder();
      taskBinder.setResourceMap( appContext.getResourceMap( getClass() ) );

      BindingFormBuilder bb = new BindingFormBuilder( builder, taskBinder );
/*
        bb.appendSeparator(task_separator)
                .appendLine(title_label, TEXTFIELD, template.description())
                .appendLine(note_label, TEXTAREA, template.note());
*/
   }
}
