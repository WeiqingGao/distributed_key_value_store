This project implements a multi-threaded Key-Value Store using Java RMI for Remote Procedure Calls (RPC). It allows clients to invoke `put()`, `get()`, and `delete()` methods on the server just like local calls, while automatically handling network communication. Concurrent requests from multiple clients are supported, ensuring that the store can process multiple operations at the same time. 

# Compile and Run Guidance
Navigate to the `src` directory and compile both the KeyValueStoreRMIServer.java and KeyValueStoreRMIClient.java files:
`javac KeyValueStoreRMIServer.java`
`javac KeyValueStoreRMIClient.java`
Run the KeyValueStoreRMIServer.java with a desired port number:
`java KeyValueStoreRMIServer <port>`
If no port specified, it will try to run on port 1099 in default.
Run the KeyValueStoreRMIClient.java with the server's IP address and the port number:
`java KeyValueStoreRMIClient <server_ip> <port>`
Then following the prompt shown and select any option to send an input. 


# Assignment Overview
From my perspective, the objective of the project 2 is to learn how to implement RMI. The details include the remote interface and how to implement it, how to deal with remote objects compared with local objects, especially the use of remote object references. Another goal is to touch the differences between TCP/UDP and RMI. As the upper layer in the architecture, the RMI hides the underlying network details, for example, we don't need to use raw sockets (TCP/UCP) and manual message parsing as we did in project 1. The scope includes 1. RMI, which means a client can invoke any of the three operations remotely on the server just as if it were a local method call (transparency); 2. multi-threading, which means the server must be able to handle concurrent requests from multiple clients at the same time; 3. mutual exclusion, which means that the server must avoid race conditions when implementing the concurrency.

# Technical Impression
During the implementation, I found it is pretty important to clarify how exceptions are thrown and interpreted on the client side. Although both serializaiton and deserialization of the arguments and results of remote invocations are generally carried out automatically by the middleware, it requires me to properly define the exceptions and method signatures to ensure clients receive clear error messages, especially in this project, there is a `MalformedRequestException` in addition to the `RemoteException`. What's more, there are also potential improvement here since I did not take internal server errors, concurrency conflict errors (although I used lock to prevent it) and other potential errors. 

I think it is also crucial to design the proper concurrency control. Because Java RMI automatically spawns multiple threads to handle incoming remote calls from clients, I must implemennt concurrency control, especially when my server uses a shared data structure (the HashMap used to store key-value pair). Based on the fact that this project involves only three operations, I used `ReentrantLock` to avoid the race conditions. But if the throughput is high, then it will not perform well at efficiency. A more detailed lock, for example the read-write locks, can be used to allow certain operations to be parallelized safely. Or using the thread pools to limit the maximum concurrent throughout and implement more complex scheduling. 

# Screenshots - RMI Client
![[Screenshot 2025-02-28 at 10.23.44 PM.png]]
![[Screenshot 2025-02-28 at 10.24.03 PM.png]]

# Screenshots - RMI Server
![[Screenshot 2025-02-28 at 10.23.10 PM.png]]
