// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Admin Page
// ═══════════════════════════════════════════════════════════════════════════
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { eventsApi } from '@/api';
import { getErrorMessage } from '@/api/client';
import { format } from 'date-fns';
import {
  Plus,
  Pencil,
  Trash2,
  Loader2,
  AlertCircle,
  CheckCircle,
  X,
  Calendar,
  MapPin,
  Users,
  DollarSign,
} from 'lucide-react';
import type { Event, CreateEventRequest, UpdateEventRequest } from '@/types';
import clsx from 'clsx';

interface EventFormData {
  name: string;
  description: string;
  eventDate: string;
  location: string;
  capacity: string;
  price: string;
  imageUrl: string;
}

const emptyForm: EventFormData = {
  name: '',
  description: '',
  eventDate: '',
  location: '',
  capacity: '',
  price: '',
  imageUrl: '',
};

export function AdminPage() {
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [editingEvent, setEditingEvent] = useState<Event | null>(null);
  const [formData, setFormData] = useState<EventFormData>(emptyForm);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  // Fetch events
  const { data: eventsPage, isLoading } = useQuery({
    queryKey: ['events', 'admin'],
    queryFn: () => eventsApi.getAll(0, 100, 'createdAt', 'desc'),
  });

  // Create mutation
  const createMutation = useMutation({
    mutationFn: (data: CreateEventRequest) => eventsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
      setShowForm(false);
      setFormData(emptyForm);
      setSuccess('Evento criado com sucesso!');
      setTimeout(() => setSuccess(''), 3000);
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: string; data: UpdateEventRequest }) => eventsApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
      setShowForm(false);
      setEditingEvent(null);
      setFormData(emptyForm);
      setSuccess('Evento atualizado com sucesso!');
      setTimeout(() => setSuccess(''), 3000);
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: eventsApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['events'] });
      setSuccess('Evento excluído com sucesso!');
      setTimeout(() => setSuccess(''), 3000);
    },
    onError: (err) => setError(getErrorMessage(err)),
  });

  const handleEdit = (event: Event) => {
    setEditingEvent(event);
    setFormData({
      name: event.name,
      description: event.description,
      eventDate: event.eventDate.slice(0, 16), // Format for datetime-local
      location: event.location,
      capacity: event.capacity.toString(),
      price: event.price.toString(),
      imageUrl: event.imageUrl || '',
    });
    setShowForm(true);
    setError('');
  };

  const handleDelete = (id: string, name: string) => {
    if (window.confirm(`Tem certeza que deseja excluir o evento "${name}"?`)) {
      deleteMutation.mutate(id);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    const data = {
      name: formData.name,
      description: formData.description,
      eventDate: new Date(formData.eventDate).toISOString(),
      location: formData.location,
      capacity: parseInt(formData.capacity),
      price: parseFloat(formData.price),
      imageUrl: formData.imageUrl || undefined,
    };

    if (editingEvent) {
      updateMutation.mutate({ id: editingEvent.id, data });
    } else {
      createMutation.mutate(data);
    }
  };

  const handleCancel = () => {
    setShowForm(false);
    setEditingEvent(null);
    setFormData(emptyForm);
    setError('');
  };

  const isSubmitting = createMutation.isPending || updateMutation.isPending;

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10 animate-fade-in">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold text-slate-800 mb-2">Painel Administrativo</h1>
          <p className="text-slate-600">Gerencie os eventos da plataforma</p>
        </div>
        {!showForm && (
          <button
            onClick={() => {
              setShowForm(true);
              setEditingEvent(null);
              setFormData(emptyForm);
              setError('');
            }}
            className="flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-xl font-medium hover:shadow-lg transition-all"
          >
            <Plus className="w-5 h-5" />
            Novo Evento
          </button>
        )}
      </div>

      {/* Success Message */}
      {success && (
        <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-xl flex items-center gap-3">
          <CheckCircle className="w-5 h-5 text-green-500" />
          <p className="text-green-700">{success}</p>
        </div>
      )}

      {/* Error Message */}
      {error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
          <div className="text-red-700 whitespace-pre-line">{error}</div>
        </div>
      )}

      {/* Event Form */}
      {showForm && (
        <div className="bg-white rounded-2xl shadow-lg border border-slate-100 p-8 mb-8">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-bold text-slate-800">
              {editingEvent ? 'Editar Evento' : 'Novo Evento'}
            </h2>
            <button onClick={handleCancel} className="text-slate-400 hover:text-slate-600">
              <X className="w-6 h-6" />
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid md:grid-cols-2 gap-6">
              {/* Name */}
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  Nome do Evento *
                </label>
                <input
                  type="text"
                  required
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
                  placeholder="Ex: Workshop de React"
                />
              </div>

              {/* Description */}
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  Descrição *
                </label>
                <textarea
                  required
                  rows={4}
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all resize-none"
                  placeholder="Descreva o evento..."
                />
              </div>

              {/* Date */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  <Calendar className="w-4 h-4 inline-block mr-1" />
                  Data e Hora *
                </label>
                <input
                  type="datetime-local"
                  required
                  value={formData.eventDate}
                  onChange={(e) => setFormData({ ...formData, eventDate: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
                />
              </div>

              {/* Location */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  <MapPin className="w-4 h-4 inline-block mr-1" />
                  Local *
                </label>
                <input
                  type="text"
                  required
                  value={formData.location}
                  onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
                  placeholder="Ex: Centro de Convenções"
                />
              </div>

              {/* Capacity */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  <Users className="w-4 h-4 inline-block mr-1" />
                  Capacidade *
                </label>
                <input
                  type="number"
                  required
                  min="1"
                  value={formData.capacity}
                  onChange={(e) => setFormData({ ...formData, capacity: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
                  placeholder="100"
                />
              </div>

              {/* Price */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  <DollarSign className="w-4 h-4 inline-block mr-1" />
                  Preço (R$) *
                </label>
                <input
                  type="number"
                  required
                  min="0"
                  step="0.01"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
                  placeholder="0.00 (gratuito)"
                />
              </div>

              {/* Image URL */}
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-slate-700 mb-2">
                  URL da Imagem (opcional)
                </label>
                <input
                  type="url"
                  value={formData.imageUrl}
                  onChange={(e) => setFormData({ ...formData, imageUrl: e.target.value })}
                  className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all"
                  placeholder="https://exemplo.com/imagem.jpg"
                />
              </div>
            </div>

            {/* Buttons */}
            <div className="flex gap-4 pt-4">
              <button
                type="button"
                onClick={handleCancel}
                className="flex-1 py-3 border border-slate-200 text-slate-700 rounded-xl font-medium hover:bg-slate-50 transition-all"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="flex-1 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-xl font-medium hover:shadow-lg disabled:opacity-50 transition-all flex items-center justify-center gap-2"
              >
                {isSubmitting ? (
                  <>
                    <Loader2 className="w-5 h-5 animate-spin" />
                    Salvando...
                  </>
                ) : editingEvent ? (
                  'Atualizar Evento'
                ) : (
                  'Criar Evento'
                )}
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Events Table */}
      {isLoading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-10 h-10 text-blue-600 animate-spin" />
        </div>
      ) : eventsPage && eventsPage.content.length > 0 ? (
        <div className="bg-white rounded-2xl shadow-lg border border-slate-100 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-slate-50 border-b border-slate-100">
                <tr>
                  <th className="text-left px-6 py-4 text-sm font-semibold text-slate-600">
                    Evento
                  </th>
                  <th className="text-left px-6 py-4 text-sm font-semibold text-slate-600">Data</th>
                  <th className="text-left px-6 py-4 text-sm font-semibold text-slate-600">
                    Local
                  </th>
                  <th className="text-center px-6 py-4 text-sm font-semibold text-slate-600">
                    Ingressos
                  </th>
                  <th className="text-right px-6 py-4 text-sm font-semibold text-slate-600">
                    Preço
                  </th>
                  <th className="text-center px-6 py-4 text-sm font-semibold text-slate-600">
                    Ações
                  </th>
                </tr>
              </thead>
              <tbody>
                {eventsPage.content.map((event) => (
                  <tr key={event.id} className="border-b border-slate-50 hover:bg-slate-50">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-lg flex items-center justify-center flex-shrink-0">
                          {event.imageUrl ? (
                            <img
                              src={event.imageUrl}
                              alt=""
                              className="w-full h-full object-cover rounded-lg"
                            />
                          ) : (
                            <Calendar className="w-5 h-5 text-white" />
                          )}
                        </div>
                        <div>
                          <p className="font-medium text-slate-800">{event.name}</p>
                          <p
                            className={clsx(
                              'text-xs',
                              event.status === 'SCHEDULED' && 'text-blue-600',
                              event.status === 'ONGOING' && 'text-green-600',
                              event.status === 'COMPLETED' && 'text-slate-500',
                              event.status === 'CANCELLED' && 'text-red-600'
                            )}
                          >
                            {event.status === 'SCHEDULED' && 'Agendado'}
                            {event.status === 'ONGOING' && 'Em andamento'}
                            {event.status === 'COMPLETED' && 'Encerrado'}
                            {event.status === 'CANCELLED' && 'Cancelado'}
                          </p>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 text-slate-600">
                      {format(new Date(event.eventDate), 'dd/MM/yyyy HH:mm')}
                    </td>
                    <td className="px-6 py-4 text-slate-600 max-w-[200px] truncate">
                      {event.location}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span
                        className={clsx(
                          'font-medium',
                          event.availableTickets === 0 && 'text-red-600',
                          event.availableTickets > 0 && 'text-slate-800'
                        )}
                      >
                        {event.availableTickets} / {event.capacity}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right text-slate-800 font-medium">
                      {event.price > 0 ? `R$ ${event.price.toFixed(2)}` : 'Grátis'}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex justify-center gap-2">
                        <button
                          onClick={() => handleEdit(event)}
                          className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                          title="Editar"
                        >
                          <Pencil className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => handleDelete(event.id, event.name)}
                          disabled={deleteMutation.isPending}
                          className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50"
                          title="Excluir"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="text-center py-20 bg-slate-50 rounded-2xl">
          <Calendar className="w-16 h-16 text-slate-300 mx-auto mb-4" />
          <h3 className="text-xl font-semibold text-slate-600 mb-2">Nenhum evento cadastrado</h3>
          <p className="text-slate-500">Clique em "Novo Evento" para começar</p>
        </div>
      )}
    </div>
  );
}
