jmeter-websocket-plugin
=======================

Plugin adds several JMeter elements:

* `WebSocket Connection Controller` - open and close websocket connection. Cache received messages until ones will be checked.
* `WebSocket Message Checker` - check messages that were cached in controller since last check
* `WebSocker Message Sender` - send message to server
* `Threads Waiter` - wait until all threads in Thread Group reach this element