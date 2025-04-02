# 📌 TODO App - Backend (Spring Boot + PostgreSQL + Redis)

## 1️⃣ Introduction

TODO App is a task management system that allows users to create, update, delete, and track tasks. The application supports task dependencies, detects circular dependencies, and sends notifications when task is upcoming or overdue.

The backend is built with **Spring Boot**, using **PostgreSQL** for data storage, **Redis** for caching, and **WebSocket** for real-time notifications.

## 2️⃣ Technologies Used

- **Spring Boot** - Main framework for the backend
- **Spring Data JPA** - Database interaction with PostgreSQL
- **PostgreSQL** - Main database
- **Redis** - Caching to optimize performance
- **Spring Scheduler** - Automating notifications
- **WebSocket** - Real-time notifications to clients
- **Docker** - Deployment and running the application in containers in local environment

## 3️⃣ Running Locally with Docker

**Pre-requisites**: **Docker** and **Docker Compose** must be installed and properly configured on your machine.

To verify, run the following commands:

```bash
docker --version
docker compose version
```

### 3.1 Configure `.env`

Create a `.env` file in the root directory with the following content:

```env
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=root
SPRING_DATASOURCE_DATABASE=todo_app
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/todo_app
SPRING_REDIS_PASSWORD=root
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
```

You can change the values of **SPRING_DATASOURCE_PASSWORD** and **SPRING_REDIS_PASSWORD** to your desired password.

### 3.2 Run Docker Compose

```bash
docker-compose up -d
```
This command will build the images and run the containers in the background. This process may take a few minutes.

### 3.3 Access the Application

- **Application**: [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
- **API Documentation**: [http://localhost:8080/api/v1/swagger-ui/index.html](http://localhost:8080/api/v1/swagger-ui/index.html#/)

## 4️⃣ Deployment On Cloud

- PostgreSQL database is deployed on **Supabase** (https://supabase.com/).
- Redis is deployed on **RedisLabs** (https://redis.io/).
- The backend is deployed on **Render**.
  - **Application**: [https://todo-app-fr56.onrender.com/api/v1](https://todo-app-fr56.onrender.com/api/v1)
  - **API Documentation**: [https://todo-app-fr56.onrender.com/api/v1/swagger-ui/index.html](https://todo-app-fr56.onrender.com/api/v1/swagger-ui/index.html)

## 5️⃣ Main Features

### 5.1 Task Management

- **Create Task**: Create a new task with a title, description, due date, and priority.
- **Update Task**: Update the title, description, due date, and priority of a task.
- **Change Status**: Change the status of a task (PENDING, IN_PROGRESS, DONE).
- **Extend Due Date**: Extend the due date of a task.
- **Delete Task**: Delete a task.
- **Get Task**: Get a task by ID.
- **Get list of Tasks**: Get a list of tasks with optional filters (title, status, priority, due date) and pagination.

### 5.2 Task Dependencies

- **Create Dependency**: Create a dependency between two tasks.
- **Delete Dependency**: Delete a dependency between two tasks.
- **Delete All Dependencies**: Delete all dependencies of a task.
- **Get Dependencies**: Get all dependencies of a task, including the direct and indirect dependencies.

### 5.3 Notifications

- **Upcoming Tasks**: Send notifications to users when a task is upcoming in 1 hour.
- **Overdue Tasks**: Send notifications to users when a task is overdue.

## 6️⃣ Some Technical Details

### 6.1 Circular Dependency Detection

**Depth First Search (DFS)** algorithm is used to detect circular dependencies between tasks. Each task is represented as a node in the graph, and each dependency is represented as a directed edge between two nodes. When a new dependency is created, the algorithm traverses the graph using DFS with recursion and a stack to track visited nodes and their ancestors. If a node is encountered again in the recursion stack, it indicates a cycle (circular dependency), meaning the dependency cannot be added.

### 6.2 Caching with Redis

**Redis** is used to cache the tasks and dependencies to optimize performance. When a task is created, updated, or deleted, the cache is deleted to ensure consistency.

### 6.3 Real-Time Notifications with WebSocket

- Scheduled tasks will be set up when a task is created or updated. The tasks will be executed at 2 different times:
  - **Upcoming Tasks**: Send notifications to users when a task is upcoming in 1 hour.
  - **Overdue Tasks**: Send notifications to users when a task is overdue.
- **WebSocket** is used to send real-time notifications to clients when a task is upcoming or overdue.

##### WebSocket connection is opened at http://localhost:8080/api/v1/ws and the client can subscribe to the topic `/notification/upcoming-tasks`, `/notification/overdue-tasks` to receive upcoming, overdue notifications

To test the WebSocket connection, you can follow the steps below:

1. Install Node.js and npm if you haven't already.
2. Create a folder for client to test and install these packages:

```bash
npm install stompjs sockjs-client
```

3. Create a file `websocket-client.js` with the following content:

```javascript
const Stomp = require('stompjs');
const SockJS = require('sockjs-client');

const socket = new SockJS('http://localhost:8080/api/v1/ws'); 
const stompClient = Stomp.over(socket);

stompClient.connect({}, function (frame) {
    console.log('Connected: ' + frame);

    // Subscribe in channel
    stompClient.subscribe('/notification/upcoming-tasks', function (message) {
        console.log('Upcoming received: ' + message.body);
        // Do something
    });

    stompClient.subscribe('/notification/overdue-tasks', function (message) {
      console.log('Overdue received: ' + message.body);
      // Do something
    });

    stompClient.subscribe('/notification/messages', function (message) {
      console.log('Message received: ' + message.body);
    });

    // Try send first message to server
    stompClient.send('/socket/sendMessage', {}, JSON.stringify({ message: 'Hello from STOMP client' }));
});
```

4. Move to the root folder of server and run the script:

```bash
docker-compose up -d
```

5. Move to the folder where the `websocket-client.js` file is located and run the script:

```bash
node websocket-client.js
```

6. You can see the logs in the console when the connection is established and the first message is sent.

7. Create a task with a due date near but not overdue 1 hour from now to receive the upcoming notification.


### Hope you run the application successfully. Good luck!
