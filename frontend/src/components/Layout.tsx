// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Layout Component
// ═══════════════════════════════════════════════════════════════════════════
import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { Calendar, Ticket, LogOut, User, Menu, X, Shield } from 'lucide-react';
import { useState } from 'react';
import { FloatingChat } from './FloatingChat';

export function Layout() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen flex flex-col bg-[#F5F5F5]">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-[#F5F5F5] sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <Link to="/" className="flex items-center gap-2 group">
              <div className="bg-[#1A3EA1] p-2 rounded-lg group-hover:scale-105 transition-transform">
                <Calendar className="w-6 h-6 text-white" />
              </div>
              <span className="text-xl font-bold text-[#0A1A40]">
                EventHub
              </span>
            </Link>

            {/* Desktop Navigation */}
            <nav className="hidden md:flex items-center gap-6">
              <Link
                to="/events"
                className="flex items-center gap-2 text-[#333333] hover:text-[#1A3EA1] transition-colors font-medium"
              >
                <Calendar className="w-4 h-4" />
                Eventos
              </Link>

              {isAuthenticated && (
                <>
                  <Link
                    to="/my-tickets"
                    className="flex items-center gap-2 text-[#333333] hover:text-[#1A3EA1] transition-colors font-medium"
                  >
                    <Ticket className="w-4 h-4" />
                    Meus Ingressos
                  </Link>
                </>
              )}

              {isAdmin && (
                <Link
                  to="/admin"
                  className="flex items-center gap-2 text-[#2F8F4E] hover:text-[#0A1A40] transition-colors font-medium"
                >
                  <Shield className="w-4 h-4" />
                  Painel de Admin
                </Link>
              )}
            </nav>

            {/* User Menu */}
            <div className="hidden md:flex items-center gap-4">
              {isAuthenticated ? (
                <div className="flex items-center gap-4">
                  <div className="flex items-center gap-2 text-[#333333]">
                    <User className="w-4 h-4" />
                    <span className="font-medium">{user?.name}</span>
                    {isAdmin && (
                      <span className="px-2 py-0.5 text-xs bg-[#2F8F4E] text-white rounded-full font-medium">
                        Admin
                      </span>
                    )}
                  </div>
                  <button
                    onClick={handleLogout}
                    className="flex items-center gap-2 px-4 py-2 text-[#333333] hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                  >
                    <LogOut className="w-4 h-4" />
                    Sair
                  </button>
                </div>
              ) : (
                <div className="flex items-center gap-3">
                  <Link
                    to="/login"
                    className="px-4 py-2 text-[#333333] hover:text-[#1A3EA1] font-medium transition-colors"
                  >
                    Entrar
                  </Link>
                  <Link
                    to="/register"
                    className="px-4 py-2 bg-[#1A3EA1] text-white rounded-lg font-medium hover:bg-[#0A1A40] hover:shadow-lg transition-all"
                  >
                    Cadastrar
                  </Link>
                </div>
              )}
            </div>

            {/* Mobile menu button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 text-[#333333] hover:bg-[#F5F5F5] rounded-lg"
            >
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>

          {/* Mobile Navigation */}
          {mobileMenuOpen && (
            <div className="md:hidden py-4 border-t border-[#F5F5F5] animate-fade-in">
              <nav className="flex flex-col gap-2">
                <Link
                  to="/events"
                  onClick={() => setMobileMenuOpen(false)}
                  className="flex items-center gap-2 px-4 py-3 text-[#333333] hover:bg-[#F5F5F5] rounded-lg"
                >
                  <Calendar className="w-5 h-5" />
                  Eventos
                </Link>

                {isAuthenticated && (
                  <>
                    <Link
                      to="/my-tickets"
                      onClick={() => setMobileMenuOpen(false)}
                      className="flex items-center gap-2 px-4 py-3 text-[#333333] hover:bg-[#F5F5F5] rounded-lg"
                    >
                      <Ticket className="w-5 h-5" />
                      Meus Ingressos
                    </Link>
                  </>
                )}

                {isAdmin && (
                  <Link
                    to="/admin"
                    onClick={() => setMobileMenuOpen(false)}
                    className="flex items-center gap-2 px-4 py-3 text-[#2F8F4E] hover:bg-[#F5F5F5] rounded-lg"
                  >
                    <Shield className="w-5 h-5" />
                    Admin
                  </Link>
                )}

                <div className="border-t border-[#F5F5F5] mt-2 pt-2">
                  {isAuthenticated ? (
                    <>
                      <div className="px-4 py-2 text-[#333333] text-sm">
                        Logado como <span className="font-medium text-[#0A1A40]">{user?.name}</span>
                      </div>
                      <button
                        onClick={() => {
                          handleLogout();
                          setMobileMenuOpen(false);
                        }}
                        className="flex items-center gap-2 w-full px-4 py-3 text-red-600 hover:bg-red-50 rounded-lg"
                      >
                        <LogOut className="w-5 h-5" />
                        Sair
                      </button>
                    </>
                  ) : (
                    <>
                      <Link
                        to="/login"
                        onClick={() => setMobileMenuOpen(false)}
                        className="block px-4 py-3 text-[#333333] hover:bg-[#F5F5F5] rounded-lg"
                      >
                        Entrar
                      </Link>
                      <Link
                        to="/register"
                        onClick={() => setMobileMenuOpen(false)}
                        className="block px-4 py-3 text-[#1A3EA1] font-medium hover:bg-[#F5F5F5] rounded-lg"
                      >
                        Cadastrar
                      </Link>
                    </>
                  )}
                </div>
              </nav>
            </div>
          )}
        </div>
      </header>

      {/* Main Content */}
      <main className="flex-1">
        <Outlet />
      </main>

      {/* Floating Chat */}
      <FloatingChat />

      {/* Footer */}
      <footer className="bg-white border-t border-[#F5F5F5] mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <div className="flex items-center gap-2">
              <div className="bg-[#1A3EA1] p-1.5 rounded">
                <Calendar className="w-4 h-4 text-white" />
              </div>
              <span className="font-semibold text-[#0A1A40]">EventHub</span>
            </div>
            <p className="text-[#333333] text-sm">
              © 2026 EventHub. Sistema de Gestão de Eventos.
            </p>
          </div>
        </div>
      </footer>
    </div>
  );
}
