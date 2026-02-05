// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Home Page
// ═══════════════════════════════════════════════════════════════════════════
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { eventsApi } from '@/api';
import { EventCard } from '@/components/EventCard';
import { Calendar, Ticket, Shield, Sparkles, ArrowRight, Loader2 } from 'lucide-react';
export function HomePage() {
  const { data: events, isLoading } = useQuery({
    queryKey: ['events', 'upcoming'],
    queryFn: eventsApi.getUpcoming,
  });
  return (
    <div className="animate-fade-in">
      {/* Hero Section */}
      <section className="relative overflow-hidden bg-gradient-to-br from-[#1A3EA1] via-[#0A1A40] to-[#0A1A40] text-white">
        <div className="absolute inset-0 opacity-20" style={{ backgroundImage: 'radial-gradient(circle at 1px 1px, rgba(255,255,255,0.3) 1px, transparent 0)', backgroundSize: '24px 24px' }} />
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 md:py-32">
          <div className="text-center">
            <h1 className="text-4xl md:text-6xl font-bold mb-6 leading-tight">
              Descubra e Participe de
              <span className="block bg-gradient-to-r from-[#2F8F4E] to-green-400 bg-clip-text text-transparent">
                Eventos Incríveis
              </span>
            </h1>
            <p className="text-lg md:text-xl text-blue-100 max-w-2xl mx-auto mb-10">
              A plataforma completa para descobrir eventos, comprar ingressos e criar memórias inesquecíveis.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <Link
                to="/events"
                className="inline-flex items-center gap-2 px-8 py-4 bg-white text-[#1A3EA1] rounded-xl font-bold text-lg hover:shadow-2xl hover:scale-105 transition-all"
              >
                <Calendar className="w-5 h-5" />
                Ver Eventos
                <ArrowRight className="w-5 h-5" />
              </Link>
              <Link
                to="/register"
                className="inline-flex items-center gap-2 px-8 py-4 bg-[#2F8F4E] hover:bg-green-600 text-white border-2 border-white/30 rounded-xl font-bold text-lg transition-all"
              >
                Criar Conta Grátis
              </Link>
            </div>
          </div>
        </div>
        {/* Wave Decoration */}
        <div className="absolute bottom-0 left-0 right-0">
          <svg viewBox="0 0 1440 120" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path
              d="M0 120L60 110C120 100 240 80 360 70C480 60 600 60 720 65C840 70 960 80 1080 85C1200 90 1320 90 1380 90L1440 90V120H1380C1320 120 1200 120 1080 120C960 120 840 120 720 120C600 120 480 120 360 120C240 120 120 120 60 120H0V120Z"
              fill="#F5F5F5"
            />
          </svg>
        </div>
      </section>
      {/* Features Section */}
      <section className="py-20 bg-[#F5F5F5]">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-bold text-[#0A1A40] mb-4">
              Por que escolher o EventHub?
            </h2>
            <p className="text-[#333333] max-w-2xl mx-auto">
              Desenvolvido com as melhores práticas e tecnologias modernas.
            </p>
          </div>
          <div className="grid md:grid-cols-3 gap-8">
            <div className="bg-white p-8 rounded-2xl shadow-sm hover:shadow-lg transition-shadow text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-blue-100 text-[#1A3EA1] rounded-2xl mb-6">
                <Ticket className="w-8 h-8" />
              </div>
              <h3 className="text-xl font-bold text-[#0A1A40] mb-3">Compra Segura</h3>
              <p className="text-[#333333]">
                Sistema robusto com tratamento de concorrência para garantir sua compra.
              </p>
            </div>
            <div className="bg-white p-8 rounded-2xl shadow-sm hover:shadow-lg transition-shadow text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-green-100 text-[#2F8F4E] rounded-2xl mb-6">
                <Shield className="w-8 h-8" />
              </div>
              <h3 className="text-xl font-bold text-[#0A1A40] mb-3">Autenticação JWT</h3>
              <p className="text-[#333333]">
                Segurança enterprise com tokens JWT e dados criptografados.
              </p>
            </div>
            <div className="bg-white p-8 rounded-2xl shadow-sm hover:shadow-lg transition-shadow text-center">
              <div className="inline-flex items-center justify-center w-16 h-16 bg-purple-100 text-purple-600 rounded-2xl mb-6">
                <Sparkles className="w-8 h-8" />
              </div>
              <h3 className="text-xl font-bold text-[#0A1A40] mb-3">Chat com IA</h3>
              <p className="text-[#333333]">
                Assistente inteligente integrado com Ollama.
              </p>
            </div>
          </div>
        </div>
      </section>
      {/* Upcoming Events Section */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center mb-10">
            <div>
              <h2 className="text-3xl font-bold text-[#0A1A40] mb-2">Próximos Eventos</h2>
              <p className="text-[#333333]">Confira os eventos que estão chegando</p>
            </div>
            <Link
              to="/events"
              className="hidden sm:inline-flex items-center gap-2 text-[#1A3EA1] hover:text-[#0A1A40] font-medium"
            >
              Ver todos
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
          {isLoading ? (
            <div className="flex justify-center py-20">
              <Loader2 className="w-10 h-10 text-[#1A3EA1] animate-spin" />
            </div>
          ) : events && events.length > 0 ? (
            <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {events.slice(0, 6).map((event) => (
                <EventCard key={event.id} event={event} />
              ))}
            </div>
          ) : (
            <div className="text-center py-20 bg-[#F5F5F5] rounded-2xl">
              <Calendar className="w-16 h-16 text-[#333333] mx-auto mb-4 opacity-30" />
              <h3 className="text-xl font-semibold text-[#333333] mb-2">Nenhum evento disponível</h3>
              <p className="text-[#333333]">Novos eventos serão adicionados em breve!</p>
            </div>
          )}
          <div className="sm:hidden mt-8 text-center">
            <Link
              to="/events"
              className="inline-flex items-center gap-2 px-6 py-3 bg-[#1A3EA1] text-white rounded-xl font-medium hover:bg-[#0A1A40] transition-colors"
            >
              Ver todos os eventos
              <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </div>
      </section>
      {/* CTA Section */}
      <section className="bg-gradient-to-r from-[#0A1A40] to-[#1A3EA1] py-20">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl md:text-4xl font-bold text-white mb-6">
            Pronto para sua próxima experiência?
          </h2>
          <p className="text-slate-300 text-lg mb-10">
            Crie sua conta gratuitamente e comece a explorar eventos incríveis.
          </p>
          <Link
            to="/register"
            className="inline-flex items-center gap-2 px-8 py-4 bg-[#2F8F4E] hover:bg-green-600 text-white rounded-xl font-bold text-lg hover:shadow-2xl hover:scale-105 transition-all"
          >
            Começar Agora
            <ArrowRight className="w-5 h-5" />
          </Link>
        </div>
      </section>
    </div>
  );
}
