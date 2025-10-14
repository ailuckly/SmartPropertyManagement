import api from '@/api/http';

/**
 * 下载文件的通用函数
 * @param {string} url - 下载的 API 端点
 * @param {string} defaultFilename - 默认文件名（如果响应头中没有文件名）
 * @param {Object} params - 查询参数（可选）
 */
export async function downloadFile(url, defaultFilename = 'download.xlsx', params = {}) {
  try {
    const response = await api.get(url, {
      params,
      responseType: 'blob', // 重要：告诉 axios 以 blob 格式接收响应
    });

    // 从响应头获取文件名（如果后端提供了）
    const contentDisposition = response.headers['content-disposition'];
    let filename = defaultFilename;
    
    if (contentDisposition) {
      const filenameMatch = contentDisposition.match(/filename=(.+)/);
      if (filenameMatch && filenameMatch[1]) {
        filename = filenameMatch[1].replace(/['"]/g, ''); // 移除引号
      }
    }

    // 创建 Blob 对象
    const blob = new Blob([response.data], {
      type: response.headers['content-type'] || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });

    // 创建下载链接
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    
    // 触发下载
    document.body.appendChild(link);
    link.click();
    
    // 清理
    document.body.removeChild(link);
    window.URL.revokeObjectURL(downloadUrl);
    
    return { success: true, filename };
  } catch (error) {
    console.error('文件下载失败:', error);
    throw error;
  }
}

/**
 * 导出物业列表
 * @param {number} ownerId - 业主ID（可选）
 */
export async function exportProperties(ownerId) {
  const params = ownerId ? { ownerId } : {};
  return downloadFile('/properties/export', '物业列表.xlsx', params);
}

/**
 * 导出租约列表
 */
export async function exportLeases() {
  return downloadFile('/leases/export', '租约列表.xlsx');
}

/**
 * 导出收支记录
 * @param {number} leaseId - 租约ID（可选）
 */
export async function exportPayments(leaseId) {
  const params = leaseId ? { leaseId } : {};
  return downloadFile('/payments/export', '收支记录.xlsx', params);
}

/**
 * 导出维修记录
 */
export async function exportMaintenanceRequests() {
  return downloadFile('/maintenance-requests/export', '维修记录.xlsx');
}
