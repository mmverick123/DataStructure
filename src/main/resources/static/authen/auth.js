document.addEventListener('DOMContentLoaded', function() {
    // DOM元素
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const dashboard = document.getElementById('dashboard');
    const showRegisterLink = document.getElementById('show-register');
    const showLoginLink = document.getElementById('show-login');
    const messageDiv = document.getElementById('message');
    
    console.log('DOM元素检查:');
    console.log('loginForm:', loginForm);
    console.log('registerForm:', registerForm);
    console.log('showRegisterLink:', showRegisterLink);
    console.log('showLoginLink:', showLoginLink);
    
    // 表单元素
    const loginFormElement = document.getElementById('login');
    const registerFormElement = document.getElementById('register');
    const logoutBtn = document.getElementById('logout-btn');
    
    console.log('表单元素检查:');
    console.log('loginFormElement:', loginFormElement);
    console.log('registerFormElement:', registerFormElement);
    console.log('logoutBtn:', logoutBtn);
    
    // 检查用户是否已登录
    checkAuthStatus();
    
    // 显示注册表单
    if (showRegisterLink) {
        showRegisterLink.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('点击了"立即注册"链接');
            loginForm.style.display = 'none';
            registerForm.style.display = 'block';
        });
    } else {
        console.error('未找到"立即注册"链接元素');
    }
    
    // 显示登录表单
    if (showLoginLink) {
        showLoginLink.addEventListener('click', function(e) {
            e.preventDefault();
            console.log('点击了"立即登录"链接');
            registerForm.style.display = 'none';
            loginForm.style.display = 'block';
        });
    } else {
        console.error('未找到"立即登录"链接元素');
    }
    
    // 登录表单提交
    if (loginFormElement) {
        loginFormElement.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const username = document.getElementById('login-username').value;
            const password = document.getElementById('login-password').value;
            
            loginUser(username, password);
        });
    }
    
    // 注册表单提交
    if (registerFormElement) {
        registerFormElement.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const username = document.getElementById('register-username').value;
            const email = document.getElementById('register-email').value;
            const password = document.getElementById('register-password').value;
            const confirmPassword = document.getElementById('register-confirm-password').value;
            
            if (password !== confirmPassword) {
                showMessage('两次输入的密码不一致', 'error');
                return;
            }
            
            registerUser(username, email, password);
        });
    }
    
    // 退出登录
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function() {
            clearAuthData();
            showAuthForms();
            showMessage('您已成功退出登录', 'success');
        });
    }
    
    // 登录用户
    function loginUser(username, password) {
        fetch('http://localhost:8081/api/auth/signin', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                password: password
            })
        })
        .then(response => {
           if (!response.ok) {
                return response.json().then(err => { 
                    throw new Error(err.message || '登录失败'); 
                });
            }
            
            return response.json();
        })
        .then(data => {
            console.log('登录响应数据:', data);
            
            if (data.accessToken) {
                // 保存token和用户信息
                localStorage.setItem('token', data.accessToken);
                localStorage.setItem('user', JSON.stringify({
                    id: data.id,
                    username: data.username,
                    email: data.email
                }));
                showMessage('登录成功', 'success');
                console.log('准备重定向到首页...');
                
                // 给消息显示一些时间后再重定向
                setTimeout(() => {
                    window.location.href = '../index.html';
                }, 1000);
            } else {
                // 如果没有token字段
                showMessage('登录成功但未收到有效的token', 'error');
                console.error('登录响应中缺少token:', data);
            }
        })
        .catch(error => {
            showMessage(error.message || '登录失败，请检查用户名和密码', 'error');
            console.error('登录错误:', error);
        });
    }
    
    // 注册用户
    function registerUser(username, email, password) {
        fetch('http://localhost:8081/api/auth/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                username: username,
                email: email,
                password: password
            })
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => { 
                    throw new Error(err.message || '注册失败'); 
                });
            }
            return response.json();
        })
        .then(data => {
            showMessage(data.message || '注册成功，请登录', 'success');
            registerForm.style.display = 'none';
            loginForm.style.display = 'block';
            document.getElementById('login-username').value = username;
        })
        .catch(error => {
            showMessage(error.message || '注册失败', 'error');
            console.error('注册错误:', error);
        });
    }
    
    // 检查认证状态
    function checkAuthStatus() {
        const token = localStorage.getItem('token');
        if (token) {
            // 假设有一个获取当前用户信息的接口
            fetch('http://localhost:8081/api/user/me', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('验证失败');
            })
            .then(userData => {
                localStorage.setItem('user', JSON.stringify(userData));
                showDashboard();
            })
            .catch(() => {
                clearAuthData();
                showAuthForms();
            });
        } else {
            showAuthForms();
        }
    }
    
    // 显示认证表单
    function showAuthForms() {
        if (loginForm) loginForm.style.display = 'block';
        if (registerForm) registerForm.style.display = 'none';
        if (dashboard) dashboard.style.display = 'none';
    }
    
    // 显示仪表盘
    function showDashboard() {
        if (!dashboard) {
            console.warn('dashboard元素不存在，无法显示仪表盘');
            return;
        }
        
        if (loginForm) loginForm.style.display = 'none';
        if (registerForm) registerForm.style.display = 'none';
        dashboard.style.display = 'block';
        
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        
        const welcomeUsername = document.getElementById('welcome-username');
        const userId = document.getElementById('user-id');
        const userEmail = document.getElementById('user-email');
        
        if (welcomeUsername) welcomeUsername.textContent = user.username || '用户';
        if (userId) userId.textContent = user.id || '';
        if (userEmail) userEmail.textContent = user.email || '';
    }
    
    // 清除认证数据
    function clearAuthData() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    }
    
    // 显示消息
    function showMessage(message, type) {
        messageDiv.textContent = message;
        messageDiv.className = type === 'success' ? 'message-success' : 'message-error';
        messageDiv.classList.add('message-visible');
        
        setTimeout(() => {
            messageDiv.classList.remove('message-visible');
        }, 3000);
    }
    
    // 示例：发送需要认证的请求
    function fetchProtectedData() {
        const token = localStorage.getItem('token');
        
        if (!token) {
            showMessage('请先登录', 'error');
            return;
        }
        
        fetch('http://localhost:8081/api/protected/data', {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    clearAuthData();
                    showAuthForms();
                    showMessage('会话已过期，请重新登录', 'error');
                }
                throw new Error('请求失败');
            }
            return response.json();
        })
        .then(data => {
            console.log('受保护的数据:', data);
        })
        .catch(error => {
            console.error('请求错误:', error);
        });
    }
});