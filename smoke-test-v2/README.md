
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

## Pull images from build artifacts

```
export ARTIFACT_URL="https://2866-9377198-gh.circle-artifacts.com/0"
wget $ARTIFACT_URL/horizon.oci
wget $ARTIFACT_URL/minion.oci
wget $ARTIFACT_URL/sentinel.oci
```

> Login to CircleCI and locate the build for the actual artifact URLs

```
docker image load -i horizon.oci
docker image load -i minion.oci
docker image load -i sentinel.oci
```

# Working with Selenium tests

Selenium tests can get finicky and going through the setup and tear down for each test run can get time consuming.
In order to speed up development time, you can setup the environment once by running the main() method in the `OpenNMSSeleniumDebugIT` class.
This method uses the same bootstrap/setup code as the standard Selenium tests do.
Once the environment is ready, you should see output similar to:
```
Web driver is available at: http://localhost:32811/wd/hub
OpenNMS is available at: http://localhost:32808/
```

You can then update your class like so:
```
public class MenuHeaderIT extends OpenNMSSeleniumDebugIT {
  public MenuHeaderIT() {
    super("http://localhost:32811/wd/hub", "http://localhost:32808/");
  }
  ...

```

Running the test will now use the pre-built environment instead of creating a new one each time.
Once your happy with the test, stop the `main()` method, update the class to extend `OpenNMSSeleniumIT` instead and run it again for validation.

# Skipping teardown

Set these environment variables for your test run:
```
SKIP_CLEANUP_ON_FAILURE=true
TESTCONTAINERS_RYUK_DISABLED=true
```
