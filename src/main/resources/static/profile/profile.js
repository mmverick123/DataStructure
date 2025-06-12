document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const profileUsername = document.getElementById('profile-username');
    const profileEmail = document.getElementById('profile-email');
    const profileJoined = document.getElementById('profile-joined');
    const diaryCount = document.getElementById('diary-count');
    const totalViews = document.getElementById('total-views');
    const avgRating = document.getElementById('avg-rating');
    const blogContainer = document.getElementById('blog-container');
    const loadingMessage = document.getElementById('loading-message');
    const usernameSpan = document.getElementById('username');
    const logoutBtn = document.getElementById('logout-btn');
    
    // 模态框相关元素
    const editProfileBtn = document.getElementById('edit-profile-btn');
    const editProfileModal = document.getElementById('edit-profile-modal');
    const closeModal = document.querySelector('.close-modal');
    const tabBtns = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');
    const editUsername = document.getElementById('edit-username');
    const editEmail = document.getElementById('edit-email');
    const passwordForm = document.getElementById('password-form');
    
    // 排序相关元素
    const sortLatestBtn = document.getElementById('sort-latest');
    const sortViewsBtn = document.getElementById('sort-views');
    const sortRatingBtn = document.getElementById('sort-rating');
    
    // 当前排序方式
    let currentOrderType = 'latest';
    
    // 当前用户信息
    let currentUser = null;
    
    // 检查用户登录状态
    function checkLoginStatus() {
        const userJson = localStorage.getItem('user');
        const token = localStorage.getItem('token');
        
        if (!userJson || !token) {
            // 未登录，重定向到登录页
            window.location.href = '../authen/authen.html';
            return;
        }
        
        try {
            currentUser = JSON.parse(userJson);
            usernameSpan.textContent = currentUser.username;
            
            // 加载个人信息
            loadUserProfile(currentUser);
            
            // 加载用户的日记
            loadUserDiaries(currentUser.id);
        } catch (error) {
            console.error('解析用户信息失败:', error);
            localStorage.removeItem('user');
            localStorage.removeItem('token');
            window.location.href = '../authen/authen.html';
        }
    }
    
    // 加载用户个人信息
    function loadUserProfile(user) {
        profileUsername.textContent = user.username;
        profileEmail.textContent = user.email || '未设置邮箱';
        
        // 这里注册时间是模拟的，实际项目中应该从后端获取
        const joinedDate = new Date();
        joinedDate.setMonth(joinedDate.getMonth() - 3); // 假设用户3个月前注册
        profileJoined.textContent = `注册时间：${formatDate(joinedDate)}`;
        
        // 设置修改信息表单中的值
        editUsername.value = user.username;
        editEmail.value = user.email || '';
    }
    
    // 加载用户的日记
    function loadUserDiaries(userId, orderType = 'latest') {
        loadingMessage.style.display = 'block';
        blogContainer.innerHTML = '';
        
        fetch(`http://localhost:8081/api/diaries/user/${userId}?orderType=${orderType}`)
            .then(response => {
                if (!response.ok) throw new Error('网络响应不正常');
                return response.json();
            })
            .then(diaries => {
                loadingMessage.style.display = 'none';
                
                if (diaries.length === 0) {
                    blogContainer.innerHTML = '<p id="no-diaries-message">您还没有发布任何日记</p>';
                    diaryCount.textContent = '0';
                    totalViews.textContent = '0';
                    avgRating.textContent = '0.0';
                    return;
                }
                
                // 更新统计信息
                updateDiaryStats(diaries);
                
                // 渲染日记列表
                renderDiaries(diaries);
            })
            .catch(error => {
                console.error('获取用户日记失败:', error);
                loadingMessage.style.display = 'none';
                blogContainer.innerHTML = '<p>加载日记失败，请稍后重试</p>';
            });
    }
    
    // 更新日记统计信息
    function updateDiaryStats(diaries) {
        diaryCount.textContent = diaries.length;
        
        // 计算总浏览量
        const views = diaries.reduce((total, diary) => total + (diary.views || 0), 0);
        totalViews.textContent = views;
        
        // 计算平均评分
        let totalRating = 0;
        let ratingCount = 0;
        
        diaries.forEach(diary => {
            if (diary.averageRating && diary.averageRating > 0) {
                totalRating += diary.averageRating;
                ratingCount++;
            }
        });
        
        const averageRating = ratingCount > 0 ? (totalRating / ratingCount).toFixed(1) : '0.0';
        avgRating.textContent = averageRating;
    }
    
    // 渲染日记列表
    function renderDiaries(diaries) {
        blogContainer.innerHTML = '';
        
        diaries.forEach(diary => {
            const diaryElement = createDiaryElement(diary);
            blogContainer.appendChild(diaryElement);
        });
    }
    
    // 创建单个日记元素
    function createDiaryElement(diary) {
        const diaryWrapper = document.createElement('div');
        diaryWrapper.className = 'blog-post';
        diaryWrapper.dataset.id = diary.id;
        
        // 删除按钮
        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'delete-btn';
        deleteBtn.title = '删除日记';
        deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
        deleteBtn.addEventListener('click', function(e) {
            e.stopPropagation(); // 阻止事件冒泡
            deleteDiary(diary.id);
        });
        
        const link = document.createElement('a');
        link.href = `../traveldiary/diary.html?id=${diary.id}`;
        link.className = 'blog-post-link';
        
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
        
        // 创建统计信息区域
        const statsDiv = document.createElement('div');
        statsDiv.className = 'diary-stats';
        
        // 创建时间
        const dateDiv = document.createElement('div');
        dateDiv.className = 'diary-stat';
        const dateIcon = document.createElement('i');
        dateIcon.className = 'fas fa-calendar-alt';
        const dateText = document.createElement('span');
        dateText.textContent = formatDate(new Date(diary.createdAt));
        dateDiv.appendChild(dateIcon);
        dateDiv.appendChild(dateText);
        
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
        
        // 添加到统计区域
        statsDiv.appendChild(dateDiv);
        statsDiv.appendChild(viewsDiv);
        statsDiv.appendChild(ratingDiv);
        
        // 添加到页脚
        footer.appendChild(statsDiv);
        
        // 添加到链接
        link.appendChild(title);
        link.appendChild(content);
        link.appendChild(footer);
        
        // 添加到卡片
        diaryWrapper.appendChild(deleteBtn);
        diaryWrapper.appendChild(link);
        
        return diaryWrapper;
    }
    
    // 删除日记
    function deleteDiary(diaryId) {
        if (!confirm('确定要删除这篇日记吗？此操作不可恢复。')) {
            return;
        }
        
        const token = localStorage.getItem('token');
        
        fetch(`http://localhost:8081/api/diaries/${diaryId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        })
        .then(response => {
            if (!response.ok) throw new Error('删除失败');
            return response.json();
        })
        .then(data => {
            // 删除成功，从DOM中移除对应元素
            const diaryElement = document.querySelector(`.blog-post[data-id="${diaryId}"]`);
            if (diaryElement) {
                diaryElement.remove();
                
                // 检查是否还有日记
                if (blogContainer.children.length === 0) {
                    blogContainer.innerHTML = '<p id="no-diaries-message">您还没有发布任何日记</p>';
                    diaryCount.textContent = '0';
                    totalViews.textContent = '0';
                    avgRating.textContent = '0.0';
                } else {
                    // 重新加载用户日记以更新统计信息
                    loadUserDiaries(currentUser.id, currentOrderType);
                }
                
                alert('日记删除成功');
            }
        })
        .catch(error => {
            console.error('删除日记失败:', error);
            alert('删除日记失败，请稍后重试');
        });
    }
    
    // 修改密码
    function changePassword(currentPassword, newPassword, confirmPassword) {
        if (newPassword !== confirmPassword) {
            alert('两次输入的新密码不一致');
            return;
        }
        
        if (newPassword.length < 6) {
            alert('密码长度不能少于6位');
            return;
        }
        
        const token = localStorage.getItem('token');
        
        fetch('http://localhost:8081/api/user/change-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({
                currentPassword,
                newPassword,
                confirmPassword
            })
        })
        .then(response => {
            if (!response.ok) throw new Error('修改密码失败');
            return response.json();
        })
        .then(data => {
            alert('密码修改成功，请重新登录');
            // 注销当前登录
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '../authen/authen.html';
        })
        .catch(error => {
            console.error('修改密码失败:', error);
            alert('修改密码失败，请确认当前密码是否正确');
        });
    }
    
    // 格式化日期
    function formatDate(date) {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }
    
    // 注册事件监听器
    function registerEventListeners() {
        // 退出登录
        logoutBtn.addEventListener('click', function() {
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = '../authen/authen.html';
        });
        
        // 排序按钮点击事件
        sortLatestBtn.addEventListener('change', function() {
            if (this.checked) {
                currentOrderType = 'latest';
                loadUserDiaries(currentUser.id, currentOrderType);
            }
        });
        
        sortViewsBtn.addEventListener('change', function() {
            if (this.checked) {
                currentOrderType = 'views';
                loadUserDiaries(currentUser.id, currentOrderType);
            }
        });
        
        sortRatingBtn.addEventListener('change', function() {
            if (this.checked) {
                currentOrderType = 'rating';
                loadUserDiaries(currentUser.id, currentOrderType);
            }
        });
        
        // 打开编辑信息模态框
        editProfileBtn.addEventListener('click', function() {
            editProfileModal.style.display = 'flex';
        });
        
        // 关闭模态框
        closeModal.addEventListener('click', function() {
            editProfileModal.style.display = 'none';
        });
        
        // 点击模态框外部关闭
        window.addEventListener('click', function(event) {
            if (event.target === editProfileModal) {
                editProfileModal.style.display = 'none';
            }
        });
        
        // 选项卡切换
        tabBtns.forEach(btn => {
            btn.addEventListener('click', function() {
                const tabId = this.dataset.tab;
                
                // 更新选项卡按钮状态
                tabBtns.forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                
                // 更新选项卡内容显示
                tabContents.forEach(content => {
                    if (content.id === tabId) {
                        content.classList.add('active');
                    } else {
                        content.classList.remove('active');
                    }
                });
            });
        });
        
        // 修改密码表单提交
        passwordForm.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const currentPassword = document.getElementById('current-password').value;
            const newPassword = document.getElementById('new-password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            
            changePassword(currentPassword, newPassword, confirmPassword);
        });
    }
    
    // 初始化
    checkLoginStatus();
    registerEventListeners();
}); 