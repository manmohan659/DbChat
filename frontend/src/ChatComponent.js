import React, { useState, useEffect } from 'react';
import './App.css';

function ChatComponent({ connectionStatus }) {
  const [messages, setMessages] = useState([]);
  const [input, setInput] = useState('');

  useEffect(() => {
    if (connectionStatus === 'success') {
      setMessages([{ text: "Hello! I'm a SQL assistant. Ask me anything about your database.", isUser: false }]);
    }
  }, [connectionStatus]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMessage = { text: input, isUser: true };
    setMessages(msgs => [...msgs, userMessage]);
    setInput('');

    try {
      const response = await fetch('/api/query/nl-to-sql', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: input })
      });

      if (response.ok) {
        const sqlQuery = await response.text();
        const aiMessage = { text: sqlQuery, isUser: false };
        setMessages(msgs => [...msgs, aiMessage]);
      } else {
        throw new Error('Failed to get SQL query');
      }
    } catch (error) {
      console.error('Error:', error);
      const errorMessage = { text: 'Sorry, there was an error processing your request.', isUser: false };
      setMessages(msgs => [...msgs, errorMessage]);
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-window">
        {messages.map((message, index) => (
          <div key={index} className={`message ${message.isUser ? 'user-message' : 'ai-message'}`}>
            <span className="message-icon">{message.isUser ? '👤' : '🥸'}</span>
            {message.text}
          </div>
        ))}
      </div>
      <form onSubmit={handleSubmit} className="input-area">
        <input
          type="text"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Type your message..."
          disabled={connectionStatus !== 'success'}
        />
        <button type="submit" disabled={connectionStatus !== 'success'}>Send</button>
      </form>
    </div>
  );
}

export default ChatComponent;