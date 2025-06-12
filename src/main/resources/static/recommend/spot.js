// 景点详情页面脚本
document.addEventListener('DOMContentLoaded', function() {
  // 获取URL参数
  const urlParams = new URLSearchParams(window.location.search);
  const spotId = urlParams.get('id');
  
  // 检查用户登录状态
  //checkLoginStatus();
  
  // 如果有景点ID，加载景点详情
  if (spotId) {
    loadSpotDetails(spotId);
  } else {
    // 无效ID，显示错误信息
    showErrorMessage('未找到有效的景点ID，请返回推荐页面重新选择景点');
  }
  
  // 注册事件监听器
  registerEventListeners();
});

// 显示错误信息
function showErrorMessage(message) {
  const container = document.querySelector('.spot-container');
  if (container) {
    container.innerHTML = `
      <div class="error-message">
        <i class="fas fa-exclamation-circle"></i>
        <h2>出错了</h2>
        <p>${message}</p>
        <a href="recommend.html" class="back-btn">返回推荐页面</a>
      </div>
    `;
  }
}

// 检查用户登录状态
function checkLoginStatus() {
  const username = localStorage.getItem('username');
  const usernameElement = document.getElementById('username');
  
  if (username) {
    usernameElement.textContent = `${username}`;
  } else {
    // 未登录，重定向到登录页
    window.location.href = '../authen/authen.html';
  }
  
  // 退出登录按钮事件
  document.getElementById('logout-btn').addEventListener('click', function() {
    localStorage.removeItem('username');
    localStorage.removeItem('userId');
    window.location.href = '../authen/authen.html';
  });
}

// 加载景点详情
function loadSpotDetails(spotId) {
  // 首先尝试从localStorage获取景点数据
  const storedSpotData = localStorage.getItem('currentSpotData');
  
  console.log('开始加载景点ID:', spotId);
  
  if (storedSpotData) {
    try {
      const spotData = JSON.parse(storedSpotData);
      console.log('从localStorage获取的数据:', spotData);
      
      // 确认ID匹配，避免加载错误数据
      if (spotData.id === spotId) {
        console.log('使用localStorage中的景点数据，ID匹配');
        updateSpotUI(spotData);
        
        // 确保spotData.name存在
        if (spotData.name) {
          loadRelatedDiaries(spotData.name);
          initMap(spotData.name);
        } else {
          console.error('景点数据中缺少name字段');
          showErrorMessage('景点数据不完整，缺少名称信息');
        }
        return;
      } else {
        console.log('localStorage中的数据ID不匹配，ID:', spotData.id);
      }
    } catch (error) {
      console.error('解析localStorage中的景点数据失败:', error);
      // 解析失败时不做处理，继续使用API获取
    }
  } else {
    console.log('localStorage中没有景点数据');
  }
  
  // 如果localStorage中没有数据或数据不匹配，则使用API获取
  console.log('准备使用API获取景点数据，ID:', spotId);
  // 使用正确的API路径获取景点数据
  fetch(`/api/attractions/${spotId}`)
    .then(response => {
      console.log('API响应状态:', response.status);
      if (!response.ok) {
        throw new Error(`服务器响应错误: ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      console.log('API返回的景点数据:', data);
      if (!data || !data.name) {
        throw new Error('获取到的景点数据无效或缺少必要字段');
      }
      updateSpotUI(data);
      loadRelatedDiaries(data.name); // 使用景点名称加载相关日记
      initMap(data.name); // 初始化地图
    })
    .catch(error => {
      console.error('加载景点数据失败:', error);
      showErrorMessage(`无法加载景点数据: ${error.message}`);
    });
}

// 更新页面UI
function updateSpotUI(spotData) {
  // 更新标题和基本信息
  document.getElementById('spot-name').textContent = spotData.name;
  document.getElementById('spot-category').textContent = spotData.category || '未分类';
  document.title = `${spotData.name} - 景点详情`;
  
  // 更新关键词标签
  const keywordsContainer = document.getElementById('spot-keywords');
  keywordsContainer.innerHTML = '';
  
  // 处理关键词，可能是字符串或数组
  let keywords = [];
  if (spotData.keywords) {
    if (typeof spotData.keywords === 'string') {
      keywords = spotData.keywords.split(',');
    } else if (Array.isArray(spotData.keywords)) {
      keywords = spotData.keywords;
    }
  }
  
  keywords.forEach(keyword => {
    const tagElement = document.createElement('span');
    tagElement.className = 'keyword-tag';
    tagElement.textContent = keyword.trim();
    keywordsContainer.appendChild(tagElement);
  });
  
  // 更新图片画廊
  const imagesContainer = document.getElementById('spot-images');
  imagesContainer.innerHTML = '';
  
  // 使用imageUrls字段获取图片
  let images = [];
  if (spotData.imageUrls && spotData.imageUrls.length > 0) {
    images = spotData.imageUrls;
  } else if (spotData.images && spotData.images.length > 0) {
    images = spotData.images; // 兼容旧格式
  }
  
  if (images.length > 0) {
    // 最多显示3张图片
    const imagesToShow = images.slice(0, 3);
    
    imagesToShow.forEach(imageUrl => {
      const imageElement = document.createElement('div');
      imageElement.className = 'gallery-image';
      
      const img = document.createElement('img');
      img.src = imageUrl;
      img.alt = spotData.name;
      
      imageElement.appendChild(img);
      imagesContainer.appendChild(imageElement);
    });
    
    // 如果图片不足3张，添加占位
    const placeholdersNeeded = 3 - imagesToShow.length;
    for (let i = 0; i < placeholdersNeeded; i++) {
      const placeholder = document.createElement('div');
      placeholder.className = 'gallery-placeholder';
      placeholder.innerHTML = '<i class="fas fa-image"></i><p>暂无图片</p>';
      imagesContainer.appendChild(placeholder);
    }
  } else {
    // 无图片时显示3个占位符
    for (let i = 0; i < 3; i++) {
      const placeholder = document.createElement('div');
      placeholder.className = 'gallery-placeholder';
      placeholder.innerHTML = '<i class="fas fa-image"></i><p>暂无图片</p>';
      imagesContainer.appendChild(placeholder);
    }
  }
  
  // 更新官方网站链接
  const officialSiteBtn = document.getElementById('official-site-btn');
  if (spotData.officialSite) {
    officialSiteBtn.href = spotData.officialSite;
  } else {
    officialSiteBtn.href = `https://www.baidu.com/s?wd=${encodeURIComponent(spotData.name)}`;
  }
  
  // 更新导航按钮链接
  document.getElementById('nav-btn').href = `../map/navigate.html?destination=${encodeURIComponent(spotData.name)}`;
}

// 初始化地图
function initMap(spotName) {
  console.log('初始化地图，景点名称:', spotName);
  
  if (!spotName) {
    console.error('无法初始化地图：缺少景点名称');
    return;
  }
  
  // 直接使用高德API搜索北京师范大学位置信息
  const searchUrl = `https://restapi.amap.com/v3/place/text?keywords=${encodeURIComponent(spotName)}&key=eaf5c2fa731dcd9b3ab32020e248e1aa&city=全国&extensions=all`;
  
  console.log('直接调用高德搜索API:', searchUrl);
  
  // 从高德搜索API获取位置信息
  fetch(searchUrl)
    .then(response => response.json())
    .then(data => {
      console.log('高德搜索API返回结果:', data);
      
      if (data.status === '1' && data.pois && data.pois.length > 0) {
        const poi = data.pois[0];
        console.log('找到POI:', poi);
        
        // 解析经纬度（位置格式：116.365499,39.961081）
        if (poi.location) {
          console.log('POI位置字符串:', poi.location);
          const locationParts = poi.location.split(',');
          
          if (locationParts.length === 2) {
            const lng = parseFloat(locationParts[0]);
            const lat = parseFloat(locationParts[1]);
            console.log('解析出的经纬度:', lng, lat);
            
            if (!isNaN(lng) && !isNaN(lat)) {
              renderMap(spotName, lng, lat, poi.address);
              return;
            }
          }
        }
        
        console.error('无法从POI解析位置信息:', poi);
        renderDefaultMap(spotName);
      } else {
        console.error('搜索API未返回有效结果:', data);
        renderDefaultMap(spotName);
      }
    })
    .catch(error => {
      console.error('API调用失败:', error);
      renderDefaultMap(spotName);
    });
}

// 渲染地图
function renderMap(spotName, lng, lat, address) {
  // 创建地图实例
  const map = new AMap.Map('spot-map', {
    zoom: 15,
    center: [lng, lat],
    viewMode: '3D'
  });
  
  // 保存地图实例到全局变量以便导航使用
  window.spotMap = map;
  
  // 添加地图控件
  map.addControl(new AMap.Scale());
  map.addControl(new AMap.ToolBar());
  map.addControl(new AMap.MapType({
    defaultType: 0
  }));
  
  // 创建标记
  const marker = new AMap.Marker({
    position: [lng, lat],
    title: spotName,
    map: map
  });
  
  // 创建信息窗体
  const infoWindow = new AMap.InfoWindow({
    isCustom: true,
    content: `
      <div class="map-info-window">
        <h3>${spotName}</h3>
        <p>${address || '位置信息暂无'}</p>
        <div class="info-actions">
          <a href="../map/navigate.html?destination=${encodeURIComponent(spotName)}&lng=${lng}&lat=${lat}" target="_blank" class="info-btn">
            <i class="fas fa-directions"></i> 导航前往
          </a>
        </div>
      </div>
    `,
    offset: new AMap.Pixel(0, -30)
  });
  
  // 点击标记时显示信息窗体
  marker.on('click', function() {
    infoWindow.open(map, marker.getPosition());
  });
  
  // 自动打开信息窗口
  infoWindow.open(map, marker.getPosition());
}

// 渲染默认地图
function renderDefaultMap(spotName) {
  console.log('渲染默认地图（北京天安门）');
  
  // 默认坐标（北京天安门）
  const defaultLng = 116.404;
  const defaultLat = 39.915;
  
  // 创建地图实例
  const map = new AMap.Map('spot-map', {
    zoom: 14,
    center: [defaultLng, defaultLat],
    viewMode: '3D'
  });
  
  // 添加控件
  map.plugin(['AMap.Scale', 'AMap.ToolBar'], function() {
    map.addControl(new AMap.Scale());
    map.addControl(new AMap.ToolBar());
  });
  
  // 显示错误提示
  const errorTip = document.createElement('div');
  errorTip.className = 'map-error-tip';
  errorTip.innerHTML = `
    <div class="map-error-icon"><i class="fas fa-exclamation-circle"></i></div>
    <div class="map-error-message">未能找到"${spotName}"的位置信息，显示默认位置</div>
  `;
  
  document.getElementById('spot-map').appendChild(errorTip);
}

// 加载相关日记
function loadRelatedDiaries(spotName) {
  if (!spotName) {
    console.error('缺少景点名称，无法加载相关日记');
    return;
  }

  // 使用正确的API路径获取相关日记
  fetch(`/api/diaries/location/${encodeURIComponent(spotName)}`)
    .then(response => {
      if (!response.ok) {
        throw new Error(`服务器响应错误: ${response.status}`);
      }
      return response.json();
    })
    .then(data => {
      if (!data) {
        throw new Error('获取到的日记数据无效');
      }
      updateRelatedDiariesUI(data);
    })
    .catch(error => {
      console.error('加载相关日记失败:', error);
      const container = document.getElementById('related-diary-container');
      container.innerHTML = `<p class="error-message">加载相关日记失败: ${error.message}</p>`;
    });
}

// 更新相关日记UI
function updateRelatedDiariesUI(diaries) {
  const container = document.getElementById('related-diary-container');
  container.innerHTML = '';
  
  if (!diaries || diaries.length === 0) {
    container.innerHTML = '<p class="no-content-message">暂无相关日记</p>';
    return;
  }
  
  // 存储原始数据以供排序使用
  window.relatedDiariesData = diaries;
  
  // 为每条日记创建UI元素
  diaries.forEach(diary => {
    const diaryLink = document.createElement('a');
    diaryLink.href = `../traveldiary/diary.html?id=${diary.id}`;
    diaryLink.className = 'diary-card';
    
    // 标题
    const title = document.createElement('h3');
    title.className = 'diary-title';
    title.textContent = diary.title;
    
    // 内容摘要
    const content = document.createElement('p');
    content.className = 'diary-excerpt';
    content.textContent = diary.content && diary.content.length > 100 ? 
      `${diary.content.substring(0, 100)}...` : (diary.content || '无内容');
    
    // 页脚信息
    const footer = document.createElement('div');
    footer.className = 'diary-footer';
    
    // 作者和日期
    const authorDate = document.createElement('div');
    authorDate.className = 'author-date';
    
    const author = document.createElement('span');
    author.className = 'diary-author';
    author.textContent = diary.user ? diary.user.username : '匿名用户';
    
    const date = document.createElement('span');
    date.className = 'diary-date';
    date.textContent = diary.createdAt ? formatDate(diary.createdAt) : '未知日期';
    
    authorDate.appendChild(author);
    authorDate.appendChild(document.createTextNode(' · '));
    authorDate.appendChild(date);
    
    // 统计信息
    const stats = document.createElement('div');
    stats.className = 'diary-stats';
    
    const views = document.createElement('span');
    views.className = 'diary-views';
    views.innerHTML = `<i class="fas fa-eye"></i> ${diary.views || 0}`;
    
    const rating = document.createElement('span');
    rating.className = 'diary-rating';
    rating.innerHTML = `<i class="fas fa-star"></i> ${diary.averageRating ? diary.averageRating.toFixed(1) : '暂无评分'}`;
    
    stats.appendChild(views);
    stats.appendChild(rating);
    
    // 组装页脚
    footer.appendChild(authorDate);
    footer.appendChild(stats);
    
    // 组装卡片
    diaryLink.appendChild(title);
    diaryLink.appendChild(content);
    diaryLink.appendChild(footer);
    
    // 添加到容器
    container.appendChild(diaryLink);
  });
}

// 根据类型对相关日记进行排序
function sortRelatedDiaries(sortType) {
  const container = document.getElementById('related-diary-container');
  
  // 确保有存储的数据
  if (!window.relatedDiariesData || !Array.isArray(window.relatedDiariesData)) {
    return;
  }
  
  // 创建数据的副本以进行排序
  const diaries = [...window.relatedDiariesData];
  
  switch (sortType) {
    case 'latest':
      // 按日期排序（最新在前）
      diaries.sort((a, b) => {
        const dateA = a.createdAt ? new Date(a.createdAt) : new Date(0);
        const dateB = b.createdAt ? new Date(b.createdAt) : new Date(0);
        return dateB - dateA;
      });
      break;
    case 'rating':
      // 按评分排序（高分在前）
      diaries.sort((a, b) => {
        const ratingA = a.averageRating || 0;
        const ratingB = b.averageRating || 0;
        return ratingB - ratingA;
      });
      break;
    case 'views':
      // 按浏览量排序（多的在前）
      diaries.sort((a, b) => {
        const viewsA = a.views || 0;
        const viewsB = b.views || 0;
        return viewsB - viewsA;
      });
      break;
    default:
      // 默认按最新排序
      diaries.sort((a, b) => {
        const dateA = a.createdAt ? new Date(a.createdAt) : new Date(0);
        const dateB = b.createdAt ? new Date(b.createdAt) : new Date(0);
        return dateB - dateA;
      });
  }
  
  // 使用排序后的数据更新UI
  updateRelatedDiariesUI(diaries);
}

// 注册事件监听器
function registerEventListeners() {
  // 注册导航按钮事件
  const navBtn = document.getElementById('nav-btn');
  navBtn.addEventListener('click', function(e) {
    e.preventDefault();
    const spotName = document.getElementById('spot-name').textContent;
    let mapInstance = window.spotMap;
    
    if (mapInstance && mapInstance.getCenter) {
      const center = mapInstance.getCenter();
      if (center) {
        window.location.href = `../map/navigate.html?destination=${encodeURIComponent(spotName)}&lng=${center.lng}&lat=${center.lat}`;
        return;
      }
    }
    
    // 如果无法获取地图中心点，仅使用景点名称
    window.location.href = `../map/navigate.html?destination=${encodeURIComponent(spotName)}`;
  });
  
  // 排序按钮
  document.getElementById('sort-latest').addEventListener('click', function() {
    sortRelatedDiaries('latest');
    
    // 更新活跃按钮样式
    document.querySelectorAll('.sort-btn').forEach(btn => btn.classList.remove('active'));
    this.classList.add('active');
  });
  
  document.getElementById('sort-rating').addEventListener('click', function() {
    sortRelatedDiaries('rating');
    
    // 更新活跃按钮样式
    document.querySelectorAll('.sort-btn').forEach(btn => btn.classList.remove('active'));
    this.classList.add('active');
  });
  
  document.getElementById('sort-views').addEventListener('click', function() {
    sortRelatedDiaries('views');
    
    // 更新活跃按钮样式
    document.querySelectorAll('.sort-btn').forEach(btn => btn.classList.remove('active'));
    this.classList.add('active');
  });
  
  // AI助手对话框
  const aiModal = document.getElementById('ai-assistant-modal');
  const aiAssistantBtn = document.getElementById('ai-assistant-btn');
  const closeModal = document.querySelector('.close-modal');
  
  aiAssistantBtn.addEventListener('click', function() {
    aiModal.style.display = 'block';
  });
  
  closeModal.addEventListener('click', function() {
    aiModal.style.display = 'none';
  });
  
  window.addEventListener('click', function(event) {
    if (event.target === aiModal) {
      aiModal.style.display = 'none';
    }
  });
  
  // AI消息发送
  const aiSendBtn = document.getElementById('ai-send-btn');
  const aiInput = document.getElementById('ai-input');
  
  aiSendBtn.addEventListener('click', sendAIMessage);
  
  aiInput.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
      sendAIMessage();
    }
  });
}

// 发送AI消息
function sendAIMessage() {
  const aiInput = document.getElementById('ai-input');
  const message = aiInput.value.trim();
  
  if (!message) return;
  
  // 添加用户消息
  const chatContainer = document.getElementById('ai-chat-container');
  const userMessageDiv = document.createElement('div');
  userMessageDiv.className = 'user-message';
  userMessageDiv.innerHTML = `
    <div class="user-avatar"><i class="fas fa-user"></i></div>
    <div class="message-content">
      <p>${message}</p>
    </div>
  `;
  chatContainer.appendChild(userMessageDiv);
  
  // 清空输入框
  aiInput.value = '';
  
  // 滚动到底部
  chatContainer.scrollTop = chatContainer.scrollHeight;
  
  // 生成AI回复
  const spotName = document.getElementById('spot-name').textContent;
  generateAIResponse(message, spotName);
}

// 生成AI回复（简单的模拟）
function generateAIResponse(message, spotName) {
  const chatContainer = document.getElementById('ai-chat-container');
  
  // 显示"正在输入"状态
  const typingDiv = document.createElement('div');
  typingDiv.className = 'ai-message typing';
  typingDiv.innerHTML = `
    <div class="ai-avatar"><i class="fas fa-robot"></i></div>
    <div class="message-content">
      <p>正在思考...</p>
    </div>
  `;
  chatContainer.appendChild(typingDiv);
  
  // 滚动到底部
  chatContainer.scrollTop = chatContainer.scrollHeight;
  
  // 模拟延迟
  setTimeout(() => {
    // 移除"正在输入"状态
    chatContainer.removeChild(typingDiv);
    
    // 基于问题生成响应
    let response = '';
    if (message.includes('推荐') || message.includes('建议')) {
      response = `建议您在参观${spotName}时，提前在网上预约门票，并在早上9点-10点或下午3点后前往，避开人流高峰。您可以在景区附近的餐厅享用当地特色美食，获得更完整的体验。`;
    } else if (message.includes('交通') || message.includes('怎么去')) {
      response = `前往${spotName}的交通方式有：1. 公共交通：可乘坐地铁或公交车；2. 自驾：有停车场，但高峰期可能较为拥挤；3. 出租车/网约车：方便但费用较高。建议使用导航软件规划最佳路线。`;
    } else if (message.includes('门票') || message.includes('价格')) {
      response = `${spotName}的门票信息：成人票约为50-100元（具体价格请查官网），学生、老人、军人等凭有效证件可享受半价优惠。建议通过官方渠道或授权的在线平台购买，可能有折扣。`;
    } else if (message.includes('开放') || message.includes('时间')) {
      response = `${spotName}的开放时间通常为上午8:00至下午5:00（旺季可能延长至晚上8:00）。周一至周日均开放，法定节假日期间可能会延长开放时间。建议出行前通过官方渠道确认具体开放时间。`;
    } else if (message.includes('特色') || message.includes('有什么好')) {
      response = `${spotName}的特色景点包括：主要建筑群、历史文化展览区和特色手工艺品展示区。最受游客喜爱的是其独特的建筑风格和丰富的文化内涵。建议您至少安排2-3小时游览，以充分体验其魅力。`;
    } else {
      response = `关于${spotName}的更多详细信息，建议您访问官方网站或咨询景区客服。您还可以下载相关旅游APP获取更多实时信息和游客评价。祝您旅途愉快！`;
    }
    
    // 添加AI回复
    const aiMessageDiv = document.createElement('div');
    aiMessageDiv.className = 'ai-message';
    aiMessageDiv.innerHTML = `
      <div class="ai-avatar"><i class="fas fa-robot"></i></div>
      <div class="message-content">
        <p>${response}</p>
      </div>
    `;
    chatContainer.appendChild(aiMessageDiv);
    
    // 滚动到底部
    chatContainer.scrollTop = chatContainer.scrollHeight;
  }, 1500);
}

// 格式化日期
function formatDate(dateString) {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
} 