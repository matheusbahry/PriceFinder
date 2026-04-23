import { Link, useLocation } from "react-router-dom";
import { useAuthStore } from "../store/useAuthStore";

export default function Navbar() {
  const location = useLocation();
  const { logout } = useAuthStore();

  const isActive = (path) =>
    location.pathname === path ? "btn-primary" : "btn-ghost";

  return (
    <div className="navbar bg-base-100 shadow-md px-6">
      
      {/* Logo */}
      <div className="flex-1">
        <Link to="/" className="text-xl font-bold">
          PriceFinder
        </Link>
      </div>

      {/* Links */}
      <div className="flex gap-2">
        <Link to="/">
          <button className={`btn btn-sm ${isActive("/")}`}>
            Home
          </button>
        </Link>

        <Link to="/price-search">
          <button className={`btn btn-sm ${isActive("/price-search")}`}>
            Buscar por Preço
          </button>
        </Link>
      </div>

      {/* Logout */}
      <div className="ml-4">
        <button className="btn btn-error btn-sm" onClick={logout}>
          Logout
        </button>
      </div>
    </div>
  );
}