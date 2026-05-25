import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/services';
import { Mail, Lock, AlertCircle } from 'lucide-react';

export default function LoginComponent() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await authService.login(email, password);
      const { token, refreshToken, userId, role, expiresIn } = response.data;

      // Store tokens and user info
      localStorage.setItem('jwtToken', token);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('userId', userId);
      localStorage.setItem('userRole', role);
      localStorage.setItem('tokenExpiration', Date.now() + expiresIn);

      // Redirect based on role
      if (role === 'DONOR') {
        navigate('/donor/dashboard');
      } else if (role === 'PATIENT') {
        navigate('/patient/dashboard');
      } else if (role === 'ADMIN') {
        navigate('/admin/dashboard');
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Login failed. Please check your credentials.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-red-50 to-red-100">
      <div className="bg-white p-8 rounded-lg shadow-lg w-96">
        <h2 className="text-2xl font-bold text-gray-800 mb-6">BloodLink Login</h2>

        {error && (
          <div className="mb-4 p-3 bg-red-100 border border-red-400 rounded-lg flex items-center gap-2">
            <AlertCircle className="w-5 h-5 text-red-600" />
            <span className="text-red-700 text-sm">{error}</span>
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div className="mb-4">
            <label className="block text-gray-700 mb-2">Email</label>
            <div className="flex items-center bg-gray-100 rounded-lg p-3">
              <Mail className="w-5 h-5 text-gray-400" />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="your@email.com"
                className="bg-transparent ml-2 flex-1 outline-none text-gray-700"
                required
              />
            </div>
          </div>

          <div className="mb-6">
            <label className="block text-gray-700 mb-2">Password</label>
            <div className="flex items-center bg-gray-100 rounded-lg p-3">
              <Lock className="w-5 h-5 text-gray-400" />
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                className="bg-transparent ml-2 flex-1 outline-none text-gray-700"
                required
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded-lg transition disabled:opacity-50"
          >
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <div className="mt-6 text-center text-gray-600">
          <p>
            Don't have an account?{' '}
            <button
              onClick={() => navigate('/register')}
              className="text-red-600 hover:underline font-semibold"
            >
              Register here
            </button>
          </p>
        </div>
      </div>
    </div>
  );
}
