import { useState } from "react";
import { useProductStore } from "../store/useProductStore";
import Navbar from "../components/Navbar";

export default function Home() {
  const [query, setQuery] = useState("");
  const [selectedProduct, setSelectedProduct] = useState(null);

  const { products, loading, searchByKeyword } = useProductStore();

  const handleSearch = async () => {
    if (!query) return;
    await searchByKeyword(query);
  };

  return (
    <div className="p-6 space-y-6">

        
      
      {/* 🔎 Busca */}
      <div className="flex gap-2 justify-center">
        <input
          type="text"
          placeholder="Buscar produto..."
          className="input input-bordered w-full max-w-md"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />

        <button className="btn btn-primary" onClick={handleSearch}>
          Buscar
        </button>
      </div>

      {/* ⏳ Loading */}
      {loading && (
        <div className="flex justify-center">
          <span className="loading loading-spinner loading-lg"></span>
        </div>
      )}

      {/* 🪟 Container dos produtos */}
      <div className="bg-base-200 p-6 rounded-2xl shadow-inner max-h-[600px] overflow-y-auto">
        
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {products.map((product) => (
            
            <div
              key={product.id}
              className="card bg-base-100 shadow-md cursor-pointer hover:scale-[1.02] transition"
              onClick={() => setSelectedProduct(product)}
            >
              
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
                  onClick={(e) => e.stopPropagation()} // evita abrir modal
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

        {/* 📭 Sem resultados */}
        {!loading && products.length === 0 && (
          <div className="text-center opacity-70">
            Nenhum produto encontrado
          </div>
        )}
      </div>

      {/* 🧠 MODAL */}
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
                {selectedProduct.currencyId} {selectedProduct.price}
              </p>

              {selectedProduct.originalPrice && (
                <p className="line-through opacity-60">
                  {selectedProduct.currencyId} {selectedProduct.originalPrice}
                </p>
              )}

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

          {/* overlay */}
          <div
            className="modal-backdrop"
            onClick={() => setSelectedProduct(null)}
          ></div>
        </div>
      )}
    </div>
  );
}