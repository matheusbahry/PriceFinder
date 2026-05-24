// pages/Favorites.jsx

import { useEffect, useState } from "react";
import {
  Heart,
  Trash2,
  ExternalLink,
} from "lucide-react";

import { useFavoriteStore } from "../store/useFavoriteStore";
import { useAuthStore } from "../store/useAuthStore";

import Navbar from "../components/Navbar";

export default function Favorites() {

  const [loadingPage, setLoadingPage] = useState(true);

  const {
    favorites,
    fetchFavorites,
    removeFavorite,
  } = useFavoriteStore();

  // 🔐 Usuário autenticado
  const user = useAuthStore((state) => state.user);

  // ✅ ID vindo do authStore
  const userId = user?.id;

  useEffect(() => {

    const loadFavorites = async () => {

      if (!userId) {
        setLoadingPage(false);
        return;
      }

      await fetchFavorites(userId);

      setLoadingPage(false);
    };

    loadFavorites();

  }, [userId]);

  const handleRemoveFavorite = async (productUrl) => {

    if (!userId) {
      alert("Você precisa estar logado");
      return;
    }

    await removeFavorite(userId, productUrl);
  };

  // 🔗 Encurta URL para exibição
  const formatUrl = (url) => {

    try {

      const parsed = new URL(url);

      return `${parsed.hostname}${parsed.pathname}`
        .replace("www.", "")
        .slice(0, 45) + "...";

    } catch {
      return url.slice(0, 45) + "...";
    }
  };

  return (
    <div className="min-h-screen bg-base-100 p-6 space-y-6">

      <Navbar />

      {/* ❤️ Header */}
      <div className="flex flex-col items-center text-center gap-3">

        <div className="bg-error text-error-content p-3 rounded-2xl">
          <Heart
            size={28}
            fill="currentColor"
          />
        </div>

        <div>
          <h1 className="text-3xl font-bold">
            Meus Favoritos
          </h1>

          <p className="opacity-70">
            Produtos salvos pelo usuário
          </p>
        </div>
      </div>

      {/* ⏳ Loading */}
      {loadingPage && (
        <div className="flex justify-center py-10">
          <span className="loading loading-spinner loading-lg"></span>
        </div>
      )}

      {/* 🔐 Não autenticado */}
      {!loadingPage && !userId && (
        <div className="bg-base-200 rounded-2xl p-10 text-center shadow-inner">

          <Heart
            size={48}
            className="mx-auto mb-4 opacity-40"
          />

          <h2 className="text-xl font-semibold mb-2">
            Usuário não autenticado
          </h2>

          <p className="opacity-70">
            Faça login para visualizar seus favoritos.
          </p>
        </div>
      )}

      {/* 📭 Sem favoritos */}
      {!loadingPage && userId && favorites.length === 0 && (
        <div className="bg-base-200 rounded-2xl p-10 text-center shadow-inner">

          <Heart
            size={48}
            className="mx-auto mb-4 opacity-40"
          />

          <h2 className="text-xl font-semibold mb-2">
            Nenhum favorito encontrado
          </h2>

          <p className="opacity-70">
            Adicione produtos aos favoritos para visualizá-los aqui.
          </p>
        </div>
      )}

      {/* 🪟 Lista */}
      {!loadingPage && userId && favorites.length > 0 && (

        <div className="flex justify-center">

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 max-w-7xl">

            {favorites.map((favorite) => (

              <div
                key={favorite.id}
                className="card bg-base-200 shadow-md hover:shadow-xl transition w-[350px]"
              >

                <div className="card-body">

                                    {/* ❤️ Topo */}
                  <div className="flex items-start justify-between gap-2">

                    <div className="flex-1">

                      <h2 className="card-title text-lg break-words">

                        {favorite.productName}

                      </h2>

                      {/* 🔗 URL clicável */}
                      <a
                        href={favorite.productUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="
                          text-sm
                          text-primary
                          hover:underline
                          break-all
                          mt-2
                          inline-block
                        "
                      >
                        {formatUrl(favorite.productUrl)}
                      </a>

                    </div>

                    <div className="badge badge-error gap-1">

                      <Heart
                        size={12}
                        fill="currentColor"
                      />

                      Favorito

                    </div>
                  </div>

                  {/* 📅 Data */}
                  <div className="text-sm opacity-70 mt-2">

                    Salvo em{" "}
                    {new Date(
                      favorite.createdAt
                    ).toLocaleString()}
                  </div>

                  {/* 🚀 Ações */}
                  <div className="card-actions justify-center mt-6 gap-3">

                    <a
                      href={favorite.productUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="btn btn-primary btn-sm"
                    >
                      <ExternalLink size={16} />
                      Abrir produto
                    </a>

                    <button
                      className="btn btn-error btn-sm"
                      onClick={() =>
                        handleRemoveFavorite(
                          favorite.productUrl
                        )
                      }
                    >
                      <Trash2 size={16} />
                      Remover
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}