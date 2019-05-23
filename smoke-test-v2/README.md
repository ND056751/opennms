
# Docker

The tests require Docker images to run.
 
## Pull existing images from DockerHub

You can pull existing images down with:
```
docker pull opennms/horizon-core-web:24.0.0-rc
docker pull opennms/minion:24.0.0-rc
docker pull opennms/sentinel:24.0.0-rc
```

And then tag them for the tests:
```
docker tag opennms/horizon-core-web:24.0.0-rc horizon
docker tag opennms/minion:24.0.0-rc minion
docker tag opennms/sentinel:24.0.0-rc sentinel
```

## Pull images from artifacts

```
wget https://2578-9377198-gh.circle-artifacts.com/0/minion.oci
wget https://2579-9377198-gh.circle-artifacts.com/0/horizon.oci
wget https://2580-9377198-gh.circle-artifacts.com/0/sentinel.oci
```

> See the CricleCI build for the actual artifact URLs

```
docker image load -i horizon.oci
docker image load -i minion.oci
docker image load -i sentinel.oci
```

# Skipping teardown

Set these environment variables for your test run:
```
SKIP_CLEANUP_ON_FAILURE=true
TESTCONTAINERS_RYUK_DISABLED=true
```
