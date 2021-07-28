/*
 * Copyright (c) Microsoft Corporation.
 * All rights reserved.
 */

rootProject.name = "dagx"

// modules for common/util code
include(":common:azure")
include(":common:util")

// EDC core modules
include(":core:bootstrap")
include(":core:iam:iam-mock")
include(":core:iam:oauth2")
include(":core:policy:policy-engine")
include(":core:policy:policy-model")
include(":core:protocol:web")
include(":core:schema")
include(":core:spi")
include(":core:transfer")

// modules for a implementations:minimal installation
include(":implementations:minimal:configuration:configuration-fs")
include(":implementations:minimal:control-http")
include(":implementations:minimal:ids:ids-api-catalog")
include(":implementations:minimal:ids:ids-api-transfer")
include(":implementations:minimal:ids:ids-core")
include(":implementations:minimal:ids:ids-policy-mock")
include(":implementations:minimal:ids:ids-spi")
include(":implementations:minimal:metadata:metadata-memory")
include(":implementations:minimal:policy:policy-registry-memory")
include(":implementations:minimal:runtime")
include(":implementations:minimal:security:security-fs")
include(":implementations:minimal:transfer:transfer-store-memory")

// modules for cloud-provider extensions
include(":extensions:aws:s3:provision")
include(":extensions:aws:s3:s3-schema")
include(":extensions:azure:blob:blob-schema")
include(":extensions:azure:blob:provision")
include(":extensions:azure:events")
include(":extensions:azure:transfer-process-store-cosmos")
include(":extensions:azure:vault")
include(":extensions:catalog-atlas")
include(":extensions:demo:demo-nifi")

// modules for external components, such as NiFi processors
include(":external:nifi:processors")

// modules for code samples
include(":samples:commandline:client")
include(":samples:commandline:client-runtime")
include(":samples:copy-file-to-s3bucket")
include(":samples:copy-with-nifi")
include(":samples:dataseed:dataseed-atlas")
include(":samples:dataseed:dataseed-aws")
include(":samples:dataseed:dataseed-azure")
include(":samples:dataseed:dataseed-nifi")
include(":samples:dataseed:dataseed-policy")
include(":samples:public-rest-api")
include(":samples:run-from-junit")
include(":samples:streaming")
include(":distributions:demo-e2e")