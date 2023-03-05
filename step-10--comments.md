This step requires local Docker. Will not working with Testcontainers.cloud

Be sure that your Docker use docker-compose v1. Testcontainers now not support v2 because of not handled 
naming convention difference between v1 (Python) and v2 (Go).

Be aware to change in further substep to change given app jar path from _build/libs/workshop.jar_ to _build/libs/test-containers-szjug-workshop.jar_