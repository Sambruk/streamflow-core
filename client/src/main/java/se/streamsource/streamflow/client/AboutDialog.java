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

package se.streamsource.streamflow.client;

import info.aduna.io.IOUtil;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.swingx.util.WindowUtils;
import org.qi4j.api.injection.scope.Service;
import org.restlet.engine.io.BioUtils;
import se.streamsource.streamflow.client.infrastructure.ui.DialogService;
import se.streamsource.streamflow.client.infrastructure.ui.i18n;
import se.streamsource.streamflow.client.ui.workspace.WorkspaceResources;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.border.BevelBorder;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static se.streamsource.streamflow.client.infrastructure.ui.i18n.*;

/**
 * JAVADOC
 */
public class AboutDialog
      extends JPanel implements ActionListener
{
   private Popup popup;
   private Box box;

   @Service
   DialogService dialogs;

   public AboutDialog( @Service ApplicationContext context )
   {
      ActionMap am = context.getActionMap( this );
      setActionMap( am );

      box = Box.createVerticalBox();

      setActionMap( Application.getInstance().getContext().getActionMap( this ) );

      try
      {
         InputStream is = getClass().getResourceAsStream( "/version.properties" );
         Properties p = IOUtil.readProperties( is );

         JTextPane txt = new JTextPane();
         txt.setEditable( false );
         txt.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
         txt.setContentType( "text/html" );
         txt.setText( "<html><body><h2> " + p.getProperty( "application.header" ) + "&#0153;</h2>" +
               " Version: " + p.getProperty( "application.version" ) + "<br>" +
               " BuildKey: " + p.getProperty( "application.buildKey" ) + "<br>" +
               " BuildNumber: " + p.getProperty( "application.buildNumber" ) + "<br>" +
               " Revision: " + p.getProperty( "application.revision" ) + "<br><br>" +
               " This is Streamsource AB’s Streamflow product.<br>" +
               " Licensed under the Apache License, Version 2.0, " +
               " see http://www.apache.org/licenses/LICENSE-2.0<br><br>" +
               " Streamflow contains software<br>" +
               " that is licensed by third parties to Streamsource AB<br>" +
               " and protected by copyright.</body></html>"
         );
         JPanel general = new JPanel();
         general.setBorder( BorderFactory.createTitledBorder( text( StreamflowResources.general_info ) ) );
         general.add( txt );
         box.add( general );

      } catch (IOException e)
      {
         box.add( new JLabel( "Version properties could not be read!" ) );
      }
      add( box );

      JPanel apachePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      apachePanel.setBorder( BorderFactory.createTitledBorder( text( StreamflowResources.apache_border ) ) );
      JButton licenseBtn = new JButton( am.get( "license" ) );
      JButton noticeBtn = new JButton( am.get( "notice" ) );

      apachePanel.add( licenseBtn );
      apachePanel.add( noticeBtn );

      JPanel thirdPartyPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
      thirdPartyPanel.setBorder( BorderFactory.createTitledBorder( text( StreamflowResources.third_party_border ) ) );
      JButton thirdPartyProductBtn = new JButton( am.get( "thirdPartyProducts" ) );
      JButton thirdPartyLicenseBtn = new JButton( am.get( "thirdPartyLicenses" ) );


      thirdPartyPanel.add( thirdPartyProductBtn );
      thirdPartyPanel.add( thirdPartyLicenseBtn );

      box.add( apachePanel );
      box.add( thirdPartyPanel );

   }

   @Action
   public void execute()
   {

   }

   @Action
   public void close()
   {
      WindowUtils.findWindow( this ).dispose();
   }

   @Action
   public void license()
   {
      showFile( "LICENSE" );
   }

   @Action
   public void notice()
   {
      showFile( "NOTICE" );
   }

   @Action
   public void thirdPartyProducts()
   {
      openFile( "Streamsource_SF_Third_Party_Software.docx" );
   }

   @Action
   public void thirdPartyLicenses()
   {
      openFile( "Streamsource_Third_Party_License_Appendix.docx" );
   }

   private void showFile( String fileName )
   {

      Box box2 = Box.createVerticalBox();
      InputStream is = getClass().getResourceAsStream( "/" + fileName );
      JTextPane txt = new JTextPane();
      txt.setBorder( BorderFactory.createLineBorder( Color.BLACK, 2 ) );
      try
      {
         String content = new String( IOUtil.readBytes( is ) );

         txt.setContentType( "text/plain; charset=iso-8859-1" );
         txt.setPreferredSize( new Dimension( 700, 400 ) );
         txt.setEditable( false );
         txt.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) );
         txt.setText( content );
         txt.setCaretPosition( 0 );

         box2.add( new JScrollPane( txt ) );

      } catch (Exception e)
      {
         // translation with message format jada jada jada
         box2.add( new JLabel( "Could not open file!" ) );
      }

      Point origin = new Point( (int) this.getLocationOnScreen().getX() - (((int) txt.getPreferredSize().getWidth() - box.getWidth()) / 2),
            (int) this.getLocationOnScreen().getY() );
      popup = PopupFactory.getSharedInstance().getPopup( this, box2, (int) origin.getX(), (int) origin.getY() );
      JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
      JButton ok = new JButton( "Ok" );
      ok.addActionListener( this );

      buttonPanel.add( ok );

      box2.add( buttonPanel );

      popup.show();
   }

   private void openFile( String fileName )
   {
      Desktop desktop = Desktop.getDesktop();
      File file = null;
      try
      {
         String[] fileNameParts = fileName.split( "\\." );
         file = File.createTempFile( fileNameParts[0] + "_", "." + fileNameParts[1] );
         FileOutputStream out = new FileOutputStream( file );

         InputStream in = getClass().getResourceAsStream( "/" + fileName );
         try
         {
            BioUtils.copy( new BufferedInputStream( in, 1024 ), new BufferedOutputStream( out, 4096 ) );
         } catch (IOException e)
         {
            in.close();
            out.close();
            throw e;
         } finally
         {
            try
            {
               in.close();
               out.close();
            } catch (IOException e)
            {
               // Ignore
            }
         }

         desktop.edit( file );
      } catch (IOException e)
      {
         try
         {
            desktop.open( file );
         } catch (IOException e1)
         {
            dialogs.showMessageDialog( AboutDialog.this, i18n.text( WorkspaceResources.could_not_open_file ), "" );
         }
      }
   }

   public void actionPerformed( ActionEvent e )
   {
      popup.hide();
      popup = null;
   }
}
