document.addEventListener('DOMContentLoaded', function() {
    // 获取DOM元素
    const diaryContainer = document.getElementById('diary-container');
    const loadingElement = document.getElementById('loading');
    const usernameSpan = document.getElementById('username');
    const logoutBtn = document.getElementById('logout-btn');

    // 设置当前用户（实际应用中应从登录状态获取）
    const currentUser = { username: 'iwenwiki' };
    usernameSpan.textContent = currentUser.username;

    // 退出登录功能
    logoutBtn.addEventListener('click', function() {
        // 实际应用中应清除登录状态并重定向
        console.log('用户退出登录');
        window.location.href = '../authen/authen.html';
    });

    // 从URL获取日记ID
    const urlParams = new URLSearchParams(window.location.search);
    const diaryId = urlParams.get('id');

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

        // 返回按钮
        const backButton = document.createElement('a');
        backButton.href = 'list.html';
        backButton.className = 'back-button';
        backButton.textContent = '返回列表';
        diaryElement.appendChild(backButton);

        // 清空容器并添加新内容
        diaryContainer.innerHTML = '';
        diaryContainer.appendChild(diaryElement);
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
});