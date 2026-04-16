import { axiosAuth } from './axiosAuth';
import type { StoredAuth } from '../auth/tokenStorage';

interface LoginResponse {
  token: string;
  userId: string;
  username: string;
  expiresAt: string;
}

export const authApi = {
  async register(username: string, password: string): Promise<void> {
    await axiosAuth.post('/auth/register', { username, password });
  },
  async login(username: string, password: string): Promise<StoredAuth> {
    const { data } = await axiosAuth.post<LoginResponse>('/auth/login', { username, password });
    return {
      token: data.token,
      userId: data.userId,
      username: data.username,
      expiresAt: data.expiresAt,
    };
  },
};
