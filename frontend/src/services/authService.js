import axios from 'axios';

export async function refreshTokenLogic() {
  try {
    const response = await axios.post('/api/users/refresh-token');

    if (response.data.JWT) {
      localStorage.setItem('token', response.data.JWT);
      return response.data.JWT;
    } else {
      throw new Error('Refresh failed. New JWT missing.');
    }
  } catch (error) {
    return null;
  }
}

export async function handleLogin(username, password) {
  try {
    const response = await axios.post('/api/users/authenticate', {
      username,
      password
    });

    if (response.data.JWT) {
      localStorage.setItem('token', response.data.JWT);
      return true; // Indicate successful login
    } else {
      console.error('Login failed. JWT or RefreshToken missing.');
      return false;
    }
  } catch (error) {
    console.error('Login error:', error);
    throw error; // Propagate the error for the caller to handle.
  }
}


export async function isTokenValid(token) {
  try {
    const response = await axios.get('/api/ping', {
      headers: {
        'token': `${token}`
      }
    });
    return response.status === 200;
  } catch (error) {
    return false;
  }
}