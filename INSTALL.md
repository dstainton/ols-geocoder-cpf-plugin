# Open Location Service Geocoder CPF Plugin Installation

## Overview

The OLS Geocoder CPF Plugin is a version of the OLS Geocoder service which runs with the Concurrent Processing Framework to provide batch geocoding services.
The plugin is built using Maven, deployed to a maven repository, installed into CPF, and configured. 

Pre-requisites to build and installation include:
- Java 11 (OpenJDK 11 with HotSpot JVM recommended)
- Apache Maven 3.6+
- CPF version 6.0
- Optionally, Apache Cassandra database for configuration
- Pre-existing installation of the OLS Geocoder service (or at least the data directory)

Please review the OLS Geocoder installation documentation before proceeding with installing the CPF plugin.

## Build the Plugin 

You should already have ols-util and ols-geocoder-core built and available in a Maven repository before building the ols-geocoder-cpf plugin.

Fetch the ols-geococer-cpf-plugin code from GitHub:

```
cd <project_dir>
git clone https://github.com/bcgov/ols-geocoder-cpf-plugin.git
```

Build the code with Maven and install into the local Maven repository:

```
cd ols-geocoder-cpf-plugin
mvn clean install
```

## Add the plugin module into CPF

Add the plugin module into CPF as per the directions: https://bcgov.github.io/cpf/admin.html#Add_Module

The Maven module Id is (something like): `ca.bc.gov.ols:ols-geocoder-cpf-plugin:4.0.0-SNAPSHOT`


## Configure the plugin module using CPF Configuration Properties

CPF allows plugin modules to have configuration properties, and provides an admin interface to manage their values.

The ols-geocoder-cpf-plugin has the following configuration properties:

| Prop (`geocoderFactory.*`) | Type | Description |
| ---- | ---- | ----------- |
|`dummyMode`|  true/false | set to true to test if the geocoder plugin module runs without requiring a complete configuration or data directory.|
|`cassandraContactPoint`|  string - defaults to "`cassandra`" | the name of the cassandra server for configuration |
|`cassandraLocalDatacenter`| string - defaults to "`datacenter1`" | the name of the local datacenter for the cassandra load balancing driver|
|`cassandraKeyspace`| string - defaults to "`bgeo`" | the name of the cassandra namespace|
|`cassandraReplicationFactor`| integer - defaults to `2` | the replication factor to set the cassandra namespace to if it has not alrady been created. We recommend creating your cassandra namespace manually before starting the geocoder, to allow for all configuration options |
|`configurationStore`| string - defaults to:<br /> "`ca.bc.gov.ols.geocoder.config`<br />`.CassandraGeocoderConfigurationStore`" | The name of the java class to use for configuration. An alternative would be FileGeocoderConfigurationStore, however this is not yet supported in the CPF plugin.|

## Restart the ols-geocoder-cpf-plugin module

Once the appropriate values have been set in the module's properties, use the CPF admin interface to restart the module. If the startup is not successful, review the CPF logs to determine the cause of the problem. Note that the URL path to the data directory, defined in the Cassandra configuration, must contain a valid geocoder dataset and be accessible to the CPF application server.
