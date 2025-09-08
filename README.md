# InterviewLanguageAI

面向「程序员面试训练」的 AI 对话与语音交互服务，内置简洁前端页面，支持：
- 登录鉴权（JWT）
- 文本聊天（REST / WebSocket）
- 语音对话（上传音频 → 识别 → AI 回复 → 语音合成）
- 面试智能体（可选方向/水平，驱动问答流程）
- MyBatis + MySQL 持久化聊天记录

参考项目：
- （抹茶聊天室）https://github.com/zongzibinbin/MallChat  
- （小智后端服务 xiaozhi-esp32-server-java）https://github.com/joey-zhou/xiaozhi-esp32-server-java

## 1. 运行环境
- JDK 8
- Maven 3.6+
- MySQL 8.x（默认本地 3306）
- 可访问外网（调用 OpenAI）

## 2. 快速开始
1) 配置 `src/main/resources/application.yml`
```yaml
server:
  port: 16666

netty:
  websocket:
    port: 8082
    path: /ws/xiaozhi/v1/

ai:
  provider: openai
  apiKey: <你的OpenAIKey>
  model: gpt-3.5-turbo

security:
  jwt:
    secret: change_this_secret_in_prod_please_32bytes_min
    expirationSeconds: 86400

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/interview_ai?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis:
  mapper-locations: classpath*:mapper/**/*.xml
  type-aliases-package: com.interview.chat
  configuration:
    map-underscore-to-camel-case: true

speech:
  provider: xunfei
  appId: your_app_id_here
  apiKey: your_api_key_here
  apiSecret: your_api_secret_here
  voskModelPath: C:/models/vosk-model-small-cn-0.22
```

2) 初始化数据库
```sql
CREATE DATABASE IF NOT EXISTS interview_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE interview_ai;

CREATE TABLE IF NOT EXISTS chat_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100),
  channel VARCHAR(32),             -- rest | ws | voice | agent
  session_id VARCHAR(64),
  user_message LONGTEXT,
  ai_message LONGTEXT,
  created_at TIMESTAMP NULL
);
```

3) 启动
```bash
mvn spring-boot:run
```
打开 `http://localhost:16666/` 访问页面。

默认内置用户：`admin / admin123`（可在前端注册新用户）。

## 3. 前端页面（内置静态，无需构建）
- 路径：`src/main/resources/static/index.html`，样式/脚本在 `static/assets/` 下。
- 功能：登录 / 文本聊天（REST/WS）/ 语音对话（REST）/ 面试智能体。
- 浏览器录音：16kHz 单声道 WAV，上传到 `/api/voice/dialog`。

## 4. API 概览
鉴权：除静态资源与 `/api/auth/**` 外，其余接口需 `Authorization: Bearer <token>`。

### 4.1 鉴权
- 注册：`POST /api/auth/register`  body: `{username,password}`
- 登录：`POST /api/auth/login`     resp: `{token}`

### 4.2 文本聊天（REST）
- `POST /api/chat/text`  body: `{message}`  resp: `{answer}`

### 4.3 语音对话（REST）
- `POST /api/voice/dialog`  Content-Type: `application/octet-stream`  body: WAV/PCM 字节
- resp: `{ transcript, answer, audioWavBase64 }`

### 4.4 WebSocket
- 地址：`ws://localhost:8082/ws/xiaozhi/v1/?device_id=browser&token=<JWT>`
- 文本帧：由 `TextWebSocketFrameHandler` 处理，返回 AI 文本
- 二进制帧（音频）：由 `BinaryWebSocketFrameHandler` 处理，返回：
  - 文本帧（JSON，含 transcript/answer）
  - 二进制帧（WAV，AI 的 TTS 回复）

### 4.5 面试智能体（Agent）
- 开始会话：`POST /api/agent/start`  body: `{role, level}`  resp: `{sessionId}`
- 发送消息：`POST /api/agent/message` body: `{sessionId, message}` resp: `{answer}`
- 人设提示由 `PromptBuilder` 生成，可按方向/水平调整问法深度与追问策略。

## 5. 代码结构
- `com.interview.security`：JWT、拦截器与安全配置
- `com.interview.ai`：AI 接入（OpenAI via OkHttp）
- `com.interview.chat`：REST 文本聊天与 ChatLog 持久化（MyBatis）
- `com.interview.voice`：Vosk STT、EdgeTTS TTS 与语音对话
- `com.interview.webstocket`：Netty WebSocket 服务与文本/二进制处理器
- `com.interview.agent`：面试智能体（会话、提示构造、业务编排、接口）

## 6. 可配置项与注意事项
- OpenAI：`ai.apiKey` 与 `ai.model` 必填/可调
- JWT：`security.jwt.secret` 请替换为生产安全值
- Vosk：`speech.voskModelPath` 指向本地模型目录（示例为中文小模型）
- MySQL：如连接信息不同，请调整 `spring.datasource.*`

## 7. 常见问题
- 连接 WS 失败：检查 `token`、`device_id`、端口 8082、防火墙
- STT 不准确：确保音频采样率 16kHz、单声道；或更换更大/更准的 Vosk 模型
- OpenAI 报错：检查 API Key、代理/网络连通性

## 8. 许可证
本项目遵循仓库内 `LICENSE` 文件所述许可证。
