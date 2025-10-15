# 文件预览功能修复说明

## 🐛 问题描述

上传图片后，无法在前端预览服务器上的图片。后端返回 404 或无法加载图片。

### 根本原因

`FileController` 中的 `previewFile()` 和 `downloadFile()` 方法试图从 UUID 格式的文件名中解析文件 ID：

```java
// ❌ 错误的代码
File fileInfo = fileService.getFile(
    Long.parseLong(fileName.substring(0, fileName.lastIndexOf('.')))
);
```

但实际的文件名格式是：`81e18cc4-c96c-4313-a088-648beae274de.jpg`（UUID），不是数字 ID。

## ✅ 修复方案

### 1. 修改 FileController

**修改前：**
```java
@GetMapping("/preview/{fileName:.+}")
public ResponseEntity<Resource> previewFile(@PathVariable String fileName) {
    Resource resource = fileService.loadFileAsResource(fileName);
    
    // ❌ 尝试从文件名中解析 ID（会失败）
    File fileInfo = fileService.getFile(
        Long.parseLong(fileName.substring(0, fileName.lastIndexOf('.')))
    );
    
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(fileInfo.getFileType()))
        .body(resource);
}
```

**修改后：**
```java
@GetMapping("/preview/{fileName:.+}")
public ResponseEntity<Resource> previewFile(@PathVariable String fileName) {
    Resource resource = fileService.loadFileAsResource(fileName);
    
    // ✅ 通过存储文件名直接查询数据库
    File fileInfo = fileService.getFileByStoredName(fileName);
    
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(fileInfo.getFileType()))
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
        .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")  // 添加缓存
        .body(resource);
}
```

### 2. 添加 FileService.getFileByStoredName() 方法

```java
/**
 * 根据存储文件名获取文件
 * 
 * @param storedFileName 存储文件名
 * @return 文件对象
 */
public File getFileByStoredName(String storedFileName) {
    return fileRepository.findByStoredFileName(storedFileName)
            .orElseThrow(() -> new IllegalArgumentException("文件不存在: " + storedFileName));
}
```

### 3. FileRepository 已有方法（无需修改）

```java
Optional<File> findByStoredFileName(String storedFileName);
```

## 🎯 工作流程

### 修复前的流程（失败）：
```
1. 前端请求: GET /api/files/preview/81e18cc4-c96c-4313-a088-648beae274de.jpg
   ↓
2. 后端尝试解析: Long.parseLong("81e18cc4-c96c-4313-a088-648beae274de")
   ↓
3. ❌ NumberFormatException - 失败！
```

### 修复后的流程（成功）：
```
1. 前端请求: GET /api/files/preview/81e18cc4-c96c-4313-a088-648beae274de.jpg
   ↓
2. 后端查询: fileRepository.findByStoredFileName("81e18cc4-c96c-4313-a088-648beae274de.jpg")
   ↓
3. 找到文件记录 → 读取磁盘文件 → 返回图片
   ↓
4. ✅ 前端成功显示图片！
```

## 📦 完整的文件上传和预览流程

### 1. 用户上传图片
```
POST /api/files/upload
- file: 图片文件
- category: PROPERTY_IMAGE
- entityId: 1
```

### 2. 后端处理
```java
// 生成 UUID 文件名
String storedFileName = UUID.randomUUID().toString() + ".jpg";
// 例如: 81e18cc4-c96c-4313-a088-648beae274de.jpg

// 保存到磁盘
Path targetLocation = uploadPath.resolve(storedFileName);
Files.copy(multipartFile.getInputStream(), targetLocation);

// 保存到数据库
file.setStoredFileName(storedFileName);
file.setFilePath("properties/81e18cc4-c96c-4313-a088-648beae274de.jpg");
fileRepository.save(file);
```

### 3. 返回响应
```json
{
  "success": true,
  "message": "文件上传成功",
  "file": {
    "id": 1,
    "originalFileName": "house.jpg",
    "storedFileName": "81e18cc4-c96c-4313-a088-648beae274de.jpg",
    "filePath": "properties/81e18cc4-c96c-4313-a088-648beae274de.jpg",
    "fileType": "image/jpeg",
    "fileSize": 1728512
  }
}
```

### 4. 前端预览
```javascript
// 构建预览 URL
const previewUrl = `http://localhost:8080/api/files/preview/${file.storedFileName}`;
// 例如: http://localhost:8080/api/files/preview/81e18cc4-c96c-4313-a088-648beae274de.jpg

// 在 img 标签中使用
<img :src="previewUrl" />
```

### 5. 后端响应预览请求
```
GET /api/files/preview/81e18cc4-c96c-4313-a088-648beae274de.jpg
   ↓
1. 查询数据库: findByStoredFileName("81e18cc4-c96c-4313-a088-648beae274de.jpg")
2. 获取文件路径: "properties/81e18cc4-c96c-4313-a088-648beae274de.jpg"
3. 读取磁盘文件: uploads/properties/81e18cc4-c96c-4313-a088-648beae274de.jpg
4. 返回图片数据
   ↓
✅ 浏览器显示图片
```

## 🔧 测试步骤

### 1. 重新编译后端（如果后端正在运行）
```bash
# Ctrl+C 停止后端
cd backend
./mvnw clean compile
./mvnw spring-boot:run
```

### 2. 测试文件上传
访问 `http://localhost:5173/file-upload-test`，上传一张图片。

### 3. 验证实时预览
- **选择图片时**：应该立即看到本地预览（blob URL）
- **上传成功后**：自动切换到服务器图片

### 4. 检查浏览器控制台
```
[FileUpload] File changed: {...}
[FileUpload] Created local preview URL: blob:http://localhost:5173/xxx
[FileUpload] Upload success response: {success: true, file: {...}}
[FileUpload] Revoked local blob URL
[FileUpload] Server preview URL: http://localhost:8080/api/files/preview/81e18cc4-c96c-4313-a088-648beae274de.jpg
```

### 5. 直接访问预览 URL
在浏览器中打开控制台输出的预览 URL，应该能看到图片。

## 📝 修改文件列表

1. ✅ `FileController.java` - 修复 `previewFile()` 和 `downloadFile()` 方法
2. ✅ `FileService.java` - 添加 `getFileByStoredName()` 方法
3. ✅ `FileUpload.vue` - 添加实时预览功能
4. ✅ `FileRepository.java` - 已有 `findByStoredFileName()` 方法

## 🎉 预期效果

### 修复前：
- ❌ 上传后看不到图片
- ❌ 预览 URL 返回 404
- ❌ 控制台报错：NumberFormatException

### 修复后：
- ✅ 选择图片时立即预览（本地 blob）
- ✅ 上传成功后显示服务器图片
- ✅ 预览 URL 正常工作
- ✅ 图片可以正常加载和显示

## 🚀 额外优化

1. **添加了缓存控制头**
   ```java
   .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
   ```
   - 浏览器会缓存图片 1 小时
   - 减少重复请求，提高性能

2. **详细的日志输出**
   - 方便调试和排查问题
   - 可以清楚地看到整个上传和预览流程

## ⚠️ 注意事项

1. **文件命名规则**：
   - 存储文件名：`UUID.扩展名`（如 `81e18cc4-c96c-4313-a088-648beae274de.jpg`）
   - 不是数字 ID 格式

2. **数据库查询**：
   - 必须通过 `storedFileName` 字段查询
   - 不能尝试从文件名解析 ID

3. **CORS 配置**：
   - 确保后端 CORS 允许前端域名
   - 已配置：`http://localhost:5173`

4. **认证要求**：
   - 预览接口需要登录认证
   - 前端使用 `withCredentials: true` 携带 Cookie

## 📚 相关文档

- [文件上传功能说明](frontend/README_FILE_UPLOAD.md)
- [本地文件系统说明](backend/test_local_upload.ps1)

## ✅ 验证清单

- [ ] 后端编译成功
- [ ] 后端启动成功
- [ ] 前端启动成功
- [ ] 可以选择图片（本地预览）
- [ ] 可以上传图片
- [ ] 上传后显示服务器图片
- [ ] 直接访问预览 URL 可以看到图片
- [ ] 控制台无错误信息

全部完成即修复成功！🎊
