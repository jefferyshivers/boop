# Boop: a gRPC/Pulsar service

This project uses a combination of a gRPC services, protocol buffers, and Pulsar 
events to create a chat-like service without messages--but rather, "boops". 
When a client connects to the service, it will register a new pair of
Pulsar consumer/producer stubs and start passing-through the boops.

## Setup instructions
You'll need Gradle and Java 8 or above to compile the project. In the root directory:

_Compile and test_
```sh
$ ./gradlew build
```

_Start the application_
```sh
$ ./gradlew -p ./boop-server run
```










