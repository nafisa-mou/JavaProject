import React, { useEffect, useState } from 'react';
import { donorService } from '../services/services';
import { MapPin, Heart, Check } from 'lucide-react';

export default function DonorSearchComponent() {
  const [donors, setDonors] = useState([]);
  const [filteredDonors, setFilteredDonors] = useState([]);
  const [loading, setLoading] = useState(false);
  const [bloodGroupFilter, setBloodGroupFilter] = useState('');
  const [latitude, setLatitude] = useState(null);
  const [longitude, setLongitude] = useState(null);

  const bloodGroups = ['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-'];

  useEffect(() => {
    fetchDonors();
    getUserLocation();
  }, []);

  useEffect(() => {
    filterDonors();
  }, [donors, bloodGroupFilter]);

  const fetchDonors = async () => {
    setLoading(true);
    try {
      const response = await donorService.getAllDonors();
      setDonors(response.data);
    } catch (error) {
      console.error('Failed to fetch donors:', error);
    } finally {
      setLoading(false);
    }
  };

  const getUserLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLatitude(position.coords.latitude);
          setLongitude(position.coords.longitude);
        },
        (error) => console.error('Location error:', error)
      );
    }
  };

  const filterDonors = () => {
    let filtered = donors;

    if (bloodGroupFilter) {
      filtered = filtered.filter((d) => d.bloodGroup === bloodGroupFilter);
    }

    filtered = filtered.filter((d) => d.isAvailable && d.isActive);
    setFilteredDonors(filtered);
  };

  const findNearbyDonors = async () => {
    if (!latitude || !longitude) {
      alert('Please enable location access');
      return;
    }

    setLoading(true);
    try {
      const response = await donorService.findNearby(latitude, longitude, 50);
      setFilteredDonors(response.data);
    } catch (error) {
      console.error('Failed to find nearby donors:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 bg-gray-50 min-h-screen">
      <h2 className="text-3xl font-bold text-gray-800 mb-6">Find Blood Donors</h2>

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-gray-700 mb-2">Blood Group</label>
            <select
              value={bloodGroupFilter}
              onChange={(e) => setBloodGroupFilter(e.target.value)}
              className="w-full p-2 border border-gray-300 rounded-lg"
            >
              <option value="">All Blood Groups</option>
              {bloodGroups.map((bg) => (
                <option key={bg} value={bg}>
                  {bg}
                </option>
              ))}
            </select>
          </div>

          <div className="flex items-end">
            <button
              onClick={findNearbyDonors}
              className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-lg transition flex items-center justify-center gap-2"
            >
              <MapPin className="w-5 h-5" />
              Find Nearby (50km)
            </button>
          </div>

          <div className="flex items-end">
            <button
              onClick={fetchDonors}
              className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded-lg transition"
            >
              Refresh
            </button>
          </div>
        </div>
      </div>

      {/* Donors List */}
      {loading ? (
        <div className="text-center text-gray-600">Loading...</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredDonors.map((donor) => (
            <div key={donor.id} className="bg-white p-6 rounded-lg shadow hover:shadow-lg transition">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <h3 className="text-lg font-bold text-gray-800">{donor.firstName} {donor.lastName}</h3>
                  <p className="text-sm text-gray-600">{donor.city}, {donor.state}</p>
                </div>
                <span className="bg-red-100 text-red-800 px-3 py-1 rounded-full font-bold">
                  {donor.bloodGroup}
                </span>
              </div>

              <div className="mb-3 space-y-2">
                <p className="text-sm text-gray-700">
                  <Heart className="w-4 h-4 inline mr-2 text-red-600" />
                  {donor.totalDonations} Donations
                </p>
                {donor.reliabilityScore && (
                  <p className="text-sm text-gray-700">
                    Score: <span className="font-bold text-green-600">{donor.reliabilityScore}/100</span>
                  </p>
                )}
              </div>

              <div className="flex gap-2">
                <button className="flex-1 bg-red-600 hover:bg-red-700 text-white py-2 px-4 rounded-lg transition flex items-center justify-center gap-2">
                  <Check className="w-4 h-4" />
                  Send Request
                </button>
                <button className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 py-2 px-4 rounded-lg transition">
                  View Profile
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && filteredDonors.length === 0 && (
        <div className="text-center py-12 text-gray-600">
          <p>No donors found matching your criteria.</p>
        </div>
      )}
    </div>
  );
}
