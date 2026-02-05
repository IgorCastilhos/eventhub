// ═══════════════════════════════════════════════════════════════════════════
// EventHub Frontend - Chat Page (Ollama AI)
// ═══════════════════════════════════════════════════════════════════════════
import { useState, useRef, useEffect } from 'react';
import { useMutation } from '@tanstack/react-query';
import { chatApi } from '@/api';
import { getErrorMessage } from '@/api/client';
import { useAuth } from '@/contexts/AuthContext';
import { Send, Bot, User, Loader2, Sparkles, AlertCircle, RefreshCw } from 'lucide-react';
import type { ChatMessage } from '@/types';
import clsx from 'clsx';

export function ChatPage() {
  const { user } = useAuth();
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      content: `Olá${user?.name ? `, ${user.name.split(' ')[0]}` : ''}! 👋 Sou o assistente virtual do EventHub. Posso te ajudar com:

• Informações sobre eventos
• Como comprar ingressos
• Dúvidas sobre a plataforma
• Recomendações de eventos

Como posso te ajudar hoje?`,
      role: 'assistant',
      timestamp: new Date(),
    },
  ]);
  const [inputMessage, setInputMessage] = useState('');
  const [conversationId, setConversationId] = useState<string | undefined>();
  const [error, setError] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // Auto-scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Chat mutation
  const chatMutation = useMutation({
    mutationFn: (message: string) => chatApi.sendMessage(message, conversationId),
    onSuccess: (data) => {
      setConversationId(data.conversationId);
      setMessages((prev) => [
        ...prev,
        {
          id: Date.now().toString(),
          content: data.response,
          role: 'assistant',
          timestamp: new Date(),
        },
      ]);
    },
    onError: (err) => {
      setError(getErrorMessage(err));
      // Remove loading message on error
      setMessages((prev) => prev.filter((m) => m.id !== 'loading'));
    },
  });

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputMessage.trim() || chatMutation.isPending) return;

    const userMessage: ChatMessage = {
      id: Date.now().toString(),
      content: inputMessage.trim(),
      role: 'user',
      timestamp: new Date(),
    };

    setMessages((prev) => [...prev, userMessage]);
    setInputMessage('');
    setError('');

    chatMutation.mutate(inputMessage.trim());
  };

  const handleNewConversation = () => {
    setConversationId(undefined);
    setMessages([
      {
        id: 'welcome',
        content: `Nova conversa iniciada! Como posso te ajudar?`,
        role: 'assistant',
        timestamp: new Date(),
      },
    ]);
    setError('');
  };

  const suggestedQuestions = [
    'Quais eventos estão disponíveis?',
    'Como comprar um ingresso?',
    'Como cancelar uma compra?',
    'Preciso de ajuda',
  ];

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-10 animate-fade-in">
      {/* Header */}
      <div className="flex justify-between items-start mb-8">
        <div>
          <div className="flex items-center gap-3 mb-2">
            <div className="bg-gradient-to-r from-purple-600 to-indigo-600 p-2 rounded-xl">
              <Sparkles className="w-6 h-6 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-slate-800">Chat com IA</h1>
          </div>
          <p className="text-slate-600">
            Assistente virtual integrado com Ollama para tirar suas dúvidas
          </p>
        </div>
        <button
          onClick={handleNewConversation}
          className="flex items-center gap-2 px-4 py-2 text-slate-600 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
        >
          <RefreshCw className="w-4 h-4" />
          Nova conversa
        </button>
      </div>

      {/* Chat Container */}
      <div className="bg-white rounded-2xl shadow-lg border border-slate-100 overflow-hidden">
        {/* Messages Area */}
        <div className="h-[500px] overflow-y-auto p-6 space-y-6">
          {messages.map((message) => (
            <div
              key={message.id}
              className={clsx(
                'flex gap-4',
                message.role === 'user' ? 'flex-row-reverse' : 'flex-row'
              )}
            >
              {/* Avatar */}
              <div
                className={clsx(
                  'flex-shrink-0 w-10 h-10 rounded-xl flex items-center justify-center',
                  message.role === 'user'
                    ? 'bg-gradient-to-r from-blue-600 to-indigo-600'
                    : 'bg-gradient-to-r from-purple-600 to-indigo-600'
                )}
              >
                {message.role === 'user' ? (
                  <User className="w-5 h-5 text-white" />
                ) : (
                  <Bot className="w-5 h-5 text-white" />
                )}
              </div>

              {/* Message */}
              <div
                className={clsx(
                  'max-w-[75%] rounded-2xl px-5 py-3',
                  message.role === 'user'
                    ? 'bg-gradient-to-r from-blue-600 to-indigo-600 text-white'
                    : 'bg-slate-100 text-slate-800'
                )}
              >
                <p className="whitespace-pre-wrap leading-relaxed">{message.content}</p>
              </div>
            </div>
          ))}

          {/* Loading indicator */}
          {chatMutation.isPending && (
            <div className="flex gap-4">
              <div className="flex-shrink-0 w-10 h-10 rounded-xl flex items-center justify-center bg-gradient-to-r from-purple-600 to-indigo-600">
                <Bot className="w-5 h-5 text-white" />
              </div>
              <div className="bg-slate-100 rounded-2xl px-5 py-3">
                <div className="flex items-center gap-2">
                  <Loader2 className="w-4 h-4 animate-spin text-slate-500" />
                  <span className="text-slate-500">Pensando...</span>
                </div>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Error Message */}
        {error && (
          <div className="mx-6 mb-4 p-4 bg-red-50 border border-red-200 rounded-xl flex items-start gap-3">
            <AlertCircle className="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
            <div>
              <p className="text-red-700 text-sm">{error}</p>
              <p className="text-red-600 text-xs mt-1">
                Certifique-se de que o Ollama está rodando e o modelo está instalado.
              </p>
            </div>
          </div>
        )}

        {/* Suggested Questions */}
        {messages.length <= 1 && (
          <div className="px-6 pb-4">
            <p className="text-slate-500 text-sm mb-3">Sugestões:</p>
            <div className="flex flex-wrap gap-2">
              {suggestedQuestions.map((question) => (
                <button
                  key={question}
                  onClick={() => setInputMessage(question)}
                  className="px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-600 rounded-full text-sm transition-colors"
                >
                  {question}
                </button>
              ))}
            </div>
          </div>
        )}

        {/* Input Area */}
        <form onSubmit={handleSend} className="border-t border-slate-100 p-4">
          <div className="flex gap-3">
            <input
              type="text"
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              placeholder="Digite sua mensagem..."
              disabled={chatMutation.isPending}
              className="flex-1 px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 outline-none transition-all disabled:bg-slate-50"
            />
            <button
              type="submit"
              disabled={!inputMessage.trim() || chatMutation.isPending}
              className="px-6 py-3 bg-gradient-to-r from-blue-600 to-indigo-600 text-white rounded-xl font-medium hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed transition-all flex items-center gap-2"
            >
              <Send className="w-5 h-5" />
            </button>
          </div>
        </form>
      </div>

      {/* Info */}
      <p className="text-center text-slate-500 text-sm mt-6">
        Powered by Ollama • As respostas são geradas por IA e podem não ser 100% precisas
      </p>
    </div>
  );
}
