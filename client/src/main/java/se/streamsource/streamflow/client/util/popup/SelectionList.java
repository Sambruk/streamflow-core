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
package se.streamsource.streamflow.client.util.popup;

import static se.streamsource.streamflow.client.util.i18n.icon;

import java.awt.Component;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.ListSelectionListener;

import se.streamsource.streamflow.client.Icons;

/**
 * 
 * @author henrikreinhold
 *
 */
public class SelectionList extends JList
{
   public SelectionList(List<String> values, final List<String> selected, final ValueToLabelConverter converter, ListSelectionListener listener)
   {
      super( values.toArray() );
      setCellRenderer( new DefaultListCellRenderer()
      {

         @Override
         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
               boolean cellHasFocus)
         {
            setFont( list.getFont() );
            setBackground( list.getBackground() );
            setForeground( list.getForeground() );
            if (selected.contains( value ))
            {
               setIcon( icon( Icons.check, 12 ) );
               setBorder( BorderFactory.createEmptyBorder( 4, 0, 0, 0 ) );
            } else
            {

               setIcon( null );
               setBorder( BorderFactory.createEmptyBorder( 4, 16, 0, 0 ) );
            }
            setText( converter.convert(value.toString()) );

            return this;
         }
      } );

      addListSelectionListener( listener );
   }
}
