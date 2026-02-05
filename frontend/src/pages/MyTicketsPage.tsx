// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - My Tickets Page
// ═══════════════════════════════════════════════════════════════════════════
import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ticketsApi } from '@/api';
import { TicketCard } from '@/components/TicketCard';
import { getErrorMessage } from '@/api/client';
import { Ticket, Loader2, AlertCircle, ChevronLeft, ChevronRight } from 'lucide-react';
import clsx from 'clsx';

export function MyTicketsPage() {
  const [page, setPage] = useState(0);
  const [activeTab, setActiveTab] = useState<'all' | 'active'>('all');
  const queryClient = useQueryClient();

  // Fetch all tickets (paginated)
  const { data: ticketsPage, isLoading } = useQuery({
    queryKey: ['tickets', 'my-tickets', page],
    queryFn: () => ticketsApi.getMyTickets(page, 10),
  });

  // Fetch active tickets
  const { data: activeTickets } = useQuery({
    queryKey: ['tickets', 'active'],
    queryFn: ticketsApi.getMyActiveTickets,
  });

  // Cancel mutation
  const cancelMutation = useMutation({
    mutationFn: ticketsApi.cancel,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['tickets'] });
      // Invalida TODAS as queries que começam com 'events'
      queryClient.invalidateQueries({ queryKey: ['events'] });
    },
  });

  const handleCancel = (id: string) => {
    if (window.confirm('Tem certeza que deseja cancelar este ingresso?')) {
      cancelMutation.mutate(id);
    }
  };

  const displayTickets = activeTab === 'active' ? activeTickets : ticketsPage?.content;

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 animate-fade-in">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-[#0A1A40] mb-2">Meus Ingressos</h1>
        <p className="text-[#333333]">Gerencie seus ingressos comprados</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 mb-8">
        <button
          onClick={() => setActiveTab('all')}
          className={clsx(
            'px-4 py-2 rounded-lg font-medium transition-all',
            activeTab === 'all'
              ? 'bg-[#1A3EA1] text-white'
              : 'bg-[#F5F5F5] text-[#333333] hover:bg-[#E0E0E0]'
          )}
        >
          Todos
          {ticketsPage && (
            <span className="ml-2 px-2 py-0.5 bg-white/20 rounded-full text-sm">
              {ticketsPage.totalElements}
            </span>
          )}
        </button>
        <button
          onClick={() => setActiveTab('active')}
          className={clsx(
            'px-4 py-2 rounded-lg font-medium transition-all',
            activeTab === 'active'
              ? 'bg-[#2F8F4E] text-white'
              : 'bg-[#F5F5F5] text-[#333333] hover:bg-[#E0E0E0]'
          )}
        >
          Ativos
          {activeTickets && (
            <span className="ml-2 px-2 py-0.5 bg-white/20 rounded-full text-sm">
              {activeTickets.length}
            </span>
          )}
        </button>
      </div>

      {/* Error Message */}
      {cancelMutation.isError && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
          <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
          <div className="text-red-700 text-sm whitespace-pre-line">{getErrorMessage(cancelMutation.error)}</div>
        </div>
      )}

      {/* Tickets List */}
      {isLoading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-10 h-10 text-[#1A3EA1] animate-spin" />
        </div>
      ) : displayTickets && displayTickets.length > 0 ? (
        <div className="space-y-6">
          {displayTickets.map((ticket) => (
            <TicketCard key={ticket.id} ticket={ticket} onCancel={handleCancel} />
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-[#F5F5F5] rounded-2xl">
          <Ticket className="w-16 h-16 text-[#333333] mx-auto mb-4 opacity-30" />
          <h3 className="text-xl font-semibold text-[#333333] mb-2">
            {activeTab === 'active' ? 'Nenhum ingresso ativo' : 'Nenhum ingresso encontrado'}
          </h3>
          <p className="text-[#333333]">
            {activeTab === 'active'
              ? 'Seus ingressos ativos aparecerão aqui'
              : 'Compre ingressos para eventos e eles aparecerão aqui'}
          </p>
        </div>
      )}

      {/* Pagination */}
      {activeTab === 'all' && ticketsPage && ticketsPage.totalPages > 1 && (
        <div className="flex justify-center items-center gap-4 mt-10">
          <button
            onClick={() => setPage(Math.max(0, page - 1))}
            disabled={ticketsPage.first}
            className={clsx(
              'flex items-center gap-2 px-4 py-2 rounded-xl font-medium transition-all',
              ticketsPage.first
                ? 'bg-slate-100 text-slate-400 cursor-not-allowed'
                : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50'
            )}
          >
            <ChevronLeft className="w-4 h-4" />
            Anterior
          </button>

          <span className="text-slate-600">
            Página {page + 1} de {ticketsPage.totalPages}
          </span>

          <button
            onClick={() => setPage(Math.min(ticketsPage.totalPages - 1, page + 1))}
            disabled={ticketsPage.last}
            className={clsx(
              'flex items-center gap-2 px-4 py-2 rounded-xl font-medium transition-all',
              ticketsPage.last
                ? 'bg-slate-100 text-slate-400 cursor-not-allowed'
                : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-50'
            )}
          >
            Próximo
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      )}
    </div>
  );
}
