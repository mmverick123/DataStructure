document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const diaryContainer = document.getElementById('diary-container');
    const loadingElement = document.getElementById('loading');
    const usernameSpan = document.getElementById('username');
    const logoutBtn = document.getElementById('logout-btn');
    // 声明selectedRating变量在更高的作用域中
    let selectedRating = null;
    // 从URL获取日记ID
    const urlParams = new URLSearchParams(window.location.search);
    const diaryId = urlParams.get('id');

    function clearAuthData() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    }
   // 从 localStorage 获取当前用户信息
   function getCurrentUser() {
    const token = localStorage.getItem('token');
    const userStr = localStorage.getItem('user');

    if (!token || !userStr) {
        // 如果没有登录，跳转到登录页
        window.location.href = '../authen/authen.html';
        return null;
    }

    try {
        const user = JSON.parse(userStr);
        return {
            username: user.username,
            id: user.id,
            token: token
        };
    } catch (e) {
        console.error('解析用户信息失败', e);
        clearAuthData(); // 清除错误的数据
        window.location.href = '../authen/authen.html';
        return null;
    }
}

const currentUser = getCurrentUser();

if (currentUser) {
    usernameSpan.textContent = currentUser.username;
} else {
    throw new Error("用户未登录");
}

    // 退出登录功能
    logoutBtn.addEventListener('click', function() {
        // 实际应用中应清除登录状态并重定向
        console.log('用户退出登录');
        window.location.href = '../authen/authen.html';
    });

    // 从URL获取日记ID
    // 已经在上面获取了日记ID，这里不需要重复
    // const urlParams = new URLSearchParams(window.location.search);
    // const diaryId = urlParams.get('id');

    if (!diaryId) {
        showError('无效的日记ID');
        return;
    }

    // 获取日记详情
    fetchDiary(diaryId);

    // 获取日记详情
    function fetchDiary(id) {
        fetch(`http://localhost:8081/api/diaries/${id}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('网络响应不正常');
                }
                return response.json();
            })
            .then(diary => {
                loadingElement.style.display = 'none';
                renderDiary(diary);
            })
            .catch(error => {
                console.error('获取日记详情失败:', error);
                loadingElement.style.display = 'none';
                showError('加载日记失败，请稍后重试');
            });
    }

    // 渲染日记详情
    function renderDiary(diary) {
        const diaryElement = document.createElement('div');
        
        // 日记标题
        const titleElement = document.createElement('h1');
        titleElement.className = 'diary-title';
        titleElement.textContent = diary.title;
        diaryElement.appendChild(titleElement);

        // 日记内容
        const contentElement = document.createElement('p');
        contentElement.className = 'diary-content';
        contentElement.textContent = diary.content;
        diaryElement.appendChild(contentElement);

        // 媒体区域
        if (diary.mediaList && diary.mediaList.length > 0) {
            const mediaContainer = document.createElement('div');
            mediaContainer.className = 'media-container';
            
            diary.mediaList.forEach(media => {
                if (media.mediaType === 'IMAGE') {
                    const imgElement = document.createElement('img');
                    imgElement.src = media.fileUrl;
                    imgElement.alt = media.fileName || '旅行图片';
                    imgElement.className = 'diary-media';
                    mediaContainer.appendChild(imgElement);
                } else if (media.mediaType === 'VIDEO') {
                    const videoElement = document.createElement('video');
                    videoElement.src = media.fileUrl;
                    videoElement.controls = true;
                    videoElement.className = 'diary-media';
                    mediaContainer.appendChild(videoElement);
                }
            });
            
            diaryElement.appendChild(mediaContainer);
        }

        // 作者与时间信息
        const metaElement = document.createElement('div');
        metaElement.className = 'diary-meta';
        
        // 格式化日期
        const createdAt = new Date(diary.createdAt);
        const formattedDate = `${createdAt.getFullYear()}年${createdAt.getMonth() + 1}月${createdAt.getDate()}日`;
        
        metaElement.innerHTML = `
            <span class="diary-author">作者：${diary.user.username}</span>
            <span class="diary-date">创作时间：${formattedDate}</span>
            <span class="diary-stats">
                点击量：${diary.views} | 
                评分：${diary.averageRating.toFixed(1)} (${diary.ratingCount}人评分)
            </span>
        `;
        diaryElement.appendChild(metaElement);
        // 创建评分区域（星星形式）
    const ratingSection = document.createElement('div');
    ratingSection.className = 'rating-section';

    if (diary.user.username !== currentUser.username) {
        ratingSection.innerHTML = `
            <p><strong>请为此日记评分：</strong></p>
            <div class="star-rating" id="star-rating">
                <span data-value="0.5">⭐</span>
                <span data-value="1.0">⭐</span>
                <span data-value="1.5">⭐</span>
                <span data-value="2.0">⭐</span>
                <span data-value="2.5">⭐</span>
                <span data-value="3.0">⭐</span>
                <span data-value="3.5">⭐</span>
                <span data-value="4.0">⭐</span>
                <span data-value="4.5">⭐</span>
                <span data-value="5.0">⭐</span>
            </div>
            <p>当前评分：<span id="current-rating-display">未评分</span></p>
            <button id="submit-rating">提交评分</button>
            <p id="rating-message"></p>
    `;
    } else {
    ratingSection.innerHTML = `<p>不能对自己的日记评分。</p>`;
    }
    diaryElement.appendChild(ratingSection);

        // 返回按钮
        const backButton = document.createElement('a');
        backButton.href = 'list.html';
        backButton.className = 'back-button';
        backButton.textContent = '返回列表';
        diaryElement.appendChild(backButton);

        // 清空容器并添加新内容
        diaryContainer.innerHTML = '';
        diaryContainer.appendChild(diaryElement);

        const starRating = document.getElementById('star-rating');
    const currentRatingDisplay = document.getElementById('current-rating-display');
        
        //星星交互逻辑
        if (starRating) {
            starRating.addEventListener('click', function(event) {
                if (event.target.dataset.value) {
                    selectedRating = parseFloat(event.target.dataset.value);
                    currentRatingDisplay.textContent = selectedRating;
                    // 高亮已选星星
                    Array.from(starRating.children).forEach(span => {
                        span.classList.toggle('active', parseFloat(span.dataset.value) <= selectedRating);
                    });
                }
            });
        }
        
        // 将提交评分事件监听器移到这里，确保它可以在DOM元素创建后立即绑定
        const submitRatingButton = document.getElementById('submit-rating');
        if (submitRatingButton) {
            submitRatingButton.addEventListener('click', async () => {
                const ratingMessage = document.getElementById('rating-message');
                
                if (!selectedRating) {
                    ratingMessage.textContent = '请选择一个评分';
                    return;
                }
                
                try {
                    const response = await fetch(`http://localhost:8081/api/ratings/${diaryId}`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${currentUser.token}`,
                        },
                        body: JSON.stringify({ score: selectedRating })
                    });
                    
                    if (!response.ok) {
                        throw new Error('评分失败');
                    }
                    
                    const result = await response.json();
                    ratingMessage.textContent = '评分成功！';
                    console.log('评分结果:', result);
                    
                    // 评分成功后重新加载页面以显示新的评分
                    setTimeout(() => {
                        location.reload();
                    }, 1500);
                    
                } catch (error) {
                    console.error('评分出错:', error);
                    ratingMessage.textContent = '评分失败，请重试。';
                }
            });
        }
        
        // 显示错误信息
        function showError(message) {
            diaryContainer.innerHTML = `
                <div class="error-message">
                    <p>${message}</p>
                    <a href="list.html" class="back-button">返回列表</a>
                </div>
            `;
        }
    }
});
