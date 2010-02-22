package se.streamsource.streamflow.client.infrastructure.ui;

import se.streamsource.streamflow.infrastructure.application.ListItemValue;

import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;

public class ToolTipTableCellRenderer extends DefaultTableCellRenderer
{
   @Override
   public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column )
   {
      String s = value.toString();
      JComponent component = (JComponent) super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
      if (s.contains( "\n" ))
      {
         StringBuilder sb = new StringBuilder( "<html>" );
         for (String line : s.split( "\n" ))
         {
            sb.append( "<p>" );
            sb.append( line );
            sb.append( "</p>" );
         }
         sb.append( "</html>" );
         component.setToolTipText( sb.toString() );
      } else
      {
         component.setToolTipText( "" );
      }
      return component;
   }
}