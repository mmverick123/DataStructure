document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const usernameSpan = document.getElementById('username');
    const logoutBtn = document.getElementById('logout-btn');
    const searchBtn = document.getElementById('search-btn');
    const searchInput = document.getElementById('search-input');
    const recommendContainer = document.getElementById('recommend-container');
    
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
    
    // API基础URL
    const API_BASE_URL = 'http://localhost:8081/api';
    
    // 获取当前用户信息
    function getCurrentUser() {
        // 从localStorage获取用户信息
        const user = JSON.parse(localStorage.getItem('user'));
        
        if (user && user.username) {
            usernameSpan.textContent = user.username;
            
            // 退出登录按钮
            logoutBtn.addEventListener('click', function() {
                localStorage.removeItem('user');
                localStorage.removeItem('token');
                window.location.href = '../authen/login.html';
            });
        } else {
            // 未登录状态下，重定向到登录页面
            window.location.href = '../authen/login.html';
        }
    }
    
    // 执行获取用户信息
    getCurrentUser();
    
    // 创建景点元素
    function createAttractionElement(attraction) {
        // 调试输出，帮助定位问题
        console.log('创建景点元素，原始数据:', attraction);
        console.log('景点类别:', attraction.category);
        
        const link = document.createElement('a');
        link.href = `spot.html?id=${attraction.id}`;
        link.className = 'blog-post-link';
        
        // 在点击链接时将景点数据存储到localStorage
        link.addEventListener('click', function(e) {
            e.preventDefault(); // 阻止默认跳转
            
            // 确保attraction对象包含必要的字段
            if (!attraction.id || !attraction.name) {
                console.error('景点数据缺少必要字段:', attraction);
                alert('景点数据不完整，无法查看详情');
                return;
            }
            
            // 复制并确保包含必要字段
            const attractionData = {
                ...attraction,
                id: attraction.id,
                name: attraction.name,
                category: attraction.category || '未分类',
                keywords: attraction.keywords || []
            };
            
            console.log('存储到localStorage的景点数据:', attractionData);
            
            // 将景点数据存储到localStorage
            try {
                localStorage.setItem('currentSpotData', JSON.stringify(attractionData));
                // 手动跳转
                window.location.href = `spot.html?id=${attraction.id}`;
            } catch (error) {
                console.error('存储景点数据到localStorage失败:', error);
                // 即使存储失败也尝试跳转
                window.location.href = `spot.html?id=${attraction.id}`;
            }
        });

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
        
        // 检查category是否存在，可能是字符串或者对象
        if (attraction.category) {
            // 如果category是对象，尝试获取其name属性
            if (typeof attraction.category === 'object' && attraction.category.name) {
                // 根据类别对象的name属性设置显示文本
                switch(attraction.category.name.toLowerCase()) {
                    case 'history':
                    case '历史文化':
                        categoryText = '历史文化';
                        break;
                    case 'nature':
                    case '自然风光':
                        categoryText = '自然风光';
                        break;
                    case 'garden':
                    case '园林景观':
                        categoryText = '园林景观';
                        break;
                    case 'education':
                    case '教育机构':
                        categoryText = '教育机构';
                        break;
                    case 'commercial':
                    case '商业区域':
                        categoryText = '商业区域';
                        break;
                    default:
                        categoryText = attraction.category.name; // 直接使用类别名称
                }
            } else if (typeof attraction.category === 'string') {
                // 如果是字符串，直接根据字符串值设置
                switch(attraction.category.toLowerCase()) {
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
                    default:
                        if (attraction.category.trim() !== '') {
                            categoryText = attraction.category; // 直接使用类别名称
                        }
                }
            }
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
    
    // 获取推荐内容
    async function fetchRecommendations(filter = 'hot') {
        try {
            let url;
            
            // 根据过滤条件构建不同的API URL
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
            
            console.log(`获取推荐内容，URL: ${url}`);
            
            // 使用真实API调用
            console.log('发起推荐请求...');
            const response = await fetch(url);
            console.log('收到推荐响应, 状态码:', response.status);
            
            // 检查响应状态
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // 尝试解析JSON
            let data;
            try {
                const text = await response.text();
                console.log('推荐API响应原始文本:', text);
                data = text ? JSON.parse(text) : [];
            } catch (parseError) {
                console.error('解析推荐JSON响应时出错:', parseError);
                throw new Error('服务器返回了无效的数据格式');
            }
            
            console.log('推荐内容原始数据:', data);
            
            // 还原页面标题为默认值
            const recommendTitle = document.querySelector('.recommend-title');
            recommendTitle.textContent = '推荐旅游景点和学校';
            
            // 验证数据格式
            if (data && Array.isArray(data)) {
                console.log(`成功获取${data.length}个推荐景点`);
                
                // 确保每个景点对象都有必要的字段
                const processedData = data.map(item => {
                    console.log('处理推荐景点数据:', item);
                    
                    // 如果category不存在或为空，设置为"未分类"
                    if (!item.category) {
                        item.category = '未分类';
                    }
                    
                    // 返回处理后的数据
                    return {
                        ...item,
                        // 确保必要字段存在
                        id: item.id,
                        name: item.name || '未命名景点',
                        totalViews: item.totalViews || 0,
                        averageRating: item.averageRating || 0
                    };
                });
                
                renderAttractions(processedData, recommendContainer);
            } else {
                console.error('获取推荐内容失败: 返回数据格式错误', data);
                recommendContainer.innerHTML = '<p>获取推荐内容失败，请稍后再试</p>';
            }
        } catch (error) {
            console.error('获取推荐内容出错:', error);
            recommendContainer.innerHTML = '<p>获取推荐内容失败，请稍后再试: ' + error.message + '</p>';
        }
    }
    
    // 搜索景点
    async function searchAttractions(keyword, orderBy = 'popularity', categories = []) {
        try {
            if (!keyword || keyword.trim() === '') {
                alert('请输入搜索关键词');
                return;
            }
            
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
            console.log('搜索URL:', url);
            
            // 使用真实API调用
            console.log('发起搜索请求...');
            const response = await fetch(url);
            console.log('收到搜索响应, 状态码:', response.status);
            
            // 检查响应状态
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            // 尝试解析JSON
            let data;
            try {
                const text = await response.text();
                console.log('API响应原始文本:', text);
                data = text ? JSON.parse(text) : [];
            } catch (parseError) {
                console.error('解析JSON响应时出错:', parseError);
                throw new Error('服务器返回了无效的数据格式');
            }
            
            console.log('搜索结果:', data);
            
            // 更新页面标题，显示这是搜索结果
            const recommendTitle = document.querySelector('.recommend-title');
            recommendTitle.textContent = `"${keyword}" 的搜索结果`;
            
            if (data && Array.isArray(data) && data.length > 0) {
                // 直接将搜索结果渲染到推荐容器中
                console.log(`成功获取${data.length}个搜索结果`);
                
                // 确保每个景点对象都有必要的字段
                const processedData = data.map(item => {
                    console.log('处理搜索景点数据:', item);
                    
                    // 如果category不存在或为空，设置为"未分类"
                    if (!item.category) {
                        item.category = '未分类';
                    }
                    
                    // 返回处理后的数据
                    return {
                        ...item,
                        // 确保必要字段存在
                        id: item.id,
                        name: item.name || '未命名景点',
                        totalViews: item.totalViews || 0,
                        averageRating: item.averageRating || 0
                    };
                });
                
                // 直接渲染到推荐容器
                renderAttractions(processedData, recommendContainer);
            } else {
                console.log('未找到匹配的景点，返回数据:', data);
                recommendContainer.innerHTML = '<p>未找到匹配的景点或学校</p>';
            }
        } catch (error) {
            console.error('搜索景点出错:', error);
            recommendContainer.innerHTML = '<p>搜索失败，请稍后再试：' + error.message + '</p>';
        }
    }
    
    // 初始化页面
    function initPage() {
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