// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Events List Page
// ═══════════════════════════════════════════════════════════════════════════
import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { eventsApi } from '@/api';
import { EventCard } from '@/components/EventCard';
import { Search, Calendar, Filter, Loader2, ChevronLeft, ChevronRight } from 'lucide-react';
import clsx from 'clsx';

export function EventsPage() {
  const [page, setPage] = useState(0);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('eventDate');
  const [sortDirection, setSortDirection] = useState('asc');

  // Fetch paginated events
  const { data: eventsPage, isLoading } = useQuery({
    queryKey: ['events', page, sortBy, sortDirection],
    queryFn: () => eventsApi.getAll(page, 12, sortBy, sortDirection),
  });

  // Search events
  const { data: searchResults, isLoading: isSearching } = useQuery({
    queryKey: ['events', 'search', searchQuery],
    queryFn: () => eventsApi.search(searchQuery),
    enabled: searchQuery.length >= 2,
  });

  const displayEvents = searchQuery.length >= 2 ? searchResults : eventsPage?.content;
  const showPagination = !searchQuery && eventsPage;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10 animate-fade-in">
      {/* Header */}
      <div className="mb-10">
        <h1 className="text-3xl md:text-4xl font-bold text-[#0A1A40] mb-2">Eventos</h1>
        <p className="text-[#333333]">Encontre eventos incríveis para participar</p>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-4 mb-8">
        {/* Search */}
        <div className="relative flex-1">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#333333]" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Buscar eventos..."
            className="w-full pl-12 pr-4 py-3 rounded-xl border border-[#F5F5F5] focus:border-[#1A3EA1] focus:ring-2 focus:ring-[#1A3EA1]/20 outline-none transition-all"
          />
          {isSearching && (
            <Loader2 className="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 text-[#1A3EA1] animate-spin" />
          )}
        </div>

        {/* Sort */}
        <div className="flex gap-2">
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="px-4 py-3 rounded-xl border border-[#F5F5F5] focus:border-[#1A3EA1] focus:ring-2 focus:ring-[#1A3EA1]/20 outline-none transition-all bg-white text-[#333333]"
          >
            <option value="eventDate">Data</option>
            <option value="name">Nome</option>
            <option value="price">Preço</option>
            <option value="availableTickets">Disponibilidade</option>
          </select>

          <button
            onClick={() => setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc')}
            className="px-4 py-3 rounded-xl border border-[#F5F5F5] hover:bg-[#F5F5F5] transition-colors flex items-center gap-2 text-[#333333]"
          >
            <Filter className="w-4 h-4" />
            {sortDirection === 'asc' ? 'Crescente' : 'Decrescente'}
          </button>
        </div>
      </div>

      {/* Events Grid */}
      {isLoading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-10 h-10 text-[#1A3EA1] animate-spin" />
        </div>
      ) : displayEvents && displayEvents.length > 0 ? (
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {displayEvents.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-[#F5F5F5] rounded-2xl">
          <Calendar className="w-16 h-16 text-[#333333] mx-auto mb-4 opacity-30" />
          <h3 className="text-xl font-semibold text-[#333333] mb-2">
            {searchQuery ? 'Nenhum evento encontrado' : 'Nenhum evento disponível'}
          </h3>
          <p className="text-[#333333]">
            {searchQuery
              ? 'Tente buscar com outros termos'
              : 'Novos eventos serão adicionados em breve!'}
          </p>
        </div>
      )}

      {/* Pagination */}
      {showPagination && eventsPage.totalPages > 1 && (
        <div className="flex justify-center items-center gap-4 mt-10">
          <button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={eventsPage.first}
            className={clsx(
              'flex items-center gap-2 px-4 py-2 rounded-xl font-medium transition-all',
              eventsPage.first
                ? 'bg-[#F5F5F5] text-[#333333] opacity-50 cursor-not-allowed'
                : 'bg-white border border-[#F5F5F5] text-[#333333] hover:bg-[#F5F5F5]'
            )}
          >
            <ChevronLeft className="w-4 h-4" />
            Anterior
          </button>

          <div className="flex items-center gap-2">
            {Array.from({ length: Math.min(5, eventsPage.totalPages) }, (_, i) => {
              let pageNum = i;
              if (eventsPage.totalPages > 5) {
                if (page > 2) {
                  pageNum = page - 2 + i;
                }
                if (pageNum >= eventsPage.totalPages) {
                  pageNum = eventsPage.totalPages - 5 + i;
                }
              }
              return (
                <button
                  key={pageNum}
                  onClick={() => setPage(pageNum)}
                  className={clsx(
                    'w-10 h-10 rounded-xl font-medium transition-all',
                    page === pageNum
                      ? 'bg-[#1A3EA1] text-white'
                      : 'bg-white border border-[#F5F5F5] text-[#333333] hover:bg-[#F5F5F5]'
                  )}
                >
                  {pageNum + 1}
                </button>
              );
            })}
          </div>

          <button
            onClick={() => setPage(Math.min(eventsPage.totalPages - 1, page + 1))}
            disabled={eventsPage.last}
            className={clsx(
              'flex items-center gap-2 px-4 py-2 rounded-xl font-medium transition-all',
              eventsPage.last
                ? 'bg-[#F5F5F5] text-[#333333] opacity-50 cursor-not-allowed'
                : 'bg-white border border-[#F5F5F5] text-[#333333] hover:bg-[#F5F5F5]'
            )}
          >
            Próximo
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Total Events */}
      {eventsPage && (
        <p className="text-center text-[#333333] text-sm mt-6">
          Mostrando {eventsPage.content.length} de {eventsPage.totalElements} eventos
        </p>
      )}
    </div>
  );
}
