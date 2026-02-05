// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - API Services
// ═══════════════════════════════════════════════════════════════════════════
import api from './client';
import type {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  Event,
  CreateEventRequest,
  UpdateEventRequest,
  Ticket,
  PurchaseTicketRequest,
  Page,
  ChatResponse,
} from '@/types';

// ═══════════════════════════════════════════════════════════════════════════
// Auth API
// ═══════════════════════════════════════════════════════════════════════════
export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/login', data);
    return response.data;
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/register', data);
    return response.data;
  },
};

// ═══════════════════════════════════════════════════════════════════════════
// Events API
// ═══════════════════════════════════════════════════════════════════════════
export const eventsApi = {
  getAll: async (page = 0, size = 20, sortBy = 'eventDate', direction = 'asc'): Promise<Page<Event>> => {
    const response = await api.get<Page<Event>>('/events', {
      params: { page, size, sortBy, direction },
    });
    return response.data;
  },

  getById: async (id: string): Promise<Event> => {
    const response = await api.get<Event>(`/events/${id}`);
    return response.data;
  },

  getUpcoming: async (): Promise<Event[]> => {
    const response = await api.get<Event[]>('/events/upcoming');
    return response.data;
  },

  search: async (query: string): Promise<Event[]> => {
    const response = await api.get<Event[]>('/events/search', {
      params: { q: query },
    });
    return response.data;
  },

  create: async (data: CreateEventRequest): Promise<Event> => {
    const response = await api.post<Event>('/events', data);
    return response.data;
  },

  update: async (id: string, data: UpdateEventRequest): Promise<Event> => {
    const response = await api.patch<Event>(`/events/${id}`, data);
    return response.data;
  },

  delete: async (id: string): Promise<void> => {
    await api.delete(`/events/${id}`);
  },
};

// ═══════════════════════════════════════════════════════════════════════════
// Tickets API
// ═══════════════════════════════════════════════════════════════════════════
export const ticketsApi = {
  purchase: async (data: PurchaseTicketRequest): Promise<Ticket> => {
    const response = await api.post<Ticket>('/tickets/purchase', data);
    return response.data;
  },

  getMyTickets: async (page = 0, size = 10): Promise<Page<Ticket>> => {
    const response = await api.get<Page<Ticket>>('/tickets/my-tickets', {
      params: { page, size },
    });
    return response.data;
  },

  getMyActiveTickets: async (): Promise<Ticket[]> => {
    const response = await api.get<Ticket[]>('/tickets/my-tickets/active');
    return response.data;
  },

  getById: async (id: string): Promise<Ticket> => {
    const response = await api.get<Ticket>(`/tickets/${id}`);
    return response.data;
  },

  cancel: async (id: string): Promise<Ticket> => {
    const response = await api.delete<Ticket>(`/tickets/${id}`);
    return response.data;
  },
};

// ═══════════════════════════════════════════════════════════════════════════
// Chat API (Ollama integration)
// ═══════════════════════════════════════════════════════════════════════════
export const chatApi = {
  sendMessage: async (message: string, conversationId?: string): Promise<ChatResponse> => {
    const response = await api.post<ChatResponse>('/chat', {
      message,
      conversationId,
    });
    return response.data;
  },
};
