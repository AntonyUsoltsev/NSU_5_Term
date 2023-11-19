import sys
import re
from sympy import symbols, diff, sympify, factor


def extract_expression(input_string):
    match = re.search(r"'(.*?)'", input_string)
    expression = match.group(1) if match else None
    return expression


def differentiate(expression, par):
    # Определение переменной
    x = symbols(par)

    try:
        # Преобразование строки в математическое выражение
        expression = sympify(expression)

        # Дифференцирование выражения по переменной x
        derivative = diff(expression, x)
        derivative = factor(derivative)

        # Вывод результата
        print(f"Исходное выражение: {expression}")
        print(f"Производная по {x}: {derivative}")
    except Exception as e:
        print(f"Произошла ошибка: {e}")


print("\n>>>"*50)
flow_str = input()
input_string = input("expression:")
par = input("parametr:")
expression = extract_expression(input_string)
differentiate(expression, par)
