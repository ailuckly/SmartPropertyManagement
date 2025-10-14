# Git推送指南

## 当前状态

✅ **已完成6个本地提交，待推送到远程仓库**

```
本地分支 (main) 领先远程分支 (origin/main) 6个提交
```

## 提交清单

```
* 63b9e73 refactor: 将Role.name从枚举类型改为String类型
* 96e9910 docs: 添加功能测试和API文档
* 59757b3 perf: 优化租约查询性能，使用EntityGraph避免N+1问题
* d68d5e9 feat: 增强收支记录查询功能，支持直接查询所有记录
* 5bd9955 fix: 修复用户认证和RefreshToken时间类型问题
* 9194e9f fix: 修复Mapper时间字段转换和Role名称处理
```

## 推送到远程仓库

### 方式1: 直接推送（推荐）
```bash
git push origin main
```

### 方式2: 设置上游分支后推送
```bash
git push -u origin main
```
使用 `-u` 参数会设置上游分支，以后可以直接使用 `git push`

### 方式3: 强制推送（谨慎使用）
```bash
git push -f origin main
```
⚠️ **警告**: 仅在确定要覆盖远程历史时使用！

## 推送前检查

### 1. 检查远程仓库状态
```bash
git fetch origin
git status
```

### 2. 查看差异
```bash
git log origin/main..main
```
显示本地有但远程没有的提交

### 3. 查看文件变更
```bash
git diff origin/main main --stat
```

## 推送后验证

### 1. 检查推送结果
```bash
git status
```
应该显示: `Your branch is up to date with 'origin/main'`

### 2. 查看远程日志
```bash
git log origin/main -5 --oneline
```

### 3. 验证GitHub/GitLab
登录远程仓库网站，确认提交已经上传

## 可能遇到的问题

### 问题1: 远程有新提交
```
! [rejected]        main -> main (fetch first)
```

**解决方案**:
```bash
# 先拉取远程更新
git pull origin main --rebase

# 然后再推送
git push origin main
```

### 问题2: 权限不足
```
fatal: Authentication failed
```

**解决方案**:
- 检查SSH密钥配置
- 或使用HTTPS并输入用户名密码
- 或配置GitHub Personal Access Token

### 问题3: 分支保护
```
remote: error: GH006: Protected branch update failed
```

**解决方案**:
- 检查仓库的分支保护规则
- 可能需要创建Pull Request而不是直接推送

## 备选方案: 创建Pull Request

如果直接推送受限，可以：

### 1. 创建新分支
```bash
git checkout -b feature/lease-payment-improvements
```

### 2. 推送到新分支
```bash
git push origin feature/lease-payment-improvements
```

### 3. 在GitHub/GitLab创建PR
- 访问远程仓库网站
- 创建从 `feature/lease-payment-improvements` 到 `main` 的Pull Request
- 等待审核和合并

## 推送后的工作

### 1. 清理本地
```bash
# 删除临时文件（可选）
git clean -fd

# 查看未跟踪的文件
git status
```

### 2. 同步标签（如果有）
```bash
git push origin --tags
```

### 3. 通知团队
- 在团队聊天工具中通知
- 或在项目管理工具中更新状态

## 快速命令参考

```bash
# 查看当前状态
git status

# 查看提交历史
git log --oneline -10

# 推送到远程
git push origin main

# 拉取远程更新
git pull origin main

# 查看远程信息
git remote -v
```

## 注意事项

1. ✅ 所有代码已编译通过
2. ⚠️ 推送前建议运行测试
3. ⚠️ 确认没有敏感信息（密码、密钥等）
4. ✅ 提交信息清晰明确
5. ✅ 代码已分模块提交

## 下一步

推送成功后：
1. 重启应用验证功能
2. 运行测试脚本
3. 更新项目文档
4. 通知团队成员

---

📝 **详细的提交说明请查看**: [COMMITS_SUMMARY.md](./COMMITS_SUMMARY.md)
