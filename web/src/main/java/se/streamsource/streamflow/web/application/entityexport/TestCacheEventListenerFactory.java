package se.streamsource.streamflow.web.application.entityexport;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Created by ruslan on 10.04.17.
 */

public class TestCacheEventListenerFactory extends CacheEventListenerFactory
{
   @Override
   public CacheEventListener createCacheEventListener( Properties properties )
   {
      return new TestCacheEventListener();
   }

   static class TestCacheEventListener implements CacheEventListener
   {

      private final Logger logger = LoggerFactory.getLogger( TestCacheEventListener.class );

      @Override
      public void notifyElementRemoved( Ehcache cache, Element element ) throws CacheException
      {

      }

      @Override
      public void notifyElementPut( Ehcache cache, Element element ) throws CacheException
      {

      }

      @Override
      public void notifyElementUpdated( Ehcache cache, Element element ) throws CacheException
      {

      }

      @Override
      public void notifyElementExpired( Ehcache cache, Element element )
      {
         logger.info( "Expired element " + element.getObjectKey().toString() + " - " + element.getObjectValue() );
      }

      @Override
      public void notifyElementEvicted( Ehcache cache, Element element )
      {
         logger.info( "Evicted element " + element.getObjectKey().toString() + " - " + element.getObjectValue() );
      }

      @Override
      public void notifyRemoveAll( Ehcache cache )
      {

      }

      @Override
      public void dispose()
      {

      }

      @Override
      public Object clone() throws CloneNotSupportedException
      {
         return null;
      }
   }
}


