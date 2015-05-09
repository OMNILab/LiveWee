KCWEE
=====

Backend logic to manipulate WiFi movement data from Kafka.


How To Run
----------

Compile the project:

    $ sbt assembly

Run the program: sniffing messages from Kafka server and store to Redis db

    $ java -cp kcwee-assembly-1.0.jar cn.edu.sjtu.omnilab.livewee.kc.MobilityInspector


Redis Schema
------------

* total users number: `livewee_user_num`
* all user ID info (hash): `livewee_all_users`
* live user session (hash): `livewee_user_status`
* user session history (list): `livewee_u[uid]_history`
* heatmap snapshots (list): `livewee_heatmap_history`