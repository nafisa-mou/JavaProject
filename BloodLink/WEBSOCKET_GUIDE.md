# WebSocket Implementation Guide

## Overview

BloodLink implements real-time bidirectional communication using Spring WebSocket with STOMP protocol and SockJS fallback. This enables instant messaging between donors and patients, real-time notifications, and live status updates.

## Architecture

```
Client (Browser)
    ↓ WebSocket / SockJS
STOMP Protocol
    ↓
Message Broker
    ↓
    ├── /topic/* (Broadcasting - One to Many)
    ├── /queue/* (Private - One to One)
    └── /app/* (Client → Server)
    ↓
Spring Services
    ↓
Database / Business Logic
```

## Connection Endpoints

### Main Endpoints

```
WebSocket (with SockJS fallback):
- ws://localhost:8080/ws/chat
- ws://localhost:8080/ws/notify
- ws://localhost:8080/ws
```

### CORS Configuration

Origins allowed:
- http://localhost:3000
- http://localhost:8080
- http://localhost:4200

## Message Protocol

### Message Types

```
CHAT                - Chat message between users
TYPING              - Typing indicator
STATUS              - Online/offline status update
NOTIFICATION        - System or alert notification
NOTIFICATION_READ   - Acknowledgement that notification was read
DELIVERY_CONFIRM    - Message delivered confirmation
```

### WebSocketMessage Structure

```json
{
  "messageId": "uuid-123",
  "type": "CHAT",
  "chatId": 1,
  "senderId": 2,
  "senderName": "John Donor",
  "recipientId": 3,
  "recipientName": "Jane Patient",
  "content": "Hello! I can help.",
  "timestamp": "2026-05-25T14:30:00",
  "deliveredAt": "2026-05-25T14:30:01",
  "readAt": "2026-05-25T14:30:05",
  "isTyping": false,
  "status": "ONLINE",
  "payload": {},
  "error": null,
  "isAck": false
}
```

## STOMP Destinations

### Broadcasting Destinations (/topic/*)

One-to-many messaging (all subscribers receive the message):

```
/topic/chat/{chatId}           - Messages in specific chat
/topic/donors/available        - New available donors
/topic/requests/critical       - Critical blood requests
/topic/user/{userId}/status    - User online/offline status
/topic/broadcast               - System-wide broadcasts
/topic/notifications           - General notifications
/topic/alerts                  - System alerts
/topic/connections             - Connection events (admin)
```

### Private Destinations (/queue/*)

One-to-one messaging (only recipient receives):

```
/user/{userId}/queue/messages       - Private messages
/user/{userId}/queue/notifications  - Private notifications
/user/{userId}/queue/typing         - Typing indicators
/user/{userId}/queue/delivery-confirm - Delivery confirmations
/user/{userId}/queue/read-confirm    - Read confirmations
/user/{userId}/queue/errors          - Error messages
/user/{userId}/queue/ack             - Acknowledgements
/user/{userId}/queue/events          - Event notifications
```

### Client-to-Server Destinations (/app/*)

```
/app/message/send                - Send chat message
/app/chat/{chatId}/typing        - Send typing indicator
/app/user/{userId}/online        - Mark user online
/app/user/{userId}/offline       - Mark user offline
/app/message/{messageId}/delivered - Confirm delivery
/app/message/{messageId}/read     - Confirm read
```

## Client Usage Examples

### JavaScript/TypeScript Client

#### 1. Connect to WebSocket

```javascript
// Import SockJS and Stomp
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

// Create connection
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

// Connect
stompClient.connect({}, function(frame) {
    console.log('Connected:', frame);
    
    // Subscribe to user's private messages
    const userId = localStorage.getItem('userId');
    stompClient.subscribe(`/user/${userId}/queue/messages`, function(message) {
        console.log('New message:', JSON.parse(message.body));
    });
    
    // Subscribe to chat
    stompClient.subscribe('/topic/chat/1', function(message) {
        console.log('Chat message:', JSON.parse(message.body));
    });
});
```

#### 2. Send Chat Message

```javascript
const message = {
    messageId: generateUUID(),
    type: 'CHAT',
    chatId: 1,
    senderId: 2,
    senderName: 'John',
    content: 'Hello!',
    timestamp: new Date().toISOString()
};

stompClient.send('/app/message/send', {}, JSON.stringify(message));
```

#### 3. Send Typing Indicator

```javascript
const typingMsg = {
    type: 'TYPING',
    chatId: 1,
    senderId: 2,
    senderName: 'John',
    isTyping: true
};

stompClient.send('/app/chat/1/typing', {}, JSON.stringify(typingMsg));
```

#### 4. Mark User Online

```javascript
const statusMsg = {
    type: 'STATUS',
    senderId: 2,
    senderName: 'John',
    status: 'ONLINE'
};

stompClient.send('/app/user/2/online', {}, JSON.stringify(statusMsg));
```

#### 5. Confirm Message Delivered

```javascript
const confirmMsg = {
    type: 'DELIVERY_CONFIRM',
    messageId: 'uuid-123',
    chatId: 1,
    recipientId: 3,
    isAck: true
};

stompClient.send('/app/message/uuid-123/delivered', {}, JSON.stringify(confirmMsg));
```

#### 6. Disconnect

```javascript
stompClient.disconnect(function() {
    console.log('Disconnected');
});
```

### React Hook Example

```javascript
import { useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

export function useWebSocket(userId) {
    const stompClient = useRef(null);
    const [messages, setMessages] = useCallback(new Map());

    useEffect(() => {
        // Connect
        const socket = new SockJS('http://localhost:8080/ws/chat');
        stompClient.current = Stomp.over(socket);

        stompClient.current.connect({}, () => {
            // Subscribe to messages
            stompClient.current.subscribe(
                `/user/${userId}/queue/messages`,
                (msg) => {
                    const data = JSON.parse(msg.body);
                    setMessages(prev => new Map(prev).set(data.messageId, data));
                }
            );

            // Subscribe to chat
            stompClient.current.subscribe(
                '/topic/chat/1',
                (msg) => {
                    const data = JSON.parse(msg.body);
                    setMessages(prev => new Map(prev).set(data.messageId, data));
                }
            );
        });

        return () => {
            stompClient.current?.disconnect();
        };
    }, [userId]);

    const sendMessage = (content) => {
        stompClient.current?.send('/app/message/send', {}, JSON.stringify({
            messageId: generateUUID(),
            type: 'CHAT',
            chatId: 1,
            senderId: userId,
            senderName: 'User',
            content: content
        }));
    };

    return { messages: Array.from(messages.values()), sendMessage };
}
```

## Backend Integration

### Sending Messages Programmatically

```java
// In a service class
@Autowired
private SimpMessagingTemplate messagingTemplate;

// Send to chat
public void broadcastToChat(Long chatId, WebSocketMessage message) {
    messagingTemplate.convertAndSend("/topic/chat/" + chatId, message);
}

// Send to user
public void sendToUser(Long userId, WebSocketMessage message) {
    messagingTemplate.convertAndSendToUser(
        userId.toString(),
        "/queue/messages",
        message
    );
}

// Notify donor of blood request
public void notifyDonorOfRequest(Long donorId, BloodRequest request) {
    WebSocketMessage msg = WebSocketMessage.createNotification(
        donorId,
        "New Blood Request",
        "Patient needs " + request.getBloodGroup(),
        Map.of("requestId", request.getId())
    );
    messagingTemplate.convertAndSendToUser(
        donorId.toString(),
        "/queue/messages",
        msg
    );
}
```

### Listening to WebSocket Events

```java
@Component
@Slf4j
public class WebSocketEventListener {
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        // User connected
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // User disconnected, cleanup
    }
}
```

## Features

### 1. Real-Time Chat

- **Instant messaging** between donors and patients
- **Delivery confirmation** - know when message reaches recipient
- **Read receipts** - see when message was read
- **Typing indicators** - show when other person is typing

### 2. Online Status

- **Real-time online/offline status** for all users
- **Last seen timestamp** tracking
- **Duration online** calculation
- **Activity statistics** dashboard

### 3. Notifications

- **Instant notifications** for:
  - New blood requests
  - Request accepted/declined
  - New messages
  - User online status changes
  - System alerts

### 4. Broadcasting

- **Critical blood requests** to all nearby donors
- **Available donors** announcement to patients
- **System notifications** to all users
- **Admin monitoring** of connections

## Performance Optimization

### Best Practices

1. **Unsubscribe when leaving** chat/destination
2. **Use message IDs** for tracking and deduplication
3. **Limit message size** (max 5000 chars)
4. **Implement reconnection logic** on client
5. **Clean up user status** on disconnect
6. **Use pagination** for message history (REST, not WebSocket)

### Scaling

For production with multiple server instances:

1. **Replace SimpleBroker** with external message broker:
   ```java
   config.enableStompBrokerRelay("/topic", "/queue")
       .setRelayHost("rabbitmq.example.com")
       .setRelayPort(61613);
   ```

2. **Use RabbitMQ, Kafka, or ActiveMQ** for message distribution
3. **Implement session clustering** for load balancing
4. **Use Redis** for storing session data across servers

## Error Handling

### Common Errors

```javascript
// Connection failed
socket.onerror = function(error) {
    console.error('Connection error:', error);
    // Retry connection
};

// Subscription failed
stompClient.subscribe('/topic/chat/1', (msg) => {}, (error) => {
    console.error('Subscription error:', error);
});

// Send failed
stompClient.send('/app/message/send', {}, data, {
    receipt: 'id-1'
});

stompClient.onreceipt = (frame) => {
    if (frame.headers.id === 'id-1') {
        console.log('Message sent successfully');
    }
};
```

### Server-Side Error Messages

```java
// Send error to user
WebSocketMessage error = WebSocketMessage.createError(
    messageId,
    "Message content cannot be empty"
);
messagingTemplate.convertAndSendToUser(
    userId.toString(),
    "/queue/errors",
    error
);
```

## Security Considerations

1. **Validate user identity** before sending/receiving
2. **Check permissions** for chat access
3. **Sanitize message content** to prevent XSS
4. **Rate limiting** on message frequency
5. **Authenticate WebSocket connection** via JWT in headers
6. **Encrypt messages** in transit (WSS for production)
7. **Log all WebSocket activity** for audit trail

## Testing

### Unit Tests

```java
@WebSocketTest
class ChatWebSocketControllerTest {
    @Autowired
    private WebSocketTestClient webSocketClient;

    @Test
    void testSendMessage() throws Exception {
        webSocketClient.performSubscribe("/topic/chat/1");
        
        WebSocketMessage message = WebSocketMessage.createChatMessage(
            1L, 2L, "John", 3L, "Hello"
        );
        
        webSocketClient.performSend("/app/message/send", message);
        webSocketClient.expectMessage("/topic/chat/1", message);
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {
    // Test full WebSocket lifecycle
}
```

## Configuration

Default configuration in [WebSocketConfig.java](src/main/java/com/bloodlink/config/WebSocketConfig.java):

- **STOMP Broker:** SimpleBroker
- **Application Prefix:** /app
- **User Destination Prefix:** /user
- **Endpoints:** /ws/chat, /ws/notify
- **CORS Origins:** localhost:3000, 8080, 4200
- **SockJS Fallback:** Enabled

## Troubleshooting

### Issue: WebSocket connection fails

**Solution:**
- Check CORS configuration
- Verify firewall/proxy settings
- Ensure SockJS fallback enabled
- Check browser console for errors

### Issue: Messages not received

**Solution:**
- Verify subscription to correct destination
- Check user ID in queue destination
- Ensure message sent to correct topic
- Verify user is still connected

### Issue: Stale user status

**Solution:**
- Implement heartbeat/ping-pong
- Clean up disconnected sessions
- Set appropriate timeout values
- Monitor for zombie connections

## Files

- `WebSocketConfig.java` - STOMP configuration
- `ChatWebSocketController.java` - Message handling
- `UserStatusService.java` - Online status tracking
- `WebSocketEventListener.java` - Connection lifecycle
- `WebSocketMessage.java` - Message protocol DTO
- `WebSocketUtil.java` - Utility functions

## Next Steps

1. Implement React/Vue WebSocket client
2. Add message persistence
3. Implement end-to-end encryption
4. Scale to production message broker
5. Add push notifications via FCM/APNs
6. Implement message archive/search
