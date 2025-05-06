document.addEventListener('DOMContentLoaded', function() {
    // DOM元素
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const dashboard = document.getElementById('dashboard');
    const showRegisterLink = document.getElementById('show-register');
    const showLoginLink = document.getElementById('show-login');
    const messageDiv = document.getElementById('message');
    
    // 表单元素
    const loginFormElement = document.getElementById('login');
    const registerFormElement = document.getElementById('register');
    const logoutBtn = document.getElementById('logout-btn');
    
    // 检查用户是否已登录
    checkAuthStatus();
    
    // 显示注册表单
    showRegisterLink.addEventListener('click', function(e) {
        e.preventDefault();
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
    });
    
    // 显示登录表单
    showLoginLink.addEventListener('click', function(e) {
        e.preventDefault();
        registerForm.style.display = 'none';
        loginForm.style.display = 'block';
    });
    
    // 登录表单提交
    loginFormElement.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const username = document.getElementById('login-username').value;
        const password = document.getElementById('login-password').value;
        
        loginUser(username, password);
    });
    
    // 注册表单提交
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
    
    // 退出登录
    logoutBtn.addEventListener('click', function() {
        clearAuthData();
        showAuthForms();
        showMessage('您已成功退出登录', 'success');
    });
    
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
            if (data.accessToken) {
                // 保存token和用户信息
                localStorage.setItem('token', data.token);
                localStorage.setItem('user', JSON.stringify({
                    id: data.id,
                    username: data.username,
                    email: data.email
                }));
                
                showDashboard();
                showMessage('登录成功', 'success');
                
                // 3秒后跳转到home.html
                setTimeout(() => {
                    window.location.href = '../index.html';
                }, 3000);
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
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        dashboard.style.display = 'none';
    }
    
    // 显示仪表盘
    function showDashboard() {
        loginForm.style.display = 'none';
        registerForm.style.display = 'none';
        dashboard.style.display = 'block';
        
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        document.getElementById('welcome-username').textContent = user.username || '用户';
        document.getElementById('user-id').textContent = user.id || '';
        document.getElementById('user-email').textContent = user.email || '';
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