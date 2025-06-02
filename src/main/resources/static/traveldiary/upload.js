document.addEventListener('DOMContentLoaded', function () {
    // 获取DOM元素
    const diaryForm = document.getElementById('diary-form');
    const fileInput = document.getElementById('media-files');
    const filePreview = document.getElementById('file-preview');
    const progressContainer = document.getElementById('progress-container');
    const progressBar = document.getElementById('progress');
    const progressText = document.getElementById('progress-text');
    const statusMessage = document.getElementById('status-message');
    const usernameSpan = document.getElementById('username');
    const logoutBtn = document.getElementById('logout-btn');

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
        // 如果未登录或解析失败，getCurrentUser 已处理跳转
        throw new Error("用户未登录");
    }

    // 退出登录功能
    logoutBtn.addEventListener('click', function () {
        // 实际应用中应清除登录状态并重定向
        console.log('用户退出登录');
        clearAuthData();
        window.location.href = '../authen/authen.html';
    });

    // 文件选择预览
    fileInput.addEventListener('change', function () {
        filePreview.innerHTML = '';
        const files = Array.from(this.files);

        if (files.length === 0) return;

        // 验证文件类型和大小
        const validFiles = files.filter(file => {
            const isValidType = file.type === 'image/jpeg' || file.type === 'video/mp4';
            const isValidSize = file.size <= 10 * 1024 * 1024; // 10MB

            if (!isValidType) {
                alert(`文件 ${file.name} 格式不支持，仅支持JPEG图片和MP4视频`);
            } else if (!isValidSize) {
                alert(`文件 ${file.name} 超过10MB大小限制`);
            }

            return isValidType && isValidSize;
        });

        // 显示预览
        validFiles.forEach(file => {
            const previewItem = document.createElement('div');
            previewItem.className = 'preview-item';

            if (file.type.startsWith('image/')) {
                const img = document.createElement('img');
                img.src = URL.createObjectURL(file);
                previewItem.appendChild(img);
            } else {
                const icon = document.createElement('div');
                icon.className = 'video-icon';
                icon.textContent = '🎬';
                previewItem.appendChild(icon);
            }

            const fileName = document.createElement('span');
            fileName.textContent = file.name;
            previewItem.appendChild(fileName);

            filePreview.appendChild(previewItem);
        });
    });

    // 表单提交
    diaryForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const title = document.getElementById('title').value;
        const content = document.getElementById('content').value;
        const location = document.getElementById('location').value;  // 新增
        const files = Array.from(fileInput.files).filter(file =>
            (file.type === 'image/jpeg' || file.type === 'video/mp4') &&
            file.size <= 10 * 1024 * 1024
        );

        if (!title || !content) {
            showStatus('请填写标题和内容', 'error');
            return;
        }

        try {
            // 显示进度条
            progressContainer.classList.remove('hidden');
            updateProgress(0, '正在创建日记...');

            // 1. 先创建日记
            const diary = await createDiary(title, content, location); // 传入 location
            updateProgress(30, '日记创建成功，准备上传媒体文件...');

            // 2. 如果有文件，上传媒体
            if (files.length > 0) {
                await uploadMediaFiles(diary.id, files);
            }

            updateProgress(100, '上传完成！');
            showStatus('日记发布成功！即将跳转到列表页...', 'success');

            // 3秒后跳转到列表页
            setTimeout(() => {
                localStorage.removeItem('diaryFormData'); // 提交成功后清除缓存
                window.location.href = 'list.html';
            }, 3000);
        } catch (error) {
            console.error('上传失败:', error);
            showStatus(`上传失败: ${error.message}`, 'error');
            progressContainer.classList.add('hidden');
        }
    });

    // 创建日记
    async function createDiary(title, content, location) {
        const response = await fetch('http://localhost:8081/api/diaries', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${currentUser.token}`
            },
            body: JSON.stringify({
                title,
                content,
                location,
            })
        });

        if (!response.ok) {
            throw new Error('创建日记失败');
        }

        return await response.json();
    }

    // 上传媒体文件
    async function uploadMediaFiles(diaryId, files) {
        const totalFiles = files.length;
        let uploadedCount = 0;

        for (const file of files) {
            try {
                const formData = new FormData();
                formData.append('file', file);

                updateProgress(
                    30 + Math.floor((uploadedCount / totalFiles) * 60),
                    `正在上传文件 (${uploadedCount + 1}/${totalFiles}): ${file.name}`
                );

                const response = await fetch(`http://localhost:8081/api/media/upload/${diaryId}`, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${currentUser.token}`
                    },
                    body: formData
                });

                if (!response.ok) {
                    throw new Error(`文件 ${file.name} 上传失败`);
                }

                uploadedCount++;
            } catch (error) {
                console.error(`文件 ${file.name} 上传失败:`, error);
                // 继续上传其他文件
            }
        }
    }

    // 更新进度条
    function updateProgress(percent, text) {
        progressBar.style.width = `${percent}%`;
        progressText.textContent = text;
    }

    // 显示状态消息
    function showStatus(message, type) {
        statusMessage.textContent = message;
        statusMessage.className = `status-message ${type}`;
        statusMessage.classList.remove('hidden');
    }

    // 解析 URL 查询参数，并设置 #location 输入框的值
    function getQueryParam(param) {
        const search = window.location.search || window.location.hash.split('?')[1] || '';
        const params = new URLSearchParams(search);
        return params.get(param);
    }

    window.addEventListener('DOMContentLoaded', function () {
        const savedData = localStorage.getItem('diaryFormData');
        if (savedData) {
            const data = JSON.parse(savedData);
            document.getElementById('title').value = data.title || '';
            document.getElementById('content').value = data.content || '';
            document.getElementById('location').value = data.location || '';
        }

        // 获取 URL 参数设置 location 的值（如果存在）
        const locationName = getQueryParam('location');
        if (locationName) {
            document.getElementById('location').value = locationName;
        }
    });

    // 保存表单数据到 localStorage
    function saveFormData() {
        const formData = {
            title: document.getElementById('title').value,
            content: document.getElementById('content').value,
            location: document.getElementById('location').value
        };
        localStorage.setItem('diaryFormData', JSON.stringify(formData));
    }

    // 跳转前保存表单数据
    document.getElementById('select-location-btn').addEventListener('click', function () {
        saveFormData();
        window.location.href = '../map/locate.html';
    });
});