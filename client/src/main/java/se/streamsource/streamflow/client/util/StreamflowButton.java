/**
 *
 * Copyright 2009-2012 Jayway Products AB
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
package se.streamsource.streamflow.client.util;

import java.awt.Insets;

import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * Common Button class that handles styling
 * 
 * @author henrikreinhold
 * 
 */
public class StreamflowButton extends JButton
{
   private static final long serialVersionUID = -4011503775863849533L;

   private static final Insets INSETS = new Insets( 3, 1, 1, 3 );

   public StreamflowButton()
   {
      super();
      setMargin( INSETS );
   }
   
   public StreamflowButton(javax.swing.Action action)
   {
      super( action );
      setMargin( INSETS );
   }

   public StreamflowButton(String string)
   {
      super( string );
      setMargin( INSETS );
   }

   public StreamflowButton(String string, ImageIcon icon)
   {
      super( string, icon );
      setMargin( INSETS );
   }

}
