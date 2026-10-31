# StudentManagement-Microservice
A full-stack Student Management System built with Java, Spring Boot Microservices, React.js, MySQL, JWT Authentication, and Google Gemini AI Assistant.
# EduManage – Student Management System

EduManage is a web-based Student Management System built using **Java, Spring Boot, React.js, MySQL, and Microservices Architecture**.

The application provides role-based access for Admin, Faculty, and Students. It helps manage student information, courses, marks, and attendance. It also includes an AI Assistant powered by Google's Gemini API to help users with academic and programming-related questions.

## Features

- **Authentication:** JWT-based login and authentication.
- **Role-based access:** Admin, Faculty, and Student permissions.
- **Student Management:** View and manage student information.
- **Course Management:** Manage course records.
- **Marks Management:** Store and retrieve student marks.
- **Attendance Management:** Track attendance and calculate attendance percentages.
- **AI Assistant:** Ask questions and receive AI-generated explanations.
- **API Gateway:** Central entry point for backend microservices.
- **React Frontend:** Interactive dashboard and user interface.

## Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React.js, Vite, JavaScript, CSS |
| Backend | Java, Spring Boot |
| Architecture | Microservices |
| Database | MySQL |
| Authentication | JWT |
| API Communication | REST APIs |
| AI Integration | Google Gemini API |
| Development Tools | Eclipse, VS Code, Postman, MySQL Workbench |
| Version Control | Git, GitHub |

## Project Architecture

The project contains the following backend services:

| Service | Port | Responsibility |
|---|---:|---|
| API Gateway | 8080 | Routes requests and validates JWT authentication |
| Student Service | 8081 | Student information |
| Course Service | 8082 | Course management |
| Marks Service | 8083 | Student marks |
| Attendance Service | 8084 | Attendance management |
| Auth Service | 8085 | Authentication |
| AI Assistant Service | 8086 | Gemini-powered AI responses |

### Request Flow

```text
                 React Frontend
                  (Port 5173)
                       |
                       v
                  API Gateway
                  (Port 8080)
                       |
       +---------------+----------------+
       |       |       |       |        |
       v       v       v       v        v
    Student  Course  Marks Attendance  Auth
    Service  Service Service Service  Service
      8081    8082    8083    8084     8085
                       |
                       v
                AI Assistant
                  Service
                  (8086)
                       |
                       v
                  Gemini API

             MySQL Database
```

## User Roles

### Admin
- Manage student information.
- Manage courses, marks, and attendance.
- Access the AI Assistant.

### Faculty
- View student information.
- Manage courses, marks, and attendance.
- Access the AI Assistant.

### Student
- View their own student information.
- View their own marks and attendance.
- Access the AI Assistant.

## Getting Started

### Prerequisites

Install the following:

- Java 21
- Node.js and npm
- MySQL Server
- Maven (or use the Maven Wrapper if available)
- Git
- Eclipse or another Java IDE
- VS Code

### 1. Clone the Repository

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
cd D:\StudentManagementSystem-Microservices
```

### 2. Configure MySQL

Create the required databases for your backend services according to their individual database configuration.

Update the database URL, username, and password in the relevant service configuration files.

Do not commit real database credentials.

### 3. Configure Environment Variables

Configure the required environment variables before starting the backend services.

Examples:

```text
GEMINI_API_KEY=your_gemini_api_key
JWT_SECRET=your_jwt_secret
```

Use your actual local values. Never upload API keys, passwords, or production secrets to GitHub.

### 4. Start Backend Services

Start each Spring Boot service using Eclipse or the Maven Wrapper.

Start the services on their configured ports:

```text
Student Service       - 8081
Course Service        - 8082
Marks Service         - 8083
Attendance Service    - 8084
Auth Service          - 8085
AI Assistant Service  - 8086
API Gateway           - 8080
```

Start the API Gateway after configuring the service URLs and JWT settings.

### 5. Start the React Frontend

Open the frontend directory in the terminal:

```bash
npm install
npm run dev
```

Open:

```text
http://localhost:5173
```

## API Endpoints

All frontend requests are routed through the API Gateway.

| Service | Endpoint | Method |
|---|---|---|
| Authentication | `/api/auth/login` | POST |
| Authentication | `/api/auth/me` | GET |
| Students | `/api/students` | GET |
| Courses | `/api/courses` | GET |
| Marks | `/api/marks` | GET |
| Attendance | `/api/attendance` | GET |
| AI Assistant | `/api/ai/chat` | POST |

The AI Assistant accepts a JSON request:

```json
{
  "message": "Explain Java inheritance with an example"
}
```

## AI Assistant

EduManage integrates Google's Gemini API through a dedicated Spring Boot AI Assistant Service.

The assistant can:
- Explain Java concepts.
- Answer programming questions.
- Help students understand academic topics.
- Provide simple examples and explanations.

The AI Assistant currently responds to user questions. Access to private student records must be implemented separately with appropriate authorization.

## Security

- JWT-based authentication.
- Role-based authorization at the API Gateway.
- Students have read-only access to their own academic records.
- Verified identity headers are forwarded by the Gateway.
- API keys and database credentials should be stored securely in environment variables.

## Future Enhancements

- AI-powered academic assistance using authorized student records.
- Chat history and conversation management.
- Additional dashboard analytics.
- Improved notifications.
- More advanced reporting features.

## Author

**Iranna Ammanagi**

Computer Science and Engineering Graduate

- GitHub: https://github.com/irannaammanagi27-png
- LinkedIn: https://www.linkedin.com/in/iranna-ammanagi-764213304/

---

*EduManage – Simplifying Student Management Through Technology.*
