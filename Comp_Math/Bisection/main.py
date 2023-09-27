import math
import sys


def bisection(l_border, r_border):
    if (f(r_border) > 0 and f(l_border) > 0) or (f(r_border) < 0 and f(l_border) < 0):
        print("BAD BEHAVIOR")
        sys.exit()
    mid = (r_border + l_border) / 2
    if abs(f(mid)) < eps:
       # return round((r_border + l_border) / 2, len(str(eps)))
       return (r_border + l_border) / 2
    elif f(mid) < -eps:
        return bisection(mid, r_border) if (f(l_border) < 0 < f(r_border)) else bisection(l_border, mid)
    elif f(mid) > eps:
        return bisection(l_border, mid) if (f(l_border) < 0 < f(r_border)) else bisection(mid, r_border)


def bisection_plus_inf(l_border):
    delta = 10
    count = 1
    while True:
        if f(l_border + delta * count) > 0:
            return bisection(l_border + delta * (count - 1), l_border + delta * count)
        count += 1


def bisection_minus_inf(r_border):
    delta = 10
    count = 1
    while True:
        if f(r_border - delta * count) < 0:
            return bisection(r_border - delta * count, r_border - delta * (count - 1))
        count += 1


def f(x):
    return x ** 3 + a * x ** 2 + b * x + c


a = float(input("insert a: "))
b = float(input("insert b: "))
c = float(input("insert c: "))
eps = float(input("insert eps (> 0): "))
if eps <= 0:
    print("Incorrect epsilon")
    sys.exit()

derivative_discr = a ** 2 - 3 * b

if derivative_discr < eps:
    multiplicity = str(1 if derivative_discr < -eps else 3)
    if abs(f(0)) < eps:
        print("1 корень = 0, (кратность", multiplicity + ")")
    elif f(0) > eps:
        print("1 корень = ", bisection_minus_inf(0), ", (кратность", multiplicity + ")")
    elif f(0) < -eps:
        print("1 корень = ", bisection_plus_inf(0), ", (кратность ", multiplicity + ")")

elif derivative_discr > eps:
    alpha = (-a - math.sqrt(derivative_discr)) / 3
    beta = (-a + math.sqrt(derivative_discr)) / 3

    if f(alpha) < -eps and f(beta) < -eps:
        print("1 корень = ", bisection_plus_inf(beta), ", (кратность 1)")

    elif f(alpha) > eps and f(beta) > eps:
        print("1 корень = ", bisection_minus_inf(alpha), ", (кратность 1)")

    elif f(alpha) > eps and f(beta) < -eps:
        print("3 корня: 1)", bisection_minus_inf(alpha), ", 2)", bisection(alpha, beta), ", 3)", bisection_plus_inf(beta))

    elif f(alpha) > eps and abs(f(beta)) < eps:
        print("2 корня: 1)", bisection_minus_inf(alpha), ", 2)", beta, "(кратность 2)")

    elif abs(f(alpha)) < eps and f(beta) < -eps:
        print("2 корня: 1)", bisection_plus_inf(beta), ", 2)", alpha, "(кратность 2)")

    elif abs(f(alpha)) < eps and abs(f(beta)) < eps:
        print("1 корень =", (alpha + beta) / 2, "(кратность 3)")
