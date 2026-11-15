"""Генерирует покадровые анимации-силуэты и layout-файлы с ViewFlipper.
Запуск: python3 tools/gen_frames.py [корень, по умолчанию app/src/main]"""
import math
import os
import random
import sys

from PIL import Image, ImageDraw

S = 360
OUT = 224
K = 2
W = S * K
N = 72
INTERVAL = 42
DARK = (8, 10, 16)
TAU = 2 * math.pi


def canvas():
    return Image.new("RGBA", (W, W), (0, 0, 0, 0))


def finish(img):
    return img.resize((S, S), Image.LANCZOS)


def leaf_points(cx, cy, length, width, ang):
    pts = []
    steps = 10
    for i in range(steps + 1):
        x = -length + 2 * length * i / steps
        y = -width * math.sin(math.pi * i / steps)
        pts.append((x, y))
    for i in range(steps, -1, -1):
        x = -length + 2 * length * i / steps
        y = width * math.sin(math.pi * i / steps)
        pts.append((x, y))
    c, s = math.cos(ang), math.sin(ang)
    return [(cx + x * c - y * s, cy + x * s + y * c) for x, y in pts]


def make_snow():
    rng = random.Random(1)
    period, copies = W, 1
    parts = [(rng.uniform(20, W - 20), rng.uniform(0, period), rng.uniform(2.5, 6) * K,
              rng.random(), rng.uniform(4, 14) * K) for _ in range(28)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for x0, y0, r, ph, amp in parts:
            x = x0 + amp * math.sin(TAU * (t + ph))
            for k in range(copies):
                y = (y0 + period * t) % period + k * period
                d.ellipse([x - r, y - r, x + r, y + r], fill=(255, 255, 255, 215))
        return finish(img)
    return frame


def make_rain():
    rng = random.Random(2)
    period, copies = W // 3, 3
    parts = [(rng.uniform(10, W), rng.uniform(0, period)) for _ in range(18)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for x, y0 in parts:
            for k in range(copies):
                y = (y0 + period * t) % period + k * period
                d.line([(x, y), (x - 6 * K, y + 18 * K)], fill=(205, 225, 255, 150), width=2 * K)
        return finish(img)
    return frame


def make_fall(seed, count, size, wid, color):
    rng = random.Random(seed)
    period, copies = W, 1
    parts = [(rng.uniform(30, W - 30), rng.uniform(0, period), rng.uniform(*size) * K,
              rng.random(), rng.choice([-1, 1]), rng.uniform(8, 20) * K) for _ in range(count)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for x0, y0, ln, ph, direction, amp in parts:
            x = x0 + amp * math.sin(TAU * (t + ph))
            ang = TAU * (direction * t + ph)
            for k in range(copies):
                y = (y0 + period * t) % period + k * period
                d.polygon(leaf_points(x, y, ln, ln * wid, ang), fill=color)
        return finish(img)
    return frame


def make_birds():
    birds = [(0.18, 30 * K, 0.0, 0.00), (0.42, 20 * K, 0.35, 0.45), (0.30, 14 * K, 0.7, 0.75)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for ry, s, ph, off in birds:
            y = ry * W
            flap = math.sin(TAU * (2 * t + ph))
            lw = max(3, int(s * 0.16))
            for shift in (0, -W):
                x = ((off + t) % 1.0) * W + shift
                ty = y - s * 0.55 * flap
                jy = y - s * 0.3 * flap - s * 0.15
                pts = [(x - s, ty), (x - s * 0.5, jy), (x, y), (x + s * 0.5, jy), (x + s, ty)]
                d.line(pts, fill=DARK + (200,), width=lw, joint="curve")
                for px, py in (pts[0], pts[2], pts[4]):
                    d.ellipse([px - lw / 2, py - lw / 2, px + lw / 2, py + lw / 2], fill=DARK + (200,))
        return finish(img)
    return frame


def make_fireflies():
    rng = random.Random(5)
    parts = [(rng.uniform(50, W - 50), rng.uniform(50, W - 50), rng.uniform(15, 45) * K,
              rng.uniform(15, 45) * K, rng.choice([1, 2]), rng.choice([1, 2]),
              rng.random(), rng.random(), rng.random()) for _ in range(10)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for x0, y0, ax, ay, kx, ky, px, py, pp in parts:
            x = x0 + ax * math.sin(TAU * (kx * t + px))
            y = y0 + ay * math.sin(TAU * (ky * t + py))
            b = 0.5 + 0.5 * math.sin(TAU * (t + pp))
            for r, a in ((11 * K, 28), (7 * K, 70), (3.5 * K, 230)):
                d.ellipse([x - r, y - r, x + r, y + r], fill=(255, 236, 130, int(a * (0.25 + 0.75 * b))))
        return finish(img)
    return frame


THEMES = {
    "snow": make_snow,
    "rain": make_rain,
    "leaves": lambda: make_fall(3, 13, (9, 14), 0.45, DARK + (185,)),
    "petals": lambda: make_fall(4, 16, (6, 9), 0.6, (255, 206, 222, 215)),
    "birds": make_birds,
    "fireflies": make_fireflies,
}


def main(root):
    drawable = os.path.join(root, "res", "drawable-nodpi")
    layout = os.path.join(root, "res", "layout")
    os.makedirs(drawable, exist_ok=True)
    os.makedirs(layout, exist_ok=True)
    for name, factory in THEMES.items():
        frame = factory()
        items = []
        for i in range(N):
            frame(i / N).resize((OUT, OUT), Image.LANCZOS).save(
                os.path.join(drawable, "fr_%s_%02d.png" % (name, i)), optimize=True)
            items.append(
                '    <ImageView android:layout_width="match_parent" '
                'android:layout_height="match_parent" android:scaleType="centerCrop" '
                'android:src="@drawable/fr_%s_%02d" />' % (name, i))
        xml = (
            '<?xml version="1.0" encoding="utf-8"?>\n'
            '<ViewFlipper xmlns:android="http://schemas.android.com/apk/res/android"\n'
            '    android:layout_width="match_parent"\n'
            '    android:layout_height="match_parent"\n'
            '    android:autoStart="true"\n'
            '    android:flipInterval="%d">\n%s\n</ViewFlipper>\n' % (INTERVAL, "\n".join(items)))
        with open(os.path.join(layout, "anim_%s.xml" % name), "w", encoding="utf-8") as f:
            f.write(xml)
    print("OK:", len(THEMES), "тем,", N, "кадров")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "app/src/main")
