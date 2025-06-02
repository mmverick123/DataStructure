// 全局变量
let map;
let markers = [];
let polylines = [];
let startMarker, endMarker;
let waypoints = [];
let currentInputId = null;
let tempPreviewMarker = null;

// 初始化地图
function initMap() {
  map = new AMap.Map('map', {
    zoom: 11,
    center: [116.397428, 39.90923]
  });

  AMap.plugin(['AMap.ToolBar', 'AMap.Scale'], function () {
    map.addControl(new AMap.ToolBar());
    map.addControl(new AMap.Scale());
  });
}

// 清除地图上所有标记和路线
function clearMap() {
  // 清除路线
  polylines.forEach(polyline => map.remove(polyline));
  polylines = [];

  // 清除标记
  if (startMarker) {
    map.remove(startMarker);
    startMarker = null;
  }
  if (endMarker) {
    map.remove(endMarker);
    endMarker = null;
  }

  waypoints.forEach(wp => {
    if (wp.marker) map.remove(wp.marker);
  });
  waypoints = [];
}

// 显示路线信息
function displayRouteInfo(route) {
  let html = `
    <div class="route-step">
      <strong>总距离:</strong> ${route.distance}米<br>
      <strong>预计时间:</strong> ${route.duration}秒
    </div>
    <h4>路径详情:</h4>
  `;

  if (Array.isArray(route.pathNames)) {
    route.pathNames.forEach((name, index) => {
      html += `<div class="route-step">${index + 1}. 沿 ${name} 行进</div>`;
    });
  }

  document.getElementById('route-details').innerHTML = html;
}

// 规划路线函数
async function planRoute() {
  const mode = document.getElementById('routeMode').value;

  if (mode === 'loop') {
    // 环游模式
    if (!window.startPosition || waypoints.length < 1) {
      alert('请设置起始点和至少一个景点');
      return;
    }

    try {
      const response = await fetch('/api/routes/within-scenic', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          startLocation: document.getElementById('start-point').value.trim(),
          waypoints: waypoints.map(w => w.name)
        })
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText);
      }

      const contentType = response.headers.get("content-type");
      let data;
      if (contentType && contentType.indexOf("application/json") !== -1) {
        data = await response.json();
      } else {
        const text = await response.text();
        throw new Error(text);
      }

      displayRouteInfo(data);

      if (data.pathPoints && data.pathPoints.length > 0) {
        const path = data.pathPoints.map(point => [point.longitude, point.latitude]);
        const polyline = new AMap.Polyline({
          path: path,
          strokeColor: "#FF33CC",
          strokeWeight: 5,
          strokeStyle: "solid",
          lineJoin: "round",
          map: map
        });
        polylines.push(polyline);
      }

      map.setFitView();
    } catch (error) {
      console.error('路径规划失败:', error);
      alert('路径规划失败: ' + error.message);
    }
  } else {
    // 直达模式
    if (!window.startPosition || !window.endPosition) {
      alert('请先设置起点和终点');
      return;
    }

    try {
      const startName = document.getElementById('start-point').value.trim();
      const endName = document.getElementById('end-point').value.trim();

      const response = await fetch('/api/routes/between-scenic', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ startLocation: startName, endLocation: endName })
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText);
      }

      const contentType = response.headers.get("content-type");
      let data;
      if (contentType && contentType.indexOf("application/json") !== -1) {
        data = await response.json();
      } else {
        const text = await response.text();
        throw new Error(text);
      }

      displayRouteInfo(data);

      if (data.pathPoints && data.pathPoints.length > 0) {
        const path = data.pathPoints.map(point => [point.longitude, point.latitude]);
        const polyline = new AMap.Polyline({
          path: path,
          strokeColor: "#3366FF",
          strokeWeight: 5,
          strokeStyle: "solid",
          lineJoin: "round",
          map: map
        });
        polylines.push(polyline);
      }

      map.setFitView();
    } catch (error) {
      console.error('路径规划失败:', error);
      alert('路径规划失败: ' + error.message);
    }
  }
}

// 绑定搜索事件（带弹窗）
function bindSearchEvent(inputId) {
  const input = document.getElementById(inputId);
  const searchBtn = input.closest('.search-box')?.querySelector('button');

  if (searchBtn) {
    searchBtn.addEventListener('click', function () {
      const keyword = input.value.trim();
      if (!keyword) {
        alert('请输入搜索关键词');
        return;
      }

      currentInputId = inputId;

      const key = 'eaf5c2fa731dcd9b3ab32020e248e1aa'; // 替换为你的高德 Key

      fetch(`https://restapi.amap.com/v3/place/text?keywords=${encodeURIComponent(keyword)}&key=${key}&city=全国`)
        .then(response => response.json())
        .then(data => {
          if (data.status === '1' && data.pois && data.pois.length > 0) {
            displaySearchResults(data.pois);
          } else {
            document.getElementById('search-results-list').innerHTML = `<p>${data.info || '未找到匹配结果'}</p>`;
          }
          document.getElementById('search-results-popup').style.display = 'flex';
        })
        .catch(error => {
          document.getElementById('search-results-list').innerHTML = `<p>请求失败: ${error.message}</p>`;
          document.getElementById('search-results-popup').style.display = 'flex';
        });
    });
  }
}

// 显示搜索结果弹窗
function displaySearchResults(results) {
  const container = document.getElementById('search-results-list');
  container.innerHTML = '';

  results.forEach(poi => {
    const item = document.createElement('div');
    item.className = 'result-item';
    item.innerHTML = `
      <strong>${poi.name}</strong><br>
      <small>${poi.address}</small>
    `;

    // 单击事件 - 在地图上显示位置
    item.addEventListener('click', function () {
      const position = [parseFloat(poi.location.split(',')[0]), parseFloat(poi.location.split(',')[1])];

      // 移除之前的临时标记
      if (tempPreviewMarker) {
        map.remove(tempPreviewMarker);
        tempPreviewMarker = null;
      }

      // 添加临时预览标记
      tempPreviewMarker = new AMap.Marker({
        position: position,
        map: map,
        content: `<div style="background-color:#2196F3;color:white;padding:2px 5px;border-radius:3px;">
                  ${currentInputId === 'start-point' ? '起点预览' : currentInputId.startsWith('waypoint') ? '途经点预览' : '终点预览'}</div>`
      });

      map.setZoomAndCenter(15, position);
    });

    // 双击事件 - 选定地点
    item.addEventListener('dblclick', function () {
      const input = document.getElementById(currentInputId);
      if (input) {
        input.value = poi.name;

        const position = [parseFloat(poi.location.split(',')[0]), parseFloat(poi.location.split(',')[1])];

        // 根据输入框类型处理
        if (currentInputId === 'start-point') {
          // 处理起点
          if (startMarker) map.remove(startMarker);
          startMarker = new AMap.Marker({
            position: position,
            map: map,
            content: '<div style="background-color:#4CAF50;color:white;padding:2px 5px;border-radius:3px;">起点</div>'
          });
          window.startPosition = position;
        } else if (currentInputId === 'end-point') {
          // 处理终点
          if (endMarker) map.remove(endMarker);
          endMarker = new AMap.Marker({
            position: position,
            map: map,
            content: '<div style="background-color:#F44336;color:white;padding:2px 5px;border-radius:3px;">终点</div>'
          });
          window.endPosition = position;
        } else if (currentInputId.startsWith('waypoint')) {
          // 处理中间点
          const waypointId = currentInputId.split('-')[1];
          const existingWaypoint = waypoints.find(w => w.id === waypointId);

          if (existingWaypoint && existingWaypoint.marker) {
            map.remove(existingWaypoint.marker);
          }

          const marker = new AMap.Marker({
            position: position,
            map: map,
            content: `<div style="background-color:#FF9800;color:white;padding:2px 5px;border-radius:3px;">途经点</div>`
          });

          // 更新或添加中间点信息
          waypoints = waypoints.filter(w => w.id !== waypointId);
          waypoints.push({
            id: waypointId,
            name: poi.name,
            position: position,
            marker: marker
          });
        }

        // 移除临时标记并关闭弹窗
        if (tempPreviewMarker) {
          map.remove(tempPreviewMarker);
          tempPreviewMarker = null;
        }
        document.getElementById('search-results-popup').style.display = 'none';
      }
    });

    container.appendChild(item);
  });
}

// 添加中间点
function addWaypoint() {
  const waypointId = 'waypoint-' + waypoints.length;
  const waypointHtml = `
    <div class="input-group" id="${waypointId}-container">
      <label>中间点 ${waypoints.length + 1}</label>
      <div class="search-box">
        <input type="text" id="${waypointId}" placeholder="输入中间点位置">
        <button class="search-waypoint-btn">搜索</button>
        <button class="remove-waypoint-btn" data-id="${waypointId}">×</button>
      </div>
    </div>
  `;

  document.getElementById('waypoints-container').insertAdjacentHTML('beforeend', waypointHtml);

  // 绑定搜索事件
  document.querySelector(`#${waypointId}-container .search-waypoint-btn`).addEventListener('click', function () {
    currentInputId = waypointId;
    const input = document.getElementById(waypointId);
    const keyword = input.value.trim();

    if (!keyword) {
      alert('请输入搜索关键词');
      return;
    }

    const key = 'eaf5c2fa731dcd9b3ab32020e248e1aa'; // 替换为你的高德 Key

    fetch(`https://restapi.amap.com/v3/place/text?keywords=${encodeURIComponent(keyword)}&key=${key}&city=全国`)
      .then(response => response.json())
      .then(data => {
        if (data.status === '1' && data.pois && data.pois.length > 0) {
          displaySearchResults(data.pois);
          document.getElementById('search-results-popup').style.display = 'flex';
        } else {
          document.getElementById('search-results-list').innerHTML = `<p>${data.info || '未找到匹配结果'}</p>`;
          document.getElementById('search-results-popup').style.display = 'flex';
        }
      })
      .catch(error => {
        document.getElementById('search-results-list').innerHTML = `<p>请求失败: ${error.message}</p>`;
        document.getElementById('search-results-popup').style.display = 'flex';
      });
  });

  // 绑定删除事件
  document.querySelector(`#${waypointId}-container .remove-waypoint-btn`).addEventListener('click', function () {
    const container = document.getElementById(`${waypointId}-container`);
    container.remove();

    // 清除对应的数据和标记
    const existingWaypoint = waypoints.find(w => w.id === waypointId);
    if (existingWaypoint && existingWaypoint.marker) {
      map.remove(existingWaypoint.marker);
    }
    waypoints = waypoints.filter(w => w.id !== waypointId);
  });
}

// 登录检查
function checkAuth() {
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
    } catch (e) {
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
  logoutBtn.addEventListener('click', function () {
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    window.location.href = '../authen/authen.html';
  });
}

// 页面加载完成后初始化
window.onload = function () {
  initMap();
  checkAuth();

  bindSearchEvent('start-point');
  bindSearchEvent('end-point');

  document.getElementById('add-waypoint-btn').addEventListener('click', addWaypoint);
  document.getElementById('plan-route-btn').addEventListener('click', planRoute);

  // 绑定模式切换事件
  document.getElementById('routeMode').addEventListener('change', function () {
    const mode = this.value;
    if (mode === 'loop') {
      document.getElementById('end-point').closest('.input-group').style.display = 'none';
    } else {
      document.getElementById('end-point').closest('.input-group').style.display = 'block';
    }

    // 切换模式时清空地图上的标记和路径
    clearMap();
  });

  document.getElementById('close-popup-btn').addEventListener('click', function () {
    document.getElementById('search-results-popup').style.display = 'none';
  });
};