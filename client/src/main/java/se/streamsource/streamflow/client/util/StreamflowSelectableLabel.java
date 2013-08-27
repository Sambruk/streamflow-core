package se.streamsource.streamflow.client.util;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;

/*
 *
 */
public class StreamflowSelectableLabel
      extends JTextPane
{

   public StreamflowSelectableLabel()
   {
      super();
      setBorder( BorderFactory.createEmptyBorder() );
      setEditable( false );
      setOpaque( false );
   }

   public StreamflowSelectableLabel( String text )
   {
      this();
      setText( text );
   }
}