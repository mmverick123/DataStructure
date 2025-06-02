document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const blogContainer = document.getElementById('blog-container');
    const sortViewsBtn = document.getElementById('sort-views');
    const sortRatingBtn = document.getElementById('sort-rating');
    const usernameSpan = document.getElementById('username');
    const logoutBtn = document.getElementById('logout-btn');

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
        // 实际应用中应清除登录状态并重定向
        console.log('用户退出登录');
        window.location.href = '../authen/authen.html';
    });

    // 排序按钮点击事件
    sortViewsBtn.addEventListener('click', function() {
        if (!this.classList.contains('active')) {
            sortRatingBtn.classList.remove('active');
            this.classList.add('active');
            fetchDiaries('views');
        }
    });

    sortRatingBtn.addEventListener('click', function() {
        if (!this.classList.contains('active')) {
            sortViewsBtn.classList.remove('active');
            this.classList.add('active');
            fetchDiaries('rating');
        }
    });

    // 获取日记列表
    function fetchDiaries(orderType = 'views') {
        //fetch(`http://localhost:8081/api/diaries/all?orderType=${orderType}`)
        fetch('http://localhost:8081/api/diaries/all')
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
    function renderDiaries(diaries) {
        blogContainer.innerHTML = ''; // 清空容器

        if (diaries.length === 0) {
            blogContainer.innerHTML = '<p>暂无日记</p>';
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
        // 截取前15个字符并添加省略号
        content.textContent = diary.content.length > 15 ? 
            `${diary.content.substring(0, 15)}...` : diary.content;

        const footer = document.createElement('div');
        footer.className = 'blog-footer';

        const author = document.createElement('span');
        author.className = 'author';
        author.textContent = diary.user.username;

        const stats = document.createElement('span');
        stats.className = 'stats';
        stats.textContent = `点击量：${diary.views} | 评分：${diary.averageRating.toFixed(1)}`;

        footer.appendChild(author);
        footer.appendChild(stats);

        post.appendChild(title);
        post.appendChild(content);
        post.appendChild(footer);

        link.appendChild(post);
        return link;
    }

    // 初始加载，默认按浏览量排序
    fetchDiaries();
});