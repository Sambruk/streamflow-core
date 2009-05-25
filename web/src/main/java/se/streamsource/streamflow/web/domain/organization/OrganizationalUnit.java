/*
 * Copyright (c) 2009, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package se.streamsource.streamflow.web.domain.organization;

import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.query.QueryBuilder;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;

/**
 * An organizational unit represents a part of an organization.
 */
@Mixins(OrganizationalUnit.OrganizationalUnitMixin.class)
public interface OrganizationalUnit
{
    Organization organization();

    class OrganizationalUnitMixin
            implements OrganizationalUnit
    {
        @Structure
        UnitOfWorkFactory uowf;

        public Organization organization()
        {
            QueryBuilder<OrganizationalUnit> queryBuilder = uowf.currentUnitOfWork().queryBuilderFactory().newQueryBuilder(OrganizationalUnit.class);
/*

            Identity identity = QueryExpressions.templateFor(Identity.class);
            queryBuilder.where(QueryExpressions.oneq(identity.identity(), ))
*/

            return null;
        }
    }
}
