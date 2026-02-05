/**
 * 获取图片完整URL
 * @param path 图片相对路径或完整URL
 * @returns 完整的图片访问URL
 */
export function getImageUrl(path: string): string {
  // 如果是完整URL或base64，直接返回
  if (!path || path.startsWith('http') || path.startsWith('data:')) {
    return path;
  }
  
  // 如果是服务器相对路径，拼接API基础URL
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
  return `${baseURL}/file/img/${path}`;
}
