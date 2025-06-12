document.addEventListener('DOMContentLoaded', function () {
    // 获取DOM元素
    const diaryForm = document.getElementById('diary-form');
    const fileInput = document.getElementById('media-files');
    const filePreview = document.getElementById('file-preview');
    const aiGenerateBtn = document.getElementById('ai-generate-btn');
    const aiImagePreview = document.getElementById('ai-image-preview');
    const progressContainer = document.getElementById('progress-container');
    const progressBar = document.getElementById('progress');
    const progressText = document.getElementById('progress-text');
    const statusMessage = document.getElementById('status-message');
    const usernameSpan = document.getElementById('username');
    const logoutBtn = document.getElementById('logout-btn');

    // 保存用户上传的文件列表
    let uploadedFiles = [];
    // 保存AI生成的图片
    let aiGeneratedImage = null;

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
        console.log('用户退出登录');
        clearAuthData();
        window.location.href = '../authen/authen.html';
    });

    // 文件选择预览
    fileInput.addEventListener('change', function () {
        // 如果没有选择文件，直接返回
        if (this.files.length === 0) {
            return;
        }
        
        // 获取选择的文件
        const file = this.files[0]; // 只处理第一个文件
        
        // 验证文件类型和大小
        const isValidType = file.type === 'image/jpeg' || file.type === 'video/mp4';
        const isValidSize = file.size <= 10 * 1024 * 1024; // 10MB
        
        if (!isValidType) {
            alert(`文件 ${file.name} 格式不支持，仅支持JPEG图片和MP4视频`);
            fileInput.value = ''; // 清空选择
            return;
        }
        
        if (!isValidSize) {
            alert(`文件 ${file.name} 超过10MB大小限制`);
            fileInput.value = ''; // 清空选择
            return;
        }
        
        // 检查是否已经添加了同名文件
        const isDuplicate = uploadedFiles.some(f => f.name === file.name);
        if (isDuplicate) {
            alert(`已经添加过文件 ${file.name}`);
            fileInput.value = ''; // 清空选择
            return;
        }
        
        // 添加到文件列表
        uploadedFiles.push(file);
        
        // 创建预览元素
        const previewItem = document.createElement('div');
        previewItem.className = 'preview-item';
        previewItem.dataset.filename = file.name; // 用于后续删除
        
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
        
        // 添加删除按钮
        const deleteBtn = document.createElement('button');
        deleteBtn.className = 'delete-file-btn';
        deleteBtn.textContent = '×';
        deleteBtn.onclick = function(e) {
            e.preventDefault();
            e.stopPropagation();
            // 从列表中移除文件
            const filename = previewItem.dataset.filename;
            uploadedFiles = uploadedFiles.filter(f => f.name !== filename);
            // 从预览区域移除
            filePreview.removeChild(previewItem);
        };
        previewItem.appendChild(deleteBtn);
        
        filePreview.appendChild(previewItem);
        
        // 清空输入框，以便于下次选择同一文件
        fileInput.value = '';
    });
    
    // AI生成图片功能
    aiGenerateBtn.addEventListener('click', async function() {
        // 创建弹窗
        const modal = document.createElement('div');
        modal.className = 'modal';
        modal.innerHTML = `
            <div class="modal-content">
                <h3>AI生成图片</h3>
                <p>请描述您想要生成的图片内容：</p>
                <textarea id="ai-prompt" rows="5" placeholder="例如：风景如画的西湖，夕阳西下，金光洒满湖面"></textarea>
                <div class="modal-actions">
                    <button id="modal-cancel" class="cancel-btn">取消</button>
                    <button id="modal-confirm" class="confirm-btn">生成</button>
                </div>
            </div>
        `;
        document.body.appendChild(modal);
        
        // 设置默认提示词
        const title = document.getElementById('title').value;
        const content = document.getElementById('content').value;
        let defaultPrompt = '';
        
        if (title && content) {
            defaultPrompt = `${title}：${content.substring(0, 50)}${content.length > 50 ? '...' : ''}`;
        } else if (title) {
            defaultPrompt = title;
        } else if (content) {
            defaultPrompt = content.substring(0, 100) + (content.length > 100 ? '...' : '');
        }
        
        document.getElementById('ai-prompt').value = defaultPrompt;
        
        // 取消按钮事件
        document.getElementById('modal-cancel').addEventListener('click', function() {
            document.body.removeChild(modal);
        });
        
        // 确认按钮事件
        document.getElementById('modal-confirm').addEventListener('click', async function() {
            const prompt = document.getElementById('ai-prompt').value.trim();
            
            if (!prompt) {
                alert('请输入图片描述');
                return;
            }
            
            try {
                // 关闭弹窗
                document.body.removeChild(modal);
                
                // 禁用按钮，显示加载状态
                aiGenerateBtn.disabled = true;
                aiGenerateBtn.textContent = '生成中...';
                
                // 调用后端生成图片API
                const response = await fetch('http://localhost:8081/api/image-generation', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${currentUser.token}`
                    },
                    body: JSON.stringify({ prompt })
                });
                
                if (!response.ok) {
                    const errorData = await response.json();
                    throw new Error(errorData.message || '生成图片请求失败');
                }
                
                const data = await response.json();
                
                if (!data.success) {
                    throw new Error('生成图片失败：' + (data.error || '未知错误'));
                }
                
                // 处理图片URL而不是Base64数据
                if (data.imageUrl) {
                    // 清空预览区域
                    aiImagePreview.innerHTML = '';
                    
                    // 创建预览元素
                    const previewItem = document.createElement('div');
                    previewItem.className = 'preview-item';
                    
                    // 创建图片元素并设置src为API返回的URL
                    const img = document.createElement('img');
                    img.src = data.imageUrl;
                    img.alt = 'AI生成图片';
                    img.onload = function() {
                        console.log('图片加载成功');
                    };
                    img.onerror = function() {
                        console.error('图片加载失败');
                        alert('图片加载失败，请检查URL是否可访问');
                    };
                    previewItem.appendChild(img);
                    
                    // 记录图片URL用于表单提交
                    aiGeneratedImage = data.imageUrl;
                    
                    const fileName = document.createElement('span');
                    fileName.textContent = 'AI生成图片';
                    previewItem.appendChild(fileName);
                    
                    // 添加删除按钮
                    const deleteBtn = document.createElement('button');
                    deleteBtn.className = 'delete-file-btn';
                    deleteBtn.textContent = '×';
                    deleteBtn.onclick = function(e) {
                        e.preventDefault();
                        e.stopPropagation();
                        // 清空AI生成的图片
                        aiGeneratedImage = null;
                        // 清空预览区域
                        aiImagePreview.innerHTML = '';
                        // 重置按钮文本
                        aiGenerateBtn.textContent = 'AI生成图片';
                    };
                    previewItem.appendChild(deleteBtn);
                    
                    aiImagePreview.appendChild(previewItem);
                    
                    // 恢复按钮状态，但文本变为重新生成
                    aiGenerateBtn.disabled = false;
                    aiGenerateBtn.textContent = '重新生成';
                } else {
                    // 如果没有imageUrl，说明返回格式不对
                    throw new Error('API返回格式错误，缺少图片URL');
                }
            } catch (error) {
                console.error('AI生成图片失败:', error);
                alert('生成图片失败：' + error.message);
                
                // 恢复按钮状态
                aiGenerateBtn.disabled = false;
                aiGenerateBtn.textContent = 'AI生成图片';
            }
        });
    });
    
    // 创建模拟图片（用于演示）
    async function createMockImage() {
        return new Promise((resolve) => {
            const canvas = document.createElement('canvas');
            canvas.width = 400;
            canvas.height = 300;
            const ctx = canvas.getContext('2d');
            
            // 绘制渐变背景
            const gradient = ctx.createLinearGradient(0, 0, 400, 300);
            gradient.addColorStop(0, '#4CAF50');
            gradient.addColorStop(1, '#2196F3');
            ctx.fillStyle = gradient;
            ctx.fillRect(0, 0, 400, 300);
            
            // 添加文本
            ctx.fillStyle = 'white';
            ctx.font = 'bold 24px Arial';
            ctx.textAlign = 'center';
            ctx.fillText('AI生成的图片', 200, 150);
            
            // 转换为Blob
            canvas.toBlob(resolve, 'image/jpeg', 0.95);
        });
    }

    // 表单提交
    diaryForm.addEventListener('submit', async function (e) {
        e.preventDefault();

        const title = document.getElementById('title').value;
        const content = document.getElementById('content').value;
        const location = document.getElementById('location').value;
        
        // 收集所有要上传的文件
        const filesToUpload = [...uploadedFiles];
        if (aiGeneratedImage) {
            filesToUpload.push(aiGeneratedImage);
        }

        if (!title || !content) {
            showStatus('请填写标题和内容', 'error');
            return;
        }
        
        if (!location) {
            showStatus('请选择位置', 'error');
            return;
        }

        try {
            // 显示进度条
            progressContainer.classList.remove('hidden');
            updateProgress(0, '正在创建日记...');

            // 1. 先创建日记
            const diary = await createDiary(title, content, location);
            updateProgress(30, '日记创建成功，准备上传媒体文件...');

            // 2. 如果有文件，上传媒体
            if (filesToUpload.length > 0) {
                await uploadMediaFiles(diary.id, filesToUpload);
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
                updateProgress(
                    30 + Math.floor((uploadedCount / totalFiles) * 60),
                    `正在上传文件 (${uploadedCount + 1}/${totalFiles})`
                );

                // 如果是字符串URL（AI生成的图片URL），则调用保存URL的API
                if (typeof file === 'string' && file.startsWith('http')) {
                    const response = await fetch(`http://localhost:8081/api/diaries/${diaryId}/imageUrl`, {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json',
                            'Authorization': `Bearer ${currentUser.token}`
                        },
                        body: JSON.stringify({ imageUrl: file })
                    });

                    if (!response.ok) {
                        throw new Error(`保存AI生成图片URL失败`);
                    }
                } 
                // 否则是普通文件对象，使用FormData上传
                else {
                    const formData = new FormData();
                    formData.append('file', file);

                    const response = await fetch(`http://localhost:8081/api/media/upload/${diaryId}`, {
                        method: 'POST',
                        headers: {
                            'Authorization': `Bearer ${currentUser.token}`
                        },
                        body: formData
                    });

                    if (!response.ok) {
                        throw new Error(`文件上传失败`);
                    }
                }

                uploadedCount++;
            } catch (error) {
                console.error(`文件上传失败:`, error);
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

    // 保存表单数据到 localStorage
    function saveFormData() {
        const formData = {
            title: document.getElementById('title').value,
            content: document.getElementById('content').value,
            location: document.getElementById('location').value,
            // 保存AI生成的图片URL
            aiGeneratedImage: aiGeneratedImage
        };
        
        // 保存已上传的文件信息（文件对象无法直接序列化，只保存元数据）
        if (uploadedFiles.length > 0) {
            const filesMetadata = uploadedFiles.map(file => ({
                name: file.name,
                type: file.type,
                size: file.size,
                lastModified: file.lastModified
            }));
            formData.uploadedFilesMetadata = filesMetadata;
            
            // 将文件存储到sessionStorage中
            uploadedFiles.forEach((file, index) => {
                const reader = new FileReader();
                reader.onload = function(e) {
                    // 存储文件内容（Base64格式）
                    sessionStorage.setItem(`uploadedFile_${index}`, e.target.result);
                };
                reader.readAsDataURL(file);
            });
            
            // 记录文件数量
            sessionStorage.setItem('uploadedFilesCount', uploadedFiles.length.toString());
        }
        
        localStorage.setItem('diaryFormData', JSON.stringify(formData));
    }

    // 跳转前保存表单数据
    document.getElementById('select-location-btn').addEventListener('click', function () {
        saveFormData();
        window.location.href = '../map/locate.html';
    });
    
    // 加载保存的表单数据
    const savedData = localStorage.getItem('diaryFormData');
    if (savedData) {
        try {
            const data = JSON.parse(savedData);
            document.getElementById('title').value = data.title || '';
            document.getElementById('content').value = data.content || '';
            document.getElementById('location').value = data.location || '';
            
            // 恢复AI生成的图片
            if (data.aiGeneratedImage) {
                aiGeneratedImage = data.aiGeneratedImage;
                
                // 清空预览区域
                aiImagePreview.innerHTML = '';
                
                // 创建预览元素
                const previewItem = document.createElement('div');
                previewItem.className = 'preview-item';
                
                // 创建图片元素
                const img = document.createElement('img');
                img.src = aiGeneratedImage;
                img.alt = 'AI生成图片';
                previewItem.appendChild(img);
                
                const fileName = document.createElement('span');
                fileName.textContent = 'AI生成图片';
                previewItem.appendChild(fileName);
                
                // 添加删除按钮
                const deleteBtn = document.createElement('button');
                deleteBtn.className = 'delete-file-btn';
                deleteBtn.textContent = '×';
                deleteBtn.onclick = function(e) {
                    e.preventDefault();
                    e.stopPropagation();
                    // 清空AI生成的图片
                    aiGeneratedImage = null;
                    // 清空预览区域
                    aiImagePreview.innerHTML = '';
                    // 重置按钮文本
                    aiGenerateBtn.textContent = 'AI生成图片';
                };
                previewItem.appendChild(deleteBtn);
                
                aiImagePreview.appendChild(previewItem);
                
                // 更新按钮文本
                aiGenerateBtn.textContent = '重新生成';
            }
            
            // 恢复已上传的文件
            const filesCount = parseInt(sessionStorage.getItem('uploadedFilesCount') || '0');
            if (filesCount > 0 && data.uploadedFilesMetadata) {
                // 清空当前上传文件列表
                uploadedFiles = [];
                
                // 恢复每个文件
                for (let i = 0; i < filesCount; i++) {
                    const fileData = sessionStorage.getItem(`uploadedFile_${i}`);
                    if (fileData) {
                        // 从Base64数据恢复文件
                        const metadata = data.uploadedFilesMetadata[i];
                        
                        // 从Base64字符串创建Blob
                        const byteString = atob(fileData.split(',')[1]);
                        const mimeType = fileData.split(',')[0].split(':')[1].split(';')[0];
                        const ab = new ArrayBuffer(byteString.length);
                        const ia = new Uint8Array(ab);
                        for (let j = 0; j < byteString.length; j++) {
                            ia[j] = byteString.charCodeAt(j);
                        }
                        const blob = new Blob([ab], { type: mimeType });
                        
                        // 创建File对象
                        const file = new File([blob], metadata.name, { 
                            type: metadata.type,
                            lastModified: metadata.lastModified
                        });
                        
                        // 添加到上传文件列表
                        uploadedFiles.push(file);
                        
                        // 创建预览
                        const previewItem = document.createElement('div');
                        previewItem.className = 'preview-item';
                        previewItem.dataset.filename = file.name;
                        
                        if (file.type.startsWith('image/')) {
                            const img = document.createElement('img');
                            img.src = fileData; // 直接使用Base64数据
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
                        
                        // 添加删除按钮
                        const deleteBtn = document.createElement('button');
                        deleteBtn.className = 'delete-file-btn';
                        deleteBtn.textContent = '×';
                        deleteBtn.onclick = function(e) {
                            e.preventDefault();
                            e.stopPropagation();
                            // 从列表中移除文件
                            const filename = previewItem.dataset.filename;
                            uploadedFiles = uploadedFiles.filter(f => f.name !== filename);
                            // 从预览区域移除
                            filePreview.removeChild(previewItem);
                        };
                        previewItem.appendChild(deleteBtn);
                        
                        filePreview.appendChild(previewItem);
                    }
                }
                
                // 清理sessionStorage
                for (let i = 0; i < filesCount; i++) {
                    sessionStorage.removeItem(`uploadedFile_${i}`);
                }
                sessionStorage.removeItem('uploadedFilesCount');
            }
        } catch (e) {
            console.error('解析保存的表单数据失败:', e);
        }
    }

    // 获取 URL 参数设置 location 的值（如果存在）
    const locationName = getQueryParam('location');
    if (locationName) {
        document.getElementById('location').value = locationName;
    }
    
    // 解析 URL 查询参数
    function getQueryParam(param) {
        const search = window.location.search || window.location.hash.split('?')[1] || '';
        const params = new URLSearchParams(search);
        return params.get(param);
    }
});