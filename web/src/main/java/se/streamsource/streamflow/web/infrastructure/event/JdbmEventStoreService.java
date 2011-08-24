/**
 *
 * Copyright 2009-2011 Streamsource AB
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

package se.streamsource.streamflow.web.infrastructure.event;

import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.RecordManagerOptions;
import jdbm.btree.BTree;
import jdbm.helper.ByteArraySerializer;
import jdbm.helper.LongComparator;
import jdbm.helper.LongSerializer;
import jdbm.helper.MRU;
import jdbm.helper.Serializer;
import jdbm.helper.Tuple;
import jdbm.helper.TupleBrowser;
import jdbm.recman.CacheRecordManager;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.io.Output;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.io.Sender;
import org.qi4j.api.io.Transforms;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.infrastructure.event.domain.TransactionDomainEvents;
import se.streamsource.streamflow.infrastructure.event.domain.source.AbstractEventStoreMixin;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventSource;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStore;
import se.streamsource.streamflow.infrastructure.event.domain.source.EventStream;
import se.streamsource.streamflow.infrastructure.event.domain.source.TransactionVisitor;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

/**
 * JAVADOC
 */
@Mixins(JdbmEventStoreService.JdbmEventStoreMixin.class)
public interface JdbmEventStoreService
        extends EventSource, EventStore, EventStream, EventManagement, Activatable, ServiceComposite
{
   class JdbmEventStoreMixin
           extends AbstractEventStoreMixin
           implements EventManagement, EventSource
   {
      @Service
      FileConfiguration fileConfig;

      private RecordManager recordManager;
      private BTree index;
      private Serializer serializer;
      private File dataFile;
      private File databaseFile;
      private File logFile;

      public void activate() throws IOException
      {
         super.activate();

         dataFile = new File(fileConfig.dataDirectory(), identity.identity() + "/events");
         databaseFile = new File(fileConfig.dataDirectory(), identity.identity() + "/events.db");
         logFile = new File(fileConfig.dataDirectory(), identity.identity() + "/events.lg");
         File directory = dataFile.getAbsoluteFile().getParentFile();
         directory.mkdirs();
         String name = dataFile.getAbsolutePath();
         Properties properties = new Properties();
         properties.put(RecordManagerOptions.AUTO_COMMIT, "false");
         properties.put(RecordManagerOptions.DISABLE_TRANSACTIONS, "false");
         initialize(name, properties);
      }

      public void passivate() throws Exception
      {
         super.passivate();

         logger.info("Close event db");
         recordManager.close();
      }

      public void removeAll() throws Exception
      {
         // Delete event files
         passivate();

         if (!databaseFile.delete())
            throw new IOException("Could not delete event database");
         if (!logFile.delete())
            throw new IOException("Could not delete event log");

         activate();
      }

      public void removeTo(Date date) throws IOException
      {

         lock();

         try
         {
            final TupleBrowser browser = index.browse();
            Tuple tuple = new Tuple();
            while (browser.getNext(tuple))
            {
               Long key = (Long) tuple.getKey();
               if (key <= date.getTime())
               {
                  index.remove(key);
               } else
               {
                  break; // We're done
               }
            }
            commit();
         } catch (IOException ex)
         {
            rollback();
         } finally
         {
            lock.unlock();
         }
      }

      public Output<String, IOException> restore()
      {
         return Transforms.lock(JdbmEventStoreMixin.this.lock, new Output<String, IOException>()
         {
            public <SenderThrowableType extends Throwable> void receiveFrom(Sender<? extends String, SenderThrowableType> sender) throws IOException, SenderThrowableType
            {
               try
               {
                  sender.sendTo(new Receiver<String, IOException>()
                  {
                     int count = 0;

                     public void receive(String item) throws IOException
                     {
                        try
                        {
                           JSONObject json = (JSONObject) new JSONTokener(item).nextValue();
                           TransactionDomainEvents transactionDomain = (TransactionDomainEvents) transactionEventsType.fromJSON(json, module);

                           storeEvents(transactionDomain);

                           count++;
                           if (count % 1000 == 0)
                              commit(); // Commit every 1000 transactions to avoid OutOfMemory issues

                        } catch (JSONException e)
                        {
                           throw new IOException(e);
                        }
                     }
                  });

                  commit();
               } catch (IOException e)
               {
                  rollback();
                  throw e;
               } catch (Throwable senderThrowableType)
               {
                  rollback();
                  throw (SenderThrowableType) senderThrowableType;
               }
            }
         });
      }

      // EventStore implementation
      public void transactionsAfter(long afterTimestamp, TransactionVisitor visitor)
      {
         // Lock datastore first
         lock();

         final Long afterTime = afterTimestamp + 1;

         try
         {
            final TupleBrowser browser = index.browse(afterTime);

            Tuple tuple = new Tuple();

            while (browser.getNext(tuple))
            {
               // Get next transaction
               TransactionDomainEvents domainEvents = readTransactionEvents(tuple);

               if (!visitor.visit(domainEvents))
               {
                  return;
               }
            }
         } catch (Exception e)
         {
            logger.warn("Could not iterate transactions", e);
         } finally
         {
            lock.unlock();
         }
      }

      public void transactionsBefore(long beforeTimestamp, TransactionVisitor visitor)
      {
         // Lock datastore first
         lock();

         final Long beforeTime = beforeTimestamp - 1;

         try
         {
            final TupleBrowser browser = index.browse(beforeTime);

            Tuple tuple = new Tuple();

            while (browser.getPrevious(tuple))
            {
               // Get previous transaction
               TransactionDomainEvents domainEvents = readTransactionEvents(tuple);

               if (!visitor.visit(domainEvents))
               {
                  return;
               }
            }
         } catch (Exception e)
         {
            logger.warn("Could not iterate transactions", e);
         } finally
         {
            lock.unlock();
         }
      }

      protected void rollback()
              throws IOException
      {
         recordManager.rollback();
      }

      protected void commit()
              throws IOException
      {
         recordManager.commit();
      }

      protected void storeEvents(TransactionDomainEvents transactionDomain)
              throws IOException
      {
         try
         {
            String jsonString = transactionDomain.toJSON();
            index.insert(transactionDomain.timestamp().get(), jsonString.getBytes("UTF-8"), false);
            recordManager.commit();
         } catch (IOException e)
         {
            recordManager.rollback();
            throw e;
         }
      }

      private void initialize(String name, Properties properties)
              throws IOException
      {
         recordManager = RecordManagerFactory.createRecordManager(name, properties);
         serializer = new ByteArraySerializer();
         recordManager = new CacheRecordManager(recordManager, new MRU(1000));
         long recid = recordManager.getNamedObject("index");
         if (recid != 0)
         {
            index = BTree.load(recordManager, recid);
         } else
         {
            LongComparator comparator = new LongComparator();
            index = BTree.createInstance(recordManager, comparator, new LongSerializer(), serializer, 16);
            recordManager.setNamedObject("index", index.getRecid());
         }
         commit();
      }

      private TransactionDomainEvents readTransactionEvents(Tuple tuple)
              throws UnsupportedEncodingException, JSONException
      {
         byte[] eventData = (byte[]) tuple.getValue();
         String eventJson = new String(eventData, "UTF-8");
         JSONTokener tokener = new JSONTokener(eventJson);
         JSONObject transaction = (JSONObject) tokener.nextValue();
         return (TransactionDomainEvents) transactionEventsType.fromJSON(transaction, module);
      }
   }
}
