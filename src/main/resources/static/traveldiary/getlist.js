document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const blogContainer = document.getElementById('blog-container');
    const sortViewsBtn = document.getElementById('sort-views');
    const sortRatingBtn = document.getElementById('sort-rating');
    const usernameSpan = document.getElementById('username');
    const logoutBtn = document.getElementById('logout-btn');
    
    // 搜索相关元素
    const searchInput = document.getElementById('search-input');
    const searchBtn = document.getElementById('search-btn');
    const searchTitleBtn = document.getElementById('search-title');
    const searchContentBtn = document.getElementById('search-content');
    const searchResultInfo = document.getElementById('search-result-info');
    const clearSearchBtn = document.getElementById('clear-search');
    
    // 当前搜索状态
    let currentSearch = {
        isSearching: false,
        keyword: '',
        type: 'title'
    };
    
    // 当前排序方式
    let currentOrderType = 'views';

    // 从 localStorage 获取当前用户（如果不存在则跳转到登录页）
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
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '../authen/authen.html';
    });

    // 排序按钮点击事件 - 使用单选按钮样式
    sortViewsBtn.addEventListener('change', function() {
        if (this.checked) {
            currentOrderType = 'views';
            if (currentSearch.isSearching) {
                performSearch(currentSearch.keyword, currentSearch.type, currentOrderType);
            } else {
                fetchDiaries(currentOrderType);
            }
        }
    });

    sortRatingBtn.addEventListener('change', function() {
        if (this.checked) {
            currentOrderType = 'rating';
            if (currentSearch.isSearching) {
                performSearch(currentSearch.keyword, currentSearch.type, currentOrderType);
            } else {
                fetchDiaries(currentOrderType);
            }
        }
    });
    
    // 搜索按钮点击事件
    searchBtn.addEventListener('click', function() {
        const keyword = searchInput.value.trim();
        if (!keyword) {
            alert('请输入搜索关键词');
            return;
        }
        
        const searchType = searchTitleBtn.checked ? 'title' : 'content';
        performSearch(keyword, searchType, currentOrderType);
    });
    
    // 回车搜索
    searchInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            const keyword = searchInput.value.trim();
            if (!keyword) {
                alert('请输入搜索关键词');
                return;
            }
            
            const searchType = searchTitleBtn.checked ? 'title' : 'content';
            performSearch(keyword, searchType, currentOrderType);
        }
    });
    
    // 清除搜索
    clearSearchBtn.addEventListener('click', function() {
        searchInput.value = '';
        currentSearch = {
            isSearching: false,
            keyword: '',
            type: 'title'
        };
        searchResultInfo.style.display = 'none';
        fetchDiaries(currentOrderType);
    });

    // 执行搜索
    function performSearch(keyword, type, orderType) {
        // 更新当前搜索状态
        currentSearch = {
            isSearching: true,
            keyword: keyword,
            type: type
        };
        
        // 显示搜索结果信息
        searchResultInfo.style.display = 'flex';
        searchResultInfo.querySelector('h3').textContent = `搜索结果: "${keyword}"`;
        
        // 显示加载中提示
        blogContainer.innerHTML = '<p>正在搜索中...</p>';
        
        let url;
        if (type === 'title') {
            // 按标题搜索
            url = `http://localhost:8081/api/diaries/diary/search_title/${encodeURIComponent(keyword)}?orderType=${orderType}`;
        } else {
            // 按内容搜索
            url = `http://localhost:8081/api/diaries/search/content/${encodeURIComponent(keyword)}?orderType=${orderType}`;
        }
        
        fetch(url)
            .then(response => {
                if (!response.ok) throw new Error('网络响应不正常');
                return response.json();
            })
            .then(diaries => {
                renderDiaries(diaries, true);
            })
            .catch(error => {
                console.error('搜索失败:', error);
                blogContainer.innerHTML = '<p>搜索失败，请稍后重试</p>';
            });
    }

    // 获取日记列表
    function fetchDiaries(orderType = 'views') {
        // 显示加载中提示
        blogContainer.innerHTML = '<p>正在加载日记列表...</p>';
        
        fetch(`http://localhost:8081/api/diaries/all?orderType=${orderType}`)
            .then(response => {
                if (!response.ok) throw new Error('网络响应不正常');
                return response.json();
            })
            .then(diaries => {
                renderDiaries(diaries);
            })
            .catch(error => {
                console.error('获取日记列表失败:', error);
                blogContainer.innerHTML = '<p>加载日记失败，请稍后重试</p>';
            });
    }

    // 渲染日记列表
    function renderDiaries(diaries, isSearchResult = false) {
        blogContainer.innerHTML = ''; // 清空容器

        if (diaries.length === 0) {
            if (isSearchResult) {
                blogContainer.innerHTML = '<p>没有找到匹配的日记</p>';
            } else {
                blogContainer.innerHTML = '<p>暂无日记</p>';
            }
            return;
        }

        diaries.forEach(diary => {
            const diaryElement = createDiaryElement(diary);
            blogContainer.appendChild(diaryElement);
        });
    }

    // 创建单个日记元素
    function createDiaryElement(diary) {
        const link = document.createElement('a');
        link.href = `diary.html?id=${diary.id}`;
        link.className = 'blog-post-link';

        const post = document.createElement('div');
        post.className = 'blog-post';

        const title = document.createElement('h2');
        title.className = 'blog-title';
        title.textContent = diary.title;

        const content = document.createElement('p');
        content.className = 'blog-content';
        // 截取前100个字符并添加省略号
        content.textContent = diary.content.length > 100 ? 
            `${diary.content.substring(0, 100)}...` : diary.content;

        const footer = document.createElement('div');
        footer.className = 'blog-footer';

        // 创建作者信息区域
        const authorDiv = document.createElement('div');
        authorDiv.className = 'diary-author';
        
        // 创建作者头像
        const avatarDiv = document.createElement('div');
        avatarDiv.className = 'diary-author-avatar';
        const avatarIcon = document.createElement('i');
        avatarIcon.className = 'fas fa-user';
        avatarDiv.appendChild(avatarIcon);
        
        // 作者名
        const authorName = document.createElement('span');
        authorName.textContent = diary.user ? diary.user.username : '匿名用户';
        
        authorDiv.appendChild(avatarDiv);
        authorDiv.appendChild(authorName);

        // 创建统计信息区域
        const statsDiv = document.createElement('div');
        statsDiv.className = 'diary-stats';
        
        // 浏览量
        const viewsDiv = document.createElement('div');
        viewsDiv.className = 'diary-stat';
        const viewsIcon = document.createElement('i');
        viewsIcon.className = 'fas fa-eye';
        const viewsText = document.createElement('span');
        viewsText.textContent = diary.views || 0;
        viewsDiv.appendChild(viewsIcon);
        viewsDiv.appendChild(viewsText);
        
        // 评分
        const ratingDiv = document.createElement('div');
        ratingDiv.className = 'diary-stat';
        const ratingIcon = document.createElement('i');
        ratingIcon.className = 'fas fa-star';
        const ratingText = document.createElement('span');
        ratingText.textContent = diary.averageRating ? diary.averageRating.toFixed(1) : '0.0';
        ratingDiv.appendChild(ratingIcon);
        ratingDiv.appendChild(ratingText);
        
        statsDiv.appendChild(viewsDiv);
        statsDiv.appendChild(ratingDiv);

        // 添加到页脚
        footer.appendChild(authorDiv);
        footer.appendChild(statsDiv);

        post.appendChild(title);
        post.appendChild(content);
        post.appendChild(footer);

        link.appendChild(post);
        return link;
    }

    // 初始加载，默认按浏览量排序
    fetchDiaries();
});