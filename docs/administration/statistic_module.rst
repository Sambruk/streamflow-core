Statistic module
################

Setup
*****

To enable statistics module needed to do following:

    #. Setup datasource described at environment setup if not was set up before

    #. Enable statistic module via visualVm at **Qi4j > StreamflowServer > Application > Statistics > Service > statistics > Configuration**. Set ``enabled`` to ``true``

    #. Enable LiquiBase service(needed for generation of DB structure) at **Qi4j > StreamflowServer > Domain > Database > Service > liquibase > Configuration**. Set ``enabled`` to ``true`` and if needed regenerate from scratch set ``lastEventDate`` value to ``0``

Usage
*****

SQL queries
===========

Case count for all closed cases, built on closing organization, between 2 dates:

    .. code-block:: sql

        select count(case_id) from cases where created_on >= "2011-01-01 00:00:01" and created_on <= "2011-12-31 23:59:59" and assigned_organization in
        (select id from organization where organization.left>=(select organization.left from organization
        where name="Jönköping") and organization.right <=(select organization.right from organization where name="Jönköping"))

Case count for all closed cases, built on case type owner, between 2 dates:
(Works as long as all case type owners are organizational unit. If there are case type owners that are projects we have to dig one level deeper!)

    .. code-block:: sql

        select count(case_id) from cases where created_on >= "2011-01-01 00:00:01" and created_on <= "2011-12-31 23:59:59" and casetype_owner in
        (select id from organization where organization.left>=(select organization.left from organization
        where name="Jönköping") and organization.right <=(select organization.right from organization where name="Jönköping"))

Check if all case types are owned by OU's: (Expected result - empty set)

    .. code-block:: sql

        select distinct casetype_owner from cases where casetype_owner not in (select id from organization);

Check cases without case type:

    .. code-block:: sql

        select count(case_id) from cases where casetype is null and created_on >= "2011-03-01 00:00:01" and created_on <= "2012-01-31 23:59:59";

List of case counts for a certain period for all case type owners( regardless if case type owner is OU or project):

.. code-block:: sql

        select descriptions.description, count(case_id) as numbers from cases, descriptions where cases.casetype_owner = descriptions.id and created_on >= "2011-03-01 00:00:01" and created_on <= "2012-01-31 23:59:59" group by casetype_owner order by numbers desc;

List of case counts per case type for a certain period:

    .. code-block:: sql

        select descriptions.description, count(case_id) as numbers from cases, descriptions where cases.casetype = descriptions.id and created_on >= "2011-03-01 00:00:01" and created_on <= "2012-01-31 23:59:59" group by casetype order by numbers desc;

Some other queries run for Oskarshamn statistic.

    .. code-block:: sql

        select date_format( closed_on, '%Y-%m' ) as period, count(case_id) as number
        from casesdescriptions
        where closed_on >= '2011-01-01'
        and closed_on <= '2012-01-31 23:59:59'
        group by period
        order by period

    .. code-block:: sql

        select date_format( closed_on, '%Y-%m' ) as period, count(case_id) as number
        from casesdescriptions
        where closed_on >= '2011-01-01'
        and closed_on <= '2012-01-31 23:59:59'
        and casetype is not null
        group by period
        order by period

    .. code-block:: sql

        select date_format( cases.closed_on, '%Y-%m' ) as period, count(case_id) as number
        from cases
        where cases.casetype_owner in (
           select id from organization
             where organization.left >= 1
             and organization.right <= 6 )
        and closed_on >= '2011-01-01'
        and closed_on <= '2012-01-31 23:59:59'
        and casetype_owner is not null
        group by period
        order by period


    .. code-block:: sql

        select casetype_owner, date_format(closed_on, '%Y-%m' ) as period, count(case_id) as number
        from casesdescriptions
        where closed_on >= '2011-01-01'
        and closed_on <= '2012-01-31 23:59:59'
        and casetype_owner is not null
        group by casetype_owner, period
        order by casetype_owner, period
