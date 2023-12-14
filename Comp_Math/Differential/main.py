import math
import pandas as pd


def solution(x: float) -> float:
    return (math.exp(x) * (math.cos(x) + math.sin(x)) - 1) / 2


def right_side(x: float) -> float:
    return math.exp(x) * math.cos(x)


# Должна ли программа работать с произвольными a и b, если да то как
def solve_1(a, b, h, h2):  # works with a == 0
    data = []
    j = 0
    y_hj = 0
    y_h2j = 0

    while a + j * h < b:
        x_j = a + j * h
        y_ex = solution(x_j)
        delta1 = abs(y_ex - y_hj)
        delta2 = abs(y_ex - y_h2j)
        p_j = math.log(delta1 / delta2, 3) if delta2 != 0 else "inf"

        row = [j, x_j, y_ex, y_hj, y_h2j, delta1, delta2, p_j]
        data.append(row)

        y_hj = y_hj + right_side(a + j * h) * h
        y_h2j = (y_h2j
                 + right_side(a + j * h + 0 * h2) * h2
                 + right_side(a + j * h + 1 * h2) * h2
                 + right_side(a + j * h + 2 * h2) * h2)
        j += 1

    df = pd.DataFrame(data,
                      columns=["j", "x_j", "y_ex(x_j)", "y_hj", "y_hj2", "delta_yh1(x_j)", "delta_yh2(x_j)", "p_j"])
    df.to_excel('table_1.xlsx', index=False)


def solve_2(a, b, h, h2):  # works with a == 0
    data = []
    j = 0
    y_hj = 0
    y_h2j = 0
    while a + j * h < b:
        x_j = a + j * h
        y_ex = solution(x_j)
        delta1 = abs(y_ex - y_hj)
        delta2 = abs(y_ex - y_h2j)
        p_j = math.log(delta1 / delta2, 3) if delta2 != 0 else "inf"

        row = [j, x_j, y_ex, y_hj, y_h2j, delta1, delta2, p_j]
        data.append(row)

        y_hj = y_hj + (right_side(a + (j + 1) * h) + right_side(a + j * h)) * h / 2
        y_h2j = (y_h2j
                 + (right_side(a + j * h) + right_side(a + j * h + 1 * h2)) * h2 / 2
                 + (right_side(a + j * h + 1 * h2) + right_side(a + j * h + 2 * h2)) * h2 / 2
                 + (right_side(a + j * h + 2 * h2) + right_side(a + j * h + 3 * h2)) * h2 / 2)
        j += 1
    df = pd.DataFrame(data,
                      columns=["j", "x_j", "y_ex(x_j)", "y_hj", "y_hj2", "delta_yh1(x_j)", "delta_yh2(x_j)", "p_j"])
    df.to_excel('table_2.xlsx', index=False)


def solve_3_1(a, b, h, h2):
    data = []
    j = 0
    y_hj = [0] * int(((b - a) / h + 1))
    y_h2j = [0] * int(((b - a) / h2 + 4))

    while a + j * h < b:
        x_j = a + j * h
        y_ex = solution(x_j)
        delta1 = abs(y_ex - y_hj[j])
        delta2 = abs(y_ex - y_h2j[3 * j])
        p_j = math.log(delta1 / delta2, 3) if delta2 != 0 else "inf"

        row = [j, x_j, y_ex, y_hj[j], y_h2j[3 * j], delta1, delta2, p_j]
        data.append(row)
        if j == 0:
            y_hj[1] = y_hj[0] + right_side(a + j * h) * h

            y_h2j[1] = y_h2j[0] + right_side(a + j * 3 * h2) * h2
            y_h2j[2] = y_h2j[0] + (h2 / 3) * (right_side(a + (j * 3 + 2) * h2)
                                              + 4 * right_side(a + (j * 3 + 1) * h2)
                                              + right_side(a + (j * 3) * h2))
            y_h2j[3] = y_h2j[1] + (h2 / 3) * (right_side(a + (j * 3 + 3) * h2)
                                              + 4 * right_side(a + (j * 3 + 2) * h2)
                                              + right_side(a + (j * 3 + 1) * h2))

        else:
            y_hj[j + 1] = y_hj[j - 1] + (h / 3) * (right_side(a + (j + 1) * h)
                                                   + 4 * right_side(a + j * h)
                                                   + right_side(a + (j - 1) * h))

            y_h2j[3 * j + 1] = y_h2j[3 * j - 1] + (h2 / 3) * (right_side(a + (j * 3 + 1) * h2)
                                                              + 4 * right_side(a + (j * 3) * h2)
                                                              + right_side(a + (j * 3 - 1) * h2))
            y_h2j[3 * j + 2] = y_h2j[3 * j] + (h2 / 3) * (right_side(a + (j * 3 + 2) * h2)
                                                          + 4 * right_side(a + (j * 3 + 1) * h2)
                                                          + right_side(a + (j * 3) * h2))
            y_h2j[3 * j + 3] = y_h2j[3 * j + 1] + (h2 / 3) * (right_side(a + (j * 3 + 3) * h2)
                                                              + 4 * right_side(a + (j * 3 + 2) * h2)
                                                              + right_side(a + (j * 3 + 1) * h2))

        j += 1
    df = pd.DataFrame(data,
                      columns=["j", "x_j", "y_ex(x_j)", "y_hj", "y_hj2", "delta_yh1(x_j)", "delta_yh2(x_j)", "p_j"])
    df.to_excel('table_3_1.xlsx', index=False)


def solve_3_2(a, b, h, h2):
    data = []
    j = 0
    y_hj = [0] * int(((b - a) / h + 1))
    y_h2j = [0] * int(((b - a) / h2 + 4))

    while a + j * h < b:
        x_j = a + j * h
        y_ex = solution(x_j)
        delta1 = abs(y_ex - y_hj[j])
        delta2 = abs(y_ex - y_h2j[3 * j])
        p_j = math.log(delta1 / delta2, 3) if delta2 > 0 else "inf"

        row = [j, x_j, y_ex, y_hj[j], y_h2j[3 * j], delta1, delta2, p_j]
        data.append(row)
        if j == 0:
            y_hj[1] = y_hj[0] + (right_side(a + (j + 1) * h) + right_side(a + j * h)) * h / 2

            y_h2j[1] = y_h2j[0] + (right_side(a + (3 * j + 1) * h2) + right_side(a + 3 * j * h2)) * h2 / 2
            y_h2j[2] = y_h2j[0] + (h2 / 3) * (right_side(a + (j * 3 + 2) * h2)
                                              + 4 * right_side(a + (j * 3 + 1) * h2)
                                              + right_side(a + (j * 3) * h2))
            y_h2j[3] = y_h2j[1] + (h2 / 3) * (right_side(a + (j * 3 + 3) * h2)
                                              + 4 * right_side(a + (j * 3 + 2) * h2)
                                              + right_side(a + (j * 3 + 1) * h2))

        else:
            y_hj[j + 1] = y_hj[j - 1] + (h / 3) * (right_side(a + (j + 1) * h)
                                                   + 4 * right_side(a + j * h)
                                                   + right_side(a + (j - 1) * h))

            y_h2j[3 * j + 1] = y_h2j[3 * j - 1] + (h2 / 3) * (right_side(a + (j * 3 + 1) * h2)
                                                              + 4 * right_side(a + (j * 3) * h2)
                                                              + right_side(a + (j * 3 - 1) * h2))
            y_h2j[3 * j + 2] = y_h2j[3 * j] + (h2 / 3) * (right_side(a + (j * 3 + 2) * h2)
                                                          + 4 * right_side(a + (j * 3 + 1) * h2)
                                                          + right_side(a + (j * 3) * h2))
            y_h2j[3 * j + 3] = y_h2j[3 * j + 1] + (h2 / 3) * (right_side(a + (j * 3 + 3) * h2)
                                                              + 4 * right_side(a + (j * 3 + 2) * h2)
                                                              + right_side(a + (j * 3 + 1) * h2))

        j += 1
    df = pd.DataFrame(data,
                      columns=["j", "x_j", "y_ex(x_j)", "y_hj", "y_hj2", "delta_yh1(x_j)", "delta_yh2(x_j)", "p_j"])
    df.to_excel('table_3_2.xlsx', index=False)


def solve_3_3(a, b, h, h2):
    data = []
    j = 0
    y_hj = [0] * int(((b - a) / h + 1))
    y_h2j = [0] * int(((b - a) / h2 + 4))

    while a + j * h < b:
        x_j = a + j * h
        y_ex = solution(x_j)
        delta1 = abs(y_ex - y_hj[j])
        delta2 = abs(y_ex - y_h2j[3 * j])
        p_j = math.log(delta1 / delta2, 3) if delta2 > 0 else "inf"

        row = [j, x_j, y_ex, y_hj[j], y_h2j[3 * j], delta1, delta2, p_j]
        data.append(row)
        if j == 0:
            y_hj[1] = y_hj[0] + (h / 12) * (5 * right_side(a + j * h) +
                                            8 * right_side(a + (j + 1) * h) -
                                            right_side(a + (j + 2) * h))

            y_h2j[1] = y_h2j[0] + (h2 / 12) * (5 * right_side(a + 3 * j * h2) +
                                               8 * right_side(a + (3 * j + 1) * h2) -
                                               right_side(a + (3 * j + 2) * h2))
            y_h2j[2] = y_h2j[0] + (h2 / 3) * (right_side(a + (j * 3 + 2) * h2)
                                              + 4 * right_side(a + (j * 3 + 1) * h2)
                                              + right_side(a + (j * 3) * h2))
            y_h2j[3] = y_h2j[1] + (h2 / 3) * (right_side(a + (j * 3 + 3) * h2)
                                              + 4 * right_side(a + (j * 3 + 2) * h2)
                                              + right_side(a + (j * 3 + 1) * h2))

        else:
            y_hj[j + 1] = y_hj[j - 1] + (h / 3) * (right_side(a + (j + 1) * h)
                                                   + 4 * right_side(a + j * h)
                                                   + right_side(a + (j - 1) * h))

            y_h2j[3 * j + 1] = y_h2j[3 * j - 1] + (h2 / 3) * (right_side(a + (j * 3 + 1) * h2)
                                                              + 4 * right_side(a + (j * 3) * h2)
                                                              + right_side(a + (j * 3 - 1) * h2))
            y_h2j[3 * j + 2] = y_h2j[3 * j] + (h2 / 3) * (right_side(a + (j * 3 + 2) * h2)
                                                          + 4 * right_side(a + (j * 3 + 1) * h2)
                                                          + right_side(a + (j * 3) * h2))
            y_h2j[3 * j + 3] = y_h2j[3 * j + 1] + (h2 / 3) * (right_side(a + (j * 3 + 3) * h2)
                                                              + 4 * right_side(a + (j * 3 + 2) * h2)
                                                              + right_side(a + (j * 3 + 1) * h2))

        j += 1
    df = pd.DataFrame(data,
                      columns=["j", "x_j", "y_ex(x_j)", "y_hj", "y_hj2", "delta_yh1(x_j)", "delta_yh2(x_j)", "p_j"])
    df.to_excel('table_3_3.xlsx', index=False)


def main():
    a = float(input("Insert a:"))
    b = float(input("Insert b:"))
    h = float(input("Insert h:"))
    h2 = h / 3
    assert a == 0
    solve_1(a, b, h, h2)
    solve_2(a, b, h, h2)
    solve_3_1(a, b, h, h2)
    solve_3_2(a, b, h, h2)
    solve_3_3(a, b, h, h2)


if __name__ == '__main__':
    main()
