/*
 * Copyright 2009-2010 Streamsource AB
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

package se.streamsource.dci.value.table;

import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueBuilderFactory;

/**
 * JAVADOC
 */
public class TableBuilder<T extends TableBuilder>
{
   protected ValueBuilderFactory vbf;

   protected ValueBuilder<TableValue> tableBuilder;

   protected ValueBuilder<RowValue> rowBuilder;

   public TableBuilder( ValueBuilderFactory vbf )
   {
      this.vbf = vbf;

      tableBuilder = vbf.newValueBuilder( TableValue.class );
   }

   public T column(String id, String label, String type)
   {
      ValueBuilder<ColumnValue> builder = vbf.newValueBuilder( ColumnValue.class );
      builder.prototype().id().set( id );
      builder.prototype().label().set( label );
      builder.prototype().columnType().set( type );
      tableBuilder.prototype().cols().get().add( builder.newInstance() );
      return (T) this;
   }

   public T row()
   {
      if (rowBuilder != null)
         endRow();

      rowBuilder = vbf.newValueBuilder( RowValue.class );
      return (T)this;
   }

   public T endRow()
   {
      tableBuilder.prototype().rows().get().add( rowBuilder.newInstance() );
      rowBuilder = null;
      return (T)this;
   }

   public T cell(Object v, String f)
   {
      ValueBuilder<CellValue> cellBuilder = vbf.newValueBuilder( CellValue.class );
      cellBuilder.prototype().v().set( v );
      cellBuilder.prototype().f().set( f );
      rowBuilder.prototype().c().get().add( cellBuilder.newInstance() );
      return (T)this;
   }

   public TableValue newTable()
   {
      if (rowBuilder != null)
         endRow();

      return tableBuilder.newInstance();
   }
}
