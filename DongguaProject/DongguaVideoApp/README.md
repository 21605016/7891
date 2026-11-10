# 冬瓜端口视频版（重制工程）

## 已实现的启动与播放逻辑
- 首次启动时请求 Android「显示在其他应用上层/悬浮窗」权限。
- 用户开启权限并返回 APP 后，进入全屏播放。
- MP4 已内置于 `app/src/main/res/raw/loop_video.mp4`，不需要网络。
- 使用 Android VideoView 播放，MediaPlayer 开启循环。
- 首次播放前询问是否将媒体音量调至最大；只有用户主动选择后才调高。之后仍可使用音量键自由调节。
- 不创建悬浮窗、不屏蔽系统导航、不阻止退出或卸载。

## 构建 APK
使用 Android Studio 打开本目录，安装 Android SDK 35，并使用 Android Gradle Plugin 8.6.1 完成 Gradle 同步，然后选择 Build > Build APK(s)。

当前执行环境没有 Android SDK/Gradle 构建工具，因此本次交付为源码工程 ZIP，尚未编译出可安装 APK。

## 复刻范围
原 APK 的 Lua 业务代码经过 ALuaDefender 加固，当前只能根据已提取资源与截图逐步还原可见界面；尚不能保证完整复制原软件全部业务功能。
