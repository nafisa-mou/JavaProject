# BloodLink React Frontend

Complete React client for BloodLink blood donor management platform.

## Features

- **Authentication**: Login/Register for Donors and Patients
- **Donor Search**: Find available donors by blood group and location
- **Blood Requests**: Create and manage urgent blood requests
- **Real-time Chat**: WebSocket-based messaging between donors and patients
- **Notifications**: Real-time notifications for requests and messages
- **Location Services**: Geolocation-based donor search

## Project Structure

```
frontend/
├── public/
│   ├── index.html
│   └── favicon.ico
├── src/
│   ├── components/
│   │   ├── LoginComponent.jsx          # Login form
│   │   ├── RegisterDonorComponent.jsx  # Donor registration
│   │   ├── DonorSearchComponent.jsx    # Find donors
│   │   ├── BloodRequestComponent.jsx   # Manage requests
│   │   ├── ChatComponent.jsx           # Real-time messaging
│   │   └── NotificationComponent.jsx   # Notifications display
│   ├── services/
│   │   ├── apiClient.js                # Axios instance with interceptors
│   │   └── services.js                 # API service methods
│   ├── hooks/
│   │   ├── useAuth.js                  # Authentication hook
│   │   ├── useWebSocket.js             # WebSocket connection hook
│   │   └── useLocation.js              # Geolocation hook
│   ├── context/
│   │   └── AuthContext.js              # Global auth state
│   ├── App.jsx
│   ├── index.js
│   └── App.css
├── package.json
├── .env
└── README.md
```

## Setup Instructions

### 1. Install Dependencies

```bash
npm install
```

### 2. Environment Variables

Create a `.env` file in the frontend directory:

```
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_WS_URL=ws://localhost:8080/ws/chat
REACT_APP_WS_NOTIFY_URL=ws://localhost:8080/ws/notify
```

### 3. Start Development Server

```bash
npm start
```

The frontend will start at `http://localhost:3000`

## API Integration

### Authentication Flow

1. User registers/logs in via `LoginComponent.jsx`
2. JWT token stored in localStorage
3. All API requests include `Authorization: Bearer {token}` header
4. Token expiration triggers auto-refresh via interceptor
5. Failed refresh redirects to login

### WebSocket Connection

```javascript
// ChatComponent.jsx example
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: `Bearer ${token}` },
  (frame) => {
    // Subscribe to chat messages
    stompClient.subscribe(`/user/${userId}/queue/chat`, (message) => {
      // Handle incoming message
    });
  }
);
```

### API Endpoints Used

#### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register-donor` - Donor registration
- `POST /api/auth/register-patient` - Patient registration
- `POST /api/auth/refresh-token` - Refresh JWT token

#### Donor Management
- `GET /api/donors` - List all donors
- `GET /api/donors/{id}` - Get donor profile
- `GET /api/donors/search?bg={bloodGroup}` - Search by blood group
- `GET /api/donors/nearby?lat={lat}&lon={lon}&radius={radius}` - Geolocation search
- `PUT /api/donors/{id}/availability` - Update availability

#### Blood Requests
- `GET /api/blood-requests/pending` - Pending requests
- `GET /api/blood-requests/critical` - Critical requests
- `POST /api/blood-requests` - Create request
- `GET /api/blood-requests/{id}/suitable-donors` - Find matching donors
- `PUT /api/blood-requests/{id}/accept` - Accept request

#### Chat & Messages
- `GET /api/chats` - Get user chats
- `POST /api/chats/start` - Start new chat
- `GET /api/messages/chat/{chatId}` - Get chat messages
- `POST /api/messages` - Send message
- `PUT /api/messages/seen` - Mark as seen

#### Notifications
- `GET /api/notifications` - Get notifications
- `GET /api/notifications/unread` - Get unread count
- `PUT /api/notifications/{id}/read` - Mark as read

## Component Usage

### LoginComponent

```jsx
import LoginComponent from './components/LoginComponent';

function App() {
  return <LoginComponent />;
}
```

**Features:**
- Email/password login
- Error handling
- Auto-redirect based on user role
- Token storage in localStorage

### DonorSearchComponent

```jsx
import DonorSearchComponent from './components/DonorSearchComponent';

function DonorSearch() {
  return <DonorSearchComponent />;
}
```

**Features:**
- Filter by blood group
- Geolocation-based search
- Donor profiles with scores
- Send request button

### BloodRequestComponent

```jsx
import BloodRequestComponent from './components/BloodRequestComponent';

function RequestList() {
  return <BloodRequestComponent />;
}
```

**Features:**
- View pending/critical/expired requests
- Display suitable donors with match scores
- Accept/decline request buttons
- Real-time emergency level indicators

### ChatComponent

```jsx
import ChatComponent from './components/ChatComponent';

function Chat() {
  return <ChatComponent />;
}
```

**Features:**
- Real-time messaging via WebSocket
- Chat list with last message preview
- Typing indicators
- Message read status
- Auto-connect on mount

## State Management

### useAuth Hook

```javascript
const { user, token, login, logout, isAuthenticated } = useAuth();
```

### useWebSocket Hook

```javascript
const { connected, message, send } = useWebSocket('ws://localhost:8080/ws/chat');
```

## Styling

Uses **Tailwind CSS** for all components. Consistent color scheme:
- Primary: Red (#DC2626) for BloodLink branding
- Secondary: Gray for backgrounds and text
- Utility classes for spacing, flexbox, and responsive design

## Error Handling

All API calls include error handling:

```javascript
try {
  const response = await apiClient.get('/donors');
  setDonors(response.data);
} catch (error) {
  const message = error.response?.data?.message || 'An error occurred';
  setError(message);
}
```

## Performance Optimization

- **Code Splitting**: React Router lazy loading for pages
- **Memoization**: useMemo/useCallback for expensive operations
- **Image Lazy Loading**: Intersection Observer API
- **API Caching**: Service methods store data in state

## Testing

```bash
npm test
```

Test components with React Testing Library and Jest.

## Deployment

### Build for Production

```bash
npm run build
```

### Docker Deployment

```dockerfile
FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM node:18-alpine
WORKDIR /app
RUN npm install -g serve
COPY --from=build /app/build ./build
EXPOSE 3000
CMD ["serve", "-s", "build", "-l", "3000"]
```

### Environment-Specific Configuration

Production (.env.production):
```
REACT_APP_API_URL=https://bloodlink-api.example.com/api
REACT_APP_WS_URL=wss://bloodlink-api.example.com/ws/chat
```

## Troubleshooting

### CORS Errors

Ensure backend CORS configuration allows `http://localhost:3000`:

```java
// In SecurityConfig.java
.cors(cors -> cors
  .configurationSource(request -> {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:3000"));
    // ... other settings
    return config;
  })
)
```

### WebSocket Connection Failed

1. Check backend WebSocket is running on `/ws/chat`
2. Verify JWT token is valid and not expired
3. Check browser WebSocket support (use SockJS fallback)

### Token Expiration Loop

If getting stuck in refresh loop:
1. Clear localStorage
2. Re-login
3. Check token expiration time in backend config

## Contributing

Follow React best practices:
- Use functional components with hooks
- Props are immutable
- Lift state to parent components
- Use custom hooks for reusable logic
- Components should be under 300 lines

## License

Apache 2.0
