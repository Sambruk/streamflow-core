Streamflow
==========

Build
-----

1. Dev environment

`mvn clean install`

2. Dev env with sign 

`mvn clean install -P sign`

.. note::

    For sign you need define properties in the `webstart/certificate.properties`.
    
Deploy
------

1. To `/snapshots` repository. You should have `SNAPSHOT` version:

`mvn clean deploy -P sign`

or if you want to define custom `settings.xml`:

`mvn -s ...setings.xml clean deploy -P sign`

.. note::

    ...settings.xml there is a path to settings.xml file.
    
2. To `/pre-releases` repository the same way as for `/snapshort`. 
You should you have pre release `stable` version (`alpha`, `beta`, `M`, `RC`).

3. To `/releases` repository:

`mvn clean deploy -P sign release`



