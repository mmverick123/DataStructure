// server.js
const jsonServer = require('json-server');
const path = require('path');

const server = jsonServer.create();
const router = jsonServer.router(path.join(__dirname, 'mock/db.json'));
const middlewares = jsonServer.defaults();
const routes = require(path.join(__dirname, 'mock/routes.json'));
const rewriter = jsonServer.rewriter(routes);

server.use(middlewares);
server.use(rewriter);       // ⬅️ 应用自定义路由
server.use(router);

const PORT = 8081;
server.listen(PORT, () => {
  console.log(`JSON Server is running at http://localhost:${PORT}`);
});
