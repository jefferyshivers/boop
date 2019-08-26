# Boop Server
This is the server-side implementation of the Boop
RPC service defined in `boop-proto`.

Build the project, or build `boop-proto` then this module.
Then start it up to see it running on port `7777`.

In the current project sub-directory:
```sh
$ ./gradlew -p ../boop-proto build
$ ./gradlew build
$ ./gradlew run
```

Note that the `boop-java` module is _not_ a dependency here;
that's a Java client API independent of the actual service/application.

## The events/messaging half of things
You won't need Pulsar running to startup the application, per say,
but you _will_ need it active at `localhost:6650` in order to invoke the
service methods successfully.

I chose to use Pulsar because of it's familiarity and simplicity, 
but if you'd rather use RabbitMQ or some other messaging service,
the Pulsar API has been abstracted from the `boop.events`
package and the rest of the application, to make it easier
to write another implementation if you'd like.
