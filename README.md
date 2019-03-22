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
            - copy the assembly jar over to the sandbox:
                    - cd /home/philippe/code/sparkTutorials/target
                    - scp -P 2222 SparkTutorial-1.0-SNAPSHOT.jar root@127.0.0.1:/root
            - submit the app (cmd below is for local mode. Other option = cluster):
                    - ssh into the sandbox: ssh -p 2222 root@127.0.0.1
                    - spark-submit --class "hortonworks.sparktutorial.TheApp" --master local ./SparkTutorial-1.0-SNAPSHOT.jar
            - verify results:
                    - open Ambari at http://127.0.0.1:8080 with maria_dev / maria_dev
                    - menu Files View --> /tmp --> you will find a directory shakespeareWordCount.


- TODOs in order:
    - add a profile so it is easy to switch local run vs sandbox run
    - add a logger to TheApp
    - start at DEPLOYING TO THE CLOUD in https://fr.hortonworks.com/tutorial/setting-up-a-spark-development-environment-with-java/
    - https://fr.hortonworks.com/tutorial/hadoop-tutorial-getting-started-with-hdp/
