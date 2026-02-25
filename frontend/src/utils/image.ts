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
  
  // 如果是服务器相对路径，使用相对路径拼接
  // 这样会自动使用当前页面的域名和端口，支持局域网访问
  return `/api/file/img/${path}`;
}

/**
 * 获取头像完整URL
 * @param fileName 头像文件名
 * @returns 完整的头像访问URL
 */
export function getAvatarUrl(fileName: string | undefined): string {
  if (!fileName) {
    return '';
  }
  
  // 如果是完整URL或base64，直接返回
  if (fileName.startsWith('http') || fileName.startsWith('data:')) {
    return fileName;
  }
  
  return `/api/file/avatar/${fileName}`;
}
