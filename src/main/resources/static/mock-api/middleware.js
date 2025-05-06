/*const fs = require('fs');
const path = require('path');

const dbFile = path.join(__dirname, 'db.json');
const getUsers = () => JSON.parse(fs.readFileSync(dbFile, 'utf-8')).users;
const saveUsers = (users) => {
  const db = JSON.parse(fs.readFileSync(dbFile, 'utf-8'));
  db.users = users;
  fs.writeFileSync(dbFile, JSON.stringify(db, null, 2), 'utf-8');
};

module.exports = (req, res, next) => {
  if (req.method === 'POST' && req.path === '/signup') {
    const { username, email, password } = req.body;
    const users = getUsers();

    const existingUser = users.find(u => u.username === username || u.email === email);
    if (existingUser) {
      return res.status(400).json({ message: '用户名或邮箱已存在' });
    }

    const newUser = {
      id: users.length ? Math.max(...users.map(u => u.id)) + 1 : 1,
      username,
      email,
      password
    };

    users.push(newUser);
    saveUsers(users);

    return res.status(201).json({ message: '用户注册成功!' });
  }

  if (req.method === 'POST' && req.path === '/signin') {
    const { username, password } = req.body;
    const users = getUsers();

    const user = users.find(u => u.username === username && u.password === password);
    if (!user) {
      return res.status(401).json({ message: '用户名或密码错误' });
    }

    return res.status(200).json({
      accessToken: 'fake-jwt-token',
      tokenType: 'Bearer',
      id: user.id,
      username: user.username,
      email: user.email
    });
  }

  next();
};
*/
const fs = require('fs');
const path = require('path');

module.exports = (req, res, next) => {
  if (req.method === 'POST' && req.path === '/api/diaries') {
    // 忽略请求体内容，直接返回全部 diaries
    const dbPath = path.join(__dirname, 'db.json');
    try {
      const db = JSON.parse(fs.readFileSync(dbPath, 'utf8'));
      const diaries = db.diaries || [];

      res.setHeader('Content-Type', 'application/json');
      res.statusCode = 200;
      res.end(JSON.stringify(diaries));
    } catch (err) {
      res.statusCode = 500;
      res.end(JSON.stringify({ error: '读取数据库失败' }));
    }
  } else {
    next(); // 继续执行 json-server 默认处理
  }
};
