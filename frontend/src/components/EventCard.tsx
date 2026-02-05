// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Event Card Component
// ═══════════════════════════════════════════════════════════════════════════
import { Link } from 'react-router-dom';
import { Calendar, MapPin, Users, Ticket } from 'lucide-react';
import { format } from 'date-fns';
import { ptBR } from 'date-fns/locale';
import type { Event } from '@/types';
import clsx from 'clsx';

interface EventCardProps {
  event: Event;
}

export function EventCard({ event }: EventCardProps) {
  const eventDate = new Date(event.eventDate);
  const isAvailable = event.availableTickets > 0;
  const isSoldOut = event.availableTickets === 0;
  const isLowStock = event.availableTickets > 0 && event.availableTickets <= 10;

  const statusColors = {
    SCHEDULED: 'bg-blue-100 text-[#1A3EA1]',
    ONGOING: 'bg-green-100 text-[#2F8F4E]',
    COMPLETED: 'bg-[#F5F5F5] text-[#333333]',
    CANCELLED: 'bg-red-100 text-red-700',
  };

  const statusLabels = {
    SCHEDULED: 'Agendado',
    ONGOING: 'Em andamento',
    COMPLETED: 'Encerrado',
    CANCELLED: 'Cancelado',
  };

  return (
    <Link
      to={`/events/${event.id}`}
      className="group bg-white rounded-xl shadow-sm hover:shadow-xl transition-all duration-300 overflow-hidden border border-[#F5F5F5] hover:border-[#1A3EA1] flex flex-col"
    >
      {/* Image / Placeholder */}
      <div className="relative h-48 bg-gradient-to-br from-[#1A3EA1] to-[#0A1A40] overflow-hidden">
        {event.imageUrl ? (
          <img
            src={event.imageUrl}
            alt={event.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
          />
        ) : (
          <div className="flex items-center justify-center h-full">
            <Calendar className="w-16 h-16 text-white/30" />
          </div>
        )}

        {/* Status Badge */}
        <div className="absolute top-3 left-3">
          <span className={clsx('px-2 py-1 rounded-full text-xs font-medium', statusColors[event.status])}>
            {statusLabels[event.status]}
          </span>
        </div>

        {/* Price Badge */}
        <div className="absolute top-3 right-3 bg-white/90 backdrop-blur-sm px-3 py-1 rounded-full">
          <span className="font-bold text-[#0A1A40]">
            {event.price > 0 ? `R$ ${event.price.toFixed(2)}` : 'Gratuito'}
          </span>
        </div>

        {/* Sold Out Overlay */}
        {isSoldOut && (
          <div className="absolute inset-0 bg-black/60 flex items-center justify-center">
            <span className="bg-red-500 text-white px-4 py-2 rounded-lg font-bold text-lg rotate-[-5deg]">
              ESGOTADO
            </span>
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-5 flex-1 flex flex-col">
        <h3 className="font-bold text-lg text-[#0A1A40] group-hover:text-[#1A3EA1] transition-colors line-clamp-2 mb-2">
          {event.name}
        </h3>

        <p className="text-[#333333] text-sm line-clamp-2 mb-4 flex-1">
          {event.description}
        </p>

        <div className="space-y-2 text-sm">
          {/* Date */}
          <div className="flex items-center gap-2 text-[#333333]">
            <Calendar className="w-4 h-4 text-[#1A3EA1] flex-shrink-0" />
            <span>{format(eventDate, "dd 'de' MMMM 'de' yyyy, HH:mm", { locale: ptBR })}</span>
          </div>

          {/* Location */}
          <div className="flex items-center gap-2 text-[#333333]">
            <MapPin className="w-4 h-4 text-[#1A3EA1] flex-shrink-0" />
            <span className="truncate">{event.location}</span>
          </div>

          {/* Availability */}
          <div className="flex items-center gap-2">
            {isLowStock ? (
              <Ticket className="w-4 h-4 text-amber-500 flex-shrink-0" />
            ) : (
              <Users className="w-4 h-4 text-[#1A3EA1] flex-shrink-0" />
            )}
            <span
              className={clsx(
                isSoldOut && 'text-red-600 font-medium',
                isLowStock && 'text-amber-600 font-medium',
                isAvailable && !isLowStock && 'text-[#333333]'
              )}
            >
              {isSoldOut
                ? 'Esgotado'
                : isLowStock
                ? `Apenas ${event.availableTickets} restantes!`
                : `${event.availableTickets} ingressos disponíveis`}
            </span>
          </div>
        </div>
      </div>

      {/* CTA */}
      <div className="px-5 pb-5">
        <div
          className={clsx(
            'w-full py-2.5 rounded-lg text-center font-medium transition-all',
            isAvailable
              ? 'bg-[#1A3EA1] text-white group-hover:bg-[#0A1A40] group-hover:shadow-lg'
              : 'bg-[#F5F5F5] text-[#333333] cursor-not-allowed'
          )}
        >
          {isAvailable ? 'Ver Detalhes' : 'Indisponível'}
        </div>
      </div>
    </Link>
  );
}
