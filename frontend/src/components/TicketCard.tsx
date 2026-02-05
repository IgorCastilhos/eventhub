// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Ticket Card Component
// ═══════════════════════════════════════════════════════════════════════════
import { Calendar, MapPin, User, Mail, Hash, Clock } from 'lucide-react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import type { Ticket } from '@/types';
import clsx from 'clsx';

interface TicketCardProps {
  ticket: Ticket;
  onCancel?: (id: string) => void;
}

export function TicketCard({ ticket, onCancel }: TicketCardProps) {
  const eventDate = new Date(ticket.event.eventDate);
  const purchaseDate = new Date(ticket.purchaseDate);
  const isPast = eventDate < new Date();

  const statusColors = {
    ACTIVE: 'bg-green-100 text-green-700 border-green-200',
    USED: 'bg-blue-100 text-blue-700 border-blue-200',
    CANCELLED: 'bg-red-100 text-red-700 border-red-200',
    EXPIRED: 'bg-slate-100 text-slate-700 border-slate-200',
  };

  const statusLabels = {
    ACTIVE: 'Ativo',
    USED: 'Utilizado',
    CANCELLED: 'Cancelado',
    EXPIRED: 'Expirado',
  };

  return (
    <div
      className={clsx(
        'bg-white rounded-xl shadow-sm border overflow-hidden transition-all hover:shadow-md',
        ticket.status === 'CANCELLED' && 'opacity-60'
      )}
    >
      {/* Header with ticket code */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-600 px-5 py-4 text-white">
        <div className="flex justify-between items-start">
          <div>
            <p className="text-blue-100 text-sm mb-1">Código do Ingresso</p>
            <p className="font-mono font-bold text-lg tracking-wider">{ticket.confirmationCode}</p>
          </div>
          <span
            className={clsx(
              'px-3 py-1 rounded-full text-xs font-medium border',
              statusColors[ticket.status]
            )}
          >
            {statusLabels[ticket.status]}
          </span>
        </div>
      </div>

      {/* Event Info */}
      <div className="p-5">
        <h3 className="font-bold text-lg text-slate-800 mb-4">{ticket.event.name}</h3>

        <div className="grid gap-3 text-sm">
          {/* Date */}
          <div className="flex items-center gap-3 text-slate-600">
            <Calendar className="w-4 h-4 text-blue-500 flex-shrink-0" />
            <span>{format(eventDate, "dd 'de' MMMM 'de' yyyy, HH:mm", { locale: ptBR })}</span>
            {isPast && ticket.status === 'ACTIVE' && (
              <span className="text-amber-600 text-xs font-medium">(Evento já ocorreu)</span>
            )}
          </div>

          {/* Location */}
          <div className="flex items-center gap-3 text-slate-600">
            <MapPin className="w-4 h-4 text-blue-500 flex-shrink-0" />
            <span>{ticket.event.location}</span>
          </div>

          {/* Participant Name */}
          <div className="flex items-center gap-3 text-slate-600">
            <User className="w-4 h-4 text-blue-500 flex-shrink-0" />
            <span>{ticket.participantName}</span>
          </div>

          {/* Participant Email */}
          <div className="flex items-center gap-3 text-slate-600">
            <Mail className="w-4 h-4 text-blue-500 flex-shrink-0" />
            <span>{ticket.participantEmail}</span>
          </div>
        </div>

        {/* Footer */}
        <div className="flex justify-between items-center mt-5 pt-4 border-t border-slate-100">
          <div className="flex items-center gap-2 text-slate-500 text-sm">
            <Clock className="w-4 h-4" />
            <span>Comprado em {format(purchaseDate, 'dd/MM/yyyy HH:mm')}</span>
          </div>

          <div className="flex items-center gap-3">
            <span className="font-bold text-slate-800">
              {ticket.event.price > 0 ? `R$ ${ticket.event.price.toFixed(2)}` : 'Gratuito'}
            </span>

            {ticket.status === 'ACTIVE' && !isPast && onCancel && (
              <button
                onClick={() => onCancel(ticket.id)}
                className="px-3 py-1.5 text-sm text-red-600 hover:bg-red-50 rounded-lg transition-colors font-medium"
              >
                Cancelar
              </button>
            )}
          </div>
        </div>
      </div>

      {/* QR Code placeholder - could be real QR in production */}
      <div className="border-t border-dashed border-slate-200 px-5 py-4 bg-slate-50">
        <div className="flex items-center justify-center gap-3">
          <Hash className="w-5 h-5 text-slate-400" />
          <span className="font-mono text-slate-500 text-sm">
            {ticket.confirmationCode}
          </span>
        </div>
      </div>
    </div>
  );
}
