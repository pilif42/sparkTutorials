# sparkTutorials
A repo to keep work related to Apache Spark tutorials.


- A few definitions:
      - an edge node = interface between the outside network and the Hadoop cluster. For this reason, it is sometimes referred to as a gateway node.
            - used to run client applications: you submit your Storm topologies and Spark jobs from it.
            - used to run cluster administration tools: Ambari, Sqoop, etc.
            - good definition at https://www.dummies.com/programming/big-data/hadoop/edge-nodes-in-hadoop-clusters/
      - a Spark Driver node and a Spark Worker node:
            - good definition at https://spark.apache.org/docs/latest/cluster-overview.html
            - see the importance of the value chosen for --deploy-mode (in the spark-submit) below.


- Installed the HDP Sandbox for Docker following https://fr.hortonworks.com/tutorial/learning-the-ropes-of-the-hortonworks-sandbox/
      - copied HDP_3.0.1_docker-deploy-scripts_18120587fc7fb to /home/philippe
      - cd /home/philippe/HDP_3.0.1_docker-deploy-scripts
      - Note that I had to edit the docker-deploy-hdp30.sh as I ran into the issue described at https://community.hortonworks.com/questions/199816/network-scoped-alias-is-supported.html
              - vi docker-deploy-hdp30.sh
              - replace '==' with '=' in the condition if [ "$flavor" = ....
      - sudo bash docker-deploy-hdp30.sh (Remarque : Vous n'aurez besoin de lancer ce script qu'une seule fois.)
      - verifier que la sandbox tourne:
          - sudo docker ps --> you see 2 containers: sandbox-proxy and sandbox-hdp
      - pour l'arreter:
          - sudo docker stop sandbox-hdp
          - sudo docker stop sandbox-proxy
      - pour la redemarrer:
          - sudo docker start sandbox-hdp
          - sudo docker start sandbox-proxy
      - pour supprimer le conteneur:
          - d'abord l'arreter
          - sudo docker rm sandbox-hdp
          - sudo docker rm sandbox-proxy
      - pour supprimer l'image:
          - d'abord supprimer le conteneur
          - sudo docker rmi hortonworks/sandbox-hdp:3.0.1
      - architecture explained with /docs/sandboxArchitecture.jpg taken from https://fr.hortonworks.com/tutorial/sandbox-architecture/


- Finalised set up following https://fr.hortonworks.com/tutorial/learning-the-ropes-of-the-hortonworks-sandbox/
      - SECURE SHELL METHOD:
          - ssh root@127.0.0.1 -p 2222
          - I had to change the password.
      - SHELL WEB CLIENT METHOD:
          - http://127.0.0.1:4200 with root / pwd
      - Transfer file from local machine to sandbox:
          - cd /home/philippe/Documents/Temp
          - scp -P 2222 train root@127.0.0.1:train
          - verify that you now have a file called train at /root in the sandbox
      - Transfer file from sandbox to local machine:
          - cd /home/philippe/Documents/Temp
          - scp -P 2222 root@127.0.0.1:anaconda-ks.cfg anaconda-ks-local.cfg
          - verify that you now have a file called anaconda-ks-local.cfg at /home/philippe/Documents/Temp
      - Verify the Splash page at http://127.0.0.1:1080/
      - Ambari dashboard at http://127.0.0.1:8080
          - reset the admin password:
              - ssh into the Sandbox
              - ambari-admin-password-reset
          - log as admin and explore the metrics, heatmaps, config.
          - other users:
              - maria_dev / maria_dev
                raj_ops	/ raj_ops
                holger_gov / holger_gov
                amy_ds / amy_ds
              - differences explained at https://fr.hortonworks.com/tutorial/learning-the-ropes-of-the-hortonworks-sandbox/
      - To figure out the sandbox version:
          - ssh into Sandbox
          - sandbox-version
      - Troubleshoot:
          - job, query or request that seems to run forever and does not complete:
                - It may be because it’s in the ACCEPTED state. A good place to begin looking is in the ResourceManager. If you know a job has completed, but the Resource Manager still thinks it’s running – kill it!


- Kafka setup:
      - to list all existing topics:
            - ssh into the sandbox
            - cd /usr/hdp/current/kafka-broker/bin
            - ./kafka-topics.sh --list --zookeeper localhost:2181
      - to create a topic called notifications:
            - ./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic notifications
      - to consume from topic notifications:
            - old way = ./kafka-console-consumer.sh --zookeeper localhost:2181 --topic notifications
                    - consumes OK but msg: Consider using the new consumer by passing [bootstrap-server] instead of [zookeeper].
            - new way = ./kafka-console-consumer.sh --bootstrap-server localhost:6667 --topic notifications
                    - does NOT consume and error = WARN [Consumer clientId=consumer-1, groupId=console-consumer-26702] Connection to node -1 could not be established. Broker may not be available. (org.apache.kafka.clients.NetworkClient)
      - Kafka logs located at /usr/hdp/current/kafka-broker/logs


- https://fr.hortonworks.com/tutorial/setting-up-a-spark-development-environment-with-java
      - shakespeare.txt:
            - saved under src/main/resources
            - uploaded to HDFS:
                    - http://127.0.0.1:8080/#/login with maria_dev / maria_dev
                    - mouse over the drop-down menu (blue calculator-style icon) on the upper-right hand corner and click on Files View.
                    - open the tmp folder and click the upload button in the upper-right corner to upload the file. Make sure it’s named shakespeare.txt.
      - to run in the Hortonworks sandbox:
            - create an assembly jar, ie a single jar file that contains both our code and all jars our code depends on. By packaging our code as an assembly we guarantee that all dependency jars (as defined in pom.xml) will be present when our code runs.
                    - cd /home/philippe/code/sparkTutorials
                    - mvn package
            - copy the log4j.properties over to the sandbox:
                    - cd /home/philippe/code/sparkTutorials
                    - scp -P 2222 env/sandbox/log4j.properties root@127.0.0.1:/root
            - copy the assembly jar over to the sandbox:
                    - scp -P 2222 target/SparkTutorial-1.0-SNAPSHOT.jar root@127.0.0.1:/root
            - delete /tmp/shakespeareWordCount in Ambari:
                    - this directory is where results are written. As we ssh into the sandbox with root to launch the job, the owner of shakespeareWordCount is root.
                    - I tried to change the owner of shakespeareWordCount with:
                            - ssh -p 2222 root@127.0.0.1
                            - hdfs dfs -chown maria_dev /tmp/shakespeareWordCount
                            --> chown: changing ownership of '/tmp/shakespeareWordCount': User root is not a super user (non-super user cannot change owner)
                    - So instead just deleted the directory with:
                            - ssh -p 2222 root@127.0.0.1
                            - hdfs dfs -rm -r /tmp/shakespeareWordCount
            - submit the app:
                    - notes:
                            - The cmd below is for local mode. Other options = yarn, etc. (for full details: see https://spark.apache.org/docs/latest/submitting-applications.html)
                                    - local mode = Spark runs locally using this computer, rather than in distributed mode.
                            - Also, note profile=sandbox that is being used in the main of TheApp.
                            - And the logging config explained further in 'Notes on logging' below.
                    - ssh into the sandbox: ssh -p 2222 root@127.0.0.1
                    - verify that you are under /root
                    - spark-submit --verbose --master local --class "hortonworks.sparktutorial.TheApp" --conf 'spark.driver.extraJavaOptions=-Dprofile=sandbox -Dlog4j.configuration=file:/root/log4j.properties -Dvm.logging.name=myapp -Dvm.logging.level=DEBUG' --conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:/root/log4j.properties -Dvm.logging.name=myapp -Dvm.logging.level=DEBUG" ./SparkTutorial-1.0-SNAPSHOT.jar
            - verify results:
                    - open Ambari at http://127.0.0.1:8080 with maria_dev / maria_dev
                    - menu Files View --> /tmp --> you will find a directory shakespeareWordCount.
            - verify logs in /var/log/spark/myapp.log: see 'Notes on logging' below.
      - to run in the Cloud:
            - set up a cluster using Hortonworks Cloud Solutions.
            - deploy your code to the cluster.:
                    - cd /home/philippe/code/sparkTutorials/target
                    - scp -P 2222 -i "key.pem" SparkTutorial-1.0-SNAPSHOT.jar root@[ip address of a master node]:root
            - submit the app:
                    - ssh -p 2222 -i "key.pem" root@[ip address of a master node]
                    - spark-submit --class "hortonworks.sparktutorial.TheApp"  --master yarn --deploy-mode client ./SparkTutorial-1.0-SNAPSHOT.jar
                               - yarn means we want Spark to run in a distributed mode rather than on a single machine, and we want to rely on YARN (a cluster
                               resource manager) to fetch available machines to run the job. If you aren’t familiar with YARN, it is especially important if you
                               want to run several jobs simultaneously on the same cluster. When configured properly, a YARN queue will provide different users
                               or process a quota of cluster resources they’re allowed to use. It also provides mechanisms for allowing a job to take full use
                               of the cluster when resources are available and scaling existing jobs down when additional users or jobs begin to submit jobs.
                               - deploy-mode client indicates we want to use the current machine as the driver machine for Spark. The driver machine is a single
                               machine that initiates a Spark job, and is also where summary results are collected when the job is finished. Alternatively, we
                               could have specified –deploy-mode cluster, which would have allowed YARN to choose the driver machine. It’s important to note that
                               a poorly written Spark program can accidentally try to bring back many Terabytes of data to the driver machine, causing it to
                               crash. For this reason, you shouldn’t use the master node of your cluster as your driver machine. Many organizations submit Spark
                               jobs from what’s called an edge node, which is a separate machine that isn’t used to store data or perform computation. Since the
                               edge node is separate from the cluster, it can go down without affecting the rest of the cluster. Edge nodes are also used for
                               data science work on aggregate data that has been retrieved from the cluster. For example, a data scientist might submit a Spark
                               job from an edge node to transform a 10 TB dataset into a 1 GB aggregated dataset, and then do analytics on the edge node using
                               tools like R and Python. If you plan on setting up an edge node, make sure that machine doesn’t have the DataNode or HostManager
                                components installed, since these are the data storage and compute components of the cluster. You can check this on the host tab
                                in Ambari.
      - to debug a remote cluster (ie not running inside the IDE):
            - on the machine where you submit your Spark job, run this command from the terminal:
                   - export SPARK_JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8086
                   - This lets you attach a debugger at port 8086. You need to make sure port 8086 is able to receive inbound connections.
                   - with the Hortonworks sandbox:
                        - ssh onto it and run the command.
            - in IntelliJ, go to Run > Edit Configurations:
                   - click the + button at the upper-left and add a new Remote configuration.
                   - name = TheAppRemotelyDebugged
                   - host = 127.0.0.1 (IP of the SandBox)
                   - port = 8086
                   - Apply
                   - Add a breakpoint in TheApp.java
                   - run this debug conf from your IDE immediately after submitting your Spark job. The debugger will attach and Spark will stop at breakpoints.
                        - TODO with our sandbox as getting: Error running 'TheAppRemotelyDebugged': Unable to open debugger port (127.0.0.1:8086): java.io.IOException "handshake failed - connection prematurally closed"


- Notes on logging:
      - when running locally on my laptop, log4j.properties under /src/main/resources is used. Hence, logs are found at /tmp/sparkTutorialLog.out.
                - Note that when running locally, if the logging level (log4j.rootCategory in log4j.properties) is set to DEBUG, then the word count is NOT happening and you see in the logs:
                java.io.IOException: HADOOP_HOME or hadoop.home.dir are not set. Raise it to INFO and everything is OK.
      - when running in the Hortonworks sandbox in Standalone mode:
                - we copy /env/sandbox/log4j.properties from laptop to sandbox under /root
                - Spark driver is running on the machine where you submit the job, and each Spark worker node will run an executor for this job. So, you need to setup log4j for both driver and executor as per the command below.
                --conf 'spark.driver.extraJavaOptions=-Dprofile=sandbox -Dlog4j.configuration=file:/root/log4j.properties -Dvm.logging.name=myapp -Dvm.logging.level=DEBUG' --conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=file:/root/log4j.properties -Dvm.logging.name=myapp -Dvm.logging.level=DEBUG"
      - when running on YARN:
                - both driver and executor use the same configuration file. That is because in yarn-cluster mode, driver is also run as a container in YARN.
                - an example of command is:
                        spark-submit
                        --master yarn-cluster
                        --files /path/to/log4j-spark.properties
                        --conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=log4j-spark.properties"
                        --conf "spark.executor.extraJavaOptions=-Dlog4j.configuration=log4j-spark.properties"


- https://fr.hortonworks.com/tutorial/hadoop-tutorial-getting-started-with-hdp/section/1/
      - Beeline (command shell to connect to Hive):
            - ssh into the SandBox with: ssh -p 2222 root@127.0.0.1
            - connect to Beeline Hive with: beeline -u jdbc:hive2://127.0.0.1:10000 -n hive
            - grant all permission access for maria_dev user with:
                        grant all on database foodmart to user maria_dev;
                        grant all on database default to user maria_dev;
            - exit the Beeline shell: !quit
            - connect to Beeline using maria_dev with: beeline -u jdbc:hive2://127.0.0.1:10000 -n maria_dev
            - test the following commands:
                        select * from foodmart.customer limit 10;
                        select * from foodmart.account limit 10;
                        select * from trucks;
                        show tables;
                        !help
                        !tables
                        !describe trucks
            - Running hive queries from the Beeline shell is much faster than through the DAS (Data Analytics Studio) UI.
                        because Hive runs the query directory in hadoop whereas in DAS, the query must be accepted by a rest server before it can submitted to hadoop.

      - Analyse the truck data:
            - In DAS, select 'Compose' and execute:
                    - CREATE TABLE truckmileage STORED AS ORC AS SELECT truckid, driverid, rdate, miles, gas, miles / gas mpg FROM trucks LATERAL VIEW stack(54, 'jun13',jun13_miles,jun13_gas,'may13',may13_miles,may13_gas,'apr13',apr13_miles,apr13_gas,'mar13',mar13_miles,mar13_gas,'feb13',feb13_miles,feb13_gas,'jan13',jan13_miles,jan13_gas,'dec12',dec12_miles,dec12_gas,'nov12',nov12_miles,nov12_gas,'oct12',oct12_miles,oct12_gas,'sep12',sep12_miles,sep12_gas,'aug12',aug12_miles,aug12_gas,'jul12',jul12_miles,jul12_gas,'jun12',jun12_miles,jun12_gas,'may12',may12_miles,may12_gas,'apr12',apr12_miles,apr12_gas,'mar12',mar12_miles,mar12_gas,'feb12',feb12_miles,feb12_gas,'jan12',jan12_miles,jan12_gas,'dec11',dec11_miles,dec11_gas,'nov11',nov11_miles,nov11_gas,'oct11',oct11_miles,oct11_gas,'sep11',sep11_miles,sep11_gas,'aug11',aug11_miles,aug11_gas,'jul11',jul11_miles,jul11_gas,'jun11',jun11_miles,jun11_gas,'may11',may11_miles,may11_gas,'apr11',apr11_miles,apr11_gas,'mar11',mar11_miles,mar11_gas,'feb11',feb11_miles,feb11_gas,'jan11',jan11_miles,jan11_gas,'dec10',dec10_miles,dec10_gas,'nov10',nov10_miles,nov10_gas,'oct10',oct10_miles,oct10_gas,'sep10',sep10_miles,sep10_gas,'aug10',aug10_miles,aug10_gas,'jul10',jul10_miles,jul10_gas,'jun10',jun10_miles,jun10_gas,'may10',may10_miles,may10_gas,'apr10',apr10_miles,apr10_gas,'mar10',mar10_miles,mar10_gas,'feb10',feb10_miles,feb10_gas,'jan10',jan10_miles,jan10_gas,'dec09',dec09_miles,dec09_gas,'nov09',nov09_miles,nov09_gas,'oct09',oct09_miles,oct09_gas,'sep09',sep09_miles,sep09_gas,'aug09',aug09_miles,aug09_gas,'jul09',jul09_miles,jul09_gas,'jun09',jun09_miles,jun09_gas,'may09',may09_miles,may09_gas,'apr09',apr09_miles,apr09_gas,'mar09',mar09_miles,mar09_gas,'feb09',feb09_miles,feb09_gas,'jan09',jan09_miles,jan09_gas ) dummyalias AS rdate, miles, gas;
                    - select * from truckmileage limit 100;
                    - SELECT truckid, avg(mpg) avgmpg FROM truckmileage GROUP BY truckid;
            - Note that you can:
                    - 'Save as' queries for future re-use.
                    - Save results of a query into a table so the result set becomes persistent:
                            - CREATE TABLE avgmileage STORED AS ORC AS SELECT truckid, avg(mpg) avgmpg FROM truckmileage GROUP BY truckid;
                            - SELECT * FROM avgmileage LIMIT 100;
                            - CREATE TABLE DriverMileage STORED AS ORC AS SELECT driverid, sum(miles) totmiles FROM truckmileage GROUP BY driverid;
                            - SELECT * FROM drivermileage;
                                    - store our results onto HDFS:
                                            - click on 'Export data' in the top right0hand corner
                                            - click on 'Save to HDFS'
                                            - store it at /tmp/data/drivermileage

      - Use Spark to compute the risk associated with each driver (The Sandbox includes Spark 2.3.1.):
            - check that Spark2 and Zeppelin Notebook are running:
                    - Log onto the Ambari Dashboard as maria_dev.
                    - At the bottom left corner of the services column, check that Spark2 and Zeppelin Notebook are running.
            - create a Zeppelin Notebook:
                    - Access the Zeppelin interface at http://127.0.0.1:9995/
                    - Click on a Notebook tab in the top left corner and select Create new note. Name your notebook: Compute Risk factor with Spark
            - create a Hive context:
                    - Copy the below in the notebook:
                            %spark2
                            val hiveContext = new org.apache.spark.sql.SparkSession.Builder().getOrCreate()
                    - Press 'Run this paragraph'.
            - use lines below to import .csv data into a data frame without a user defined schema:
                    /**
                     * Let us first see what temporary views already exist on our Sandbox
                     */
                    - hiveContext.sql("SHOW TABLES").show()
                    - val geoLocationDataFrame = spark.read.format("csv").option("header", "true").load("hdfs:///tmp/data/geolocation.csv")
                    /**
                     * Now that we have the data loaded into a DataFrame, we can register a temporary view.
                     */
                    - geoLocationDataFrame.createOrReplaceTempView("geolocation")
                    - hiveContext.sql("SELECT * FROM geolocation LIMIT 15").show()
                    - hiveContext.sql("DESCRIBE geolocation").show()
            - use lines below to import .csv data into a data frame with a user defined schema:
                    /**
                     * The SQL Types library allows us to define the data types of our schema
                     */
                    - import org.apache.spark.sql.types._
                    /**
                     * Recall from the previous tutorial section that the driverid schema only has two relations:
                     * driverid (a String), and totmiles (a Double).
                     */
                    - val drivermileageSchema = new StructType().add("driverid",StringType,true).add("totmiles",DoubleType,true)
                    /**
                    * Now we can populate drivermileageSchema with our CSV files residing in HDFS
                    */
                    - val drivermileageDataFrame = spark.read.format("csv").option("header", "true").schema(drivermileageSchema)load("hdfs:///tmp/data/drivermileage.csv")
                    /**
                    * Finally, let’s create a temporary view
                    */
                    - drivermileageDataFrame.createOrReplaceTempView("drivermileage")
                    /**
                    * We can use SparkSession and SQL to query drivermileage
                    */
                    - hiveContext.sql("SELECT * FROM drivermileage LIMIT 15").show()
            - use lines below to build RDDs:
                    val geolocation_temp0 = hiveContext.sql("SELECT * FROM geolocation")
                    geolocation_temp0.createOrReplaceTempView("geolocation_temp0")

                    val drivermileage_temp0 = hiveContext.sql("SELECT * FROM drivermileage")
                    drivermileage_temp0.createOrReplaceTempView("drivermileage_temp0")

                    /**
                    * SELECT operation is a RDD transformation and therefore does not return anything.
                    */
                    val geolocation_temp1 = hiveContext.sql("SELECT driverid, COUNT(driverid) occurance from geolocation_temp0 WHERE event!='normal' GROUP BY driverid")
                    geolocation_temp1.show(10) --> does NOT show anything so you have to do the below to see results.
                    geolocation_temp1.createOrReplaceTempView("geolocation_temp1")
                    hiveContext.sql("SELECT * FROM geolocation_temp1 LIMIT 15").show()

                    /**
                    * JOIN example
                    */
                    val joined = hiveContext.sql("select a.driverid,a.occurance,b.totmiles from geolocation_temp1 a,drivermileage_temp0 b where a.driverid=b.driverid")
                    joined.createOrReplaceTempView("joined")
                    hiveContext.sql("SELECT * FROM joined LIMIT 10").show()

                    /**
                    * Compute risk factor
                    */
                    val risk_factor_spark = hiveContext.sql("SELECT driverid, occurance, totmiles, totmiles/occurance riskfactor FROM joined")
                    risk_factor_spark.createOrReplaceTempView("risk_factor_spark")
                    hiveContext.sql("SELECT * FROM risk_factor_spark LIMIT 15").show()

                    /**
                    * Save results as a .csv on HDFS: There will be a directory structure with our data under user/maria_dev/data/ named riskfactor. There, we can find our csv file with a Spark auto-generated name.
                    */
                    risk_factor_spark.coalesce(1).write.csv("hdfs:///tmp/data/riskfactor")


- TODOs:
    - TODO: build a Spark app which reads these notifications, persists them in Hive, Cassandra, MariaDB or? so the business can create reports using https://www.tableau.com or similar tool.
    - TODO: build a Spark app which reads these events, aggregates them over a 24-hr period and raises an alert (SMS, email or ?) if more than 3 speeding events for the same truck. Check https://www.baeldung.com/kafka-spark-data-pipeline.
    - TODO: build a Spark app which reads 2 Kafka streams (1 containing Events, 1 containing itemsToDeliver), compares them and publishes notifications to Kafka if an item has not been delivered.
          - try with spark.streaming.DStream
          - try with structured streaming
