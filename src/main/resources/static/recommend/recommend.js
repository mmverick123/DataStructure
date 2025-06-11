document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const usernameSpan = document.getElementById('username');
    const logoutBtn = document.getElementById('logout-btn');
    const searchBtn = document.getElementById('search-btn');
    const searchInput = document.getElementById('search-input');
    const searchResult = document.getElementById('search-result');
    const recommendContainer = document.getElementById('recommend-container');
    const relatedDiaries = document.getElementById('related-diaries');
    const navBtn = document.getElementById('nav-btn');
    const closeSearchBtn = document.getElementById('close-search');
    
    // 排序和过滤选项
    const sortPopularity = document.getElementById('sort-popularity');
    const sortRating = document.getElementById('sort-rating');
    const sortComprehensive = document.getElementById('sort-comprehensive');
    
    // 类别过滤选项
    const categoryHistory = document.getElementById('category-history');
    const categoryNature = document.getElementById('category-nature');
    const categoryGarden = document.getElementById('category-garden');
    const categoryEducation = document.getElementById('category-education');
    const categoryCommercial = document.getElementById('category-commercial');
    
    // 侧边栏推荐过滤选项
    const filterHot = document.getElementById('filter-hot');
    const filterTopRated = document.getElementById('filter-top-rated');
    const filterComprehensive = document.getElementById('filter-comprehensive');
    
    // 地图相关变量
    let map;
    let currentMarker = null;
    let currentLocation = null;
    
    // API基础URL
    const API_BASE_URL = 'http://localhost:8081/api';
    
    // 从 localStorage 获取当前用户
    function getCurrentUser() {
        const userJson = localStorage.getItem('user');
        if (!userJson) {
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
    }
    
    // 退出登录功能
    logoutBtn.addEventListener('click', function() {
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        window.location.href = '../authen/authen.html';
    });
    
    // 初始化地图
    function initMap() {
        map = new AMap.Map('map', {
            zoom: 15,
            center: [116.397428, 39.90923]
        });
        
        AMap.plugin(['AMap.ToolBar', 'AMap.Scale'], function() {
            map.addControl(new AMap.ToolBar());
            map.addControl(new AMap.Scale());
        });
    }
    
    // 在地图上标记位置
    function markLocation(lnglat, name) {
        if (currentMarker) {
            map.remove(currentMarker);
        }
        
        currentMarker = new AMap.Marker({
            position: lnglat,
            map: map,
            content: `<div style="background-color:#4CAF50;color:white;padding:2px 5px;border-radius:3px;">${name}</div>`
        });
        
        map.setCenter(lnglat);
        currentLocation = {lnglat, name};
        
        // 更新导航按钮的URL
        navBtn.href = `navigate.html?destination=${encodeURIComponent(name)}&lng=${lnglat[0]}&lat=${lnglat[1]}`;
    }
    
    // 创建景点元素
    function createAttractionElement(attraction) {
        const link = document.createElement('a');
        link.href = `attraction.html?id=${attraction.id}`;
        link.className = 'blog-post-link';

        const post = document.createElement('div');
        post.className = 'blog-post';

        // 添加图片
        const image = document.createElement('img');
        image.className = 'blog-image';
        image.src = (attraction.imageUrls && attraction.imageUrls.length > 0) 
            ? attraction.imageUrls[0] 
            : 'https://via.placeholder.com/300x200?text=暂无图片';
        image.alt = attraction.name;
        
        // 信息区域
        const infoDiv = document.createElement('div');
        infoDiv.className = 'blog-info';

        // 标题
        const title = document.createElement('h2');
        title.className = 'blog-title';
        title.textContent = attraction.name;
        
        // 关键词/标签
        const keywordsDiv = document.createElement('div');
        keywordsDiv.className = 'blog-keywords';
        
        // 添加类别标签
        const categoryTag = document.createElement('span');
        categoryTag.className = 'keyword-tag';
        
        // 根据类别设置显示文本
        let categoryText = '未分类';
        switch(attraction.category) {
            case 'history':
                categoryText = '历史文化';
                break;
            case 'nature':
                categoryText = '自然风光';
                break;
            case 'garden':
                categoryText = '园林景观';
                break;
            case 'education':
                categoryText = '教育机构';
                break;
            case 'commercial':
                categoryText = '商业区域';
                break;
        }
        categoryTag.textContent = categoryText;
        keywordsDiv.appendChild(categoryTag);
        
        // 如果有特色标签，也添加进去
        if (attraction.features && attraction.features.length > 0) {
            attraction.features.slice(0, 2).forEach(feature => {
                const featureTag = document.createElement('span');
                featureTag.className = 'keyword-tag';
                featureTag.textContent = feature;
                keywordsDiv.appendChild(featureTag);
            });
        }
        
        // 页脚统计信息
        const footer = document.createElement('div');
        footer.className = 'blog-footer';
        
        // 浏览量和评分容器
        const statsContainer = document.createElement('div');
        statsContainer.className = 'stats-container';
        
        // 浏览量
        const viewsCount = document.createElement('div');
        viewsCount.className = 'views-count';
        viewsCount.innerHTML = `<i class="fas fa-eye"></i> ${attraction.totalViews || 0}`;
        
        // 评分
        const ratingContainer = document.createElement('div');
        ratingContainer.className = 'rating-container';
        ratingContainer.innerHTML = `<i class="fas fa-star"></i> ${attraction.averageRating?.toFixed(1) || '暂无评分'}`;
        
        // 组装统计信息
        statsContainer.appendChild(viewsCount);
        statsContainer.appendChild(ratingContainer);
        footer.appendChild(statsContainer);

        // 组装所有元素
        infoDiv.appendChild(title);
        infoDiv.appendChild(keywordsDiv);
        
        post.appendChild(image);
        post.appendChild(infoDiv);
        post.appendChild(footer);

        link.appendChild(post);
        return link;
    }
    
    // 创建日记元素（用于搜索结果中显示相关日记）
    function createDiaryElement(diary) {
        const link = document.createElement('a');
        link.href = `diary.html?id=${diary.id}`;
        link.className = 'blog-post-link';

        const post = document.createElement('div');
        post.className = 'blog-post';

        // 添加图片，如果有的话
        if (diary.images && diary.images.length > 0) {
            const image = document.createElement('img');
            image.className = 'blog-image';
            image.src = diary.images[0];
            image.alt = diary.title;
            post.appendChild(image);
        }

        // 信息区域
        const infoDiv = document.createElement('div');
        infoDiv.className = 'blog-info';

        const title = document.createElement('h2');
        title.className = 'blog-title';
        title.textContent = diary.title;
        
        const content = document.createElement('p');
        content.className = 'blog-content';
        content.textContent = diary.content.length > 60 ? 
            `${diary.content.substring(0, 60)}...` : diary.content;

        const footer = document.createElement('div');
        footer.className = 'blog-footer';
        
        const statsContainer = document.createElement('div');
        statsContainer.className = 'stats-container';
        
        const viewsCount = document.createElement('div');
        viewsCount.className = 'views-count';
        viewsCount.innerHTML = `<i class="fas fa-eye"></i> ${diary.views || 0}`;
        
        const ratingContainer = document.createElement('div');
        ratingContainer.className = 'rating-container';
        ratingContainer.innerHTML = `<i class="fas fa-star"></i> ${diary.averageRating?.toFixed(1) || '暂无评分'}`;
        
        statsContainer.appendChild(viewsCount);
        statsContainer.appendChild(ratingContainer);
        
        const author = document.createElement('span');
        author.className = 'author';
        author.textContent = diary.user.username;

        footer.appendChild(statsContainer);
        footer.appendChild(author);

        infoDiv.appendChild(title);
        infoDiv.appendChild(content);
        
        post.appendChild(infoDiv);
        post.appendChild(footer);

        link.appendChild(post);
        return link;
    }
    
    // 渲染景点列表
    function renderAttractions(attractions, container) {
        container.innerHTML = ''; // 清空容器

        if (!attractions || attractions.length === 0) {
            container.innerHTML = '<p>暂无相关景点</p>';
            return;
        }

        attractions.forEach(attraction => {
            const attractionElement = createAttractionElement(attraction);
            container.appendChild(attractionElement);
        });
    }
    
    // 渲染日记列表
    function renderDiaries(diaries, container) {
        container.innerHTML = ''; // 清空容器

        if (!diaries || diaries.length === 0) {
            container.innerHTML = '<p>暂无相关日记</p>';
            return;
        }

        diaries.forEach(diary => {
            const diaryElement = createDiaryElement(diary);
            container.appendChild(diaryElement);
        });
    }
    
    // 从后端获取推荐内容
    async function fetchRecommendations(filter = 'hot') {
        try {
            let url;
            switch(filter) {
                case 'hot':
                    url = `${API_BASE_URL}/attractions/recommendations/popular?limit=12`;
                    break;
                case 'top-rated':
                    url = `${API_BASE_URL}/attractions/recommendations/top-rated?limit=12`;
                    break;
                case 'comprehensive':
                    url = `${API_BASE_URL}/attractions/recommendations/composite?limit=12`;
                    break;
                default:
                    url = `${API_BASE_URL}/attractions/recommendations/popular?limit=12`;
            }
            
            // 使用真实API调用
            const response = await fetch(url);
            const data = await response.json();
            
            // 渲染推荐内容
            if (data) {
                renderAttractions(data, recommendContainer);
            } else {
                console.error('获取推荐内容失败');
                recommendContainer.innerHTML = '<p>获取推荐内容失败，请稍后再试</p>';
            }
        } catch (error) {
            console.error('获取推荐内容出错:', error);
            recommendContainer.innerHTML = '<p>获取推荐内容失败，请稍后再试</p>';
        }
    }
    
    // 搜索景点
    async function searchAttractions(keyword, orderBy = 'popularity', categories = []) {
        try {
            // 转换排序参数以匹配后端API
            let sortBy;
            switch(orderBy) {
                case 'popularity':
                    sortBy = 'views';
                    break;
                case 'rating':
                    sortBy = 'rating';
                    break;
                case 'comprehensive':
                    sortBy = 'composite';
                    break;
                default:
                    sortBy = 'views';
            }

            // 构建API URL
            let categoryParam = '';
            if (categories.length > 0) {
                categoryParam = `&category=${categories[0]}`;  // 后端API只支持单个类别
            }
            
            const url = `${API_BASE_URL}/attractions/search?searchTerm=${encodeURIComponent(keyword)}&sortBy=${sortBy}${categoryParam}&limit=10`;
            
            // 使用真实API调用
            const response = await fetch(url);
            const data = await response.json();
            
            if (data && data.length > 0) {
                // 显示第一个匹配结果
                const attraction = data[0];
                markLocation([attraction.location.lng, attraction.location.lat], attraction.name);
                
                // 获取相关日记
                fetchRelatedDiaries(attraction.id);
                
                // 显示结果区域
                searchResult.style.display = 'block';
            } else {
                alert('未找到匹配的景点或学校');
                searchResult.style.display = 'none';
            }
        } catch (error) {
            console.error('搜索景点出错:', error);
            alert('搜索失败，请稍后再试');
            searchResult.style.display = 'none';
        }
    }
    
    // 获取与景点相关的日记
    async function fetchRelatedDiaries(attractionId) {
        try {
            const url = `${API_BASE_URL}/diaries?attractionId=${attractionId}&limit=5`;
            
            // 使用真实API调用
            const response = await fetch(url);
            const data = await response.json();
            
            if (data) {
                renderDiaries(data, relatedDiaries);
            } else {
                console.error('获取相关日记失败');
                relatedDiaries.innerHTML = '<p>获取相关日记失败，请稍后再试</p>';
            }
        } catch (error) {
            console.error('获取相关日记出错:', error);
            relatedDiaries.innerHTML = '<p>获取相关日记失败，请稍后再试</p>';
        }
    }
    
    // 初始化页面
    function initPage() {
        initMap();
        fetchRecommendations('hot'); // 默认加载热门推荐
        
        // 添加事件监听器
        
        // 搜索按钮事件
        searchBtn.addEventListener('click', function() {
            const keyword = searchInput.value.trim();
            let orderBy = 'popularity';
            
            // 获取排序方式
            if (sortRating.checked) {
                orderBy = 'rating';
            } else if (sortComprehensive.checked) {
                orderBy = 'comprehensive';
            }
            
            // 获取选中的类别
            const categories = [];
            if (categoryHistory.checked) categories.push('history');
            if (categoryNature.checked) categories.push('nature');
            if (categoryGarden.checked) categories.push('garden');
            if (categoryEducation.checked) categories.push('education');
            if (categoryCommercial.checked) categories.push('commercial');
            
            searchAttractions(keyword, orderBy, categories);
        });
        
        // 关闭搜索结果
        closeSearchBtn.addEventListener('click', function() {
            searchResult.style.display = 'none';
        });
        
        // 侧边栏过滤器事件
        filterHot.addEventListener('change', function() {
            if (this.checked) fetchRecommendations('hot');
        });
        
        filterTopRated.addEventListener('change', function() {
            if (this.checked) fetchRecommendations('top-rated');
        });
        
        filterComprehensive.addEventListener('change', function() {
            if (this.checked) fetchRecommendations('comprehensive');
        });
        
        // 按下回车键也能搜索
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchBtn.click();
            }
        });
    }
    
    // 页面加载完成后初始化
    initPage();
});