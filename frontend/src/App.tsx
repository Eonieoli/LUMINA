import "./App.css";
import Main from "./pages/Main";
import Auth from "./pages/Auth";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import ScrollToTop from "./components/ScrollToTop";

function App() {
  return (
    <BrowserRouter>
      <ScrollToTop />
      <Routes>
        <Route path="/auth" element={<Auth/>} />
        <Route path="/*" element={<Main/>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
