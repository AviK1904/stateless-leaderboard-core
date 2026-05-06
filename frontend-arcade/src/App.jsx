import { useState, useEffect } from 'react';
import { Client } from '@stomp/stompjs';

function App() {
  const [players, setPlayers] = useState([]);
  const [username, setUsername] = useState('');
  const [score, setScore] = useState('');

  // --- 1. STATE UPDATES ---
  const [loginUser, setLoginUser] = useState('');
  const [loginPass, setLoginPass] = useState('');
  // We deleted the old 'credentials' state. Now we just use a true/false switch!
  const [isLoggedIn, setIsLoggedIn] = useState(false); 
  const [isRegistering, setIsRegistering] = useState(false);

  const fetchLeaderboard = () => {
    fetch('http://localhost:8080/api/players/top')
      .then(response => response.json())
      .then(data => setPlayers(data))
      .catch(error => console.error("Error:", error));
  };

  // --- 2. STARTUP MEMORY CHECK ---
  // When the app opens, check if they already have a wristband from earlier!
  useEffect(() => {
    const token = localStorage.getItem("arcade_jwt");
    if (token) {
      setIsLoggedIn(true);
    }
  }, []);

  // WebSockets (Unchanged)
  useEffect(() => {
    fetchLeaderboard();

    const stompClient = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      onConnect: () => {
        stompClient.subscribe('/topic/leaderboard', () => {
          fetchLeaderboard();
        });
      }
    });

    stompClient.activate();
    return () => stompClient.deactivate();
  }, []);

  const handleLogin = async (e) => {
    e.preventDefault(); 
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username: loginUser, password: loginPass }) 
      });

      if (response.ok) {
        const token = await response.text(); 
        localStorage.setItem("arcade_jwt", token); // Save wristband to glovebox
        
        alert("Login Successful! Wristband acquired.");
        setIsLoggedIn(true); // Flip the switch to true!
        setLoginUser('');
        setLoginPass('');
      } else {
        alert("Invalid username or password!");
      }
    } catch (error) {
      console.error("Login failed:", error);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: loginUser, password: loginPass })
      });

      if (response.ok) {
        alert("Registration Successful! You can now log in.");
        setIsRegistering(false); 
        setLoginPass(''); 
      } else {
        const errorData = await response.json(); 
        
        // We alert the specific "message" from the JSON object
        alert("Registration Failed: " + errorData.message);
      }
    } catch (error) {
      console.error("Registration failed:", error);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("arcade_jwt"); // Empty glovebox
    setIsLoggedIn(false); // Flip switch to false
    alert("You have been logged out!");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newPlayer = { username: username, score: parseInt(score) };

    // --- 3. GRAB WRISTBAND ---
    const token = localStorage.getItem("arcade_jwt");

    try {
      const response = await fetch('http://localhost:8080/api/players', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}` // Show wristband to bouncer!
        },
        body: JSON.stringify(newPlayer),
      });

      // Catch expired token
      if (response.status === 401 || response.status === 403) {
         alert("Your session has expired! Please log in again.");
         handleLogout(); 
         return;
      }

      if (response.ok) {
        setUsername('');
        setScore('');
      } else {
        alert("Action Denied: Invalid Credentials!");
      }
    } catch (error) {
      console.error("Failed to submit score:", error);
    }
  };

  const handleDelete = async (id) => {
    // --- 4. GRAB WRISTBAND FOR DELETE ---
    const token = localStorage.getItem("arcade_jwt");

    try {
      const response = await fetch(`http://localhost:8080/api/players/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}` // Show wristband!
        }
      });

      // Catch expired token here too
      if (response.status === 401 || response.status === 403) {
         alert("Your session has expired! Please log in again.");
         handleLogout(); 
         return;
      }

      if (!response.ok) {
        alert("Action Denied: Invalid Credentials!");
      }
    } catch (error) {
      console.error("Failed to delete player:", error);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 text-teal-400 font-mono flex flex-col items-center pt-8 pb-12 relative">

      {/* LOGIN BAR */}
      <div className="absolute top-4 right-4 flex gap-4">
        {/* Changed 'credentials' to 'isLoggedIn' */}
        {!isLoggedIn ? (
          <form onSubmit={isRegistering ? handleRegister : handleLogin} className="flex gap-2 items-center">
            <span className="text-sm font-bold">{isRegistering ? "CREATE ADMIN" : "ADMIN LOGIN"}</span>
            <input 
              type="text" placeholder="Username" value={loginUser} onChange={(e) => setLoginUser(e.target.value)} required
              className="bg-gray-800 border border-teal-700 text-teal-400 p-1 outline-none text-sm"
            />
            <input 
              type="password" placeholder="Password" value={loginPass} onChange={(e) => setLoginPass(e.target.value)} required
              className="bg-gray-800 border border-teal-700 text-teal-400 p-1 outline-none text-sm"
            />
            <button type="submit" className="bg-teal-700 text-gray-900 font-bold px-3 py-1 hover:bg-teal-500 text-sm">
              {isRegistering ? "REGISTER" : "LOGIN"}
            </button>
            <button type="button" onClick={() => setIsRegistering(!isRegistering)} className="text-xs text-teal-400 underline hover:text-teal-200">
              {isRegistering ? "Switch to Login" : "New Admin?"}
            </button>
          </form>
        ) : (
          <div className="flex gap-4 items-center">
            <span className="text-yellow-400 font-bold text-sm">⭐ ADMIN MODE ACTIVE</span>
            <button onClick={handleLogout} className="bg-red-600 text-white font-bold px-3 py-1 hover:bg-red-500 text-sm">LOGOUT</button>
          </div>
        )}
      </div>

      <h1 className="text-5xl font-bold drop-shadow-[0_0_15px_#2dd4bf] mb-8 mt-8">
        🏆 ARCADE LEADERBOARD 🏆
      </h1>

      {/* CONDITIONAL RENDERING FOR FORMS */}
      {/* Changed 'credentials' to 'isLoggedIn' */}
      {isLoggedIn && (
        <form onSubmit={handleSubmit} className="mb-10 bg-gray-800 p-6 rounded-lg shadow-[0_0_15px_rgba(45,212,191,0.2)] border border-teal-500 flex gap-4">
          <input type="text" placeholder="ENTER INITIALS..." value={username} onChange={(e) => setUsername(e.target.value)} required className="bg-gray-900 border border-teal-500 text-teal-400 p-3 outline-none" />
          <input type="number" placeholder="SCORE..." value={score} onChange={(e) => setScore(e.target.value)} required className="bg-gray-900 border border-teal-500 text-teal-400 p-3 outline-none w-32" />
          <button type="submit" className="bg-teal-500 text-gray-900 font-bold px-6 py-3 hover:bg-teal-400">INSERT COIN</button>
        </form>
      )}

      <table className="w-1/2 bg-gray-800 shadow-[0_0_20px_rgba(45,212,191,0.3)] border-collapse border border-gray-700">
        <thead>
          <tr className="bg-teal-500 text-gray-900 text-xl tracking-widest">
            <th className="p-4 border-b border-gray-700">RANK</th>
            <th className="p-4 border-b border-gray-700">USERNAME</th>
            <th className="p-4 border-b border-gray-700">SCORE</th>
            {/* Changed 'credentials' to 'isLoggedIn' */}
            {isLoggedIn && <th className="p-4 border-b border-gray-700"></th>}
          </tr>
        </thead>
        <tbody>
          {players.map((player, index) => {
            let rankColor = "text-gray-300";
            if (index === 0) rankColor = "text-yellow-400 font-extrabold";
            if (index === 1) rankColor = "text-gray-400 font-bold";
            if (index === 2) rankColor = "text-orange-400 font-bold";

            return (
              <tr key={player.id} className="hover:bg-gray-700 text-center border-b border-gray-700 text-lg">
                <td className={`p-4 ${rankColor}`}>#{index + 1}</td>
                <td className={`p-4 ${rankColor}`}>{player.username}</td>
                <td className={`p-4 ${rankColor}`}>{player.score.toLocaleString()}</td>

                {/* Changed 'credentials' to 'isLoggedIn' */}
                {isLoggedIn && (
                  <td className="p-4">
                    <button onClick={() => handleDelete(player.id)} className="text-red-500 hover:text-red-400 font-bold text-xl" title="Delete Player">✕</button>
                  </td>
                )}
              </tr>
            );
          })}
        </tbody>
      </table>

    </div>
  );
}

export default App;