// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Authentication Context
// ═══════════════════════════════════════════════════════════════════════════
import { createContext, useContext, useState, useCallback, useEffect, type ReactNode } from 'react';
import { authApi } from '@/api';
import type { User, LoginRequest, RegisterRequest } from '@/types';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isLoading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

// ═══════════════════════════════════════════════════════════════════════════
// Auth Provider Component
// ═══════════════════════════════════════════════════════════════════════════
export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Load user from localStorage on mount
  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('token');

    if (storedUser && storedToken) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
      }
    }
    setIsLoading(false);
  }, []);

  // Login handler
  const login = useCallback(async (data: LoginRequest) => {
    const response = await authApi.login(data);
    const user: User = {
      id: '',
      username: response.username,
      email: '',
      name: response.username,
      role: response.role,
    };
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify(user));
    setUser(user);
  }, []);

  // Register handler
  const register = useCallback(async (data: RegisterRequest) => {
    const response = await authApi.register(data);
    const user: User = {
      id: '',
      username: response.username,
      email: data.email,
      name: data.name,
      role: response.role,
    };
    localStorage.setItem('token', response.token);
    localStorage.setItem('user', JSON.stringify(user));
    setUser(user);
  }, []);

  // Logout handler
  const logout = useCallback(() => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isAdmin: user?.role === 'ADMIN',
    isLoading,
    login,
    register,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

// ═══════════════════════════════════════════════════════════════════════════
// useAuth Hook
// ═══════════════════════════════════════════════════════════════════════════
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
