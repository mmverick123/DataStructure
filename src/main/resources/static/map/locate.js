    let map;
    let marker;
    let searchService;
    let currentLocation = null;
    
    // 初始化地图
    function initMap() {
      // 默认显示北京市中心
      const defaultCenter = new TMap.LatLng(39.90469, 116.40717);
      
      map = new TMap.Map(document.getElementById('map'), {
        center: defaultCenter,
        zoom: 11
      });
      
      // 初始化标记点
      marker = new TMap.MultiMarker({
        map: map,
        geometries: []
      });
      
      // 初始化搜索服务
      searchService = new TMap.service.Search({
        pageSize: 10, // 每页结果数
        pageIndex: 1 // 页码
      });
      
      // 地图点击事件
      map.on('click', function(evt) {
        currentLocation = evt.latLng;
        document.getElementById('current-coords').textContent = 
          `${currentLocation.lat.toFixed(6)}, ${currentLocation.lng.toFixed(6)}`;
      });
    }
    
    // 搜索功能
    document.getElementById('search-btn').addEventListener('click', searchPlaces);
    document.getElementById('search-input').addEventListener('keypress', function(e) {
      if (e.key === 'Enter') searchPlaces();
    });
    
    // 替换原来的searchPlaces函数
function searchPlaces() {
  const keyword = document.getElementById('search-input').value.trim();
  if (!keyword) {
    alert('请输入搜索关键词');
    return;
  }
  
  // 使用JSONP方式调用腾讯地图WebService API
  const callbackName = 'jsonpCallback_' + Date.now();
  const url = `https://apis.map.qq.com/ws/place/v1/search?keyword=${encodeURIComponent(keyword)}&boundary=region(北京,0)&page_size=10&page_index=1&key=DEABZ-7BGKB-N7FUG-NGGLJ-BICGV-IIBQG&output=jsonp&callback=${callbackName}`;
  
  // 创建JSONP回调
  window[callbackName] = function(response) {
    delete window[callbackName];
    document.body.removeChild(script);
    
    if (response.status !== 0) {
      console.error('搜索失败:', response.message);
      alert('搜索失败: ' + response.message);
      return;
    }
    
    displayResults(response.data);
  };
  
  // 创建script标签发起JSONP请求
  const script = document.createElement('script');
  script.src = url;
  document.body.appendChild(script);
}
    
    // 显示搜索结果
    function displayResults(places) {
      const resultsList = document.getElementById('results-list');
      
      if (!places || places.length === 0) {
        resultsList.innerHTML = '<div class="no-results">未找到匹配结果</div>';
        return;
      }
      
      let html = '';
      places.forEach((place, index) => {
        html += `
          <div class="result-item" 
               data-lat="${place.location.lat}" 
               data-lng="${place.location.lng}"
               data-id="${place.id}">
            <h3>${place.title || '未命名地点'}</h3>
            <p>${place.category || '未知类别'}</p>
            <p class="address">${place.address || '地址不详'}</p>
          </div>
        `;
      });
      
      resultsList.innerHTML = html;
      
      // 绑定点击事件
      document.querySelectorAll('.result-item').forEach(item => {
        // 单击 - 在地图上显示
        item.addEventListener('click', function() {
          const lat = parseFloat(this.getAttribute('data-lat'));
          const lng = parseFloat(this.getAttribute('data-lng'));
          
          if (!isNaN(lat) && !isNaN(lng)) {
            const location = new TMap.LatLng(lat, lng);
            moveToLocation(location);
            
            // 更新标记
            marker.setGeometries([{
              position: location,
              id: 'marker',
              styleId: 'default'
            }]);
            
            // 更新当前坐标显示
            document.getElementById('current-coords').textContent = 
              `${lat.toFixed(6)}, ${lng.toFixed(6)}`;
          }
        });
        
        // 双击 - 跳转至 upload.html 并带上位置名称
        item.addEventListener('dblclick', function () {
          const placeName = this.querySelector('h3').innerText;
          const lat = this.getAttribute('data-lat');
          const lng = this.getAttribute('data-lng');

        // 跳转到上传页面并携带位置信息
        const url = `../traveldiary/upload.html?location=${encodeURIComponent(placeName)}&lat=${lat}&lng=${lng}`;
        window.location.href = url;});
      });
    }
    
    // 移动到指定位置
    function moveToLocation(location) {
      map.setCenter(location);
      map.setZoom(16); // 放大到更近的级别
    }
    
    // 页面加载完成后初始化地图
    window.onload = initMap;

    document.addEventListener('DOMContentLoaded', function(){
        const usernameSpan = document.getElementById('username');
        const logoutBtn = document.getElementById('logout-btn');
        function getCurrentUser() {
        const userJson = localStorage.getItem('user');
        if (!userJson) {
            // 如果没有登录，跳转到登录页
            window.location.href = '../authen/authen.html';
            return null;
        }

        try {
        return JSON.parse(userJson);
        }catch (e) {
            console.error('解析用户信息失败', e);
            localStorage.removeItem('user');
            window.location.href = '../authen/authen.html';
            return null;
        }
    }

    const currentUser = getCurrentUser();

    if (currentUser) {
        usernameSpan.textContent = currentUser.username;
    } else {
        // 如果未登录或解析失败，getCurrentUser 已处理跳转
        throw new Error("用户未登录");
    }
    const token = localStorage.getItem('token');
    if (!token) {
     window.location.href = '../authen/authen.html';
    }

    // 退出登录功能
    logoutBtn.addEventListener('click', function() {
        // 实际应用中应清除登录状态并重定向
        console.log('用户退出登录');
        window.location.href = '../authen/authen.html';
    });
    })