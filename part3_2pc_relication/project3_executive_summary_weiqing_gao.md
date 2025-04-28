# Assignment Overview
From my perspective, this assignment extends the previous RMI-based Key-Value Store project by introducing replication and consistency among multiple server instances. That is to say, I touched the implementation of a real distributed system. The objective is to guarantee the availability and the consistency across 5 distinct server replicas. The most critical learning goal of this assignment is the implementation of Two-Phase Commit (2PC) protocol, which is the most commonly used atomic commit protocol. Another important goal is to ensure that replicas can dynamically discover and connect to each other, even if they are started at different times. This involves understanding distributed system bootstrapping, dynamic connection management, and handling temporary failures in replica communication.

Overall, this assignment helps us explore distributed consistency protocols (2PC), replication strategies, and fault recovery mechanisms, extending beyond basic client-server communication to a more realistic distributed system setting.
# Technical Impression
During this assignment, I realized that distributed consistency is a non-trivial problem. Unlike the previous project, where only a single server handled client requests, this project requires coordination and agreement among all replicas before committing any update to the store. To achieve this, I implemented Two-Phase Commit (2PC):
- In Phase 1 (Voting Phase), the server receiving a client request acts as the coordinator, sending a prepare request to all replicas. Each replica replies with an ACK if ready, or a NACK if any issue.
- In Phase 2 (Commit/Abort Phase), based on responses, the coordinator decides whether to commit or abort, and sends the decision to all replicas.
The first impressive point to me is the differences between distributed consistency and fault tolerance. I found that some types of errors should lead to a transaction abort, while some should not. Because some errors are local, or non-fatal. I realized that incorrectly aborting due to transient, non-fatal errors would introduce excessive coordination overhead and unnecessary transaction failures.
Another major technical lesson was managing dynamic replica discovery when servers started at different time. At the very beginning, I did not even realize that this could be an issue. Thus, I started five replicas sequentially by `java KeyValueStoreRMIServer 1099 localhost:1100 localhost:1101 localhost:1102 localhost:1103`. Then I found that each server could only connect to replicas that were already running when it started. Thus, only when the client connects to the last running server, the only one knew all other replicas, the distributed system can work properly. To solve this, I implemented a retry mechanism that allows each server to periodically attempt connections to replicas it previously failed to connect to. At first, I used a Timer to periodically retry connections. However, when all replicas had finally been connected, I wanted to print a success message like `"All replicas found!"`. Unfortunately, this message kept printing repeatedly, because Timer tasks cannot be stopped easily without external control, and my retry loop ran indefinitely even after achieving its goal. To address this, I refactored the retry logic using `ScheduledExecutorService` (Scheduler), through which I can invoke `scheduler.shutdown()` to stop retries, solving the infinite message issue.

Furthermore, I observed that dealing with failures during 2PC is challenging: if one replica is down, should we abort the whole transaction, or wait? Although this project assumes no permanent failures, it would be an import aspect to make improvement in the future.
# Screenshots
The client connects to the server with the port 1102.

first server 1099
![[Screenshot 2025-03-16 at 11.47.24 PM.png]]
![[Screenshot 2025-03-16 at 11.47.06 PM.png]]
second server 1100
![[Screenshot 2025-03-16 at 11.50.03 PM.png]]
third server 1101
![[Screenshot 2025-03-16 at 11.50.37 PM.png]]
forth server 1102
![[Screenshot 2025-03-16 at 11.51.50 PM.png]]
fifth server 1103
![[Screenshot 2025-03-16 at 11.52.06 PM.png]]
client
![[Screenshot 2025-03-16 at 11.52.57 PM.png]]![[Screenshot 2025-03-16 at 11.53.17 PM.png]]