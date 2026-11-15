"""Дополнительные темы анимаций. Запуск: python3 tools/gen_more.py"""
import math
import random
import sys

from PIL import ImageDraw

import gen_frames as g

K, W, TAU, DARK = g.K, g.W, g.TAU, g.DARK
canvas, finish = g.canvas, g.finish


def star_points(cx, cy, r):
    pts = []
    for i in range(8):
        rad = r if i % 2 == 0 else r * 0.28
        a = math.pi / 4 * i - math.pi / 2
        pts.append((cx + rad * math.cos(a), cy + rad * math.sin(a)))
    return pts


def make_sparkles():
    rng = random.Random(11)
    parts = [(rng.uniform(25, W - 25), rng.uniform(0, W), rng.uniform(5, 11) * K,
              rng.random(), rng.uniform(6, 16) * K) for _ in range(16)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for x0, y0, r, ph, amp in parts:
            x = x0 + amp * math.sin(TAU * (t + ph))
            y = (y0 - W * t) % W
            s = r * (0.55 + 0.45 * math.sin(TAU * (2 * t + ph)))
            d.polygon(star_points(x, y, s), fill=(235, 240, 255, 225))
        return finish(img)
    return frame


def make_stars():
    rng = random.Random(12)
    parts = [(rng.uniform(15, W - 15), rng.uniform(15, W - 15), rng.uniform(1.2, 2.8) * K,
              rng.random(), rng.choice([1, 2])) for _ in range(26)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for x, y, r, ph, k in parts:
            a = int(70 + 170 * (0.5 + 0.5 * math.sin(TAU * (k * t + ph))))
            d.ellipse([x - r, y - r, x + r, y + r], fill=(255, 255, 255, a))
        if t < 0.3:
            u = t / 0.3
            fade = math.sin(math.pi * u)
            x1, y1, x2, y2 = 0.12 * W, 0.08 * W, 0.78 * W, 0.58 * W
            hx, hy = x1 + (x2 - x1) * u, y1 + (y2 - y1) * u
            dx, dy = (x2 - x1), (y2 - y1)
            ln = math.hypot(dx, dy)
            for i in range(10):
                f0, f1 = i / 10, (i + 1) / 10
                p0 = (hx - dx / ln * 90 * K * f0 / 2, hy - dy / ln * 90 * K * f0 / 2)
                p1 = (hx - dx / ln * 90 * K * f1 / 2, hy - dy / ln * 90 * K * f1 / 2)
                d.line([p0, p1], fill=(255, 255, 255, int(230 * fade * (1 - f0))),
                       width=max(2, int(3 * K * (1 - f0))))
        return finish(img)
    return frame


def make_glow(seed, count, color, rmax):
    rng = random.Random(seed)
    parts = [(rng.uniform(40, W - 40), rng.uniform(40, W - 40), rng.uniform(15, 55) * K,
              rng.uniform(15, 55) * K, rng.choice([1, 2]), rng.choice([1, 2]),
              rng.random(), rng.random(), rng.random()) for _ in range(count)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for x0, y0, ax, ay, kx, ky, px, py, pp in parts:
            x = x0 + ax * math.sin(TAU * (kx * t + px))
            y = y0 + ay * math.sin(TAU * (ky * t + py))
            b = 0.5 + 0.5 * math.sin(TAU * (t + pp))
            for r, a in ((rmax * K, 26), (rmax * 0.6 * K, 70), (rmax * 0.3 * K, 190)):
                d.ellipse([x - r, y - r, x + r, y + r], fill=color + (int(a * (0.3 + 0.7 * b)),))
        return finish(img)
    return frame


def make_water():
    rng = random.Random(13)
    bubbles = [(rng.uniform(20, W - 20), rng.uniform(0, W), rng.uniform(3, 9) * K,
                rng.random(), rng.uniform(4, 12) * K) for _ in range(15)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for x0, y0, r, ph, amp in bubbles:
            x = x0 + amp * math.sin(TAU * (2 * t + ph))
            y = (y0 - W * t) % W
            d.ellipse([x - r, y - r, x + r, y + r], outline=(255, 255, 255, 190), width=max(2, K))
            d.ellipse([x - r * 0.45, y - r * 0.5, x - r * 0.1, y - r * 0.15], fill=(255, 255, 255, 190))
        fx = ((t + 0.1) % 1.0) * W
        fy = 0.60 * W + 10 * K * math.sin(TAU * 2 * t)
        wag = math.sin(TAU * 6 * t) * 7 * K
        for shift in (0, -W):
            x = fx + shift
            d.ellipse([x - 24 * K, fy - 11 * K, x + 24 * K, fy + 11 * K], fill=DARK + (150,))
            d.polygon([(x - 20 * K, fy), (x - 38 * K, fy - 13 * K + wag), (x - 38 * K, fy + 13 * K + wag)],
                      fill=DARK + (150,))
        for amp, lam, direction, alpha in ((7 * K, W / 2, 1, 55), (5 * K, W / 3, -1, 45)):
            pts = [(x, 0.88 * W + amp * math.sin(TAU * (x / lam - direction * t)))
                   for x in range(0, W + 1, 12)]
            d.polygon(pts + [(W, W), (0, W)], fill=(255, 255, 255, alpha))
        return finish(img)
    return frame


def make_confetti():
    rng = random.Random(14)
    colors = [(255, 99, 132), (255, 205, 86), (75, 192, 192), (54, 162, 235), (153, 102, 255), (255, 159, 64)]
    parts = [(rng.uniform(20, W - 20), rng.uniform(0, W), rng.uniform(5, 9) * K, rng.uniform(2.5, 4) * K,
              rng.random(), rng.choice([-2, -1, 1, 2]), rng.uniform(6, 16) * K, rng.choice(colors))
             for _ in range(32)]

    def frame(t):
        img = canvas()
        d = ImageDraw.Draw(img)
        for x0, y0, w, h, ph, spin, amp, col in parts:
            x = x0 + amp * math.sin(TAU * (t + ph))
            y = (y0 + W * t) % W
            a = TAU * (spin * t + ph)
            c, s = math.cos(a), math.sin(a)
            pts = [(x + px * c - py * s, y + px * s + py * c)
                   for px, py in ((-w, -h), (w, -h), (w, h), (-w, h))]
            d.polygon(pts, fill=col + (235,))
        return finish(img)
    return frame


g.THEMES.update({
    "sparkles": make_sparkles,
    "stars": make_stars,
    "drift": lambda: make_glow(7, 14, (200, 255, 210), 7),
    "water": make_water,
    "confetti": make_confetti,
})

if __name__ == "__main__":
    g.main(sys.argv[1] if len(sys.argv) > 1 else "app/src/main")
