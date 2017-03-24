package se.streamsource.streamflow.web.application.entityexport;

import org.apache.commons.collections.map.SingletonMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.qi4j.api.value.ValueComposite;
import org.qi4j.runtime.types.CollectionType;
import org.qi4j.runtime.types.MapType;
import org.qi4j.runtime.types.ValueCompositeType;
import org.qi4j.runtime.value.ValuePropertyModel;
import org.qi4j.spi.composite.StateDescriptor;
import org.qi4j.spi.property.PropertyDescriptor;
import org.qi4j.spi.property.ValueType;
import org.qi4j.spi.value.ValueDescriptor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ruslan on 06.03.17.
 */
public class ValueExportHelper extends AbstractExportHelper
{

   public static final String COLUMN_DESCRIPTION_SUFFIX = "_table";
   private ValueComposite value;

   public SingletonMap help() throws Exception
   {
      final SingletonMap singletonMap = new SingletonMap( 1234, "not_implemented" );
      return singletonMap;
   }

   private boolean jsonEmpty( String json )
   {
      return json.isEmpty() || json.equals( "{}" ) || json.equals( "[]" );
   }

   @Override
   protected String tableName()
   {
      return toSnackCaseFromCamelCase( classSimpleName( name() ) );
   }

   private String name()
   {
      return value.type().getName();
   }

   // setters

   public void setValue( ValueComposite value )
   {
      this.value = value;
   }


}
