import React from 'react';
import './App.css';
import ChatComponent from './ChatComponent';

function App() {
  return (
    <div className="App">
      <div className="sidebar">
        <h2>Settings</h2>
        <p>This is a simple chat application using MySQL. Connect to the database and start chatting.</p>
        <input type="text" placeholder="Host" defaultValue="localhost" />
        <input type="text" placeholder="Port" defaultValue="3306" />
        <input type="text" placeholder="User" defaultValue="root" />
        <input type="password" placeholder="Password" />
        <input type="text" placeholder="Database" defaultValue="Chinook" />
        <button className="connect-button">Connect</button>
      </div>
      <main className="main-content">
        <h1>Chat with MySQL</h1>
        <ChatComponent />
      </main>
    </div>
  );
}

export default App;