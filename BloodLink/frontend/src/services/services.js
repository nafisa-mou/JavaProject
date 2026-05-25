import apiClient from './apiClient';

// Authentication Service
export const authService = {
  registerDonor: (data) =>
    apiClient.post('/auth/register-donor', data),

  registerPatient: (data) =>
    apiClient.post('/auth/register-patient', data),

  login: (email, password) =>
    apiClient.post('/auth/login', { email, password }),

  refreshToken: (refreshToken) =>
    apiClient.post('/auth/refresh-token', { refreshToken }),

  changePassword: (userId, currentPassword, newPassword) =>
    apiClient.post(`/auth/change-password/${userId}`, {
      currentPassword,
      newPassword,
    }),

  logout: () => {
    localStorage.removeItem('jwtToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userRole');
  },
};

// Donor Service
export const donorService = {
  getAllDonors: () =>
    apiClient.get('/donors'),

  getDonorById: (id) =>
    apiClient.get(`/donors/${id}`),

  searchByBloodGroup: (bloodGroup) =>
    apiClient.get(`/donors/search?bg=${bloodGroup}`),

  findNearby: (lat, lon, radius = 50) =>
    apiClient.get(`/donors/nearby?lat=${lat}&lon=${lon}&radius=${radius}`),

  getProfile: (id) =>
    apiClient.get(`/donors/${id}/profile`),

  getScore: (id) =>
    apiClient.get(`/donors/${id}/score`),

  checkEligibility: (id) =>
    apiClient.get(`/donors/${id}/eligible`),

  updateProfile: (id, data) =>
    apiClient.put(`/donors/${id}`, data),

  updateAvailability: (id, isAvailable) =>
    apiClient.put(`/donors/${id}/availability`, { isAvailable }),

  recordDonation: (id) =>
    apiClient.post(`/donors/${id}/donation`, {}),
};

// Blood Request Service
export const bloodRequestService = {
  getPendingRequests: () =>
    apiClient.get('/blood-requests/pending'),

  getRequestById: (id) =>
    apiClient.get(`/blood-requests/${id}`),

  getCriticalRequests: () =>
    apiClient.get('/blood-requests/critical'),

  getExpiredRequests: () =>
    apiClient.get('/blood-requests/expired'),

  findSuitableDonors: (requestId) =>
    apiClient.get(`/blood-requests/${requestId}/suitable-donors`),

  getPriorityScore: (requestId) =>
    apiClient.get(`/blood-requests/${requestId}/priority-score`),

  acceptRequest: (id) =>
    apiClient.put(`/blood-requests/${id}/accept`, {}),

  declineRequest: (id) =>
    apiClient.put(`/blood-requests/${id}/decline`, {}),

  completeRequest: (id) =>
    apiClient.put(`/blood-requests/${id}/complete`, {}),

  createRequest: (data) =>
    apiClient.post('/blood-requests', data),

  getStatistics: () =>
    apiClient.get('/blood-requests/statistics'),
};

// Chat Service
export const chatService = {
  getChats: () =>
    apiClient.get('/chats'),

  getChatById: (id) =>
    apiClient.get(`/chats/${id}`),

  startChat: (otherUserId) =>
    apiClient.post('/chats/start', { otherUserId }),

  archiveChat: (id) =>
    apiClient.put(`/chats/${id}/archive`, {}),

  blockChat: (id) =>
    apiClient.put(`/chats/${id}/block`, {}),

  canUsersChat: (userId1, userId2) =>
    apiClient.get(`/chats/can-chat?user1=${userId1}&user2=${userId2}`),

  getStatistics: () =>
    apiClient.get('/chats/statistics'),
};

// Message Service
export const messageService = {
  sendMessage: (chatId, content) =>
    apiClient.post('/messages', { chatId, content }),

  getChatMessages: (chatId) =>
    apiClient.get(`/messages/chat/${chatId}`),

  markAsSeen: (messageIds) =>
    apiClient.put('/messages/seen', { messageIds }),

  deleteMessage: (id) =>
    apiClient.delete(`/messages/${id}`),

  getUnreadCount: () =>
    apiClient.get('/messages/unread-count'),

  searchMessages: (query) =>
    apiClient.get(`/messages/search?q=${query}`),
};

// Notification Service
export const notificationService = {
  getNotifications: () =>
    apiClient.get('/notifications'),

  getUnreadNotifications: () =>
    apiClient.get('/notifications/unread'),

  markAsRead: (id) =>
    apiClient.put(`/notifications/${id}/read`, {}),

  markAllAsRead: () =>
    apiClient.put('/notifications/read-all', {}),

  deleteNotification: (id) =>
    apiClient.delete(`/notifications/${id}`),

  getByType: (type) =>
    apiClient.get(`/notifications/type/${type}`),
};

// Search Service
export const searchService = {
  findMatches: (bloodGroup, latitude, longitude, radius = 50) =>
    apiClient.get('/search/match', {
      params: { bloodGroup, latitude, longitude, radius },
    }),

  getEmergencySuggestions: (latitude, longitude) =>
    apiClient.get('/search/emergency', {
      params: { latitude, longitude },
    }),

  getAnalytics: () =>
    apiClient.get('/search/analytics'),
};

// Patient Service
export const patientService = {
  getPatients: () =>
    apiClient.get('/patients'),

  getPatientById: (id) =>
    apiClient.get(`/patients/${id}`),

  getProfile: (id) =>
    apiClient.get(`/patients/${id}/profile`),

  getBloodRequests: (id) =>
    apiClient.get(`/patients/${id}/blood-requests`),

  getMedicalRecords: (id) =>
    apiClient.get(`/patients/${id}/medical-records`),

  searchDonors: (bloodGroup) =>
    apiClient.get(`/patients/${id}/search-donors?bg=${bloodGroup}`),

  updateProfile: (id, data) =>
    apiClient.put(`/patients/${id}`, data),

  addMedicalRecord: (id, data) =>
    apiClient.post(`/patients/${id}/medical-records`, data),

  deleteBloodRequest: (id, requestId) =>
    apiClient.delete(`/patients/${id}/blood-requests/${requestId}`),
};
