import axios from "axios";

const BASE_URL = "http://localhost:8080/api";

const api = axios.create({
  baseURL: BASE_URL,
});

const authHeader = (token: string) => ({
  headers: { 
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json" 
  },
});

export const postService = {
 

  addPost: async (formData: FormData, token: string) => {
    const res = await api.post("/post/", formData, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  },

  showAllPosts: async (token: string) => {
    const res = await api.get("/post/", authHeader(token));
    return res.data;
  },

  postByPostId: async (postId: number, token: string) => {
    const res = await api.get(`/post/${postId}`, authHeader(token));
    return res.data;
  },

  updatePost: async (postId: number, formData: FormData, token: string) => {
    const res = await api.put(`/post/${postId}`, formData, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return res.data;
  },

  removePost: async (postId: number, token: string) => {
    const res = await api.delete(`/post/${postId}`, authHeader(token));
    return res.data;
  },

  addLike: async (payload: any, token: string) => {
    const res = await api.post(`/post/like`, payload, authHeader(token));
    return res.data;
  },

  getLikeByPostId: async (postId: number, token: string) => {
    const res = await api.get(`/post/likeByPostId/${postId}`, authHeader(token));
    return res.data;
  },

  getLikeByCommentId: async (commentId: number, token: string) => {
    const res = await api.get(`/post/likeByCommentId/${commentId}`, authHeader(token));
    return res.data;
  },

  removeLikeByPost: async (postId: number, employeeId: number, token: string) => {
    const res = await api.delete(`/post/postLike/${postId}/${employeeId}`, authHeader(token));
    return res.data;
  },
  
  removeLikeByComment: async (commentId: number, employeeId: number, token: string) => {
    const res = await api.delete(`/post/commentLike/${commentId}/${employeeId}`, authHeader(token));
    return res.data;
  },
  
  addComment: async (payload: any, token: string) => {
    const res = await api.post(`/post/comment`, payload, authHeader(token));
    return res.data;
  },  

  getCommentsById: async (postId: number, token: string) => {
    const res = await api.get(`/post/comment/${postId}`, authHeader(token));
    return res.data;
  },

  removeComment: async (commentId: number, token: string) => {
    const res = await api.delete(`/post/comment/${commentId}`, authHeader(token));
    return res.data;
  },

  getPostTagsById: async (postId: number, token: string) => {
    const res = await api.get(`/post/postTag/${postId}`, authHeader(token));
    return res.data;
  },

  addPostTagOnPost: async (postId: number, tagId: number, token: string) => {
    const res = await api.post(`/post/tag/${postId}/${tagId}`, null, authHeader(token));
    return res.data;
  },

  removePostTagFromPost: async (postId: number, tagId: number, token: string) => {
    const res = await api.delete(`/post/tag/${postId}/${tagId}`, authHeader(token));
    return res.data;  
  },

};