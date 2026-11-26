# 知微中心 · Zhiwei Hub

知微中心是「始源创想科技IT」出品的知微系列应用统一管理中枢，用于一键检查系列应用的安装状态、运行情况与资源占用，并内置 GitHub Release 安装中心，未安装的应用可直接下载安装。

Zhiwei Hub is a unified control center for the Zhiwei app family by "始源创想科技IT", checking install status, running state and resource usage of each app, with a built-in GitHub Release install center for one-tap download & install.

## 功能特性 · Features

- **应用状态检测** · App status probe — 扫描知微系列 7 款应用的安装、版本、进程存活、前台用量（公开 API，无需 Shizuku/Root）。
- **安装中心** · Install center — 对接 GitHub Release，未安装或可升级时一键下载 APK 并调起系统安装器。
- **自定义主页背景** · Custom backdrop — 支持相册图片 / 纯色渐变 / 暗化 / 模糊，随偏好持久化。
- **QZXUI 设计语言** · QZXUI design language — 分层导航、果冻悬浮玻璃底栏、边缘光卡片、自适应大标题。

## 收录应用 · Catalog

| 应用 | 包名 | 仓库 |
| --- | --- | --- |
| 知微智能体 | `com.zhiwei.agent` | zhiwei-agent |
| 知微浏览器 | `com.java.myapplication` | zhiwei-browser |
| 知微管理器 | `com.zhiwei.filemanager` | zhiwei-manager |
| 知微QQ机器人 | `com.zhiwei.qbot` | zhiwei-qbot |
| 知微安全 | `com.zhiwei.security` | zhiwei-security |
| 知微限制 | `com.zhiwei.limiter` | zhiwei-limiter |
| 知微应用商店 | `com.zhiwei.store` | zhiwei-store |

## 构建 · Build

正式发布包由 GitHub Actions 自动构建，本机构建仅用于调试。

```bash
./gradlew assembleDebug     # 调试包
./gradlew assembleRelease   # 发布包（需 keystore/zhiwei-hub.jks）
```

## 下载 · Download

- Release 安装包见仓库 [Releases](https://github.com/mingxiozsg-star/zhiwei-hub/releases) 页面。
- 官方交流群 QQ：`1137595303`
- 联系邮箱：`ailixiya@qycxkjit.wecom.work`

## 许可 · License

© 始源创想科技IT · https://github.com/mingxiozsg-star
