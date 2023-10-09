from sympy import *
from scipy import integrate

a = 5
b = 7


def func(x):
    return exp(x) * cos(x)


def trapezia(h, N):
    summ = 0
    for i in range(N):
        summ += (func(a + i * h) + func(a + (i + 1) * h)) * h / 2
    return summ


def simpson(h, N):
    summ = 0
    for i in range(int(N / 2)):
        summ += (func(a + 2 * i * h) + 4 * func(a + (2 * i + 1) * h) + func(a + (2 * i + 2) * h)) * h / 3
    return summ


def threelynome(h, N):
    summ = 0
    for i in range(int(N / 3)):
        summ += (func(a + 3 * i * h) + 3 * func(a + (3 * i + 1) * h) + 3 * func(a + (3 * i + 2) * h) +
                 func(a + (3 * i + 3) * h)) * 3 * h / 8
    return summ


if __name__ == '__main__':
    N = int(input("Введите количество отрезков N, кратное 6: "))
    if N % 6 != 0 or N <= 0:
        print("Incorrect N")

    h = (b - a) / N

    result = integrate.quad(func, a, b)
    print("Exact result:      ", result[0])

    g = trapezia(h, N)
    print("Trapezia result:   ", g, abs(g - result[0]))

    k = simpson(h, N)
    print("Simpson result:    ", k, abs(k - result[0]))

    c = threelynome(h, N)
    print("Threelynome result:", c, abs(c - result[0]))
