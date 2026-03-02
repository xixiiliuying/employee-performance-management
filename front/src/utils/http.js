// utils/http.js
import axios from 'axios'
import router from '@/router/router-static'
import storage from '@/utils/storage'
import base from '@/utils/base' // <--- 确保引入 base 配置

// 获取最新的 baseURL (例如: http://localhost:8081/springboot/)
const BASE_URL = base.get().url;

const http = axios.create({
    timeout: 1000 * 86400,
    withCredentials: true,
    // 使用 base.js 中的配置作为 baseURL
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json; charset=utf-8'
    }
})

http.interceptors.request.use(config => {
    // ❌ 不要给文件下载请求加 JSON 类型
    if (config.responseType !== 'blob' && config.responseType !== 'arraybuffer') {
        if (config.method === 'post' && !config.headers['Content-Type']) {
            config.headers['Content-Type'] = 'application/json; charset=utf-8'
        }
    }

    // ✅ Token
    config.headers['Token'] = storage.get('Token')
    return config
}, error => Promise.reject(error))


// 响应拦截
http.interceptors.response.use(response => {
    // 重要：如果是blob类型，直接返回原始响应，不进行任何处理
    if (response.config.responseType === 'blob' || response.config.responseType === 'arraybuffer') {
        console.log('🔍 检测到blob响应，直接返回原始响应')
        return response
    }

    // 401, token失效 (判断 code 而非 HTTP Status Code)
    if (response.data && response.data.code === 401) {
        router.push({ name: 'login' });
    }
    return response
}, error => {
    // 可以在这里处理 HTTP Status Code 401
    if (error.response && error.response.status === 401) {
        // 如果后端使用 HTTP 401 响应表示未授权
        router.push({ name: 'login' });
    }
    return Promise.reject(error)
})
export default http