# Description

The application is a service that allows storing information in the form of small information cards, consisting of a name and description.
Cards can be united by common tags.

## Usage

One of the main purposes of the service is to separate the application's information messages from the application's code and reuse them.
For example, users have access to the services Service1, Service2, ServiceN.
Each of these services can throw to the user a message about insufficient access rights.
This message is assigned its own ID, such as ABC01, and all services provide the user with this ID with a link to the Glossy location,
where the user can go to get more information about this error.
If the error description needs to be changed, it's enough to simply change the description in Glossy,
and users of all services will immediately have access to up-to-date information.


To create/edit information, it needs to log in under a profile that allows to make changes.
Without authorization, only viewing is available.

## Role model

Authorization is performed by selecting a profile and entering its key on the access mode change page in the service interface.

### Available profiles

- `OBSERVER` - does not require authorization, only viewing of information is available
- `MODERATOR` - authorization by key, creation/editing of information is available
- `ADMIN` - authorization by key, available creation/editing/deletion of information, viewing the history of changes

# Configuration

Service provides default values for all necessary parameters, so you can just launch and use.

*By default, if datasource location not provided, application will create it in current directory and connects to it automatically*.
So you can always backup it or remove as you wish.

## Parameters

The source of the configuration is a file [application.yaml](../../src/main/resources/application.yaml).

To override default values you can set new ones to system environment or just put `.env` file with it in directory, which from you are launching application instance.

**GLOSSY_HTTP_PORT** - HTTP port to access service

**GLOSSY_DATASOURCE_DRIVER** - Kind of database (`h2` and `postgresql` values are currently supported)  
**GLOSSY_DATASOURCE_LOCATION** - Path to database  
**GLOSSY_DATASOURCE_CONNECTION_PARAMETERS** - Additional connection paramters (ssl, etc)  
**GLOSSY_DATASOURCE_LOGIN** - Database username credentials  
**GLOSSY_DATASOURCE_PWD** - Database password credentials

**GLOSSY_SECURITY_PROFILE_MODERATOR_SECRET** - Password of profile 'MODERATOR'  
**GLOSSY_SECURITY_PROFILE_ADMIN_SECRET** - Password of profile 'ADMIN'

**GLOSSY_HISTORY_INFORMATION_NOTES_ENABLED** - Should or not service save history when information cards changes  

# Build

The application is written in Java and requires version 21 or higher.

## JVM

```shell
./mvnw clean package
```

The compiled artifact is located in the `target` directory with the name `glossy-app.jar`.  
This version of the application requires 64Mb of RAM to work correctly.

## Native

The service supports building into a native executable with [GraalVM](https://www.graalvm.org/) installed or without it (Docker installed is required).  
The compiled artifact is located in the `target` directory with the name `glossy-app`.  
This version of the application requires 32Mb of RAM to work correctly.

### With GraalVM installed

```shell
./mvnw clean package -Dnative
```

### With Docker (Container build)

**Produced artifact will be Linux executable**.

```shell
export GLOSSY_NATIVE_CONTAINER_BIULD=true && ./mvnw clean package -Dnative
```

# Containerizing