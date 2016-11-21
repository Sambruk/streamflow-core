/**
 *
 * Copyright
 * 2009-2015 Jayway Products AB
 * 2016-2017 Föreningen Sambruk
 *
 * Licensed under AGPL, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/agpl.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.streamsource.streamflow.client.ui.menu;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.qi4j.api.injection.scope.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for menus
 */
public abstract class AbstractMenu
      extends JMenu
{
   final Logger logger = LoggerFactory.getLogger( "menu" );
   private ApplicationContext context;

   public void init( @Service ApplicationContext context )
   {
      setActionMap( context.getActionMap() );
      this.context = context;
      init();
   }

   abstract protected void init();

   protected void menu( String menuName, String... menuItems )
   {
      ActionMap am = getActionMap();

      ResourceMap resourceMap = context.getResourceMap( getClass(), AbstractMenu.class );
      String menuTitle = resourceMap.getString( menuName );
      setText( menuTitle );
      setMnemonic( menuTitle.charAt( 0 ) );
      for (String menuItem : menuItems)
      {
         if (menuItem.equals( "---" ))
         {
            add( new JSeparator() );
         } else
         {
            String actionName = menuItem.startsWith( "*" ) ? menuItem.substring( 1 ) : menuItem;
            Action menuItemAction = am.get( actionName );

            if (menuItemAction == null)
            {
               logger.warn( "Could not find menu action:" + actionName );
               continue;
            }
            JMenuItem item = menuItem.startsWith( "*" ) ? new JCheckBoxMenuItem() : new JMenuItem();
            item.setAction( menuItemAction );
            item.setIcon( null );
            add( item );
         }
      }
   }
}