// store/useFavoriteStore.js

import { create } from "zustand";
import api from "../config/axiosInstance.config.js";

export const useFavoriteStore = create((set, get) => ({

  favorites: [],

  loadingFavorites: false,

  favoriteError: null,

  // ===============================
  // ⭐ Buscar favoritos do usuário
  // ===============================

  fetchFavorites: async (userId) => {

    set({
      loadingFavorites: true,
      favoriteError: null,
    });

    try {

      console.log(
        "Buscando favoritos do usuário:",
        userId
      );

      const { data } =
        await api.get(`/favorites/${userId}`);

      console.log(
        "Favoritos recebidos:",
        data
      );

      set({
        favorites: data,
        loadingFavorites: false,
      });

      return data;

    } catch (err) {

      console.error(
        "Erro ao buscar favoritos:",
        err
      );

      console.error(
        "Response:",
        err.response
      );

      console.error(
        "Response data:",
        err.response?.data
      );

      set({
        favoriteError:
          err.response?.data?.message ||
          "Erro ao buscar favoritos",

        loadingFavorites: false,
      });
    }
  },

  // ===============================
  // ❤️ Verifica favorito
  // ===============================

  isFavorite: (productUrl) => {

    console.log(
      "Verificando favorito:",
      productUrl
    );

    return get().favorites.some(
      (favorite) =>
        favorite.productUrl === productUrl
    );
  },

  // ===============================
  // ➕ Adicionar favorito
  // ===============================

  addFavorite: async (
    userId,
    productName,
    productUrl
  ) => {

    try {

      console.log("=== ADD FAVORITE ===");

      console.log("userId:", userId);

      console.log("productName:", productName);

      console.log("productUrl:", productUrl);

      if (!productUrl) {

        throw new Error(
          "productUrl está undefined"
        );
      }

      if (!productName) {

        throw new Error(
          "productName está undefined"
        );
      }

      const payload = {
        userId,
        productName,
        productUrl,
      };

      console.log(
        "Payload:",
        payload
      );

      console.log(
        "Payload JSON:",
        JSON.stringify(payload)
      );

      const { data } = await api.post(
        "/favorites",
        payload
      );

      console.log(
        "Resposta da API:",
        data
      );

      set((state) => ({
        favorites: [
          ...state.favorites,
          data,
        ],
      }));

      return data;

    } catch (err) {

      console.error(
        "Erro addFavorite:",
        err
      );

      console.error(
        "Response:",
        err.response
      );

      console.error(
        "Response data:",
        err.response?.data
      );

      throw new Error(
        "Erro ao adicionar favorito: " +
        (
          err.response?.data?.message ||
          err.message
        )
      );
    }
  },

  // ===============================
  // ❌ Remover favorito
  // ===============================

  removeFavorite: async (
    userId,
    productUrl
  ) => {

    try {

      console.log("=== REMOVE FAVORITE ===");

      console.log("userId:", userId);

      console.log("productUrl:", productUrl);

      await api.delete(
        "/favorites",
        {
          data: {
            userId,
            productUrl,
          },
        }
      );

      console.log(
        "Favorito removido com sucesso"
      );

      set((state) => ({
        favorites:
          state.favorites.filter(
            (favorite) =>
              favorite.productUrl !==
              productUrl
          ),
      }));

    } catch (err) {

      console.error(
        "Erro removeFavorite:",
        err
      );

      console.error(
        "Response:",
        err.response
      );

      console.error(
        "Response data:",
        err.response?.data
      );

      throw new Error(
        "Erro ao remover favorito: " +
        (
          err.response?.data?.message ||
          err.message
        )
      );
    }
  },

  // ===============================
  // 🔁 Toggle favorito
  // ===============================

  toggleFavorite: async (
    userId,
    productName,
    productUrl
  ) => {

    console.log("=== TOGGLE FAVORITE ===");

    console.log("userId:", userId);

    console.log("productName:", productName);

    console.log("productUrl:", productUrl);

    const favorite =
      get().isFavorite(productUrl);

    console.log(
      "Já é favorito?",
      favorite
    );

    if (favorite) {

      await get().removeFavorite(
        userId,
        productUrl
      );

    } else {

      await get().addFavorite(
        userId,
        productName,
        productUrl
      );
    }
  },
}));