import React, { useState } from 'react';
import axios from 'axios';

function ChatComponent() {
  const [message, setMessage] = useState('');
  const [responses, setResponses] = useState([
    { sender: 'ai', text: "Hello! I'm a SQL assistant. Ask me anything about your database." }
  ]);

  const handleSubmit = async () => {
    if (message.trim() === '') return;
    setResponses([...responses, { sender: 'user', text: message }]);
    try {
      const response = await axios.post('http://localhost:8080/assistant', { message });
      setResponses(prev => [...prev, { sender: 'ai', text: response.data }]);
    } catch (error) {
      console.error('Error sending message:', error);
      setResponses(prev => [...prev, { sender: 'ai', text: 'Sorry, something went wrong.' }]);
    }
    setMessage('');
  };

  return (
    <div className="chat-container">
      <div className="chat-window">
        {responses.map((msg, index) => (
          <div key={index} className={`message ${msg.sender}-message`}>
            <span className="message-icon">{msg.sender === 'user' ? '👤' : '🤖'}</span>
            <span className="message-text">{msg.text}</span>
          </div>
        ))}
      </div>
      <div className="input-area">
        <input
          type="text"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          placeholder="Type a message..."
        />
        <button onClick={handleSubmit}>Send</button>
      </div>
    </div>
  );
}

export default ChatComponent;