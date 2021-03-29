# Project

> The Data Appliance GX project is intended as a proving ground for GAIA-X and data transfer techologies.

# Getting Started

The project requires JDK 11+. To get started:

``` git clone https://github.com/microsoft/Data-Appliance-GX ```

``` cd Data-Appliance-GX ```

```./gradlew clean shadowJar```

To launch the runtime and client from the root build directory, respectively:

```java -jar runtime/build/libs/dagx-runtime.jar```

```java -jar client/build/libs/dagx-client.jar```

# Build Profiles

The runtime can be configured with custom modules be enabling various build profiles.

By default, no vault is configured. To build with the file system vault, enable the security profile:

```./gradlew -Dsecurity.type=fs clean shadowJar ```

The runtime can then be started from the root clone directory using:

``` java -Ddagx.vault=secrets/dagx-vault.properties -Ddagx.keystore=secrets/dagx-test-keystore.jks -Ddagx.keystore.password=test123 -jar runtime/build/libs/dagx-runtime.jar ```

Note the secrets directory referenced above is configured to be ignored. A test key store and vault must be added (or the launch command modified to point to different locations).
Also, set the keystore password accordingly.

# Building and running with Docker
## Without a security profile
The runtime is containerized so that it can be run inside a docker container. First, the image needs to be built:
```shell
docker build -t microsoft/dagx .
```

Run the container:

```shell
docker run --name dagx -p 8181:8181 microsoft/dagx
```

## With the `fs` security profile
```shell
docker build --build-arg SECURITY=fs -t microsoft/dagx .
```
Run the container:
```shell
docker run --rm --name dagx --mount type=bind,source="$(pwd)"/secrets,target=/etc/dagx/secrets -p 8181:8181 microsoft/dagx
```
Note that when using the `fs` security profile the `--mount` argument is **not** optional as the runtime expects two files to exist at `/etc/dagx/secrets`:
1. `dagx-vault.properties`: the "filesystem vault" that we use for development purposes
1. `dagx-test-keystore.jks`

Consequently, those two files must exist on the host machine at `<pwd>/secrets` or whatever `source` directory that was specificed in the `run` command of the container. 



## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and
actually do, grant us the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., status check, comment). Simply
follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see
the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or
comments.

## Trademarks

This project may contain trademarks or logos for projects, products, or services. Authorized use of Microsoft trademarks or logos is subject to and must follow
[Microsoft's Trademark & Brand Guidelines](https://www.microsoft.com/en-us/legal/intellectualproperty/trademarks/usage/general). Use of Microsoft trademarks or logos in modified
versions of this project must not cause confusion or imply Microsoft sponsorship. Any use of third-party trademarks or logos are subject to those third-party's policies.
