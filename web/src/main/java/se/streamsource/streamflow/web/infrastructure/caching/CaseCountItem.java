package se.streamsource.streamflow.web.infrastructure.caching;

import java.io.Serializable;

public class CaseCountItem implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 2215168683765433245L;

   int count;
   int unread;

   public void addToCount(int increment)
   {
      count += increment;
   }

   public void addToUnread(int increment)
   {
      unread += increment;
   }

   public int getCount()
   {
      return count;
   }

   public int getUnread()
   {
      return unread;
   }

}
