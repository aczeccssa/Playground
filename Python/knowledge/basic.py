import math
from datetime import datetime

# Output
print("Programming Playground 2025 @ Lester E ")

# Input
# BMI = W / H ** 2
user_input_weight = input("Enter you weight(kg): ")
user_input_height = input("Enter you height(cm): ")
user_weight = float(user_input_weight)
user_height = float(user_input_height) / 100
user_mbi = user_weight / user_height ** 2
print(f"[INPUT:CONVERTED:{datetime.now()}] BMI is {user_mbi}")

# Math calculate
# Next: -x^2 - 2x + 3 = 0
a = -1
b = -2
c = 3
x = (-b - math.sqrt((b ** 2 - 4 * a * c))) / (2 * a)
print(f"[MATH:CALCULATE:RESULT:{datetime.now()}] x = {x}")


# Functions
def judge(*flags):
    """
    Comments
    """
    for flag in flags:
        print(f"\"{"True " if flag else "False"}\" <-- \"{flag}\"")


judge(0, False, "", None)
