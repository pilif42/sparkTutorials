# sparkTutorials
A repo to keep work related to Apache Spark tutorials.


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
            - on the machine where you plan on submitting your Spark job, run this line from the terminal:
                   - export SPARK_JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8086
                   - This lets you attach a debugger at port 8086. You need to make sure port 8086 is able to receive inbound connections.
                   - with the Hortonworks sandbox:
                        - ssh onto it and run the command.
                        - TODO Error running 'TheAppRemotelyDebugged': Unable to open debugger port (127.0.0.1:8086): java.io.IOException "handshake failed - connection prematurally closed"
            - in IntelliJ, go to Run > Edit Configurations:
                   - click the + button at the upper-left and add a new Remote configuration.
                   - name = TheAppRemotelyDebugged
                   - host = 127.0.0.1 (IP of the sandbox)
                   - port = 8086
                   - Apply
                   - Add a breakpoint in TheApp.java
                   - run this debug conf from your IDE immediately after submitting your Spark job. The debugger will attach and Spark will stop at breakpoints.


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


- TODOs in order:
    - start at LIVE DEBUGGING: port to be open as per 'OPEN A PORT FOR CUSTOM USE' in https://fr.hortonworks.com/tutorial/learning-the-ropes-of-the-hortonworks-sandbox/
    - find a diagram showing edge node - driver node - executor nodes
    - https://fr.hortonworks.com/tutorial/hadoop-tutorial-getting-started-with-hdp/
    - TODO: play with other modes (master = yarn, etc.) so we have at least 1 driver node, 1 executor node
    - TODO: app which reads 2 Kafka streams, compares them and publishes results to Kafka or HDFS.
