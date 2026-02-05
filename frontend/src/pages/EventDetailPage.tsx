// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Event Detail Page
// ═══════════════════════════════════════════════════════════════════════════
import { useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { eventsApi, ticketsApi } from '@/api';
import { useAuth } from '@/contexts/AuthContext';
import { getErrorMessage } from '@/api/client';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import {
  Calendar,
  MapPin,
  Users,
  Ticket,
  ArrowLeft,
  Loader2,
  AlertCircle,
  CheckCircle,
  Clock,
  Share2,
} from 'lucide-react';
import clsx from 'clsx';

export function EventDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, isAuthenticated } = useAuth();

  const [purchaseForm, setPurchaseForm] = useState({
    participantName: user?.name || '',
    participantEmail: user?.email || '',
  });
  const [showPurchaseForm, setShowPurchaseForm] = useState(false);
  const [purchaseSuccess, setPurchaseSuccess] = useState(false);
  const [error, setError] = useState('');

  // Fetch event details
  const { data: event, isLoading } = useQuery({
    queryKey: ['events', id],
    queryFn: () => eventsApi.getById(id!),
    enabled: !!id,
  });

  // Purchase mutation
  const purchaseMutation = useMutation({
    mutationFn: (data: { eventId: string; participantName: string; participantEmail: string }) =>
      ticketsApi.purchase(data),
    onSuccess: () => {
      setPurchaseSuccess(true);
      setShowPurchaseForm(false);
      // Invalida TODAS as queries que começam com 'events'
      queryClient.invalidateQueries({ queryKey: ['events'] });
      queryClient.invalidateQueries({ queryKey: ['tickets'] });
    },
    onError: (err) => {
      setError(getErrorMessage(err));
    },
  });

  const handlePurchase = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!id) return;

    purchaseMutation.mutate({
      eventId: id,
      participantName: purchaseForm.participantName,
      participantEmail: purchaseForm.participantEmail,
    });
  };

  const handleShare = async () => {
    if (navigator.share && event) {
      try {
        await navigator.share({
          title: event.name,
          text: event.description,
          url: window.location.href,
        });
      } catch {
        // User cancelled or error
      }
    } else {
      navigator.clipboard.writeText(window.location.href);
      alert('Link copiado!');
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-[60vh] flex items-center justify-center">
        <Loader2 className="w-10 h-10 text-blue-600 animate-spin" />
      </div>
    );
  }

  if (!event) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-20 text-center">
        <AlertCircle className="w-16 h-16 text-slate-300 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-slate-800 mb-2">Evento não encontrado</h2>
        <p className="text-slate-600 mb-6">O evento que você procura não existe ou foi removido.</p>
        <Link
          to="/events"
          className="inline-flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-xl font-medium hover:bg-blue-700 transition-colors"
        >
          <ArrowLeft className="w-4 h-4" />
          Ver todos os eventos
        </Link>
      </div>
    );
  }

  const eventDate = new Date(event.eventDate);
  const isAvailable = event.availableTickets > 0 && event.status === 'SCHEDULED';
  const isPast = eventDate < new Date();

  const statusColors = {
    SCHEDULED: 'bg-blue-100 text-blue-700',
    ONGOING: 'bg-green-100 text-green-700',
    COMPLETED: 'bg-slate-100 text-slate-700',
    CANCELLED: 'bg-red-100 text-red-700',
  };

  const statusLabels = {
    SCHEDULED: 'Agendado',
    ONGOING: 'Em andamento',
    COMPLETED: 'Encerrado',
    CANCELLED: 'Cancelado',
  };

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10 animate-fade-in">
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-slate-600 hover:text-blue-600 mb-6 transition-colors"
      >
        <ArrowLeft className="w-4 h-4" />
        Voltar
      </button>

      <div className="grid lg:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="lg:col-span-2">
          {/* Image */}
          <div className="relative h-64 sm:h-80 lg:h-96 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-2xl overflow-hidden mb-8">
            {event.imageUrl ? (
              <img
                src={event.imageUrl}
                alt={event.name}
                className="w-full h-full object-cover"
              />
            ) : (
              <div className="flex items-center justify-center h-full">
                <Calendar className="w-24 h-24 text-white/30" />
              </div>
            )}

            {/* Status Badge */}
            <div className="absolute top-4 left-4">
              <span className={clsx('px-3 py-1.5 rounded-full text-sm font-medium', statusColors[event.status])}>
                {statusLabels[event.status]}
              </span>
            </div>

            {/* Share Button */}
            <button
              onClick={handleShare}
              className="absolute top-4 right-4 p-2 bg-white/90 backdrop-blur-sm rounded-full hover:bg-white transition-colors"
            >
              <Share2 className="w-5 h-5 text-slate-700" />
            </button>
          </div>

          {/* Event Details */}
          <h1 className="text-3xl md:text-4xl font-bold text-slate-800 mb-4">{event.name}</h1>

          <div className="flex flex-wrap gap-4 mb-6">
            <div className="flex items-center gap-2 text-slate-600">
              <Calendar className="w-5 h-5 text-blue-500" />
              <span>{format(eventDate, "EEEE, dd 'de' MMMM 'de' yyyy", { locale: ptBR })}</span>
            </div>
            <div className="flex items-center gap-2 text-slate-600">
              <Clock className="w-5 h-5 text-blue-500" />
              <span>{format(eventDate, 'HH:mm')}</span>
            </div>
            <div className="flex items-center gap-2 text-slate-600">
              <MapPin className="w-5 h-5 text-blue-500" />
              <span>{event.location}</span>
            </div>
          </div>

          <div className="prose prose-slate max-w-none">
            <h3 className="text-xl font-semibold text-slate-800 mb-3">Sobre o evento</h3>
            <p className="text-slate-600 leading-relaxed whitespace-pre-wrap">{event.description}</p>
          </div>
        </div>

        {/* Sidebar - Purchase Card */}
        <div className="lg:col-span-1">
          <div className="sticky top-24 bg-white rounded-2xl shadow-lg border border-slate-100 overflow-hidden">
            {/* Price Header */}
            <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-6 text-white">
              <p className="text-blue-100 text-sm mb-1">Preço do ingresso</p>
              <p className="text-4xl font-bold">
                {event.price > 0 ? `R$ ${event.price.toFixed(2)}` : 'Gratuito'}
              </p>
            </div>

            <div className="p-6">
              {/* Availability */}
              <div className="flex items-center justify-between mb-6 pb-6 border-b border-slate-100">
                <div className="flex items-center gap-2 text-slate-600">
                  <Users className="w-5 h-5" />
                  <span>Ingressos disponíveis</span>
                </div>
                <span
                  className={clsx(
                    'font-bold',
                    event.availableTickets === 0 && 'text-red-600',
                    event.availableTickets > 0 && event.availableTickets <= 10 && 'text-amber-600',
                    event.availableTickets > 10 && 'text-green-600'
                  )}
                >
                  {event.availableTickets} / {event.capacity}
                </span>
              </div>

              {/* Success Message */}
              {purchaseSuccess && (
                <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-xl flex items-start gap-3">
                  <CheckCircle className="w-5 h-5 text-green-500 flex-shrink-0 mt-0.5" />
                  <div>
                    <p className="text-green-700 font-medium">Ingresso comprado com sucesso!</p>
                    <Link to="/my-tickets" className="text-green-600 text-sm hover:underline">
                      Ver meus ingressos →
                    </Link>
                  </div>
                </div>
              )}

              {/* Error Message */}
              {error && (
                <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
                  <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
                  <div className="text-red-700 text-sm whitespace-pre-line">{error}</div>
                </div>
              )}

              {/* Purchase Form */}
              {showPurchaseForm && isAuthenticated ? (
                <form onSubmit={handlePurchase} className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      Nome do participante
                    </label>
                    <input
                      type="text"
                      required
                      value={purchaseForm.participantName}
                      onChange={(e) =>
                        setPurchaseForm({ ...purchaseForm, participantName: e.target.value })
                      }
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-2">
                      E-mail do participante
                    </label>
                    <input
                      type="email"
                      required
                      value={purchaseForm.participantEmail}
                      onChange={(e) =>
                        setPurchaseForm({ ...purchaseForm, participantEmail: e.target.value })
                      }
                      className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
                    />
                  </div>

                  <div className="flex gap-3">
                    <button
                      type="button"
                      onClick={() => setShowPurchaseForm(false)}
                      className="flex-1 py-3 border border-slate-200 text-slate-700 rounded-xl font-medium hover:bg-slate-50 transition-all"
                    >
                      Cancelar
                    </button>
                    <button
                      type="submit"
                      disabled={purchaseMutation.isPending}
                      className="flex-1 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-xl font-medium hover:shadow-lg disabled:opacity-50 transition-all flex items-center justify-center gap-2"
                    >
                      {purchaseMutation.isPending ? (
                        <>
                          <Loader2 className="w-5 h-5 animate-spin" />
                          Processando...
                        </>
                      ) : (
                        <>
                          <Ticket className="w-5 h-5" />
                          Confirmar
                        </>
                      )}
                    </button>
                  </div>
                </form>
              ) : (
                <>
                  {!isAuthenticated ? (
                    <div className="space-y-3">
                      <p className="text-slate-600 text-sm text-center mb-4">
                        Faça login para comprar ingressos
                      </p>
                      <Link
                        to="/login"
                        state={{ from: { pathname: `/events/${id}` } }}
                        className="block w-full py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-xl font-medium text-center hover:shadow-lg transition-all"
                      >
                        Fazer login
                      </Link>
                      <Link
                        to="/register"
                        className="block w-full py-3 border border-slate-200 text-slate-700 rounded-xl font-medium text-center hover:bg-slate-50 transition-all"
                      >
                        Criar conta
                      </Link>
                    </div>
                  ) : isPast ? (
                    <div className="text-center py-4">
                      <p className="text-slate-500">Este evento já ocorreu</p>
                    </div>
                  ) : !isAvailable ? (
                    <div className="text-center py-4">
                      <p className="text-red-600 font-medium">Evento esgotado</p>
                    </div>
                  ) : (
                    <button
                      onClick={() => setShowPurchaseForm(true)}
                      className="w-full py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-xl font-medium hover:shadow-lg transition-all flex items-center justify-center gap-2"
                    >
                      <Ticket className="w-5 h-5" />
                      Comprar Ingresso
                    </button>
                  )}
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
