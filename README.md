# cossintin-Java

sin / cos / tan 弧度制动态演示

## 功能

- **左侧单位圆**：动点旋转，红/蓝线段分别是 cosθ（x 投影）和 sinθ（y 投影），橙色延长线交 x=1 切线处高度即 tanθ
- **右侧曲线图**：弧度刻度横轴，π/2、3π/2 处虚线渐近线，tan 越界自动断开，粗线随动画扫出
- **顶部**实时显示 θ（rad/°）及三个函数值
- **底部**控制：速度滑块（0.2–6 rad/s）、暂停/继续、重置

## 文件说明

- `TrigDynamicDemo.java` — Swing GUI 版本
- `TrigDynamicDemo.html` — HTML Canvas 版本（浏览器直接打开）
- `TrigDynamicDemo*.class` — Java 编译产物

## 运行

### Java 版本

```bash
javac TrigDynamicDemo.java
java TrigDynamicDemo
```

### HTML 版本

直接在浏览器中打开 `TrigDynamicDemo.html`

## 预览

```
θ = 1.047 rad ( 60.0°)   sinθ = +0.866   cosθ = +0.500   tanθ = +1.732
```
