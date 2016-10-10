JBus
====

## Building the app

You can use and to build the .jar

## Using the bus

0. Add jar to libs and attach to your project as library
1. Make event as simple object implements Bus.Event interface or extends SyncEvent for sync responce
2. Send events using Bus.setInstance().post() method
3. All receivers need to inplement Bus.Subscriber interface and define onReceive() method
4. Register your receivers using Bus.setInstance().register() method with evet className
5. Unregister your receivers using Bus.setInstance().unRegister() method.
