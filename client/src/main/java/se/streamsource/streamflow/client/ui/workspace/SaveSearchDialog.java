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

package se.streamsource.streamflow.client.ui.workspace;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;
import org.jdesktop.application.Action;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.resource.user.profile.SearchValue;
import se.streamsource.streamflow.util.Strings;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;

/**
 * Save a search.
 */
public class SaveSearchDialog
      extends JPanel
{
   @Structure
   ValueBuilderFactory vbf;

   @Service
   DialogService dialogs;

   public JTextField name;
   public JTextField query;

   SearchValue search;

   public SaveSearchDialog( @Service ApplicationContext context )
   {
      super( new BorderLayout() );

      setActionMap( context.getActionMap( this ) );

      FormLayout layout = new FormLayout( "40dlu, 5dlu, 120dlu:grow", "pref, pref" );

      JPanel form = new JPanel( layout );
      form.setFocusable( false );
      DefaultFormBuilder builder = new DefaultFormBuilder( layout,
            form );

      name = new JTextField();
      query = new JTextField();

      builder.add( new JLabel( i18n.text( WorkspaceResources.name_label ) ) );
      builder.nextColumn( 2 );
      builder.add( name );

      builder.nextLine();
      builder.add( new JLabel( i18n.text( WorkspaceResources.query_label ) ) );
      builder.nextColumn( 2 );
      builder.add( query );

      add( form, BorderLayout.CENTER );
   }

   public SearchValue search()
   {
      return search;
   }

   @Action
   public void execute()
   {
      if (Strings.notEmpty( name.getText() ) && Strings.notEmpty( query.getText() ))
      {
         ValueBuilder<SearchValue> builder = vbf.newValueBuilder( SearchValue.class );
         builder.prototype().name().set( name.getText() );
         builder.prototype().query().set( query.getText() );

         search = builder.newInstance();

         WindowUtils.findWindow( this ).dispose();
      } else
      {
         dialogs.showOkDialog( WindowUtils.findWindow( this ), new JLabel( i18n.text( WorkspaceResources.incomplete_data ) ) );
      }
   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   public void presetQuery( String query )
   {
      this.query.setText( query );
   }
}