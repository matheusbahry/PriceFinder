import { useState } from "react";
import { useProductStore } from "../store/useProductStore";

export default function PriceSearch() {
  const [query, setQuery] = useState("");
  const [min, setMin] = useState("");
  const [max, setMax] = useState("");
  const [selectedProduct, setSelectedProduct] = useState(null);

  const { products, loading, error, searchByPriceRange } = useProductStore();

  const handleSearch = async () => {
    if (!query || !min || !max) return;

    await searchByPriceRange(query, min, max);
  };

  return (
    <div className="p-6 space-y-6">

      {/* 🔎 Filtros */}
      <div className="flex flex-wrap gap-2 justify-center">
        
        <input
          type="text"
          placeholder="Produto (ex: iphone)"
          className="input input-bordered"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />

        <input
          type="number"
          placeholder="Preço mínimo"
          className="input input-bordered w-32"
          value={min}
          onChange={(e) => setMin(e.target.value)}
        />

        <input
          type="number"
          placeholder="Preço máximo"
          className="input input-bordered w-32"
          value={max}
          onChange={(e) => setMax(e.target.value)}
        />

        <button className="btn btn-primary" onClick={handleSearch}>
          Buscar
        </button>
      </div>

      {/* ⚠️ Erro */}
      {error && (
        <div className="alert alert-error text-sm">
          {error}
        </div>
      )}

      {/* ⏳ Loading */}
      {loading && (
        <div className="flex justify-center">
          <span className="loading loading-spinner loading-lg"></span>
        </div>
      )}

      {/* 🪟 Container */}
      <div className="bg-base-200 p-6 rounded-2xl shadow-inner max-h-[600px] overflow-y-auto">
        
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {products.map((product) => (
            
            <div
              key={product.id}
              className="card bg-base-100 shadow-md cursor-pointer hover:scale-[1.02] transition"
              onClick={() => setSelectedProduct(product)}
            >
              
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
                  {product.currencyId} {product.price}
                </p>

                <p className="text-xs opacity-70">
                  {product.condition}
                </p>

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
          ))}
        </div>

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
              className="w-full max-h-64 object-contain mb-4"
            />

            <div className="space-y-2">
              <p>
                <strong>Preço:</strong>{" "}
                {selectedProduct.currencyId} {selectedProduct.price}
              </p>

              <p>
                <strong>Condição:</strong> {selectedProduct.condition}
              </p>

              <p>
                <strong>Frete grátis:</strong>{" "}
                {selectedProduct.freeShipping ? "Sim" : "Não"}
              </p>
            </div>

            <div className="modal-action">
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
                onClick={() => setSelectedProduct(null)}
              >
                Fechar
              </button>
            </div>
          </div>

          <div
            className="modal-backdrop"
            onClick={() => setSelectedProduct(null)}
          ></div>
        </div>
      )}
    </div>
  );
}