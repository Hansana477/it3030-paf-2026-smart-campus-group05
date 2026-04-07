import { BrowserRouter, Routes, Route } from "react-router-dom";
import "./App.css";
import React from "react";
import Register from "./components/Register/Register";

function App() {
  return (
    <BrowserRouter>
    <div className="App">
      <Routes>
      <Route path="/" element={<Register />} />
      </Routes>
    </div>

    </BrowserRouter>
  );
}

export default App;
