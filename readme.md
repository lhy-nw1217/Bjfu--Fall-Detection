# bjfu电子系统综合设计（摔倒检测-FallDetection）

## arduino开发（全部完成）

1.元件选型

2.pcb(文件暂缺)

3.开发板验证

4.传感器信息采集

5.蓝牙通信

## 安卓app开发

1.环境配置 Android studio （ed）

2.借鉴17年老版本代码，重修编译，使用新的AndroidX库，使用新的支持函数（ed）

[MSLabZJU/Fall-Detection-Android: MSL 实验室跌倒检测 Android 端 (github.com)

[[lhy-nw1217/Fall-Detection: 老人跌倒检测app (github.com)](https://github.com/lhy-nw1217/Fall-Detection)

[](https://github.com/MSLabZJU/Fall-Detection-Android)

3.全局替换旧的函数和包支持（ed）

​	重构-迁移到AndroidX-确认

​	继续修改一切xml中使用旧支持的，例如抽屉类

### 4. debug，handler闪退（ing）

​	安卓8.0以上必须手动建立进程Channel，否则报错

​	弹窗权限错误：Android: permission denied for window type 2038

​	xml加入<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

```java
 alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
```

 给新的权限TYPE_APPLICATION_OVERLAY

打开app的悬浮窗权限



​	

​	

5.修改数据传入，使用外来蓝牙串口数据检测（todo）