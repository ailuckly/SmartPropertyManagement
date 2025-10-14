# 数据可视化界面设计文档

## 📊 概述

为智慧物业管理平台设计一个全面的数据可视化界面，展示关键业务指标和趋势分析。

## 🎯 设计目标

1. **直观展示** - 用图表直观展示业务数据
2. **实时更新** - 数据实时刷新
3. **交互友好** - 支持筛选、钻取等交互
4. **响应式** - 适配不同屏幕尺寸

## 📐 页面布局

```
┌─────────────────────────────────────────────────────────────┐
│                   数据概览（4个统计卡片）                      │
├──────────────────────┬──────────────────────────────────────┤
│                      │                                      │
│   收入趋势图         │      物业状态分布图                  │
│   (折线图/柱状图)    │      (饼图/环形图)                   │
│                      │                                      │
├──────────────────────┴──────────────────────────────────────┤
│                   维修工单状态分布（柱状图）                  │
├──────────────────────┬──────────────────────────────────────┤
│                      │                                      │
│   城市物业分布       │      租约状态统计                    │
│   (柱状图)           │      (饼图)                          │
│                      │                                      │
└──────────────────────┴──────────────────────────────────────┘
```

## 📊 图表设计

### 1. 数据概览卡片（顶部）

**4个核心指标：**
- 总收入（本月）
- 物业总数
- 活跃租约数
- 待处理维修数

**样式：**
- 使用 `el-statistic` 组件
- 带图标和颜色区分
- 支持同比/环比变化显示

### 2. 收入趋势图（左上）

**图表类型：** 折线图 + 柱状图（组合）

**数据维度：**
- X轴：月份（最近6个月）
- Y轴：金额（元）
- 数据：每月租金收入

**功能：**
- 显示趋势
- hover 显示详细数据
- 支持导出

### 3. 物业状态分布图（右上）

**图表类型：** 环形图（Doughnut）

**数据维度：**
- 可出租（绿色）
- 已出租（蓝色）
- 维护中（橙色）

**功能：**
- 显示百分比
- 点击查看详情

### 4. 维修工单状态分布（中部）

**图表类型：** 横向柱状图

**数据维度：**
- 待处理、处理中、已完成、已取消
- 不同颜色区分状态

### 5. 城市物业分布（左下）

**图表类型：** 柱状图

**数据维度：**
- X轴：城市（郑州、洛阳、开封等）
- Y轴：物业数量
- 按类型堆叠显示

### 6. 租约状态统计（右下）

**图表类型：** 饼图

**数据维度：**
- 生效中、已过期、已终止

## 🛠️ 技术实现

### 图表库选择

**推荐：Apache ECharts**
- Vue 3 官方支持
- 图表类型丰富
- 性能优秀
- 文档完善

**安装：**
```bash
npm install echarts
```

### 组件结构

```
AnalyticsView.vue
├── DataOverview（数据概览）
├── RevenueChart（收入趋势）
├── PropertyStatusChart（物业状态）
├── MaintenanceChart（维修统计）
├── CityDistributionChart（城市分布）
└── LeaseStatusChart（租约状态）
```

## 🎨 配色方案

```javascript
const colorPalette = {
  primary: '#409EFF',    // 蓝色 - 主色
  success: '#67C23A',    // 绿色 - 成功/可用
  warning: '#E6A23C',    // 橙色 - 警告/维护
  danger: '#F56C6C',     // 红色 - 危险/已取消
  info: '#909399',       // 灰色 - 信息/已过期
  
  // 渐变色
  gradients: {
    blue: ['#409EFF', '#79bbff'],
    green: ['#67C23A', '#95d475'],
    purple: ['#9c27b0', '#ba68c8']
  }
};
```

## 📡 API 接口设计

### 统计数据接口

**GET `/api/analytics/overview`**
```json
{
  "totalRevenue": 45600,
  "totalProperties": 20,
  "activeLeases": 10,
  "pendingMaintenance": 4
}
```

**GET `/api/analytics/revenue-trend`**
```json
{
  "months": ["2024-08", "2024-09", "2024-10", "2024-11", "2024-12", "2025-01"],
  "revenue": [13000, 16500, 24500, 28000, 41300, 14000]
}
```

**GET `/api/analytics/property-status`**
```json
{
  "AVAILABLE": 7,
  "LEASED": 11,
  "UNDER_MAINTENANCE": 2
}
```

**GET `/api/analytics/maintenance-status`**
```json
{
  "PENDING": 4,
  "IN_PROGRESS": 3,
  "COMPLETED": 10,
  "CANCELLED": 2
}
```

**GET `/api/analytics/city-distribution`**
```json
{
  "cities": ["郑州", "洛阳", "开封"],
  "distribution": {
    "郑州": { "APARTMENT": 14, "HOUSE": 2, "COMMERCIAL": 3 },
    "洛阳": { "APARTMENT": 3 },
    "开封": { "APARTMENT": 2 }
  }
}
```

**GET `/api/analytics/lease-status`**
```json
{
  "ACTIVE": 10,
  "EXPIRED": 3,
  "TERMINATED": 2
}
```

## 🔄 数据刷新策略

1. **页面加载**：立即获取所有数据
2. **定时刷新**：每60秒自动刷新一次（可配置）
3. **手动刷新**：提供刷新按钮
4. **实时更新**：数据变更后主动通知（可选，使用 WebSocket）

## 📱 响应式设计

### 桌面端（≥1200px）
- 2列布局
- 所有图表同屏显示

### 平板端（768px-1199px）
- 2列布局
- 图表自适应缩放

### 移动端（<768px）
- 1列布局
- 图表垂直排列
- 简化展示

## ✨ 交互设计

### 图表交互
- **hover**: 显示详细数据
- **click**: 跳转到详情页面
- **legend**: 点击筛选数据系列
- **zoom**: 支持图表缩放（时间轴）

### 数据筛选
- 时间范围选择器
- 城市筛选
- 物业类型筛选

## 🎯 开发优先级

### P0 - 核心功能（必须）
1. ✅ 数据概览卡片
2. ✅ 收入趋势图
3. ✅ 物业状态分布图

### P1 - 重要功能（应该）
4. ✅ 维修工单统计
5. ✅ 租约状态统计

### P2 - 增强功能（可以）
6. 城市分布图
7. 数据导出功能
8. 图表动画效果

## 📝 代码示例

### ECharts 基础配置

```javascript
const chartOption = {
  title: {
    text: '收入趋势',
    left: 'center',
    textStyle: {
      color: '#303133',
      fontSize: 16,
      fontWeight: 500
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'cross'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: months
  },
  yAxis: {
    type: 'value',
    name: '金额（元）'
  },
  series: [{
    name: '收入',
    type: 'line',
    smooth: true,
    data: revenue,
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ]
      }
    }
  }]
};
```

## 🚀 性能优化

1. **按需加载**：使用 ECharts 按需引入
2. **数据缓存**：缓存统计数据，减少请求
3. **图表懒加载**：滚动到可见区域再渲染
4. **防抖节流**：图表 resize 事件防抖

## 📚 参考资料

- [Apache ECharts 官方文档](https://echarts.apache.org/zh/index.html)
- [Element Plus Statistic 组件](https://element-plus.org/zh-CN/component/statistic.html)
- [Vue ECharts](https://github.com/ecomfe/vue-echarts)

---

**最后更新**: 2025-01-14
