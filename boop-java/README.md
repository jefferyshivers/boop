# Boop Java client
If building this library individually, you may need to build
`:boop-proto` first, as it is a dependency.

This library creates a friendly facade around 
the gRPC client API. There are not yet any implementations of
the library beyond the tests.

## API
The main class itself is mostly immutable; create an 
instance of the `BoopClient` class using the provided builder.
```java
BoopClient client = BoopClient.builder()
        .host("localhost")
        .port(8080)
        .apiKey("123")
        .build();
```

Start a connection simply calling `connect` with
a callback which is invoked on every `onNext` from the 
server-side observer (i.e. incoming "boops").
```java
BoopEvent lastBoop;
client.connect(boop -> {
    lastBoop = boop;
});
```

Send a boop with `send`:
```java
BoopEvent myBoop = BoopEvent.newBuilder()
        .setUser(BoopUser.newBuilder()
                .setName("Eugene Krabs")
                .build())
        .setMessage("Hello world.")
        .build();
client.send(boop);
```

You can close the connection explicitly and check the 
connection status at any point.
```java
client.isConnected(); // true
client.disconnect();
client.isConnected(); // false
```
