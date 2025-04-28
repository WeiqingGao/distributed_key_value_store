# Assignment Overview
From my perspective, this assignment has two main objectives. First, we have learned the theory of how to communicate between a server and a client, this assignment requires us to implement such communication process using programming, focusing on the use of communication protocols. This involves setting up a server and a client, assigning ports, binding sockets to interfaces, using buffer to hold the incoming message, processing client requests, and sending responses. This process helps us understand fundamental networking concepts and learn how to implement reliable data transmission in practical applications. Second, by implementing both UDP and TCP protocols, this assignment allows us to compare their differences in terms of implementation, data transmission mechanisms, error handling, and reliability. UDP, being a connectionless protocol, emphasizes low latency and efficient transmission, whereas TCP, as a connection-oriented protocol, ensures data reliability and integrity. Of course, it also involves some points like the timeout mechanism, which can be used to deal with the blocking method `receive()` of DatagramSocket used by UDP protocol. Through this assignment, we not only became familiar with fundamental network programming techniques but also gained a deeper understanding of the suitability of different protocols in various application scenarios.
# Technical Impression
During this assignment, I gained a deeper understanding of the complexity of network programming, particularly in implementing client-server communication. Regardless of whether I used UDP or TCP, setting up the basic framework was complex but not overly difficult. The key, for me, was to have a clear understanding of the entire communication process; otherwise, it was easy to get lost in implementation details. For example, where should request handling be implemented? At the first place, I placed it as a method of the server. But finally I find that it is a protocol parsing operating, thus I moved it to the Protocol class. And once the server binds to a port and receives a request, how should it extract the received data? How should a `byte[]` be converted into a `Request` (a custom data structure), extract key-value information, and then re-encode it into a `byte[]` for efficient transmission? These questions involved protocol design, data encoding/decoding, and efficient data processing.

During the implementation, I spent a significant amount of time on details like error handling, logging, and timestamp formatting, to ensure that both the server and client logs contained clear, timestamped information that facilitated debugging. Additionally, while comparing UDP and TCP, I observed that TCP provides more reliable data transmission but requires more complex connection management and flow control, whereas UDP offers higher efficiency but lacks reliability. This experience highlighted the importance of choosing the appropriate protocol based on the application's needs.

If I try to implement a chat application, I will use TCP to ensure reliable delivery of messages, maintaining a persistent connection between users to prevent message loss, while I will use UDP for real-time notifications or status updates where speed is more critical than guaranteed delivery.

# Screenshots
## Screenshots - UDP Client
![[Screenshot 2025-02-01 at 10.39.17 PM.png]]

![[Screenshot 2025-02-01 at 10.39.46 PM.png]]
## Screenshots - UDP Server
![[Screenshot 2025-02-01 at 10.40.37 PM.png]]
![[Screenshot 2025-02-01 at 10.40.54 PM.png]]

## Screenshots - TCP Client
![[Screenshot 2025-02-01 at 11.08.31 PM.png]]
![[Screenshot 2025-02-01 at 11.09.19 PM.png]]
## Screenshots - TCP Server
![[Screenshot 2025-02-01 at 11.10.58 PM.png]]
![[Screenshot 2025-02-01 at 11.11.16 PM.png]]