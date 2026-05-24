import { useEffect, useState } from "react";
import { Heart } from "lucide-react";

import { useProductStore } from "../store/useProductStore";
import { useFavoriteStore } from "../store/useFavoriteStore";
import { useAuthStore } from "../store/useAuthStore";

import Navbar from "../components/Navbar";

export default function Home() {

  const [query, setQuery] = useState("");
  const [selectedProduct, setSelectedProduct] = useState(null);

  const {
    products,
    loading,
    searchByKeyword
  } = useProductStore();

  const {
    fetchFavorites,
    toggleFavorite,
    isFavorite
  } = useFavoriteStore();

  // 🔐 Usuário autenticado
  const user = useAuthStore((state) => state.user);

  // ✅ ID do usuário vindo do authStore
  const userId = user?.id;

  useEffect(() => {

    // evita chamada sem login
    if (!userId) return;

    fetchFavorites(userId);

  }, [userId]);

  const handleSearch = async () => {

    if (!query) return;

    await searchByKeyword(query);
  };

const handleToggleFavorite = async (
  product
) => {

  console.log("PRODUTO:", product);

  console.log("TITLE:", product.title);

  console.log("PERMALINK:", product.permalink);

  await toggleFavorite(
    user.id,
    product.title,
    product.permalink
  );
};
  return (
    <div className="p-6 space-y-6">

      <Navbar />

      {/* 🔎 Busca */}
      <div className="flex gap-2 justify-center">

        <input
          type="text"
          placeholder="Buscar produto..."
          className="input input-bordered w-full max-w-md"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />

        <button
          className="btn btn-primary"
          onClick={handleSearch}
        >
          Buscar
        </button>
      </div>

      {/* ⏳ Loading */}
      {loading && (
        <div className="flex justify-center">
          <span className="loading loading-spinner loading-lg"></span>
        </div>
      )}

      {/* 🪟 Produtos */}
      <div className="bg-base-200 p-6 rounded-2xl shadow-inner max-h-[600px] overflow-y-auto">

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">

          {products.map((product) => {

            const favorite = isFavorite(product.permalink);

            return (
              <div
                key={product.id}
                className="card bg-base-100 shadow-md cursor-pointer hover:scale-[1.02] transition relative"
                onClick={() => setSelectedProduct(product)}
              >

                {/* ❤️ Favorito */}
                <button
                  className={`absolute top-3 right-3 btn btn-circle btn-sm z-10 ${
                    favorite ? "btn-error" : "btn-ghost"
                  }`}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleToggleFavorite(product);
                  }}
                >
                  <Heart
                    size={18}
                    fill={favorite ? "currentColor" : "none"}
                  />
                </button>

                {/* 🖼️ Imagem */}
                <figure className="p-4">
                  <img
                    src={product.thumbnail}
                    alt={product.title}
                    className="h-40 object-contain"
                  />
                </figure>

                {/* 📄 Conteúdo */}
                <div className="card-body p-4">

                  <h2 className="text-sm font-semibold line-clamp-2">
                    {product.title}
                  </h2>

                  <p className="text-lg font-bold text-primary">
                    {product.currencyId} {product.price}
                  </p>

                  <p className="text-xs opacity-70">
                    {product.condition}
                  </p>

                  {/* 🚀 Ações */}
                  <div
                    className="card-actions justify-end mt-2"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <a
                      href={product.permalink}
                      target="_blank"
                      rel="noreferrer"
                      className="btn btn-sm btn-secondary"
                    >
                      Ver produto
                    </a>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        {/* 📭 Sem resultados */}
        {!loading && products.length === 0 && (
          <div className="text-center opacity-70">
            Nenhum produto encontrado
          </div>
        )}
      </div>

      {/* 🧠 Modal */}
      {selectedProduct && (
        <div className="modal modal-open">

          <div className="modal-box max-w-2xl">

            <h3 className="font-bold text-lg mb-4">
              {selectedProduct.title}
            </h3>

            <img
              src={selectedProduct.thumbnail}
              alt={selectedProduct.title}
              className="w-full max-h-64 object-contain mb-4"
            />

            <div className="space-y-2">

              <p>
                <strong>Preço:</strong>{" "}
                {selectedProduct.currencyId}{" "}
                {selectedProduct.price}
              </p>

              {selectedProduct.originalPrice && (
                <p className="line-through opacity-60">
                  {selectedProduct.currencyId}{" "}
                  {selectedProduct.originalPrice}
                </p>
              )}

              <p>
                <strong>Condição:</strong>{" "}
                {selectedProduct.condition}
              </p>

              <p>
                <strong>Frete grátis:</strong>{" "}
                {selectedProduct.freeShipping ? "Sim" : "Não"}
              </p>
            </div>

            <div className="modal-action">

              {/* ❤️ Favorito */}
              <button
                className={`btn ${
                  isFavorite(selectedProduct.permalink)
                    ? "btn-error"
                    : "btn-outline"
                }`}
                onClick={() =>
                  handleToggleFavorite(selectedProduct)
                }
              >
                <Heart size={18} />

                {isFavorite(selectedProduct.permalink)
                  ? "Remover favorito"
                  : "Adicionar favorito"}
              </button>

              {/* 🚀 Produto */}
              <a
                href={selectedProduct.permalink}
                target="_blank"
                rel="noreferrer"
                className="btn btn-primary"
              >
                Ir para o produto
              </a>

              {/* ❌ Fechar */}
              <button
                className="btn"
                onClick={() => setSelectedProduct(null)}
              >
                Fechar
              </button>
            </div>
          </div>

          {/* Overlay */}
          <div
            className="modal-backdrop"
            onClick={() => setSelectedProduct(null)}
          ></div>
        </div>
      )}
    </div>
  );
}