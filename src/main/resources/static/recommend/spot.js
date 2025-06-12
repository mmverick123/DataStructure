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
  
  // 保存地图实例和位置信息到全局变量以便导航和周边设施查询使用
  window.spotMap = map;
  window.spotLocation = {
    longitude: lng,
    latitude: lat,
    name: spotName,
    address: address
  };
  
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
    container.innerHTML = '<div class="no-diaries-message">暂无相关日记</div>';
    return;
  }
  
  // 为每条日记创建UI元素
  diaries.forEach(diary => {
    const diaryCard = document.createElement('div');
    diaryCard.className = 'diary-card';
    
    const diaryLink = document.createElement('a');
    diaryLink.href = `../traveldiary/diary.html?id=${diary.id}`;
    diaryLink.className = 'diary-card-link';
    
    // 内容区域
    const content = document.createElement('div');
    content.className = 'diary-content';
    
    // 标题
    const title = document.createElement('h3');
    title.className = 'diary-title';
    title.textContent = diary.title;
    
    // 内容摘要
    const excerpt = document.createElement('p');
    excerpt.className = 'diary-excerpt';
    excerpt.textContent = diary.content && diary.content.length > 150 ? 
      `${diary.content.substring(0, 150)}...` : (diary.content || '无内容');
    
    // 添加到内容区域
    content.appendChild(title);
    content.appendChild(excerpt);
    
    // 页脚信息
    const footer = document.createElement('div');
    footer.className = 'diary-footer';
    
    // 作者信息
    const authorDiv = document.createElement('div');
    authorDiv.className = 'diary-author';
    
    const authorAvatar = document.createElement('div');
    authorAvatar.className = 'diary-author-avatar';
    
    // 使用用户名首字母作为头像
    const username = diary.user ? diary.user.username : '游客';
    authorAvatar.textContent = username.charAt(0).toUpperCase();
    
    const authorName = document.createElement('span');
    authorName.textContent = username;
    
    authorDiv.appendChild(authorAvatar);
    authorDiv.appendChild(authorName);
    
    // 统计信息
    const stats = document.createElement('div');
    stats.className = 'diary-stats';
    
    const views = document.createElement('div');
    views.className = 'diary-stat';
    views.innerHTML = `<i class="fas fa-eye"></i> ${diary.views || 0}`;
    
    const rating = document.createElement('div');
    rating.className = 'diary-stat';
    rating.innerHTML = `<i class="fas fa-star"></i> ${diary.averageRating ? diary.averageRating.toFixed(1) : '0.0'}`;
    
    stats.appendChild(views);
    stats.appendChild(rating);
    
    // 组装页脚
    footer.appendChild(authorDiv);
    footer.appendChild(stats);
    
    // 组装整个卡片
    diaryLink.appendChild(content);
    diaryLink.appendChild(footer);
    diaryCard.appendChild(diaryLink);
    
    // 添加到容器
    container.appendChild(diaryCard);
  });
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
  
  // 周边设施查询按钮
  const facilityTypeButtons = document.querySelectorAll('.facility-type-btn');
  facilityTypeButtons.forEach(button => {
    button.addEventListener('click', function() {
      // 切换按钮选中状态
      button.classList.toggle('active');
      
      // 获取所有选中的设施类型
      const selectedFacilityTypes = [];
      document.querySelectorAll('.facility-type-btn.active').forEach(btn => {
        selectedFacilityTypes.push(btn.dataset.type);
      });
      
      // 如果有选中的设施类型，进行查询
      if (selectedFacilityTypes.length > 0) {
        searchNearbyFacilities(selectedFacilityTypes.join('|'));
      } else {
        // 清空设施列表
        document.getElementById('nearby-facilities-list').innerHTML = '<div class="no-facilities-message">请选择设施类型进行查询</div>';
      }
    });
  });
  
  // AI助手弹窗
  const aiAssistantBtn = document.getElementById('ai-assistant-btn');
  const aiModal = document.getElementById('ai-assistant-modal');
  const closeModal = document.querySelector('.close-modal');
  
  aiAssistantBtn.addEventListener('click', function() {
    aiModal.style.display = 'flex';
  });
  
  closeModal.addEventListener('click', function() {
    aiModal.style.display = 'none';
  });
  
  window.addEventListener('click', function(event) {
    if (event.target == aiModal) {
      aiModal.style.display = 'none';
    }
  });
  
  // 发送消息按钮
  const sendButton = document.getElementById('ai-send-btn');
  sendButton.addEventListener('click', sendAIMessage);
  
  // 输入框回车发送
  const inputField = document.getElementById('ai-input');
  inputField.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
      sendAIMessage();
    }
  });
  
  // 清除聊天记录按钮
  const clearChatBtn = document.getElementById('clear-chat-btn');
  clearChatBtn.addEventListener('click', clearAIChat);
}

// 发送AI消息
async function sendAIMessage() {
  const aiInput = document.getElementById('ai-input');
  const message = aiInput.value.trim();
  
  if (!message) return;
  
  // 禁用输入框和发送按钮，防止用户连续发送
  const aiSendBtn = document.getElementById('ai-send-btn');
  aiInput.disabled = true;
  aiSendBtn.disabled = true;
  
  try {
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
    await generateAIResponse(message, spotName);
  } catch (error) {
    console.error('处理消息时出错:', error);
    const chatContainer = document.getElementById('ai-chat-container');
    const errorMessageDiv = document.createElement('div');
    errorMessageDiv.className = 'ai-message error';
    errorMessageDiv.innerHTML = `
      <div class="ai-avatar"><i class="fas fa-robot"></i></div>
      <div class="message-content">
        <p>抱歉，处理您的消息时出现了错误。请稍后再试。</p>
      </div>
    `;
    chatContainer.appendChild(errorMessageDiv);
    chatContainer.scrollTop = chatContainer.scrollHeight;
  } finally {
    // 重新启用输入框和发送按钮
    aiInput.disabled = false;
    aiSendBtn.disabled = false;
    aiInput.focus();
  }
}

// 清除AI聊天记录
function clearAIChat() {
  const chatContainer = document.getElementById('ai-chat-container');
  // 保留第一条欢迎消息
  const welcomeMessage = chatContainer.firstChild;
  chatContainer.innerHTML = '';
  if (welcomeMessage) {
    chatContainer.appendChild(welcomeMessage);
  } else {
    // 如果没有欢迎消息，创建一个新的
    const welcomeMessageDiv = document.createElement('div');
    welcomeMessageDiv.className = 'ai-message';
    welcomeMessageDiv.innerHTML = `
      <div class="ai-avatar"><i class="fas fa-robot"></i></div>
      <div class="message-content">
        <p>你好！我是你的AI旅行助手。我可以帮你了解更多关于这个景点的信息，规划行程，或推荐附近美食。请问有什么可以帮到你的吗？</p>
      </div>
    `;
    chatContainer.appendChild(welcomeMessageDiv);
  }
}

// 生成AI回复
async function generateAIResponse(message, spotName) {
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
  
  try {
    // 调用DeepSeek API获取回复
    const response = await callAIApi(message, spotName);
    
    // 移除"正在输入"状态
    chatContainer.removeChild(typingDiv);
    
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
  } catch (error) {
    // 移除"正在输入"状态
    chatContainer.removeChild(typingDiv);
    
    // 显示错误信息
    const errorMessageDiv = document.createElement('div');
    errorMessageDiv.className = 'ai-message error';
    errorMessageDiv.innerHTML = `
      <div class="ai-avatar"><i class="fas fa-robot"></i></div>
      <div class="message-content">
        <p>抱歉，我暂时无法回答您的问题。请稍后再试。</p>
      </div>
    `;
    chatContainer.appendChild(errorMessageDiv);
    console.error('AI API调用失败:', error);
  }
  
  // 滚动到底部
  chatContainer.scrollTop = chatContainer.scrollHeight;
}

// 调用DeepSeek API
async function callAIApi(message, spotName) {
  // DeepSeek API 配置
  const API_KEY = "sk-43b3270b0c5843f9b6f937fa6464eb7b";
  const API_URL = "https://api.deepseek.com/v1/chat/completions";
  
  // 使用CORS代理解决跨域问题
  // 使用多个代理以便在某个代理不可用时可以尝试其他代理
  const CORS_PROXIES = [
    "https://corsproxy.io/?",
    "https://api.allorigins.win/raw?url=",
    "https://proxy.cors.sh/",
    "https://corsanywhere.herokuapp.com/"
  ];
  
  // 默认使用第一个代理
  const CORS_PROXY = CORS_PROXIES[0];
  
  try {
    console.log('正在调用DeepSeek API...');
    
    // 构建API URL，使用CORS代理
    const requestUrl = `${CORS_PROXY}${encodeURIComponent(API_URL)}`;
    
    // 构建请求体
    const requestBody = {
      model: "deepseek-chat",
      messages: [
        {
          role: "system",
          content: `你是一个旅游助手，专门回答关于景点"${spotName}"的问题。提供简洁、实用的回答，字数控制在200字以内。使用中文回复。`
        },
        {
          role: "user",
          content: message
        }
      ],
      temperature: 0.7,
      max_tokens: 800
    };
    
    console.log('发送请求，URL:', requestUrl);
    console.log('请求内容:', requestBody);
    
    // 设置请求超时
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 10000); // 10秒超时
    
    try {
      // 发送请求到DeepSeek API
      const response = await fetch(requestUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${API_KEY}`,
          'Accept': 'application/json',
          'Origin': window.location.origin
        },
        body: JSON.stringify(requestBody),
        signal: controller.signal
      }).finally(() => clearTimeout(timeoutId));
      
      // 检查HTTP状态码
      if (!response.ok) {
        console.error('DeepSeek API调用失败:', response.status, response.statusText);
        
        // 尝试读取错误详情
        try {
          const errorText = await response.text();
          console.error('错误详情:', errorText);
        } catch (e) {}
        
        throw new Error(`API调用失败，状态码: ${response.status}`);
      }
      
      // 解析响应数据
      const data = await response.json();
      console.log('DeepSeek API响应:', data);
      
      // 从响应中提取AI的回复
      if (data && data.choices && data.choices.length > 0 && data.choices[0].message) {
        return data.choices[0].message.content.trim();
      } else {
        throw new Error('API返回的数据格式不正确');
      }
    } catch (apiError) {
      // 如果超时或网络错误，尝试使用备用模拟响应
      if (apiError.name === 'AbortError') {
        console.warn('API调用超时');
      } else if (apiError.message.includes('Failed to fetch') || apiError.message.includes('NetworkError')) {
        console.warn('网络错误，可能是CORS问题');
      }
      
      console.warn('使用备用响应:', apiError);
      return generateMockResponse(message, spotName);
    }
  } catch (error) {
    console.error('处理AI响应时出错:', error);
    return '抱歉，我暂时无法回答您的问题。请稍后再试。';
  }
}

// 生成模拟响应
function generateMockResponse(message, spotName) {
  console.log('使用模拟响应，景点:', spotName, '问题:', message);
  
  // 基于问题生成响应
  let response = '';
  
  // 提取关键词以更智能地匹配问题
  const keywords = {
    交通: ['交通', '怎么去', '到达', '路线', '车', '地铁', '公交', '自驾', '打车'],
    门票: ['门票', '价格', '费用', '多少钱', '优惠', '便宜', '贵'],
    时间: ['时间', '开放', '关门', '几点', '工作日', '周末', '假期', '营业'],
    特色: ['特色', '有什么好', '值得', '看点', '特点', '著名', '有名', '特别'],
    推荐: ['推荐', '建议', '攻略', '游览', '路线', '行程', '安排', '计划'],
    住宿: ['住宿', '酒店', '宾馆', '民宿', '住宿', '过夜', '附近'],
    美食: ['美食', '吃', '餐厅', '小吃', '特色', '美味', '特产', '当地'],
    购物: ['购物', '买', '纪念品', '特产', '商店', '商业'],
    历史: ['历史', '文化', '故事', '来历', '背景', '传说'],
    季节: ['季节', '什么时候去', '最佳', '旺季', '淡季', '春', '夏', '秋', '冬']
  };
  
  // 检查消息中是否包含各类关键词
  let matchedCategory = null;
  let highestMatchCount = 0;
  
  for (const [category, categoryKeywords] of Object.entries(keywords)) {
    const matchCount = categoryKeywords.filter(keyword => message.includes(keyword)).length;
    if (matchCount > highestMatchCount) {
      highestMatchCount = matchCount;
      matchedCategory = category;
    }
  }
  
  // 根据匹配的类别生成回复
  switch (matchedCategory) {
    case '交通':
      response = `前往${spotName}的交通方式有：1. 公共交通：可乘坐地铁或公交车到达附近站点，然后步行5-10分钟；2. 自驾：导航到景点地址，周边有收费停车场；3. 出租车/网约车：方便但费用较高。建议提前规划路线，高峰期可能会有交通拥堵。`;
      break;
    case '门票':
      response = `${spotName}的门票信息：成人票约为50-100元（旺季可能上浮），学生、老人、军人等持有效证件可享受半价优惠。部分节假日可能有特殊定价。建议通过官方网站或授权的在线平台提前购票，可能有折扣优惠。`;
      break;
    case '时间':
      response = `${spotName}的开放时间通常为上午8:00至下午5:00（旺季可能延长至晚上8:00）。周一至周日均开放，法定节假日不休息。特殊情况下可能会临时调整开放时间，建议出行前通过官方渠道确认最新开放信息。`;
      break;
    case '特色':
      response = `${spotName}的特色景点包括：主要建筑群、历史文化展览区和特色手工艺品展示区。最受游客喜爱的是其独特的建筑风格和丰富的文化内涵。还有多处适合拍照的绝佳位置。建议您至少安排2-3小时游览，以充分体验其魅力。`;
      break;
    case '推荐':
      response = `游览${spotName}的建议：1.提前在网上预约门票；2.选择工作日或非节假日，避开人流高峰；3.早上9点-10点或下午3点后前往，游客较少；4.主要景点请勿错过（中心区域、历史展览区）；5.可以请讲解员或使用语音导览深入了解景点历史；6.附近有特色餐馆，可品尝当地美食。`;
      break;
    case '住宿':
      response = `${spotName}周边有多种住宿选择：1.高档酒店（距离景区0.5-1公里，价格约400-800元/晚）；2.经济型酒店（距离1-2公里，价格约200-400元/晚）；3.特色民宿（有当地风情，价格约300-500元/晚）。建议提前1-2周预订，旺季可能需要更早。`;
      break;
    case '美食':
      response = `${spotName}附近的特色美食包括：本地特色小吃（如小吃一条街）、传统风味餐厅（推荐XX楼）以及现代餐饮。景区内也有餐饮区，但价格偏高。建议在游览结束后到景区外500米处的美食街享用当地特色美食，性价比更高。`;
      break;
    case '购物':
      response = `${spotName}及其周边有多处购物点：1.景区内纪念品商店（价格偏高但种类齐全）；2.景区出口处的小商品市场（可适当砍价）；3.附近的商业街。推荐购买当地特色手工艺品、传统工艺品或特产，作为纪念或送给亲友都很不错。`;
      break;
    case '历史':
      response = `${spotName}有着悠久的历史，始建于XX年代，经历了多次扩建和修缮。它曾是XX时期的重要场所，见证了许多历史事件。这里的建筑融合了传统与现代元素，体现了深厚的文化底蕴。景区内的历史展览区详细介绍了其发展历程，值得仔细参观。`;
      break;
    case '季节':
      response = `参观${spotName}的最佳季节是春季（3-5月）和秋季（9-11月），气温适宜，景色优美。夏季（6-8月）游客较多，可能较为拥挤且气温较高。冬季（12-2月）游客相对较少，但部分室外景点可能受天气影响。特别推荐在XX节期间前往，有特别活动。`;
      break;
    default:
      response = `关于${spotName}的更多详细信息，我可以为您提供游览建议、交通指南、门票信息、开放时间、周边美食及住宿推荐等。您还可以通过景区官方网站或咨询景区客服获取最新信息。如果您有特定问题，请随时向我询问，我会尽力提供帮助。祝您旅途愉快！`;
  }
  
  return response;
}

// 格式化日期
function formatDate(dateString) {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

// 处理API错误
function handleApiError(error, errorType) {
  // 记录详细错误信息
  console.error(`${errorType} 错误:`, error);
  
  // 尝试提取更多错误详情
  let errorMessage = error.message || '未知错误';
  if (error.response) {
    try {
      errorMessage += ` (状态码: ${error.response.status})`;
    } catch (e) {
      // 忽略提取状态码时的错误
    }
  }
  
  // 对不同类型的错误进行分类处理
  switch (errorType) {
    case 'NETWORK':
      console.error('网络错误，可能是CORS或连接问题');
      break;
    case 'API':
      console.error('API错误，可能是参数或认证问题');
      break;
    case 'PARSING':
      console.error('解析错误，API返回的数据格式可能不符合预期');
      break;
    default:
      console.error('其他错误');
  }
  
  // 记录到控制台以便调试
  console.debug('错误详情:', {
    type: errorType,
    message: errorMessage,
    originalError: error
  });
  
  return errorMessage;
}

// 搜索周边设施
function searchNearbyFacilities(facilityType) {
  console.log('开始搜索周边设施，类型:', facilityType);
  
  // 检查是否有位置信息
  if (!window.spotLocation || !window.spotLocation.longitude || !window.spotLocation.latitude) {
    console.error('缺少位置信息，无法搜索周边设施');
    document.getElementById('nearby-facilities-list').innerHTML = '<div class="error-message">无法获取当前位置信息，请刷新页面重试</div>';
    return;
  }
  
  // 显示加载状态
  document.getElementById('nearby-facilities-list').innerHTML = '<div class="loading-message"><i class="fas fa-spinner fa-spin"></i> 正在搜索周边设施...</div>';
  
  // 构建请求参数
  const requestData = {
    longitude: window.spotLocation.longitude,
    latitude: window.spotLocation.latitude,
    facilityType: facilityType,
    limit: 20,
    radius: 2000
  };
  
  console.log('发送周边设施查询请求:', requestData);
  
  // 调用API
  fetch('/api/facilities/nearby/search', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(requestData)
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`服务器响应错误: ${response.status}`);
    }
    return response.json();
  })
  .then(data => {
    console.log('周边设施查询结果:', data);
    updateFacilitiesUI(data);
  })
  .catch(error => {
    console.error('搜索周边设施失败:', error);
    document.getElementById('nearby-facilities-list').innerHTML = `<div class="error-message">搜索周边设施失败: ${error.message}</div>`;
  });
}

// 更新周边设施UI
function updateFacilitiesUI(facilitiesData) {
  const facilitiesList = document.getElementById('nearby-facilities-list');
  facilitiesList.innerHTML = '';
  
  // 检查是否有结果
  if (!facilitiesData || !facilitiesData.facilities || facilitiesData.facilities.length === 0) {
    facilitiesList.innerHTML = '<div class="no-facilities-message">未找到周边设施</div>';
    return;
  }
  
  // 创建设施列表
  const facilities = facilitiesData.facilities;
  facilities.forEach(facility => {
    const facilityItem = document.createElement('div');
    facilityItem.className = 'facility-item';
    facilityItem.setAttribute('data-lng', facility.longitude);
    facilityItem.setAttribute('data-lat', facility.latitude);
    
    // 设施图标
    let iconClass = 'fas fa-store';
    if (facility.type && facility.type.includes('卫生间') || facility.type.includes('厕所') || facility.type.includes('洗手间')) {
      iconClass = 'fas fa-toilet';
    } else if (facility.type && facility.type.includes('便利店')) {
      iconClass = 'fas fa-shopping-basket';
    } else if (facility.type && facility.type.includes('超市')) {
      iconClass = 'fas fa-shopping-cart';
    }
    
    // 设施内容
    facilityItem.innerHTML = `
      <div class="facility-icon"><i class="${iconClass}"></i></div>
      <div class="facility-info">
        <h4 class="facility-name">${facility.name}</h4>
        <p class="facility-address">${facility.address || '暂无地址信息'}</p>
        <div class="facility-meta">
          <span class="facility-distance">${facility.distance ? (facility.distance < 1000 ? Math.round(facility.distance) + '米' : (facility.distance / 1000).toFixed(1) + '公里') : '未知距离'}</span>
          ${facility.telephone ? `<span class="facility-tel"><i class="fas fa-phone"></i> ${facility.telephone}</span>` : ''}
        </div>
      </div>
    `;
    
    // 添加单击事件：在地图上显示
    facilityItem.addEventListener('click', function() {
      const lng = parseFloat(this.getAttribute('data-lng'));
      const lat = parseFloat(this.getAttribute('data-lat'));
      
      if (!isNaN(lng) && !isNaN(lat) && window.spotMap) {
        // 移动地图中心点到设施位置
        window.spotMap.setCenter([lng, lat]);
        window.spotMap.setZoom(17); // 放大视图
        
        // 创建临时标记
        const marker = new AMap.Marker({
          position: [lng, lat],
          title: facility.name,
          map: window.spotMap,
          animation: 'AMAP_ANIMATION_DROP' // 添加动画效果
        });
        
        // 创建信息窗体
        const infoWindow = new AMap.InfoWindow({
          isCustom: true,
          content: `
            <div class="map-info-window facility-info-window">
              <h3>${facility.name}</h3>
              <p>${facility.address || '暂无地址信息'}</p>
              ${facility.telephone ? `<p><i class="fas fa-phone"></i> ${facility.telephone}</p>` : ''}
              <div class="info-actions">
                <a href="../map/navigate.html?destination=${encodeURIComponent(facility.name)}&lng=${lng}&lat=${lat}" class="info-btn">
                  <i class="fas fa-directions"></i> 导航前往
                </a>
              </div>
            </div>
          `,
          offset: new AMap.Pixel(0, -30)
        });
        
        // 打开信息窗口
        infoWindow.open(window.spotMap, marker.getPosition());
        
        // 将当前标记存储在全局变量中，以便在显示其他标记时可以清除
        if (window.currentFacilityMarker) {
          window.currentFacilityMarker.setMap(null); // 移除前一个标记
        }
        window.currentFacilityMarker = marker;
        
        if (window.currentFacilityInfoWindow) {
          window.currentFacilityInfoWindow.close(); // 关闭前一个信息窗口
        }
        window.currentFacilityInfoWindow = infoWindow;
      }
    });
    
    // 添加双击事件：跳转到导航页面
    facilityItem.addEventListener('dblclick', function() {
      const lng = parseFloat(this.getAttribute('data-lng'));
      const lat = parseFloat(this.getAttribute('data-lat'));
      
      if (!isNaN(lng) && !isNaN(lat)) {
        window.location.href = `../map/navigate.html?destination=${encodeURIComponent(facility.name)}&lng=${lng}&lat=${lat}`;
      }
    });
    
    // 添加到列表
    facilitiesList.appendChild(facilityItem);
  });
  
  // 添加提示信息
  const tipElement = document.createElement('div');
  tipElement.className = 'facilities-tip';
  tipElement.innerHTML = `
    <p><i class="fas fa-info-circle"></i> 提示：单击设施可在地图中查看位置，双击可直接跳转到导航页面</p>
  `;
  facilitiesList.appendChild(tipElement);
} 