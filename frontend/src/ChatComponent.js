import React, { useState, useEffect, useRef, useCallback } from 'react';

function ChatComponent({ connectionStatus }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const chatWindowRef = useRef(null);

  // Load chat history and initialize session
  useEffect(() => {
    const initializeChat = async () => {
      const sessionId = localStorage.getItem('sessionId') || `session-${Date.now()}`;
      localStorage.setItem('sessionId', sessionId);
      
      if (connectionStatus === 'success') {
        try {
          const response = await fetch('/api/query/history', {
            headers: {
              'X-Session-ID': sessionId
            }
          });
          
          if (response.ok) {
            const history = await response.json();
            const formattedHistory = history.map(msg => ({
              text: msg.message,
              isUser: msg.isUser,
              type: msg.type,
              sql: msg.generatedSql
            }));
            
            setMessages(formattedHistory);
          }
        } catch (error) {
          console.error('Error loading chat history:', error);
        }
      }
    };

    initializeChat();
  }, [connectionStatus]);

  // Add welcome message when connection is successful
  useEffect(() => {
    if (connectionStatus === 'success' && messages.length === 0) {
      setMessages([{
        text: "Hello! I'm your SQL assistant. Ask me anything about your database.",
        isUser: false,
        type: 'SYSTEM'
      }]);
    }
  }, [connectionStatus, messages.length]);

  // Scroll to bottom when messages change
  const scrollToBottom = useCallback(() => {
    if (messagesEndRef.current) {
      const chatWindow = chatWindowRef.current;
      const isScrolledToBottom = chatWindow.scrollHeight - chatWindow.scrollTop <= chatWindow.clientHeight + 100;
      
      if (isScrolledToBottom) {
        messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
      }
    }
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.trim() || isLoading) return;

    const userMessage = { text: input, isUser: true, type: 'QUERY' };
    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    try {
      const sessionId = localStorage.getItem('sessionId');
      const response = await fetch('/api/query/nl-to-sql', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Session-ID': sessionId
        },
        body: JSON.stringify({ query: input })
      });

      const data = await response.json();
      
      if (response.ok) {
        const aiMessage = {
          text: data.message,
          isUser: false,
          type: data.type || 'RESPONSE',
          sql: data.generatedSql
        };
        setMessages(prev => [...prev, aiMessage]);
      } else {
        throw new Error(data.message || 'Failed to process query');
      }
    } catch (error) {
      console.error('Error:', error);
      const errorMessage = {
        text: error.message || 'Sorry, there was an error processing your request.',
        isUser: false,
        type: 'ERROR'
      };
      setMessages(prev => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const getMessageClassName = useCallback((message) => {
    let baseClass = 'message';
    if (message.isUser) {
      baseClass += ' user-message';
    } else {
      baseClass += ' ai-message';
      if (message.type === 'ERROR') baseClass += ' error-message';
      if (message.type === 'SYSTEM') baseClass += ' system-message';
    }
    return baseClass;
  }, []);

  return (
    <div className="chat-container">
      <div className="chat-window" ref={chatWindowRef}>
        {messages.map((message, index) => (
          <div key={index} className={getMessageClassName(message)}>
            <span className="message-icon">
              {message.isUser ? '👤' : message.type === 'ERROR' ? '⚠️' : '🤖'}
            </span>
            <div className="message-content">
              <div className="message-text">{message.text}</div>
              {message.sql && (
                <div className="sql-query">
                  <span className="sql-label">Generated SQL:</span>
                  <code>{message.sql}</code>
                </div>
              )}
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>
      <form onSubmit={handleSubmit} className="input-area">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder={connectionStatus === 'success' ? "Type your query..." : "Connect to database first..."}
          disabled={connectionStatus !== 'success' || isLoading}
        />
        <button 
          type="submit" 
          disabled={connectionStatus !== 'success' || isLoading || !input.trim()}
        >
          {isLoading ? 'Processing...' : 'Send'}
        </button>
      </form>
    </div>
  );
}

export default ChatComponent;