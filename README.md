# 退休倒计时 · Retirement Countdown

一个只在本地运行的 Android 退休倒计时小组件。设定生日和退休身份，自动按 2025 年起施行的渐进式延迟退休政策算出退休日；桌面或负一屏卡片每天 00:01 自动刷新。

- 当前版本：**v0.1**
- 安装包：在 [Releases](https://github.com/hzxs163/retirement-countdown/releases) 下载显示名为 `倒计时APP v0.1.apk` 的资产（实际文件名 `RetirementCountdown-v0.1.apk`，原因见下方）
- 包名：`com.hzxs163.retirement`
- 最低系统：Android 8.0（API 26）
- 没有 `INTERNET` 权限；生日和设置只保存在本机

## 能做什么

- 4×2 桌面小组件，可横向/纵向缩放，点击卡片打开设置
- 设定出生日期，自动计算法定退休日、退休年龄、延迟月数和剩余天数
- 三种政策身份 + 自定义退休年龄
- 每天 00:01 自动刷新；开机、修改时间/时区、应用更新后补刷新
- 应用内一键请求添加小组件；负一屏/主屏两种添加路径都有说明
- 自动刷新状态可见：精确闹钟、省电限制、下次刷新时间

## 退休年龄口径

依据 2024 年 9 月全国人大常委会通过的《关于实施渐进式延迟法定退休年龄的决定》，自 2025 年 1 月 1 日起实施：

| 身份 | 原法定年龄 | 延迟节奏 | 封顶 | 过渡起点 |
| --- | --- | --- | --- | --- |
| 男职工 | 60 岁 | 每 4 个出生月 +1 个月 | 63 岁（+36 个月） | 1965 年 1 月 |
| 女职工（原 55 岁档） | 55 岁 | 每 4 个出生月 +1 个月 | 58 岁（+36 个月） | 1970 年 1 月 |
| 女职工（原 50 岁档） | 50 岁 | 每 2 个出生月 +1 个月 | 55 岁（+60 个月） | 1975 年 1 月 |

计算方式：

```
延迟月数 = min(封顶月数, (出生月 - 过渡起点月) / 步长 + 1)
退休日 = 生日 + 原法定年龄 + 延迟月数
```

- 1965 年 1—4 月出生的男职工：+1 个月；1976 年 9—12 月出生：+36 个月到 63 岁
- 1981 年 9—12 月出生的女职工（原 55 岁）：+36 个月到 58 岁
- 1984 年 11—12 月出生的女职工（原 50 岁）：+60 个月到 55 岁
- 出生在过渡起点之前的，不延迟
- 闰日和月末按 `java.time` 的月末收缩规则处理

**不包含**：弹性提前/延后退休、最低缴费年限、特殊工种、病退、跨省参保认定等。结果仅供参考，最终以社保经办机构核定为准。

边界用例见 `app/src/test/java/com/hzxs163/retirement/RetirementCalculatorTest.java`，另有一份独立的 Python 镜像校验：

```bash
py -3 tools/policy_check.py
```

## 每天自动更新

- 小组件通过 `AlarmManager` 安排在次日 00:01 刷新；有精确闹钟权限时用 `setExactAndAllowWhileIdle`，否则降级为 `setAndAllowWhileIdle`
- 收到 `BOOT_COMPLETED`、`TIME_SET`、`TIMEZONE_CHANGED`、`MY_PACKAGE_REPLACED` 等系统广播时补刷新并重排下一次
- Android 12 及以上需要在应用内点“精确闹钟 → 未授权”并允许“闹钟和提醒”
- 小米/红米还需要：设置 → 应用设置 → 应用管理 → 退休倒计时 → 允许“自启动”，省电策略设为“无限制”

## 添加到负一屏（小米 / 红米）

1. 滑到最左侧的负一屏，点右上角“+”，进入小部件中心
2. 搜索“退休倒计时”；如果推荐里没有，进入“全部应用 / 安卓小部件”分类
3. 选中 4×2 卡片 → 添加到负一屏

**如果负一屏里找不到它**：部分 HyperOS / MIUI 版本只允许系统卡片放负一屏，不开放第三方“安卓小部件”。这不是 App 权限问题，第三方应用无法绕过。可以改放主屏：

> 长按桌面空白处 → 添加小部件 → 全部应用 / 安卓小部件 → 退休倒计时

## 构建与发布

仓库不包含 `gradle-wrapper.jar`，云构建由 GitHub Actions 提供 Gradle 8.11.1：

- JDK 17（Temurin）
- AGP 8.7.3，compileSdk 35，targetSdk 34，minSdk 26
- `testDebugUnitTest` + `assembleRelease`
- 推送到 `main`：构建并上传 APK artifact（不发布）
- 推送 `v*` 标签：构建并发布 Release
  - 发布页显示名：`倒计时APP v<版本>.apk`
  - 实际下载文件名：`RetirementCountdown-v<版本>.apk`

本地构建需要 JDK 17 和 Gradle 8.11.1：

```bash
gradle testDebugUnitTest assembleRelease
```

## 为什么下载文件名不是中文

GitHub Releases 会在服务端清洗资产名：中文字符被移除、空格变成点，即使通过 API 显式传中文名也会被清洗（实测返回 `already_exists`）。这是 GitHub 的平台限制，App 和 workflow 都无法绕过。

因此 v0.1 使用：

- 发布页显示名（asset label）：`倒计时APP v0.1.apk`
- 实际下载文件名：`RetirementCountdown-v0.1.apk`

如果必须让“下载到本机的文件名”也完全是中文，需要另加一个 GitHub Pages 镜像；当前没有启用。

## 签名说明

仓库内的 `keystore/release.p12` 是固定自签名密钥，密码写在 `app/build.gradle`，用于让 v0.2、v0.3 等后续版本能直接覆盖安装。

这是**公开仓库里的演示签名密钥**，只适合个人侧载使用；如果要正式分发，请换成自己的私有密钥并放到 GitHub Actions Secrets。不要删除这个密钥库，否则已安装的版本无法直接升级，需要先卸载。

## 隐私

- 无 `INTERNET` 权限，没有统计、广告或远程配置
- `allowBackup=false`，数据不进入系统云备份
- 生日数据存放在应用私有 `SharedPreferences`
