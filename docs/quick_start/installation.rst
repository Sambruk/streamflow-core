Installation
============


#. Checkout
#. Comment all content in methods assemble and testEventMixin at **EventPropertyChangeMixinTest.java**
#. Change IOException to Exception at line *109* at file **KnowledgebaseService.java**
#. Add folowing line at **velocity.properties** file under line *118*
	.. code-block:: properties

		velocimacro.library =

#. Modify root **pom.xml** file:
	* Add under line *22*
	.. code-block:: maven

	    <repositories>
		<repository>
		    <id>maven-restlet</id>
		    <name>Public online Restlet repository</name>
		    <url>http://maven.restlet.org</url>
		</repository>
		<repository>
		    <id>nexus</id>
		    <url>http://79.125.6.136/nexus/content/groups/public</url>
		</repository>
		<repository>
            	    <id>releases</id>
            	    <name>Cloudbees Release Repo</name>
            	    <url>http://repository-streamflow.forge.cloudbees.com/release/</url>
        	</repository>
		<!--Unchecked repo only for cardme -->
		<repository>
		    <id>basex</id>
		    <name>BaseX Maven Repository</name>
		    <url>http://files.basex.org/maven</url>
		</repository>
	    </repositories>


	* Add folowing dependency under line *825*. Before lines
	.. code-block:: maven

	       	</dependencies>
	   </dependencyManagement>

	This:
    .. code-block:: maven

		<dependency>
        	        <groupId>org.codehaus.mojo</groupId>
        	        <artifactId>keytool-api-1.7</artifactId>
        	        <version>1.5</version>
        	</dependency>

#. Modify **/web/pom.xml**:
	* Change:
	.. code-block:: maven

		<dependency>
		    <groupId>cardme</groupId>
		    <artifactId>cardme</artifactId>
		    <version>0.2.6</version>
		</dependency>

		To:
	.. code-block:: maven

		<dependency>
		    <groupId>org.deepfs.external</groupId>
		    <artifactId>cardme</artifactId>
		    <version>0.2.6</version>
		</dependency>

#. Modify **/webstart/pom.xml**:
	* Change version of *webstart-maven-plugin* to **1.0-beta-6** at line *22*
	* Add folowing dependecies under line *22*:
		.. code-block:: maven

			<dependencies>
		            <dependency>
		                <groupId>org.codehaus.mojo</groupId>
		                <artifactId>webstart-pack200-impl</artifactId>
		                <version>1.0-beta-6</version>
		            </dependency>
		            <dependency>
		                <groupId>org.codehaus.mojo</groupId>
		                <artifactId>keytool-api-1.7</artifactId>
		                <version>1.4</version>
		            </dependency>
		        </dependencies>

#. Run mvn clean package for *streamflow-core* **pom.xml**
#. Check if exist folowing folders **.StreamflowServer***
#. Get **streamflow-web-1.28-SNAPSHOT.war** from *streamflow-core/web/target/* folder and rename it to **streamflow.war**
#. Now u can deploy it

.. important::

    Be sure that there are no **.StreamflowServer*** folders before deploying, in another case you will get lock error, in case of them remove and restart server.

.. note::
    After deploying all urls are works but they dont include streamflow root path. They must look like * /streamflow/workspace/ instead of * /workspace/
