import React, { useEffect, useState } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { messageService, chatService } from '../services/services';
import { Send, MessageCircle } from 'lucide-react';

export default function ChatComponent() {
  const [chats, setChats] = useState([]);
  const [selectedChat, setSelectedChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [stompClient, setStompClient] = useState(null);
  const [loading, setLoading] = useState(false);
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    loadChats();
    connectWebSocket();

    return () => {
      if (stompClient) {
        stompClient.disconnect();
      }
    };
  }, []);

  useEffect(() => {
    if (selectedChat) {
      loadChatMessages(selectedChat.id);
    }
  }, [selectedChat]);

  const connectWebSocket = () => {
    try {
      const socket = new SockJS('http://localhost:8080/ws/chat');
      const client = Stomp.over(socket);
      const token = localStorage.getItem('jwtToken');

      client.connect(
        { Authorization: `Bearer ${token}` },
        (frame) => {
          console.log('WebSocket connected:', frame);
          setStompClient(client);

          // Subscribe to private notifications
          client.subscribe(`/user/${userId}/queue/chat`, (message) => {
            const receivedMessage = JSON.parse(message.body);
            setMessages((prev) => [...prev, receivedMessage]);
          });
        },
        (error) => {
          console.error('WebSocket error:', error);
        }
      );
    } catch (error) {
      console.error('Connection failed:', error);
    }
  };

  const loadChats = async () => {
    setLoading(true);
    try {
      const response = await chatService.getChats();
      setChats(response.data);
    } catch (error) {
      console.error('Failed to load chats:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadChatMessages = async (chatId) => {
    try {
      const response = await messageService.getChatMessages(chatId);
      setMessages(response.data);

      // Mark messages as seen
      const unreadIds = response.data
        .filter((m) => !m.isSeen && m.senderId !== userId)
        .map((m) => m.id);

      if (unreadIds.length > 0) {
        await messageService.markAsSeen(unreadIds);
      }
    } catch (error) {
      console.error('Failed to load messages:', error);
    }
  };

  const sendMessage = async (e) => {
    e.preventDefault();

    if (!newMessage.trim() || !selectedChat) return;

    try {
      const message = {
        chatId: selectedChat.id,
        content: newMessage,
        senderId: userId,
        timestamp: new Date().toISOString(),
      };

      // Send via WebSocket
      if (stompClient && stompClient.connected) {
        stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
      }

      setNewMessage('');
    } catch (error) {
      console.error('Failed to send message:', error);
    }
  };

  return (
    <div className="flex h-screen bg-white">
      {/* Chat List */}
      <div className="w-1/4 bg-gray-100 border-r border-gray-300 overflow-y-auto">
        <div className="p-4 border-b border-gray-300">
          <h2 className="text-xl font-bold text-gray-800">Messages</h2>
        </div>

        {loading ? (
          <div className="p-4 text-center text-gray-600">Loading...</div>
        ) : (
          <div>
            {chats.map((chat) => (
              <div
                key={chat.id}
                onClick={() => setSelectedChat(chat)}
                className={`p-4 border-b border-gray-200 cursor-pointer transition ${
                  selectedChat?.id === chat.id
                    ? 'bg-red-100 border-l-4 border-red-600'
                    : 'hover:bg-gray-200'
                }`}
              >
                <h3 className="font-semibold text-gray-800">
                  {chat.otherUser?.firstName} {chat.otherUser?.lastName}
                </h3>
                <p className="text-sm text-gray-600 truncate">{chat.lastMessage?.content}</p>
                <p className="text-xs text-gray-500 mt-1">
                  {new Date(chat.lastMessage?.timestamp).toLocaleTimeString()}
                </p>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Chat Window */}
      <div className="w-3/4 flex flex-col">
        {selectedChat ? (
          <>
            {/* Chat Header */}
            <div className="p-4 bg-red-600 text-white border-b border-gray-300">
              <h3 className="text-lg font-bold">
                {selectedChat.otherUser?.firstName} {selectedChat.otherUser?.lastName}
              </h3>
              <p className="text-sm opacity-90">Blood Group: {selectedChat.otherUser?.bloodGroup}</p>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-y-auto p-4 space-y-4">
              {messages.map((message) => (
                <div
                  key={message.id}
                  className={`flex ${
                    message.senderId === userId ? 'justify-end' : 'justify-start'
                  }`}
                >
                  <div
                    className={`p-3 rounded-lg max-w-xs ${
                      message.senderId === userId
                        ? 'bg-red-600 text-white'
                        : 'bg-gray-200 text-gray-800'
                    }`}
                  >
                    <p>{message.content}</p>
                    <p className="text-xs opacity-70 mt-1">
                      {new Date(message.timestamp).toLocaleTimeString()}
                    </p>
                  </div>
                </div>
              ))}
            </div>

            {/* Message Input */}
            <div className="p-4 bg-gray-50 border-t border-gray-300">
              <form onSubmit={sendMessage} className="flex gap-2">
                <input
                  type="text"
                  value={newMessage}
                  onChange={(e) => setNewMessage(e.target.value)}
                  placeholder="Type a message..."
                  className="flex-1 p-2 border border-gray-300 rounded-lg outline-none focus:border-red-600"
                />
                <button
                  type="submit"
                  className="bg-red-600 hover:bg-red-700 text-white p-2 rounded-lg transition flex items-center gap-2"
                >
                  <Send className="w-5 h-5" />
                </button>
              </form>
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center text-gray-600">
            <div className="text-center">
              <MessageCircle className="w-16 h-16 mx-auto mb-4 text-gray-400" />
              <p>Select a chat to start messaging</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
