import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.function.DoubleUnaryOperator;

/**
 * sin / cos / tan 弧度制动态演示
 *
 * 左侧：单位圆上动点旋转
 *   - 红色横线段 = cosθ（x 投影）
 *   - 蓝色竖线段 = sinθ（y 投影）
 *   - 橙色延长线与 x=1 处切线的交点高度 = tanθ
 * 右侧：三条曲线随 θ（弧度）同步扫出，虚线竖线为 tan 的渐近线（π/2、3π/2）
 * 顶部：实时显示 θ 的弧度值、角度值及 sinθ / cosθ / tanθ
 *
 * 编译运行：javac TrigDynamicDemo.java && java TrigDynamicDemo
 */
public class TrigDynamicDemo extends JFrame {

    static final double TWO_PI = 2 * Math.PI;
    static final Color SIN_C = new Color(25, 105, 220);   // sin - 蓝
    static final Color COS_C = new Color(205, 45, 60);    // cos - 红
    static final Color TAN_C = new Color(235, 135, 20);   // tan - 橙

    double theta = 0.0;            // 当前角度（弧度），范围 [0, 2π)
    double speed = 1.2;            // 角速度 rad/s
    boolean running = true;
    long lastNs = System.nanoTime();
    JLabel info;

    public TrigDynamicDemo() {
        super("sin / cos / tan —— 弧度动态演示");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1150, 560));
        setLayout(new BorderLayout(6, 6));

        // 顶部实时数值
        info = new JLabel(" ", SwingConstants.CENTER);
        info.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
        info.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        add(info, BorderLayout.NORTH);

        // 中部：单位圆 + 曲线图
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new CirclePanel(), new GraphPanel());
        split.setResizeWeight(0.40);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        // 底部控制条
        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        JButton playBtn = new JButton("暂停");
        playBtn.addActionListener(e -> {
            running = !running;
            playBtn.setText(running ? "暂停" : "继续");
            lastNs = System.nanoTime();
        });
        JButton resetBtn = new JButton("重置");
        resetBtn.addActionListener(e -> theta = 0.0);

        JSlider speedSlider = new JSlider(2, 60, 12);   // 实际速度 = 值/10 rad/s
        speedSlider.setMajorTickSpacing(10);
        speedSlider.setPaintTicks(true);
        speedSlider.setPreferredSize(new Dimension(220, 32));
        speedSlider.addChangeListener(e -> speed = speedSlider.getValue() / 10.0);

        ctrl.add(new JLabel("速度"));
        ctrl.add(speedSlider);
        ctrl.add(playBtn);
        ctrl.add(resetBtn);
        add(ctrl, BorderLayout.SOUTH);

        // 动画主循环（约 60fps）
        new Timer(16, e -> {
            long now = System.nanoTime();
            if (running) {
                double dt = (now - lastNs) / 1e9;
                theta += speed * dt;
                if (theta >= TWO_PI) theta -= TWO_PI;
            }
            lastNs = now;
            updateInfo();
            repaint();
        }).start();
    }

    void updateInfo() {
        double s = Math.sin(theta), c = Math.cos(theta);
        String tanStr;
        if (Math.abs(c) < 0.02) {
            tanStr = Math.tan(theta) > 0 ? "→ +∞" : "→ −∞";
        } else {
            tanStr = String.format("%+5.3f", Math.tan(theta));
        }
        info.setText(String.format(
                "θ = %5.3f rad (%5.1f°)      sinθ = %+5.3f      cosθ = %+5.3f      tanθ = %s",
                theta, Math.toDegrees(theta), s, c, tanStr));
    }

    /* ==================== 左侧：单位圆 ==================== */

    class CirclePanel extends JPanel {
        CirclePanel() { setBackground(Color.WHITE); }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            double cx = w / 2.0, cy = h / 2.0;
            double R = Math.min(w, h) / 2.0 - 46;

            // 坐标轴
            g.setColor(new Color(215, 215, 215));
            g.draw(new Line2D.Double(cx - R - 16, cy, cx + R + 16, cy));
            g.draw(new Line2D.Double(cx, cy - R - 16, cx, cy + R + 16));
            g.setColor(Color.GRAY);
            g.setFont(g.getFont().deriveFont(12f));
            g.drawString("0", (int) cx - 10, (int) cy + 15);
            g.drawString("1", (int) (cx + R) - 4, (int) cy + 15);
            g.drawString("π/2", (int) cx + 5, (int) (cy - R) - 6);

            // 单位圆
            g.setColor(new Color(120, 120, 130));
            g.setStroke(new BasicStroke(1.6f));
            g.draw(new Ellipse2D.Double(cx - R, cy - R, 2 * R, 2 * R));

            // x = 1 处的切线（tan 的几何构造位置）
            g.setColor(new Color(TAN_C.getRed(), TAN_C.getGreen(), TAN_C.getBlue(), 110));
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{5f, 5f}, 0f));
            g.draw(new Line2D.Double(cx + R, cy - R, cx + R, cy + R));

            double px = cx + R * Math.cos(theta);   // 动点屏幕坐标
            double py = cy - R * Math.sin(theta);

            // cosθ：水平投影（圆心 → 动点正下方）
            g.setStroke(new BasicStroke(2.4f));
            g.setColor(COS_C);
            g.draw(new Line2D.Double(cx, cy, px, cy));

            // sinθ：竖直投影（(px,cy) → 动点）
            g.setColor(SIN_C);
            g.draw(new Line2D.Double(px, cy, px, py));

            // tanθ：半径延长线与切线 x=1 的交点到动点的延长段
            double t = Math.tan(theta);
            boolean showTan = Math.abs(Math.cos(theta)) > 0.02 && Math.abs(t) < 3.5;
            if (showTan) {
                double ex = cx + R, ey = cy - R * t;
                g.setColor(TAN_C);
                g.draw(new Line2D.Double(px, py, ex, ey));
                g.fill(new Ellipse2D.Double(ex - 4, ey - 4, 8, 8));
            }

            // 角度弧
            g.setColor(new Color(90, 90, 90));
            g.setStroke(new BasicStroke(1.4f));
            g.draw(new Arc2D.Double(cx - 30, cy - 30, 60, 60,
                    0, -Math.toDegrees(theta), Arc2D.OPEN));

            // 半径
            g.setColor(Color.BLACK);
            g.setStroke(new BasicStroke(2.0f));
            g.draw(new Line2D.Double(cx, cy, px, py));

            // 动点及投影点
            g.fill(new Ellipse2D.Double(px - 6, py - 6, 12, 12));
            g.setColor(COS_C);
            g.fill(new Ellipse2D.Double(px - 4, cy - 4, 8, 8));
            g.setColor(SIN_C);
            g.fill(new Ellipse2D.Double(px - 4, py - 4, 8, 8));

            // 标签
            g.setFont(g.getFont().deriveFont(Font.BOLD, 13f));
            g.setColor(COS_C);
            g.drawString("cosθ = x", (int) ((cx + px) / 2) - 30, (int) cy - 8);
            g.setColor(SIN_C);
            g.drawString("sinθ = y", (int) px + 8, (int) ((cy + py) / 2) + 4);
            if (showTan) {
                double ey = cy - R * t;
                g.setColor(TAN_C);
                g.drawString("tanθ", (int) (cx + R) - 26,
                        (float) (ey + (t > 0 ? -8 : 16)));
            }

            // 图例
            g.setFont(g.getFont().deriveFont(13f));
            int ly = h - 20;
            drawLegend(g, 12, ly, SIN_C, "sinθ");
            drawLegend(g, 84, ly, COS_C, "cosθ");
            drawLegend(g, 156, ly, TAN_C, "tanθ");
        }

        private void drawLegend(Graphics2D g, int x, int y, Color c, String text) {
            g.setColor(c);
            g.fillRect(x, y - 11, 11, 11);
            g.setColor(Color.DARK_GRAY);
            g.drawString(text, x + 15, y);
        }
    }

    /* ==================== 右侧：函数曲线 ==================== */

    class GraphPanel extends JPanel {
        static final double Y_MAX = 2.4;      // 纵轴范围 [-2.4, 2.4]，tan 超出部分截断
        static final double STEP = 0.008;     // 曲线采样步长（弧度）

        GraphPanel() { setBackground(Color.WHITE); }

        double X(double th) { return 34 + (getWidth() - 14 - 34) * th / TWO_PI; }
        double Y(double v)  { return (getHeight() - 14 - 30 + 14) / 2.0 - v * (getHeight() - 14 - 30) / 2.0 / Y_MAX; }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);

            double yc = Y(0);
            g.setFont(g.getFont().deriveFont(12f));

            // 网格：横线 v = -2,-1,0,1,2
            for (int v = -2; v <= 2; v++) {
                g.setColor(v == 0 ? new Color(160, 160, 160) : new Color(228, 228, 228));
                g.setStroke(new BasicStroke(v == 0 ? 1.4f : 1f));
                g.draw(new Line2D.Double(34, Y(v), getWidth() - 14, Y(v)));
                if (v != 0) {
                    g.setColor(Color.GRAY);
                    g.drawString(String.valueOf(v), 14, (int) Y(v) + 4);
                }
            }

            // 网格：竖线 t = k·π/2；渐近线（π/2、3π/2）画虚线
            String[] marks = {"0", "π/2", "π", "3π/2", "2π"};
            for (int k = 0; k <= 4; k++) {
                boolean asym = (k == 1 || k == 3);
                g.setColor(asym ? new Color(200, 200, 200) : new Color(228, 228, 228));
                g.setStroke(asym
                        ? new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{5f, 5f}, 0f)
                        : new BasicStroke(1f));
                g.draw(new Line2D.Double(X(k * Math.PI / 2), 14, X(k * Math.PI / 2), getHeight() - 30));
                g.setColor(Color.GRAY);
                g.drawString(marks[k], (int) X(k * Math.PI / 2) - 12, getHeight() - 14);
            }
            g.drawString("θ（弧度）", getWidth() - 88, getHeight() - 14);

            // 三条完整曲线（淡色底图）
            plot(g, Math::sin, 0, TWO_PI, new Color(25, 105, 220, 60), 1.2f, false);
            plot(g, Math::cos, 0, TWO_PI, new Color(205, 45, 60, 60), 1.2f, false);

            // 已扫过的部分（粗线）
            if (theta > 0.002) {
                plot(g, Math::sin, 0, theta, SIN_C, 2.8f, false);
                plot(g, Math::cos, 0, theta, COS_C, 2.8f, false);
                plot(g, Math::tan, 0, theta, TAN_C, 2.8f, true);
            }

            // 当前 θ 的扫描线
            g.setColor(new Color(120, 120, 120, 150));
            g.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                    10f, new float[]{4f, 4f}, 0f));
            g.draw(new Line2D.Double(X(theta), 14, X(theta), getHeight() - 30));

            // 曲线上当前位置的点
            dot(g, Math.sin(theta), SIN_C);
            dot(g, Math.cos(theta), COS_C);
            double tv = Math.tan(theta);
            if (!Double.isNaN(tv) && Math.abs(tv) <= Y_MAX) dot(g, tv, TAN_C);

            // 图例（右上角）
            g.setFont(g.getFont().deriveFont(Font.BOLD, 13f));
            int lx = getWidth() - 210, ly = 26;
            g.setColor(SIN_C); g.drawString("sinθ", lx, ly);
            g.setColor(COS_C); g.drawString("cosθ", lx + 50, ly);
            g.setColor(TAN_C); g.drawString("tanθ", lx + 100, ly);
        }

        void plot(Graphics2D g, DoubleUnaryOperator fn, double from, double to,
                  Color c, float width, boolean isTan) {
            if (to <= from) return;
            g.setColor(c);
            g.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Path2D path = new Path2D.Double();
            boolean open = false;
            for (double th = from; th <= to; th += STEP) {
                double v = fn.applyAsDouble(th);
                // tan 在渐近线附近断开，其余函数越界截断
                if (Double.isNaN(v) || Double.isInfinite(v) || Math.abs(v) > Y_MAX) {
                    open = false;
                    continue;
                }
                double x = X(th), y = Y(v);
                if (!open) { path.moveTo(x, y); open = true; }
                else path.lineTo(x, y);
            }
            g.draw(path);
        }

        void dot(Graphics2D g, double v, Color c) {
            g.setColor(c);
            g.fill(new Ellipse2D.Double(X(theta) - 5, Y(v) - 5, 10, 10));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TrigDynamicDemo f = new TrigDynamicDemo();
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
