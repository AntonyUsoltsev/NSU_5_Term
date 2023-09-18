def bisection(l_border, r_border):
    if (f(r_border) > 0 and f(l_border) > 0) or (f(r_border) < 0 and f(l_border) < 0):
        print("BAD BEHAVIOR")
        return
    mid = (r_border + l_border) / 2
    if abs(f(mid)) < eps:
        return (r_border + l_border) / 2
    elif f(mid) < -eps:
        return bisection(mid, r_border)
    elif f(mid) > eps:
        return bisection(l_border, mid)


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
eps = float(input("insert eps: "))

if (a ** 2 - 3 * b) < eps:
    if abs(f(0)) < eps:
        print("1 корень = 0, кратность = 3")
    elif f(0) > eps:
        print("1 корень = ", bisection_minus_inf(0), "кратность = 3")
    elif f(0) < -eps:
        print("1 корень = ", bisection_plus_inf(0), "кратность = 3")

