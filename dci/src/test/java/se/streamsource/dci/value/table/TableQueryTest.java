/**
 *
 * Copyright 2009-2012 Streamsource AB
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * TODO
 */
public class TableQueryTest
{
   private SingletonAssembler assembler;

   @Before
   public void setup()
   {
      assembler = new SingletonAssembler()
      {
         public void assemble(ModuleAssembly module) throws AssemblyException
         {
            module.values(TableQuery.class);
         }
      };
   }

   @Test
   public void testQueryParsing1()
   {
      ValueBuilder<TableQuery> builder = assembler.valueBuilderFactory().newValueBuilder(TableQuery.class);
      builder.prototype().tq().set("select a,b,c order by foo offset 5 limit 1");
      TableQuery tq = builder.newInstance();

      Assert.assertThat(tq.select(), equalTo("a,b,c"));
      Assert.assertThat(tq.orderBy(), equalTo("foo"));
      Assert.assertThat(tq.offset(), equalTo("5"));
      Assert.assertThat(tq.limit(), equalTo("1"));
   }

   @Test
   public void testQueryParsing2()
   {
      ValueBuilder<TableQuery> builder = assembler.valueBuilderFactory().newValueBuilder(TableQuery.class);
      builder.prototype().tq().set("select caseid description created owner status href order by `created` limit 6 offset 0");
      TableQuery tq = builder.newInstance();

      Assert.assertThat(tq.select(), equalTo("caseid description created owner status href"));
      Assert.assertThat(tq.orderBy(), equalTo("`created`"));
      Assert.assertThat(tq.offset(), equalTo("0"));
      Assert.assertThat(tq.limit(), equalTo("6"));
   }
}
