import { useEffect, useState } from "react";
import { Heart } from "lucide-react";

import { useProductStore } from "../store/useProductStore";
import { useFavoriteStore } from "../store/useFavoriteStore";
import { useAuthStore } from "../store/useAuthStore";

import Navbar from "../components/Navbar";

export default function Home() {
  const [query, setQuery] = useState("");
  const [selectedProduct, setSelectedProduct] = useState(null);
  const [searchMessage, setSearchMessage] = useState("");

  const {
    products,
    loading,
    searchByKeyword,
  } = useProductStore();

  const {
    fetchFavorites,
    toggleFavorite,
    isFavorite,
  } = useFavoriteStore();

  const user = useAuthStore((state) => state.user);
  const userId = user?.id;

  useEffect(() => {
    if (!userId) return;

    fetchFavorites(userId);
  }, [userId]);

  const handleSearch = async () => {
    if (!query) return;

    const MAX_RETRIES = 15;

    setSearchMessage("Buscando produtos...");

    for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        await searchByKeyword(query);

        await new Promise((resolve) =>
          setTimeout(resolve, 500)
        );

        const currentProducts =
          useProductStore.getState().products;

        if (currentProducts.length > 0) {
          setSearchMessage("");
          return;
        }

        if (attempt < MAX_RETRIES) {
          setSearchMessage(
            `Nenhum resultado encontrado. Tentando novamente (${attempt}/${MAX_RETRIES})...`
          );
        }
      } catch (error) {
        console.error(error);
      }

      await new Promise((resolve) =>
        setTimeout(resolve, 1000)
      );
    }

    setSearchMessage(
      "Não foi possível encontrar produtos para esta busca. Tente outro termo."
    );
  };

  const handleToggleFavorite = async (
    product
  ) => {
    if (!userId) {
      alert("Você precisa estar logado");
      return;
    }

    await toggleFavorite(
      userId,
      product.title,
      product.permalink
    );
  };

  return (
    <div className="p-6 space-y-6">
      <Navbar />

      <div className="flex gap-2 justify-center">
        <input
          type="text"
          placeholder="Buscar produto..."
          className="input input-bordered w-full max-w-md"
          value={query}
          onChange={(e) =>
            setQuery(e.target.value)
          }
        />

        <button
          className="btn btn-primary"
          onClick={handleSearch}
        >
          Buscar
        </button>
      </div>

      {searchMessage && (
        <div className="alert alert-info">
          {searchMessage}
        </div>
      )}

      {loading && (
        <div className="flex flex-col items-center gap-3 py-8">
          <span className="loading loading-spinner loading-lg"></span>
          <p className="text-sm opacity-70">
            Procurando os melhores preços...
          </p>
        </div>
      )}

      <div className="bg-base-200 p-6 rounded-2xl shadow-inner max-h-[600px] overflow-y-auto">
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {products.map((product) => {
            const favorite = isFavorite(
              product.permalink
            );

            return (
              <div
                key={product.id}
                className="card bg-base-100 shadow-md cursor-pointer hover:scale-[1.02] transition relative"
                onClick={() =>
                  setSelectedProduct(product)
                }
              >
                <button
                  className={`absolute top-3 right-3 btn btn-circle btn-sm z-10 ${
                    favorite
                      ? "btn-error"
                      : "btn-ghost"
                  }`}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleToggleFavorite(
                      product
                    );
                  }}
                >
                  <Heart
                    size={18}
                    fill={
                      favorite
                        ? "currentColor"
                        : "none"
                    }
                  />
                </button>

                <figure className="p-4">
                  <img
                    src={product.thumbnail}
                    alt={product.title}
                    className="h-40 object-contain"
                  />
                </figure>

                <div className="card-body p-4">
                  <h2 className="text-sm font-semibold line-clamp-2">
                    {product.title}
                  </h2>

                  <p className="text-lg font-bold text-primary">
                    {product.currencyId}{" "}
                    {product.price}
                  </p>

                  <p className="text-xs opacity-70">
                    {product.condition}
                  </p>

                  <div
                    className="card-actions justify-end mt-2"
                    onClick={(e) =>
                      e.stopPropagation()
                    }
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

        {!loading &&
          products.length === 0 &&
          !searchMessage && (
            <div className="text-center opacity-70">
              Nenhum produto encontrado
            </div>
          )}
      </div>

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
                  {
                    selectedProduct.currencyId
                  }{" "}
                  {
                    selectedProduct.originalPrice
                  }
                </p>
              )}

              <p>
                <strong>Condição:</strong>{" "}
                {selectedProduct.condition}
              </p>

              <p>
                <strong>Frete grátis:</strong>{" "}
                {selectedProduct.freeShipping
                  ? "Sim"
                  : "Não"}
              </p>
            </div>

            <div className="modal-action">
              <button
                className={`btn ${
                  isFavorite(
                    selectedProduct.permalink
                  )
                    ? "btn-error"
                    : "btn-outline"
                }`}
                onClick={() =>
                  handleToggleFavorite(
                    selectedProduct
                  )
                }
              >
                <Heart size={18} />

                {isFavorite(
                  selectedProduct.permalink
                )
                  ? "Remover favorito"
                  : "Adicionar favorito"}
              </button>

              <a
                href={selectedProduct.permalink}
                target="_blank"
                rel="noreferrer"
                className="btn btn-primary"
              >
                Ir para o produto
              </a>

              <button
                className="btn"
                onClick={() =>
                  setSelectedProduct(null)
                }
              >
                Fechar
              </button>
            </div>
          </div>

          <div
            className="modal-backdrop"
            onClick={() =>
              setSelectedProduct(null)
            }
          />
        </div>
      )}
    </div>
  );
}