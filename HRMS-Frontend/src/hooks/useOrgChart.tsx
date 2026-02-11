import axios from "axios";
import { useQuery } from "@tanstack/react-query";

export type Product = {
    id: number;
    title: string;
    price: number;
    category: string;
    stock: number;
    thumbnail: string;
    description?: string;
    discountPercentage?: number;
    rating?:number;
    brand?:string;
}

export const fetchAllProduct = async (): Promise<Product[]> => {
  const { data } = await axios.get<{ products: Product[] }>("https://dummyjson.com/products");
  return data.products;
};  

export const searchProduct = async (term: string): Promise<Product[]> => {
  const { data } = await axios.get<{ products: Product[] }>(`https://dummyjson.com/products/search?q=${term}`);
  return data.products;
}

export const searchProductByCat = async (category: string): Promise<Product[]> => {
  const { data } = await axios.get<{ products: Product[] }>(`https://dummyjson.com/products/category/${category}`);
  return data.products;
}

export const fetchProductById = async (id: number): Promise<Product> => {
  const { data } = await axios.get<Product>(`https://dummyjson.com/products/${id}`);
  return data;
}

export const fetchProductsCat = async (): Promise<string[]> => {
  const { data } = await axios.get<string[]>(`https://dummyjson.com/products/category-list`);
  return data;
}

export function useAllProduct(){
    return useQuery({
        queryKey: ["products","all"],
        queryFn: fetchAllProduct,
        staleTime: 5 * 60 * 1000,
    })
}

export function useCategories(){
    return useQuery({
        queryKey: ["categories"],
        queryFn: fetchProductsCat,
        staleTime: Infinity,
    })
}

export function useProductSearch(term: string){
    return useQuery({
        queryKey: ["products","search",term],
        queryFn: () => searchProduct(term),
        enabled: term.length > 0,
        staleTime: 5 * 60 * 1000,
    })
}

export function useProductByCat(category: string){
    return useQuery({
        queryKey: ["products","category",category],
        queryFn: () => searchProductByCat(category),
        enabled: category.length > 0,
        staleTime: 5 * 60 * 1000,
    })
}

export function useSingleProduct(id: number){
    return useQuery({
        queryKey: ["products",id],
        queryFn: () => fetchProductById(id),
        enabled: id > 0,
        staleTime: 5 * 60 * 1000,
    })
}
