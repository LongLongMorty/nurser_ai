<template>
  <div class="bed-diagram">
    <!-- 页面头部 -->
    <div class="page-header">
      <h2>床位示意图</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="statistics-cards">
      <el-row :gutter="20">
        <el-col :span="6">          <el-card class="stat-card total">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><House /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.totalBeds }}</div>
                <div class="stat-label">总床位数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card available">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Check /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.availableBeds }}</div>
                <div class="stat-label">空闲床位</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card occupied">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><User /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.occupiedBeds }}</div>
                <div class="stat-label">有人床位</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card out">
            <div class="stat-content">
              <div class="stat-icon">
                <el-icon><Right /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.outBeds }}</div>
                <div class="stat-label">外出床位</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 楼层选择 -->
    <div class="floor-selector">
      <el-card>
        <div class="selector-content">
          <span class="selector-label">选择楼层：</span>
          <el-radio-group v-model="selectedFloor" @change="handleFloorChange">
            <el-radio-button 
              v-for="floor in floorOptions" 
              :key="floor" 
              :value="floor"
            >
              {{ floor }}楼
            </el-radio-button>
          </el-radio-group>
        </div>
      </el-card>
    </div>

    <!-- 床位图表 -->
    <div class="bed-diagram-content">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>{{ selectedFloor }}楼 床位分布图</span>
            <div class="legend">
              <span class="legend-item available">
                <span class="legend-color"></span>
                空闲
              </span>
              <span class="legend-item occupied">
                <span class="legend-color"></span>
                有人
              </span>
              <span class="legend-item out">
                <span class="legend-color"></span>
                外出
              </span>
            </div>
          </div>
        </template>

        <div v-loading="loading" class="diagram-container">
          <div v-if="roomsData.length === 0" class="empty-floor">
            <el-empty description="该楼层暂无房间数据" />
          </div>
          <div v-else class="rooms-grid">
            <div 
              v-for="room in roomsData" 
              :key="room.roomId"
              class="room-card"
            >
              <div class="room-header">
                <h4>{{ room.roomNo }}</h4>
                <span class="room-name">{{ room.roomName }}</span>
              </div>
              <div class="beds-container">
                <div 
                  v-for="bed in room.beds"
                  :key="bed.bedId"
                  :class="['bed-item', getBedStatusClass(bed.bedStatus)]"
                  @click="handleBedClick(bed, room)"
                >
                  <div class="bed-header">
                    <span class="bed-no">{{ bed.bedNo }}</span>
                    <span class="bed-type">{{ formatBedType(bed.bedType) }}</span>
                  </div>
                  <div v-if="bed.customerName" class="customer-info">
                    <el-icon><User /></el-icon>
                    <span>{{ bed.customerName }}</span>
                  </div>
                  <div v-else class="bed-empty">
                    <el-icon><Bed /></el-icon>
                    <span>空床</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 床位详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      title="床位详情"
      width="600px"
    >
      <div v-if="selectedBedDetail" class="bed-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="床位号">{{ selectedBedDetail.bedNo }}</el-descriptions-item>
          <el-descriptions-item label="床位类型">{{ formatBedType(selectedBedDetail.bedType) }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ selectedBedDetail.roomNo }}</el-descriptions-item>
          <el-descriptions-item label="床位状态">
            <el-tag :type="getStatusTagType(selectedBedDetail.bedStatus)">
              {{ formatBedStatus(selectedBedDetail.bedStatus) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedBedDetail.customerName" label="客户姓名">
            {{ selectedBedDetail.customerName }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedBedDetail.careLevel" label="护理级别">
            {{ selectedBedDetail.careLevel }}
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedBedDetail.healthManagerName" label="健康管家">
            {{ selectedBedDetail.healthManagerName }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { House, Check, User, Right } from '@element-plus/icons-vue'
import { bedApi, type BedStatistics, type BedDiagram, type BedDiagramItem } from '@/api/bed'

interface RoomData {
  roomId: number
  roomNo: string
  roomName: string
  roomType: string
  beds: BedDiagramItem[]
}

// 响应式数据
const loading = ref(false)
const selectedFloor = ref(1)
const detailVisible = ref(false)
const selectedBedDetail = ref<BedDiagramItem | null>(null)

const statistics = reactive<BedStatistics>({
  totalBeds: 0,
  availableBeds: 0,
  occupiedBeds: 0,
  outBeds: 0,
  occupancyRate: 0,
  byFloor: [],
  byType: []
})

const roomsData = ref<RoomData[]>([])
const floorOptions = ref([1, 2, 3, 4, 5]) // 修改为5层楼

// 获取床位统计数据
const fetchStatistics = async () => {
  try {
    const response = await bedApi.getStatistics()
    console.log('统计数据响应:', response)
    Object.assign(statistics, response)
  } catch (error) {
    console.error('获取床位统计失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

// 获取楼层床位数据
const fetchFloorData = async (floorNo: number) => {
  loading.value = true
  try {
    const response = await bedApi.getBedDiagram()
    console.log('API响应:', response)
    console.log('数据类型:', typeof response)
    
    if (!response || !Array.isArray(response)) {
      console.error('响应数据格式不正确:', response)
      ElMessage.error('获取的数据格式不正确')
      return
    }
    
    const floorData = response.find((floor: BedDiagram) => floor.floorNo === floorNo)
    console.log('找到的楼层数据:', floorData)
    roomsData.value = floorData?.rooms || []
  } catch (error) {
    console.error('获取楼层数据失败:', error)
    ElMessage.error('获取楼层数据失败')
  } finally {
    loading.value = false
  }
}

// 楼层切换
const handleFloorChange = (floorNo: number) => {
  fetchFloorData(floorNo)
}

// 床位点击
const handleBedClick = (bed: BedDiagramItem, room: RoomData) => {
  selectedBedDetail.value = {
    ...bed,
    roomNo: room.roomNo,
    roomName: room.roomName
  }
  detailVisible.value = true
}

// 格式化方法
const formatBedType = (type: string) => {
  const typeMap: Record<string, string> = {
    'STANDARD': '标准床',
    'CARE': '护理床'
  }
  return typeMap[type] || type
}

const formatBedStatus = (status: string) => {
  const statusMap: Record<string, string> = {
    'AVAILABLE': '空闲',
    'OCCUPIED': '有人',
    'OUT': '外出'
  }
  return statusMap[status] || status
}

const getBedStatusClass = (status: string) => {
  const classMap: Record<string, string> = {
    'AVAILABLE': 'available',
    'OCCUPIED': 'occupied', 
    'OUT': 'out'
  }
  return classMap[status] || ''
}

const getStatusTagType = (status: string) => {
  const typeMap: Record<string, string> = {
    'AVAILABLE': 'success',
    'OCCUPIED': 'warning',
    'OUT': 'info'
  }
  return typeMap[status] || 'info'
}

onMounted(() => {
  fetchStatistics()
  fetchFloorData(selectedFloor.value)
})
</script>

<style scoped>
.bed-diagram {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 20px;
}

.page-header h2 {
  color: white;
  font-size: 28px;
  font-weight: 600;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

/* 统计卡片样式 */
.statistics-cards {
  margin-bottom: 20px;
}

.stat-card {
  border: none;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.2);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
}

.stat-icon .el-icon {
  font-size: 24px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  line-height: 1;
  margin-bottom: 5px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 不同状态的卡片颜色 */
.stat-card.total .stat-icon {
  background: linear-gradient(45deg, #3f51b5, #2196f3);
}

.stat-card.total .stat-number {
  color: #3f51b5;
}

.stat-card.available .stat-icon {
  background: linear-gradient(45deg, #4caf50, #8bc34a);
}

.stat-card.available .stat-number {
  color: #4caf50;
}

.stat-card.occupied .stat-icon {
  background: linear-gradient(45deg, #ff9800, #ffc107);
}

.stat-card.occupied .stat-number {
  color: #ff9800;
}

.stat-card.out .stat-icon {
  background: linear-gradient(45deg, #9e9e9e, #607d8b);
}

.stat-card.out .stat-number {
  color: #9e9e9e;
}

/* 楼层选择器 */
.floor-selector {
  margin-bottom: 20px;
}

.selector-content {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px;
}

.selector-label {
  margin-right: 20px;
  font-weight: 500;
  color: #333;
}

/* 床位图表 */
.bed-diagram-content {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.legend {
  display: flex;
  gap: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-right: 6px;
}

.legend-item.available .legend-color {
  background: #67c23a;
}

.legend-item.occupied .legend-color {
  background: #e6a23c;
}

.legend-item.out .legend-color {
  background: #909399;
}

/* 房间网格 */
.diagram-container {
  min-height: 400px;
}

.rooms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  padding: 20px;
}

.room-card {
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  padding: 15px;
  background: #fafafa;
  transition: all 0.3s ease;
}

.room-card:hover {
  border-color: #409eff;
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.2);
}

.room-header {
  text-align: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #e4e7ed;
}

.room-header h4 {
  margin: 0 0 5px 0;
  color: #303133;
  font-size: 18px;
}

.room-name {
  color: #606266;
  font-size: 14px;
}

/* 床位容器 */
.beds-container {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.bed-item {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.bed-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.bed-item.available {
  border-color: #67c23a;
  background: #f0f9ff;
}

.bed-item.available:hover {
  background: #e1f3d8;
}

.bed-item.occupied {
  border-color: #e6a23c;
  background: #fdf6ec;
}

.bed-item.occupied:hover {
  background: #faecd8;
}

.bed-item.out {
  border-color: #909399;
  background: #f4f4f5;
}

.bed-item.out:hover {
  background: #e9e9eb;
}

.bed-header {
  margin-bottom: 8px;
}

.bed-no {
  font-weight: bold;
  color: #303133;
  display: block;
  margin-bottom: 2px;
}

.bed-type {
  font-size: 12px;
  color: #909399;
}

.customer-info, .bed-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  color: #606266;
}

.customer-info {
  color: #e6a23c;
}

.bed-empty {
  color: #67c23a;
}

.empty-floor {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
}

/* 床位详情 */
.bed-detail {
  padding: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bed-diagram {
    padding: 10px;
  }
  
  .rooms-grid {
    grid-template-columns: 1fr;
    padding: 10px;
  }
  
  .legend {
    flex-direction: column;
    gap: 10px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .selector-content {
    flex-direction: column;
    gap: 10px;
  }
}
</style>
