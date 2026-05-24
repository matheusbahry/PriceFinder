import { create } from 'zustand'
import api from '../config/axiosInstance.config.js'

export const useProductStore = create((set) => ({
  products: [],
  selectedProduct: null,
  loading: false,
  error: null,

  setLoading: (loading) => set({ loading }),
  setError: (error) => set({ error }),

  // ===============================
  // 🔎 Buscar por palavra-chave
  // ===============================
  searchByKeyword: async (query) => {
    set({ loading: true, error: null })

    try {
      const { data } = await api.get('/api/ebay/products/search', {
        params: { q: query },
      })

      set({
        products: data,
        loading: false,
      })

      return data
    } catch (err) {
      const message = 'Erro ao buscar produtos'
      set({ error: message, loading: false })
      throw new Error(message)
    }
  },

  // ===============================
  // 💰 Buscar por faixa de preço
  // ===============================
  searchByPriceRange: async (query, min, max) => {
    set({ loading: true, error: null })

    try {
      const { data } = await api.get('/api/ebay/products/price-range', {
        params: { q: query, min, max },
      })

      set({
        products: data,
        loading: false,
      })

      return data
    } catch (err) {
      const message = 'Erro ao buscar por faixa de preço'
      set({ error: message, loading: false })
      throw new Error(message)
    }
  },

  // ===============================
  // ⭐ Buscar ordenado
  // ===============================
  searchSorted: async (query, sort = 'bestMatch') => {
    set({ loading: true, error: null })

    try {
      const { data } = await api.get('/api/ebay/products/sorted', {
        params: { q: query, sort },
      })

      set({
        products: data,
        loading: false,
      })

      return data
    } catch (err) {
      const message = 'Erro ao buscar produtos ordenados'
      set({ error: message, loading: false })
      throw new Error(message)
    }
  },

  // ===============================
  // 🆔 Buscar por ID
  // ===============================
  getById: async (id) => {
    set({ loading: true, error: null })

    try {
      const { data } = await api.get(`/api/ebay/products/${id}`)

      set({
        selectedProduct: data,
        loading: false,
      })

      return data
    } catch (err) {
      const message = 'Erro ao buscar produto'
      set({ error: message, loading: false })
      throw new Error(message)
    }
  },

  // ===============================
  // 🧹 Reset
  // ===============================
  clearProducts: () => set({ products: [] }),
  clearSelectedProduct: () => set({ selectedProduct: null }),
}))