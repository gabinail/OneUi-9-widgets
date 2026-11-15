"""Тема монет. Запуск: python3 tools/gen_extra.py"""
import math
import random
import sys

from PIL import ImageDraw

import gen_more as m

g = m.g
K, W, TAU = g.K, g.W, g.TAU


def make_coins():
    rng = random.Random(21)
    parts = [(rng.uniform(25, W - 25), rng.uniform(0, W), rng.uniform(9, 15) * K,
              rng.random(), rng.choice([-2, -1, 1, 2]), rng.uniform(6, 16) * K) for _ in range(12)]

    def frame(t):
        img = g.canvas()
        d = ImageDraw.Draw(img)
        for x0, y0, r, ph, spin, amp in parts:
            x = x0 + amp * math.sin(TAU * (t + ph))
            y = (y0 + W * t) % W
            w = max(0.18 * r, r * abs(math.cos(TAU * (spin * t + ph))))
            d.ellipse([x - w, y - r, x + w, y + r], fill=(255, 204, 72, 235),
                      outline=(190, 130, 20, 235), width=max(2, K))
            if w > 0.5 * r:
                d.ellipse([x - w * 0.6, y - r * 0.6, x + w * 0.6, y + r * 0.6],
                          outline=(190, 130, 20, 200), width=K)
        return g.finish(img)
    return frame


g.THEMES["coins"] = make_coins

if __name__ == "__main__":
    g.main(sys.argv[1] if len(sys.argv) > 1 else "app/src/main")
