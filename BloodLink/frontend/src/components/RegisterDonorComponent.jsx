import React, { useState } from 'react';
import { authService } from '../services/services';
import { useNavigate } from 'react-router-dom';
import { Mail, Lock, User, Droplet, AlertCircle, Check } from 'lucide-react';

export default function RegisterDonorComponent() {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '',
    bloodGroup: 'O+',
    dateOfBirth: '',
    phone: '',
    city: '',
    state: '',
    latitude: '',
    longitude: '',
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const bloodGroups = ['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-'];

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const validateForm = () => {
    if (!formData.firstName || !formData.lastName || !formData.email || !formData.password) {
      setError('Please fill in all required fields');
      return false;
    }

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match');
      return false;
    }

    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters');
      return false;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formData.email)) {
      setError('Invalid email format');
      return false;
    }

    return true;
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    if (!validateForm()) return;

    setLoading(true);

    try {
      const payload = {
        firstName: formData.firstName,
        lastName: formData.lastName,
        email: formData.email,
        password: formData.password,
        bloodGroup: formData.bloodGroup,
        dateOfBirth: formData.dateOfBirth,
        phone: formData.phone,
        city: formData.city,
        state: formData.state,
        latitude: formData.latitude ? parseFloat(formData.latitude) : 0,
        longitude: formData.longitude ? parseFloat(formData.longitude) : 0,
      };

      const response = await authService.registerDonor(payload);
      const { token, refreshToken, userId, role } = response.data;

      localStorage.setItem('jwtToken', token);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('userId', userId);
      localStorage.setItem('userRole', role);

      setSuccess(true);
      setTimeout(() => {
        navigate('/donor/dashboard');
      }, 2000);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Registration failed. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  const getLocationFromCoordinates = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setFormData((prev) => ({
            ...prev,
            latitude: position.coords.latitude.toFixed(6),
            longitude: position.coords.longitude.toFixed(6),
          }));
        },
        (error) => setError('Failed to get your location')
      );
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-red-50 to-red-100 p-4">
      <div className="max-w-2xl mx-auto bg-white rounded-lg shadow-lg p-8">
        <h2 className="text-3xl font-bold text-gray-800 mb-2">Register as Blood Donor</h2>
        <p className="text-gray-600 mb-6">Join our community to save lives</p>

        {error && (
          <div className="mb-6 p-4 bg-red-100 border border-red-400 rounded-lg flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
            <span className="text-red-700">{error}</span>
          </div>
        )}

        {success && (
          <div className="mb-6 p-4 bg-green-100 border border-green-400 rounded-lg flex items-start gap-3">
            <Check className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
            <span className="text-green-700">Registration successful! Redirecting...</span>
          </div>
        )}

        <form onSubmit={handleRegister}>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            {/* First Name */}
            <div>
              <label className="block text-gray-700 mb-2">First Name *</label>
              <div className="flex items-center bg-gray-100 rounded-lg p-3">
                <User className="w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  className="bg-transparent ml-2 flex-1 outline-none text-gray-700"
                  required
                />
              </div>
            </div>

            {/* Last Name */}
            <div>
              <label className="block text-gray-700 mb-2">Last Name *</label>
              <div className="flex items-center bg-gray-100 rounded-lg p-3">
                <User className="w-5 h-5 text-gray-400" />
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  className="bg-transparent ml-2 flex-1 outline-none text-gray-700"
                  required
                />
              </div>
            </div>

            {/* Email */}
            <div className="md:col-span-2">
              <label className="block text-gray-700 mb-2">Email *</label>
              <div className="flex items-center bg-gray-100 rounded-lg p-3">
                <Mail className="w-5 h-5 text-gray-400" />
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  className="bg-transparent ml-2 flex-1 outline-none text-gray-700"
                  required
                />
              </div>
            </div>

            {/* Blood Group */}
            <div>
              <label className="block text-gray-700 mb-2">Blood Group *</label>
              <div className="flex items-center bg-gray-100 rounded-lg p-3">
                <Droplet className="w-5 h-5 text-gray-400" />
                <select
                  name="bloodGroup"
                  value={formData.bloodGroup}
                  onChange={handleChange}
                  className="bg-transparent ml-2 flex-1 outline-none text-gray-700"
                >
                  {bloodGroups.map((bg) => (
                    <option key={bg} value={bg}>
                      {bg}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Date of Birth */}
            <div>
              <label className="block text-gray-700 mb-2">Date of Birth</label>
              <input
                type="date"
                name="dateOfBirth"
                value={formData.dateOfBirth}
                onChange={handleChange}
                className="w-full p-3 border border-gray-300 rounded-lg outline-none focus:border-red-600"
              />
            </div>

            {/* Phone */}
            <div>
              <label className="block text-gray-700 mb-2">Phone Number</label>
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                className="w-full p-3 border border-gray-300 rounded-lg outline-none focus:border-red-600"
              />
            </div>

            {/* City */}
            <div>
              <label className="block text-gray-700 mb-2">City</label>
              <input
                type="text"
                name="city"
                value={formData.city}
                onChange={handleChange}
                className="w-full p-3 border border-gray-300 rounded-lg outline-none focus:border-red-600"
              />
            </div>

            {/* State */}
            <div>
              <label className="block text-gray-700 mb-2">State</label>
              <input
                type="text"
                name="state"
                value={formData.state}
                onChange={handleChange}
                className="w-full p-3 border border-gray-300 rounded-lg outline-none focus:border-red-600"
              />
            </div>

            {/* Password */}
            <div>
              <label className="block text-gray-700 mb-2">Password *</label>
              <div className="flex items-center bg-gray-100 rounded-lg p-3">
                <Lock className="w-5 h-5 text-gray-400" />
                <input
                  type="password"
                  name="password"
                  value={formData.password}
                  onChange={handleChange}
                  className="bg-transparent ml-2 flex-1 outline-none text-gray-700"
                  required
                />
              </div>
            </div>

            {/* Confirm Password */}
            <div>
              <label className="block text-gray-700 mb-2">Confirm Password *</label>
              <div className="flex items-center bg-gray-100 rounded-lg p-3">
                <Lock className="w-5 h-5 text-gray-400" />
                <input
                  type="password"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  className="bg-transparent ml-2 flex-1 outline-none text-gray-700"
                  required
                />
              </div>
            </div>
          </div>

          {/* Location Button */}
          <button
            type="button"
            onClick={getLocationFromCoordinates}
            className="w-full mb-4 bg-gray-200 hover:bg-gray-300 text-gray-800 font-semibold py-2 px-4 rounded-lg transition"
          >
            Use My Current Location
          </button>

          {/* Register Button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-3 px-4 rounded-lg transition disabled:opacity-50"
          >
            {loading ? 'Registering...' : 'Register as Donor'}
          </button>
        </form>

        <div className="mt-6 text-center text-gray-600">
          <p>
            Already have an account?{' '}
            <button
              onClick={() => navigate('/login')}
              className="text-red-600 hover:underline font-semibold"
            >
              Login here
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
