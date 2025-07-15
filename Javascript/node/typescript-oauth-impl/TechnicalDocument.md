# OAuth + JWT 认证系统技术文档

## 一、概述
本系统包含一个基于 TypeScript 的标准 OAuth + JWT 认证服务器，以及一个依赖该认证服务器的服务，还新增了一个基于用户 JWT 登录令牌认证的 WebSocket 聊天室。认证服务器负责用户身份验证并颁发 JWT 令牌，依赖服务通过获取令牌访问受保护的资源，WebSocket 聊天室则利用 JWT 令牌进行用户认证。

## 二、系统架构
### 2.1 认证服务器
- **功能**：处理用户登录请求，验证用户身份，生成并颁发 JWT 令牌。
- **技术栈**：Express.js、JWT、UUID。

### 2.2 依赖服务
- **功能**：模拟用户登录，获取令牌，并使用令牌访问认证服务器的受保护接口。
- **技术栈**：Express.js、Axios。

### 2.3 WebSocket 聊天室
- **功能**：基于用户 JWT 登录令牌进行认证，实现用户间的实时聊天。
- **技术栈**：Express.js、WebSocket、JWT。

## 三、认证流程
### 3.1 通用认证流程
1. 用户向认证服务器发送登录请求，包含用户名和密码。
2. 认证服务器验证用户信息，若验证通过则生成 JWT 令牌并返回给用户。
3. 依赖服务或 WebSocket 聊天室使用获取到的令牌访问受保护的接口或建立连接。
4. 认证服务器或 WebSocket 服务验证令牌的有效性，若有效则返回受保护的资源或允许建立连接。

### 3.2 WebSocket 聊天室认证流程
1. 用户通过认证服务器获取 JWT 令牌。
2. 用户在连接 WebSocket 聊天室时，将 JWT 令牌作为查询参数传递。
3. WebSocket 服务验证令牌的有效性，若有效则允许用户连接，否则关闭连接。

## 四、接口设计
### 4.1 认证服务器接口
#### 4.1.1 登录接口
- **URL**：`/login`
- **方法**：`POST`
- **请求参数**：
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- **响应参数**：
  ```json
  {
    "token": "string"
  }
  ```

#### 4.1.2 受保护接口
- **URL**：`/protected`
- **方法**：`GET`
- **请求头**：`Authorization: Bearer <token>`
- **响应参数**：
  ```json
  {
    "message": "string",
    "user": {
      "userId": "string",
      "username": "string"
    }
  }
  ```

### 4.2 WebSocket 聊天室接口
- **URL**：`ws://localhost:3002?token=<token>`
- **认证方式**：通过查询参数传递 JWT 令牌。
- **消息格式**：纯文本消息，服务器会将消息广播给所有连接的客户端，并添加发送者的用户名前缀。

## 五、环境配置
### 5.1 依赖安装
运行以下命令安装项目依赖：
```bash
npm install express body-parser jsonwebtoken uuid axios ws
```

### 5.2 启动服务
#### 5.2.1 启动认证服务器
```bash
npx ts-node auth-server.ts
```

#### 5.2.2 启动依赖服务
```bash
npx ts-node dependent-service.ts
```

#### 5.2.3 启动 WebSocket 聊天室
```bash
npx ts-node websocket-chat-room.ts
```

## 六、安全注意事项
- 生产环境中应使用更复杂的密钥替换 `your-secret-key`。
- 妥善保管用户密码，避免明文存储。
- 设置合理的令牌过期时间。
- WebSocket 连接仅允许携带有效 JWT 令牌的用户访问。