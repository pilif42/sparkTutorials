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
      - Verify the Spash page at http://127.0.0.1:1080/
      - Ambari dashboard at http://127.0.0.1:8080
          - reset the admin password:
              - ssh into the Sandbox
              - ambari-admin-password-reset
          - log as admin and explore the metrics, heatmaps, config.
          - other users:
              - maria_dev	/ maria_dev
                raj_ops	/ raj_ops
                holger_gov / holger_gov
                amy_ds / amy_ds
              - differences explained at https://fr.hortonworks.com/tutorial/learning-the-ropes-of-the-hortonworks-sandbox/
      - Start at OPEN A PORT FOR CUSTOM USE
      

- TODOs in order:
    - https://fr.hortonworks.com/tutorial/setting-up-a-spark-development-environment-with-java
    - https://fr.hortonworks.com/tutorial/hadoop-tutorial-getting-started-with-hdp/
