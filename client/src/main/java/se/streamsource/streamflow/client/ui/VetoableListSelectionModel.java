package se.streamsource.streamflow.client.ui;

import javax.swing.DefaultListSelectionModel;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;

/**
 * Quick impl of a list selection model which respects a veto before
 * changing selection state. The veto is effect in SINGLE_SELECTION mode
 * only.
 * Remark: Thanks to Jeanette Winzenburg (SwingLabs project) for the code example.
 */
public class VetoableListSelectionModel extends DefaultListSelectionModel
{
   private VetoableChangeSupport vetoableChangeSupport;

   /**
    * Defaults to SINGLE_SELECTION mode.
    */
   public VetoableListSelectionModel()
   {
      super();
      setSelectionMode( SINGLE_SELECTION );
   }

   public void addVetoableChangeListener( VetoableChangeListener l )
   {
      if (vetoableChangeSupport == null)
      {
         vetoableChangeSupport = new VetoableChangeSupport( this );
      }
      vetoableChangeSupport.addVetoableChangeListener( l );
   }


   public void removeVetoableChangeListener( VetoableChangeListener l )
   {
      if (vetoableChangeSupport == null) return;
      vetoableChangeSupport.removeVetoableChangeListener( l );
   }

   private void fireVetoableChange( int oldSelectionIndex, int newSelectionIndex ) throws
         PropertyVetoException
   {
      if (!isVetoable()) return;
      vetoableChangeSupport.fireVetoableChange( "selectedIndex", oldSelectionIndex, newSelectionIndex );

   }

   private boolean isVetoable()
   {
      if ((vetoableChangeSupport == null)
            || (getSelectionMode() != SINGLE_SELECTION)) return false;
      return vetoableChangeSupport.hasListeners( null );
   }


   @Override
   public void clearSelection()
   {
      if (isSelectionEmpty()) return;
      if (isVetoable())
      {
         try
         {
            fireVetoableChange( -1, getMinSelectionIndex() );
         } catch (PropertyVetoException e)
         {
            // vetoed - do nothing
            return;
         }
      }
      super.clearSelection();
   }


   @Override
   public void moveLeadSelectionIndex( int leadIndex )
   {
      if (isVetoable())
      {
         try
         {
            fireVetoableChange( getLeadSelectionIndex(), leadIndex );
         } catch (PropertyVetoException e)
         {
            // vetoed - do nothing
            return;
         }
      }
      super.moveLeadSelectionIndex( leadIndex );
   }


   @Override
   public void removeSelectionInterval( int index0, int index1 )
   {
      if (isSelectionEmpty() || invalidRange( index0, index1 )) return;
      if (isVetoable() && inRange( index0, index1 ))
      {
         try
         {
            fireVetoableChange( getMinSelectionIndex(), -1 );
         } catch (PropertyVetoException e)
         {
            // vetoed - do nothing
            return;
         }

      }
      super.removeSelectionInterval( index0, index1 );
   }

   /**
    * @param index0 one end of the range, must be non-negative
    * @param index1 the other end of the range, must be non-negative
    * @return true if current min selection in range, false otherwise
    */
   private boolean inRange( int index0, int index1 )
   {
      int clearMin = Math.min( index0, index1 );
      int clearMax = Math.max( index0, index1 );
      return getMinSelectionIndex() >= clearMin &&
            getMinSelectionIndex() <= clearMax;
   }


   /**
    * @param index0
    * @param index1
    */
   private boolean invalidRange( int index0, int index1 )
   {
      if (index0 == -1 || index1 == -1)
      {
         // super does nothing
         return true;
      }
      return false;
   }


   @Override
   public void setLeadSelectionIndex( int leadIndex )
   {
      if (isVetoable())
      {
         try
         {
            fireVetoableChange( getLeadSelectionIndex(), leadIndex );
         } catch (PropertyVetoException e)
         {
            // vetoed - do nothing
            return;
         }
      }
      super.setLeadSelectionIndex( leadIndex );
   }


   @Override
   public void setSelectionInterval( int index0, int index1 )
   {
      if (isVetoable())
      {
         try
         {
            fireVetoableChange( getMinSelectionIndex(), index0 );
         } catch (PropertyVetoException e)
         {
            // vetoed - do nothing
            return;
         }
      }
      super.setSelectionInterval( index0, index1 );
   }


}
