// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Type Definitions
// ═══════════════════════════════════════════════════════════════════════════

// ═══════════════════════════════════════════════════════════════════════════
// Auth Types
// ═══════════════════════════════════════════════════════════════════════════
export interface User {
  id: string;
  username: string;
  email: string;
  name: string;
  role: 'USER' | 'ADMIN';
}

export interface AuthResponse {
  token: string;
  username: string;
  role: 'USER' | 'ADMIN';
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  name: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Event Types
// ═══════════════════════════════════════════════════════════════════════════
export interface Event {
  id: string;
  name: string;
  description: string;
  eventDate: string;
  location: string;
  capacity: number;
  availableTickets: number;
  price: number;
  imageUrl?: string;
  status: 'SCHEDULED' | 'ONGOING' | 'COMPLETED' | 'CANCELLED';
  ticketsSold: number;
  soldPercentage: number;
  isAvailable: boolean;
  isPast: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEventRequest {
  name: string;
  description: string;
  eventDate: string;
  location: string;
  capacity: number;
  price: number;
  imageUrl?: string;
}

export interface UpdateEventRequest {
  name?: string;
  description?: string;
  eventDate?: string;
  location?: string;
  capacity?: number;
  price?: number;
  imageUrl?: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Ticket Types
// ═══════════════════════════════════════════════════════════════════════════
export interface Ticket {
  id: string;
  confirmationCode: string;
  status: 'ACTIVE' | 'USED' | 'CANCELLED' | 'EXPIRED';
  purchaseDate: string;
  checkInAt?: string | null;
  participantName: string;
  participantEmail: string;
  event: Event;
}

export interface PurchaseTicketRequest {
  eventId: string;
  participantName: string;
  participantEmail: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// Pagination Types
// ═══════════════════════════════════════════════════════════════════════════
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// ═══════════════════════════════════════════════════════════════════════════
// Chat Types
// ═══════════════════════════════════════════════════════════════════════════
export interface ChatMessage {
  id: string;
  content: string;
  role: 'user' | 'assistant';
  timestamp: Date;
}

export interface ChatRequest {
  message: string;
  conversationId?: string;
}

export interface ChatResponse {
  response: string;
  conversationId: string;
}

// ═══════════════════════════════════════════════════════════════════════════
// API Error Types
// ═══════════════════════════════════════════════════════════════════════════
export interface ApiError {
  status: number;
  message: string;
  timestamp: string;
  path: string;
  errors?: Record<string, string>;
}
