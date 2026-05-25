import React, { useEffect, useState } from 'react';
import { bloodRequestService, donorService } from '../services/services';
import { AlertCircle, CheckCircle, Clock, AlertTriangle } from 'lucide-react';

export default function BloodRequestComponent() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState('all');
  const [selectedRequest, setSelectedRequest] = useState(null);
  const [suitableDonors, setSuitableDonors] = useState([]);

  useEffect(() => {
    loadBloodRequests();
  }, [filter]);

  const loadBloodRequests = async () => {
    setLoading(true);
    try {
      let response;
      switch (filter) {
        case 'critical':
          response = await bloodRequestService.getCriticalRequests();
          break;
        case 'expired':
          response = await bloodRequestService.getExpiredRequests();
          break;
        default:
          response = await bloodRequestService.getPendingRequests();
      }
      setRequests(response.data);
    } catch (error) {
      console.error('Failed to load requests:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadSuitableDonors = async (requestId) => {
    try {
      const response = await bloodRequestService.findSuitableDonors(requestId);
      setSuitableDonors(response.data);
    } catch (error) {
      console.error('Failed to load suitable donors:', error);
    }
  };

  const handleSelectRequest = (request) => {
    setSelectedRequest(request);
    loadSuitableDonors(request.id);
  };

  const acceptRequest = async (requestId) => {
    try {
      await bloodRequestService.acceptRequest(requestId);
      loadBloodRequests();
      setSelectedRequest(null);
    } catch (error) {
      console.error('Failed to accept request:', error);
    }
  };

  const getEmergencyIcon = (level) => {
    switch (level) {
      case 'LIFE_THREATENING':
        return <AlertCircle className="w-6 h-6 text-red-600" />;
      case 'CRITICAL':
        return <AlertTriangle className="w-6 h-6 text-orange-600" />;
      case 'URGENT':
        return <Clock className="w-6 h-6 text-yellow-600" />;
      default:
        return <CheckCircle className="w-6 h-6 text-green-600" />;
    }
  };

  const getEmergencyColor = (level) => {
    switch (level) {
      case 'LIFE_THREATENING':
        return 'bg-red-100 text-red-800';
      case 'CRITICAL':
        return 'bg-orange-100 text-orange-800';
      case 'URGENT':
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-green-100 text-green-800';
    }
  };

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      <h2 className="text-3xl font-bold text-gray-800 mb-6">Blood Requests</h2>

      {/* Filter Buttons */}
      <div className="mb-6 flex gap-4">
        {['all', 'critical', 'expired'].map((filterOption) => (
          <button
            key={filterOption}
            onClick={() => setFilter(filterOption)}
            className={`px-4 py-2 rounded-lg font-semibold transition ${
              filter === filterOption
                ? 'bg-red-600 text-white'
                : 'bg-white text-gray-700 border border-gray-300 hover:border-red-600'
            }`}
          >
            {filterOption.charAt(0).toUpperCase() + filterOption.slice(1)} Requests
          </button>
        ))}
      </div>

      {loading ? (
        <div className="text-center text-gray-600">Loading...</div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Requests List */}
          <div className="lg:col-span-1 space-y-4">
            {requests.map((request) => (
              <div
                key={request.id}
                onClick={() => handleSelectRequest(request)}
                className={`p-4 rounded-lg cursor-pointer transition border-l-4 ${
                  selectedRequest?.id === request.id
                    ? 'bg-white border-l-red-600 shadow-lg'
                    : 'bg-white border-l-gray-300 hover:shadow-md'
                }`}
              >
                <div className="flex items-start justify-between mb-2">
                  <h3 className="font-bold text-gray-800">{request.patientName}</h3>
                  <span className={`text-xs px-2 py-1 rounded-full ${getEmergencyColor(request.emergencyLevel)}`}>
                    {request.emergencyLevel}
                  </span>
                </div>
                <p className="text-sm text-gray-600">
                  Blood Group: <span className="font-bold text-red-600">{request.bloodGroup}</span>
                </p>
                <p className="text-sm text-gray-600">
                  Status: <span className="font-semibold">{request.status}</span>
                </p>
              </div>
            ))}
          </div>

          {/* Request Details and Suitable Donors */}
          {selectedRequest && (
            <div className="lg:col-span-2 space-y-6">
              {/* Request Details */}
              <div className="bg-white p-6 rounded-lg shadow">
                <div className="flex items-start justify-between mb-4">
                  <h3 className="text-2xl font-bold text-gray-800">{selectedRequest.patientName}</h3>
                  {getEmergencyIcon(selectedRequest.emergencyLevel)}
                </div>

                <div className="grid grid-cols-2 gap-4 mb-4">
                  <div>
                    <p className="text-gray-600 text-sm">Blood Group</p>
                    <p className="text-2xl font-bold text-red-600">{selectedRequest.bloodGroup}</p>
                  </div>
                  <div>
                    <p className="text-gray-600 text-sm">Units Needed</p>
                    <p className="text-2xl font-bold text-gray-800">{selectedRequest.unitsNeeded}</p>
                  </div>
                  <div>
                    <p className="text-gray-600 text-sm">Emergency Level</p>
                    <p className={`text-lg font-bold ${getEmergencyColor(selectedRequest.emergencyLevel).split(' ')[1]}`}>
                      {selectedRequest.emergencyLevel}
                    </p>
                  </div>
                  <div>
                    <p className="text-gray-600 text-sm">Status</p>
                    <p className="text-lg font-bold text-gray-800">{selectedRequest.status}</p>
                  </div>
                </div>

                <div className="mb-4">
                  <p className="text-gray-600 text-sm mb-2">Medical Reason</p>
                  <p className="text-gray-800">{selectedRequest.medicalReason}</p>
                </div>

                {selectedRequest.status === 'PENDING' && (
                  <button
                    onClick={() => acceptRequest(selectedRequest.id)}
                    className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded-lg transition"
                  >
                    Accept This Request
                  </button>
                )}
              </div>

              {/* Suitable Donors */}
              <div className="bg-white p-6 rounded-lg shadow">
                <h4 className="text-lg font-bold text-gray-800 mb-4">Suitable Donors ({suitableDonors.length})</h4>

                {suitableDonors.length === 0 ? (
                  <p className="text-gray-600">No suitable donors found at this time.</p>
                ) : (
                  <div className="space-y-3">
                    {suitableDonors.map((donor) => (
                      <div key={donor.id} className="p-3 bg-gray-50 rounded-lg border border-gray-200 hover:border-red-600 transition">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="font-semibold text-gray-800">
                              {donor.firstName} {donor.lastName}
                            </p>
                            <p className="text-sm text-gray-600">
                              {donor.city} • {donor.totalDonations} donations
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="font-bold text-green-600">Score: {donor.matchScore}%</p>
                            <button className="text-red-600 hover:text-red-700 font-semibold text-sm mt-1">
                              Contact
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {!loading && requests.length === 0 && (
        <div className="text-center py-12 text-gray-600">
          <p>No blood requests found.</p>
        </div>
      )}
    </div>
  );
}
