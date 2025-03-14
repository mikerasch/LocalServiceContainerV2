# Important Note
This project is still in early production and may contain bugs or incomplete features. We are actively working on improving it, but please use it with caution in production environments. Your feedback and contributions are highly appreciated as we continue to develop and stabilize the system.

# LocalServiceContainer
An example of a modern distributed service registry container designed to handle service discovery, heartbeat management, and status tracking for distributed services.

# Technologies Used

- Redis
- ETCD
- Spring Boot and various spring modules

# Requirements

- Maven 3
- Java 23
- Docker (Easiest way to build and run)

## Architecture Overview

- **Service Registry**: Services are registered with their application name, URL, version, and port. The registry maintains a list of active services and their health status.
- **Heartbeat System**: Services send periodic heartbeat requests to notify the container of their status. If a service fails to send a heartbeat, it is marked as `DOWN`.
- **Event Publishing**: Changes in service statuses (e.g., `HEALTHY` to `DOWN`) are published as events to notify other components of the system.
  - Depending on the transition, certain actions are taken. Ex: If a service goes DOWN, all services depending on said service are notified.
  - For more information on the state transitions and events, see the `fsm.status` package, which handles the finite state machine logic and status change events.
- **Health Check**: The container periodically checks the health of registered services and ensures that only healthy services are active in the registry.
- **Leader-Follower**: Upon startup, the system checks leader-election status. If the service is elected as the **Leader**, it performs operations that are specific to the leader role. If the service is elected as a **Follower**, it limits certain actions based on the follower's restrictions, such as skipping health checks or registration updates. The system handles transitions between these roles dynamically during operation. See the `distributed.election` package for more information.
  - In an ideal world, the network should be set up where reads go to followers and writes go to leaders. However, this technically does not matter as much, as followers can handle writes. In practice, the system ensures that followers can still participate in writing operations without any critical failures.

# Docker

To quickly set up the system, you can use the `standalone-docker-compose.yml` file, which bootstraps all the necessary services and configurations.

For running Postman tests, you can utilize the `postman-docker-compose.yml` file, which sets up the required environment for testing the services via Postman.

# Bugs

If you encounter any bugs or issues while using the LocalServiceContainer, please report them by following these steps:

1) **Create a New Issue**: If your issue is not listed, please open a new issue with the following details:
    - **Description**: A clear and concise description of the problem.
    - **Steps to Reproduce**: If applicable, include a list of steps to reproduce the bug.
    - **Expected Behavior**: Describe what you expected to happen.
    - **Actual Behavior**: Describe what actually happened.
    - **Environment**: Include details about your environment (e.g., OS, Docker version, etc.).
    - **Logs/Stack Traces**: Provide relevant logs or stack traces that may help in diagnosing the problem.

# Suggestions

We welcome suggestions and improvements to the LocalServiceContainer project! If you have an idea for enhancing the functionality or usability of the system, feel free to share it by following these steps:

1) **Submit a New Suggestion**: If your idea is unique, please create a new issue in the repository with the following details:
    - **Title**: A clear and concise title summarizing the suggestion.
    - **Description**: Provide a detailed description of your suggestion and why it would improve the project.
    - **Use Case**: Describe any specific scenarios or use cases where the suggestion would be beneficial.
    - **Additional Information**: Include any additional details, such as alternative solutions or relevant resources, that could help in evaluating the suggestion.

We appreciate all contributions and look forward to making the project even better with your input!