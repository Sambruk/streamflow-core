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
package se.streamsource.streamflow.web.application.entityexport;

import net.sf.ehcache.Element;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.configuration.Configuration;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.injection.scope.This;
import org.qi4j.api.io.Outputs;
import org.qi4j.api.io.Receiver;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.spi.entitystore.BackupRestore;
import org.qi4j.spi.entitystore.StateChangeListener;
import org.qi4j.spi.structure.ModuleSPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.streamsource.infrastructure.database.DataSourceConfiguration;
import se.streamsource.streamflow.infrastructure.configuration.FileConfiguration;
import se.streamsource.streamflow.web.infrastructure.caching.Caches;
import se.streamsource.streamflow.web.infrastructure.caching.Caching;
import se.streamsource.streamflow.web.infrastructure.caching.CachingService;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static se.streamsource.streamflow.web.application.entityexport.AbstractExportHelper.LINE_SEPARATOR;
import static se.streamsource.streamflow.web.application.entityexport.SchemaCreatorHelper.IDENTITY_MODIFIED_INFO_TABLE_NAME;

/**
 * Service encapsulates interaction with cache for entity export.
 * It fires on startup (writes from index to cache)
 * and helps to write to cache when application started.
 * <br/>
 * Search response sized with 1000 because tests show this's the optimal value.
 */
@Mixins({ EntityExportService.Mixin.class, EntityStateChangeListener.class })
public interface EntityExportService
        extends StateChangeListener,
        ServiceComposite,
        Activatable,
        Configuration<EntityExportConfiguration>
{

   int SIZE_THRESHOLD = 1000;

   boolean isExported();

   EntityDBCheckJob saveToCache(String identity, long modified, String transaction);

   List<String> getNextEntities(Connection connection);

   String getSchemaInfoFileAbsPath();

   void savedSuccess( String identity, long modified, Connection connection );

   Map<String, Set<String>> getTables() throws IOException, ClassNotFoundException;

   void setTables( Map<String, Set<String>> tables );

   DbVendor getDbVendor();

   abstract class Mixin
           implements EntityExportService
   {
      private static final Logger logger = LoggerFactory.getLogger( EntityExportService.class );

      @This
      Configuration<EntityExportConfiguration> thisConfig;

      @Service
      FileConfiguration config;
      @Service
      CachingService cachingService;
      @Service
      ServiceReference<DataSource> dataSource;
      @Service
      BackupRestore backupRestoreService;

      @Structure
      ModuleSPI moduleSPI;
      @Structure
      Module module;

      private AtomicBoolean isExported = new AtomicBoolean();

      private DbVendor dbVendor;
      private String schemaInfoFileAbsPath;
      private Map<String, Set<String>> tables;

      //statistics
      private AtomicInteger statisticsCounter = new AtomicInteger();

      private Caching caching;
      private Caching tempCaching;

      @Override
      public void activate() throws Exception
      {

         if ( dataSource.isAvailable()
                 && dataSource.isActive()
                 && thisConfig.configuration().enabled().get() )
         {

            caching = new Caching( cachingService, Caches.ENTITYSTATES );
            caching.removeAll();

            tempCaching = new Caching( cachingService, Caches.ENTITYSTATESTEMP );
            tempCaching.removeAll();

            try
            {

               tables = readSchemaStateFromFile();
               dbVendor = _getDbVendor();

               try ( final Connection connection = dataSource.get().getConnection() )
               {
                  createBaseSchema( connection );
               }

               export();

               tempCaching.removeAll();

            } catch ( Throwable e )
            {
               logger.error( "Unexpected exception: ", e );
            }
         }

      }

      private Map<String, Set<String>> readSchemaStateFromFile() throws IOException, ClassNotFoundException, JSONException
      {
         final File infoFile = new File( config.dataDirectory(), "entityexport/schema.info" );
         schemaInfoFileAbsPath = infoFile.getAbsolutePath();

         Map<String, Set<String>> result = new HashMap<>();

         if ( !infoFile.exists() )
         {
            final File parentDirectory = infoFile.getParentFile();
            if ( !parentDirectory.exists() )
            {
               parentDirectory.mkdir();
            }

            infoFile.createNewFile();

            return result;
         }

         String fileContent = FileUtils.readFileToString( infoFile, StandardCharsets.UTF_8.name() );
         if ( fileContent.isEmpty() )
         {
            return result;
         }
         JSONObject jsonObject = new JSONObject( fileContent );
         Iterator keys = jsonObject.keys();
         while ( keys.hasNext() )
         {
            String tableName = ( String ) keys.next();
            JSONArray jsonArray = jsonObject.getJSONArray( tableName );
            Set<String> columns = new LinkedHashSet<>();
            for ( int i = 0; i < jsonArray.length(); i++ )
            {
               columns.add( jsonArray.getString( i ) );
            }
            result.put( tableName, columns );
         }
         return result;
      }

      @Override
      public EntityDBCheckJob saveToCache(String identity, long modified, String transaction){
         EntityDBCheckJob entityDBCheckJob = null;

         //Resolved possible NPE
         if (caching != null)
         {
            entityDBCheckJob = new EntityDBCheckJob(dbVendor, dataSource, caching);
            entityDBCheckJob.setIdentity(identity);
            entityDBCheckJob.setModified(modified);
            entityDBCheckJob.setTransaction(transaction);
         }
         return entityDBCheckJob;
      }

      @Override
      public boolean isExported()
      {
         return isExported.get();
      }

      @Override
      public List<String> getNextEntities(Connection connection)
      {
         try (final Statement statement = connection.createStatement()) {
             final String select = "SELECT TOP(" + EntityExportService.SIZE_THRESHOLD + ") [identity] FROM "
                     + IDENTITY_MODIFIED_INFO_TABLE_NAME
                     + " WHERE proceed = 0 ORDER BY modified";
             final ResultSet resultSet = statement.executeQuery(select);
             final ArrayList<String> result = new ArrayList<>(SIZE_THRESHOLD);
             while (resultSet.next()) {
                final String identity = resultSet.getString(1);
                 Element cacheElement = caching.get(identity);
                 if (cacheElement != null) {
                     final String entityAsString = cacheElement.getObjectValue().toString();
                     result.add(entityAsString);
                 } else {
                    logger.info("Not found at cache. " + identity);

                    if (this.removeArchivedCase(identity)) {
                       logger.info("Removed from SQL DB: " + identity);
                    } else {
                       logger.info("Failed to remove from SQL DB: " + identity);
                    }
                 }
             }
             return result;
         } catch (Exception e) {
            logger.error("Unexpected exception. ", e);
            return new ArrayList<>();
         }

      }

      @Override
      public Map<String, Set<String>> getTables() throws IOException, ClassNotFoundException
      {
         return tables;
      }

      @Override
      public void setTables( Map<String, Set<String>> tables )
      {
         this.tables = tables;
      }

      @Override
      public DbVendor getDbVendor() {
         return dbVendor;
      }

      private DbVendor _getDbVendor() {
         final UnitOfWork uow = module.unitOfWorkFactory().newUnitOfWork( UsecaseBuilder.newUsecase( "Get Datasource configuration" ) );
         final DataSourceConfiguration dataSourceConfiguration = uow.get( DataSourceConfiguration.class, dataSource.identity() );
         return DbVendor.from( dataSourceConfiguration.dbVendor().get() );
      }

      @Override
      public void savedSuccess( String identity, long modified, Connection connection )
      {
         try
         {
            try (final Statement statement = connection.createStatement()) {
               final String sqlUpdateEntity = updateEntityInfoSql(identity, modified, true);
               statement.executeUpdate(sqlUpdateEntity);
            }

            connection.commit();

            caching.remove(identity);

            final boolean showSqlEntitiesCount = thisConfig.configuration().showSqlEntitiesCount().get();
            if ( showSqlEntitiesCount )
            {
               if ( statisticsCounter.incrementAndGet() % SIZE_THRESHOLD == 0)
               {
                  logger.info("Exported " + statisticsCounter.get() + " entities to sql");
               }
            }

         } catch ( Exception e )
         {
            logger.error( "Error: ", e );
         }

      }

      @Override
      public String getSchemaInfoFileAbsPath()
      {
         return schemaInfoFileAbsPath;
      }

      @Override
      public void passivate() throws Exception
      {

      }

      private void createBaseSchema( Connection connection ) throws Exception
      {
         final SchemaCreatorHelper schemaUpdater = new SchemaCreatorHelper();
         schemaUpdater.setModule( moduleSPI );
         schemaUpdater.setConnection( connection );
         schemaUpdater.setDbVendor( dbVendor );
         schemaUpdater.setSchemaInfoFileAbsPath( schemaInfoFileAbsPath );
         schemaUpdater.setTables( tables );
         schemaUpdater.setShowSql( thisConfig.configuration().showSql().get() );
         schemaUpdater.create();
      }

      private void export() throws Throwable
      {
         logger.info( "Started entities export from database to cache." );
         logger.info( "Checking..." );

         long totalExported = 0L;
         try (final Connection connection = dataSource.get().getConnection()) {
            try (final Statement statement = connection.createStatement()) {
               final EntityFromStoreReceiver receiver = new EntityFromStoreReceiver(statement);
               backupRestoreService.backup().transferTo(Outputs.withReceiver(receiver));
               statement.executeBatch();
               logger.info( "Checked " + receiver.numberOfProceedEntities + " entities" );
            }

            final String select = "SELECT TOP(" + SIZE_THRESHOLD + ") [identity] FROM " + IDENTITY_MODIFIED_INFO_TABLE_NAME + LINE_SEPARATOR +
                    "WHERE [proceed] = 0 ";
            boolean hasNext = true;
            String condition = "";

            while (hasNext) {
               final String resultSql = select + condition;
               try (final PreparedStatement preparedStatement = connection.prepareStatement(resultSql)) {
                  final ResultSet resultSet = preparedStatement.executeQuery();
                  int i = 0;
                   while (resultSet.next()) {
                       final String identity = resultSet.getString(1);
                       Element cacheElement = tempCaching.get(identity);
                       if (cacheElement != null) {
                           caching.put(new Element(identity, cacheElement.getObjectValue().toString()));
                           totalExported++;
                           condition = "AND [identity] > '" + identity + "'";
                           i++;
                       }
                       else {
                          logger.info("Not found at cache. "+ identity);

                          if (this.removeArchivedCase(identity)) {
                             logger.info("Removed from SQL DB: " + identity);
                          } else {
                             logger.info("Failed to remove from SQL DB: " + identity);
                          }
                       }
                   }

                  if (totalExported % SIZE_THRESHOLD == 0) {
                     logger.info("Exported " + totalExported + " entities to cache");
                  }

                  hasNext = i != 0;
               }
            }
         }

         logger.info( "Finished entities export from database to cache. Total exported to cache = " + totalExported );

         isExported.set( true );
      }

      private class EntityFromStoreReceiver implements Receiver<String, Throwable>{
         final Statement statement;
         long numberOfProceedEntities = 0L;

         private EntityFromStoreReceiver(Statement statement) {
            this.statement = statement;
         }

         @Override
         public void receive(String entityAsString) throws Throwable {
            final JSONObject entity = new JSONObject(entityAsString);

            final String identity = entity.getString("identity");
            final String updateEntityInfoSql = updateEntityInfoSql(identity, entity.getLong("modified"), false);

            statement.addBatch(updateEntityInfoSql);
            tempCaching.put(new Element(identity, entityAsString));

            numberOfProceedEntities++;

            if (numberOfProceedEntities % SIZE_THRESHOLD == 0) {
               logger.info( "Checked " + numberOfProceedEntities + " entities" );
               statement.executeBatch();
            }

         }
      }

      private String updateEntityInfoSql(String identity, long modified, boolean saveProceed) {
         switch (dbVendor) {
            case mssql:
               return "UPDATE " + IDENTITY_MODIFIED_INFO_TABLE_NAME + LINE_SEPARATOR  +
                       (saveProceed
                               ? "SET proceed = 1," + LINE_SEPARATOR
                               :
                       "SET proceed          = CASE" + LINE_SEPARATOR  +
                       "                       WHEN proceed = 0 AND modified <= " + modified + LINE_SEPARATOR  +
                       "                         THEN 0" + LINE_SEPARATOR  +
                       "                       ELSE 1" + LINE_SEPARATOR  +
                       "                       END," + LINE_SEPARATOR
                       ) +
                       "" + LINE_SEPARATOR  +
                       (saveProceed
                               ? "modified = " + modified + LINE_SEPARATOR
                               :
                       "  modified = CASE" + LINE_SEPARATOR  +
                       "                       WHEN proceed = 0 AND modified <= " + modified + LINE_SEPARATOR  +
                       "                         THEN modified" + LINE_SEPARATOR  +
                       "                       ELSE " + modified + LINE_SEPARATOR  +
                       "                       END" + LINE_SEPARATOR
                       ) +
                       "WHERE [identity] = '" + identity + "'" + LINE_SEPARATOR  +
                       "IF (@@ROWCOUNT = 0)" + LINE_SEPARATOR  +
                       "  INSERT INTO " + IDENTITY_MODIFIED_INFO_TABLE_NAME +  " ([identity], modified, proceed) VALUES ('" + identity + "',  " + modified +  ", 0)";

            default: return "";

         }
      }

       private boolean removeArchivedCase(String identity) {
           boolean isRemoveSuccess = false;

           final String removeIdentityFromLastModified = "DELETE FROM " + IDENTITY_MODIFIED_INFO_TABLE_NAME + " WHERE [identity] = ?";

           //Log
           final String removeCaseLogEntries = "DELETE clev FROM case_log_entry_value clev " +
                   "           INNER JOIN case_log_entity_entries_cross_ref r ON clev.id = r.link_id " +
                   "           INNER JOIN case_entity ce ON ce.[caselog] = r.owner_id " +
                   "           WHERE ce.[identity] = ?";

           final String removeCaseLogReferences = "DELETE r " +
                   "FROM case_log_entity_entries_cross_ref r " +
                   "           INNER JOIN case_entity ce ON ce.[caselog] = r.owner_id " +
                   "           WHERE ce.[identity] = ?";

           final String removeCaseLogReference = "UPDATE case_entity SET caselog = NULL WHERE [identity] = ?;";

           //Notes
           final String removeCaseNoteEntries = "DELETE nv " +
                   "FROM note_value nv" +
                   "  INNER JOIN notes_time_line_entity_notes_cross_ref ntle ON nv.id = ntle.link_id" +
                   "  INNER JOIN case_entity ce ON ntle.owner_id = ce.notes " +
                   "WHERE ce.[identity] = ?;";

           final String removeCaseNoteReferences = "DELETE  ntle " +
                   "FROM notes_time_line_entity_notes_cross_ref ntle " +
                   "  INNER JOIN case_entity ce ON ntle.owner_id = ce.notes " +
                   "WHERE ce.[identity] = ?;";

           final String removeCaseNoteReference = "UPDATE case_entity SET notes = NULL WHERE [identity] = ?;";


           //History
           final String removeCaseMessageEntries = "DELETE message " +
                   "FROM message_entity message" +
                   "  INNER JOIN conversation_entity converssation ON message.conversation = converssation.[identity] " +
                   "  INNER JOIN case_entity_conversations_cross_ref ceccr ON converssation.[identity] = ceccr.link_id " +
                   "WHERE ceccr.[owner_id] = ?;";

           final String removeCaseConversations = "DELETE converssation " +
                   "FROM conversation_entity converssation" +
                   "  INNER JOIN case_entity_conversations_cross_ref ceccr ON converssation.[identity] = ceccr.link_id " +
                   "WHERE ceccr.[owner_id] = ?;";

           final String removeCaseConversationReferences = "DELETE ceccr " +
                   "FROM case_entity_conversations_cross_ref ceccr " +
                   "WHERE ceccr.[owner_id] = ?;";

           final String removeCaseConversationReference = "UPDATE case_entity SET history = NULL WHERE [identity] = ?;";

           //Attachments
           final String removeCaseAttachments = "DELETE ae " +
                   "FROM attachment_entity ae" +
                   " INNER JOIN case_entity_attachments_cross_ref ceacr ON ae.[identity] = ceacr.link_id " +
                   "WHERE ceacr.[owner_id] = ?;";

           final String removeCaseAttachmentsReferences = "DELETE ceacr " +
                   "FROM case_entity_attachments_cross_ref ceacr " +
                   "WHERE ceacr.[owner_id] = ?;";

           //Forms
           final String removeSubmittedFieldValues = "DELETE fv " +
                   "FROM submitted_field_value fv" +
                   "" +
                   "  JOIN submitted_page_value_fields_cross_ref spvfcr ON fv.id = spvfcr.link_id" +
                   "  JOIN submitted_page_value spv ON spvfcr.owner_id = spv.id" +
                   "" +
                   "  JOIN submitted_form_value_pages_cross_ref sfvpcr ON spv.id = sfvpcr.link_id" +
                   "  JOIN submitted_form_value sfv ON sfvpcr.owner_id = sfv.id" +
                   "" +
                   "  JOIN case_entity_submitted_forms_cross_ref cesfcr ON sfv.id = cesfcr.link_id " +
                   "WHERE cesfcr.[owner_id] = ?;";

           final String removeSubmittedPageReferences = "DELETE spvfcr " +
                   "FROM submitted_page_value_fields_cross_ref spvfcr" +
                   "  JOIN submitted_page_value spv ON spvfcr.owner_id = spv.id" +
                   "" +
                   "  JOIN submitted_form_value_pages_cross_ref sfvpcr ON spv.id = sfvpcr.link_id" +
                   "  JOIN submitted_form_value sfv ON sfvpcr.owner_id = sfv.id" +
                   "" +
                   "  JOIN case_entity_submitted_forms_cross_ref cesfcr ON sfv.id = cesfcr.link_id " +
                   "WHERE cesfcr.[owner_id] = ?;";

           final String removeSubmittedPageValues = "DELETE spv " +
                   "FROM submitted_page_value spv " +
                   "  JOIN submitted_form_value_pages_cross_ref sfvpcr ON spv.id = sfvpcr.link_id" +
                   "  JOIN submitted_form_value sfv ON sfvpcr.owner_id = sfv.id" +
                   "" +
                   "  JOIN case_entity_submitted_forms_cross_ref cesfcr ON sfv.id = cesfcr.link_id " +
                   "WHERE cesfcr.[owner_id] = ?;";


           final String removeSubmittedFormPageReferences = "DELETE sfvpcr " +
                   "FROM submitted_form_value_pages_cross_ref sfvpcr" +
                   "  JOIN submitted_form_value sfv ON sfvpcr.owner_id = sfv.id" +
                   "" +
                   "  JOIN case_entity_submitted_forms_cross_ref cesfcr ON sfv.id = cesfcr.link_id " +
                   "WHERE cesfcr.[owner_id] = ?;";

           final String removeSubmittedFormValues = "DELETE sfv " +
                   "FROM submitted_form_value sfv" +
                   "  JOIN case_entity_submitted_forms_cross_ref cesfcr ON sfv.id = cesfcr.link_id " +
                   "WHERE cesfcr.[owner_id] = ?;";

           final String removeSubmittedFormReferences = "DELETE cesfcr " +
                   "FROM case_entity_submitted_forms_cross_ref cesfcr " +
                   "WHERE cesfcr.[owner_id] = ?;";

           try (final Connection connection = dataSource.get().getConnection()) {
               connection.setAutoCommit(false);
               try {
                   removeIdentityWiredData(connection, identity, removeIdentityFromLastModified);

                   //Case log
                   removeIdentityWiredData(connection, identity, removeCaseLogReference);

                   removeIdentityWiredData(connection, identity, removeCaseLogEntries);

                   removeIdentityWiredData(connection, identity, removeCaseLogReferences);

                   //Notes
                   removeIdentityWiredData(connection, identity, removeCaseNoteReference);

                   removeIdentityWiredData(connection, identity, removeCaseNoteEntries);

                   removeIdentityWiredData(connection, identity, removeCaseNoteReferences);

                   //History
                   removeIdentityWiredData(connection, identity, removeCaseConversationReference);

                   removeIdentityWiredData(connection, identity, removeCaseConversationReferences);

                   removeIdentityWiredData(connection, identity, removeCaseMessageEntries);

                   removeIdentityWiredData(connection, identity, removeCaseConversations);

                   //Attachments
                   removeIdentityWiredData(connection, identity, removeCaseAttachments);

                   removeIdentityWiredData(connection, identity, removeCaseAttachmentsReferences);

                   //Forms
                   removeIdentityWiredData(connection, identity, removeSubmittedPageReferences);

                   removeIdentityWiredData(connection, identity, removeSubmittedFieldValues);

                   removeIdentityWiredData(connection, identity, removeSubmittedFormPageReferences);

                   removeIdentityWiredData(connection, identity, removeSubmittedPageValues);

                   removeIdentityWiredData(connection, identity, removeSubmittedFormReferences);

                   removeIdentityWiredData(connection, identity, removeSubmittedFormValues);

               } catch (SQLException e) {
                   logger.error("Failed to remove archived entity " + e.getMessage());
                   connection.rollback();
                   connection.setAutoCommit(true);
               }
               connection.commit();
               connection.setAutoCommit(true);
               isRemoveSuccess = true;
           } catch (SQLException e) {
               logger.error("Failed during remove cached entity " + e.getMessage());
           }

           return isRemoveSuccess;
       }

       private void removeIdentityWiredData(Connection connection, String identity, String query) throws SQLException {
           PreparedStatement ps;
           ps = connection.prepareStatement(query);
           ps.setString(1, identity);
           ps.execute();
       }
   }
}
