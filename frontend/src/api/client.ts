// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Axios API Client
// ═══════════════════════════════════════════════════════════════════════════
import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiError } from '@/types';
// Create axios instance
const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});
// ═══════════════════════════════════════════════════════════════════════════
// Request Interceptor - Add JWT token to requests
// ═══════════════════════════════════════════════════════════════════════════
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);
// ═══════════════════════════════════════════════════════════════════════════
// Response Interceptor - Handle errors globally
// ═══════════════════════════════════════════════════════════════════════════
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    // Handle 401 Unauthorized - Clear token and redirect to login
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      // Only redirect if not already on login page
      if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);
export default api;
// ═══════════════════════════════════════════════════════════════════════════
// Helper to extract error message from API response
// ═══════════════════════════════════════════════════════════════════════════
export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data as ApiError;
    
    // Se houver erros de validação específicos por campo
    if (apiError?.errors && Object.keys(apiError.errors).length > 0) {
      const fieldErrors = Object.entries(apiError.errors)
        .map(([field, message]) => {
          // Traduzir nomes dos campos para português
          const fieldNameMap: Record<string, string> = {
            'name': 'Nome',
            'description': 'Descrição',
            'eventDate': 'Data do Evento',
            'location': 'Local',
            'capacity': 'Capacidade',
            'price': 'Preço',
            'imageUrl': 'URL da Imagem',
            'username': 'Usuário',
            'email': 'Email',
            'password': 'Senha',
            'participantName': 'Nome do Participante',
            'participantEmail': 'Email do Participante',
          };
          
          const translatedField = fieldNameMap[field] || field;
          return `• ${translatedField}: ${message}`;
        })
        .join('\n');
      
      return `Erros:\n${fieldErrors}`;
    }
    
    // Se houver uma mensagem de erro geral
    if (apiError?.message) {
      return apiError.message;
    }
    
    if (error.response?.status === 401) {
      return 'Sessão expirada. Por favor, faça login novamente.';
    }
    if (error.response?.status === 403) {
      return 'Você não tem permissão para realizar esta ação.';
    }
    if (error.response?.status === 404) {
      return 'Recurso não encontrado.';
    }
    if (error.response?.status === 500) {
      return 'Erro interno do servidor. Tente novamente mais tarde.';
    }
    if (error.message === 'Network Error') {
      return 'Erro de conexão. Verifique sua internet.';
    }
  }
  return 'Ocorreu um erro inesperado.';
}
