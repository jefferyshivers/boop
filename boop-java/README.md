# Java client
If building this library individually, you may need to build
`:boop-proto` first, as it is a dependency.

This library simply creates a slightly simpler facade around 
the gRPC client API. There are not yet any implementations of
the library beyond the tests.

## API
The main class itself is mostly immutable;; create an 
instance of the `BoopClient` class using the builder.

```java
BoopClient client = BoopClient.builder()
        .host("localhost")
        .port(8080)
        .apiKey("123")
        .build();
```

Start a connection simply calling `connect`, providing
a callback which is invoked on every `onNext` from the 
server-side observer (i.e. incoming "boops").

```java
BoopEvent lastBoop;
client.connect(boop -> {
    lastBoop = boop;
});
```
You can close the connection explicitly and check the 
connection status at any point.
```java
client.isConnected(); // true
client.disconnect();
client.isConnected(); // false
```
