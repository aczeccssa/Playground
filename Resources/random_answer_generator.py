import random
import shutil

INPUT_FILE = "./template/answer_list_template.txt"
OUTPUT_FILE = "./template/random_answer_list_1m.txt"
LINES = 100000

MAGENTA = "\033[35m"
GREEN = "\033[32m"
RESET = "\033[0m"


def __process_output_percentage_msg__(percent: float, w: int) -> str:
    show_percentage = f"{percent:3d}"
    progress_width = w - 6 - len(show_percentage) - 1
    completed_length = int((progress_width * percent) / 100)
    remaining_length = progress_width - completed_length - 1
    return f"\r[{"=" * completed_length}{">"}{" " * remaining_length}] {show_percentage}%"


with open(INPUT_FILE, "r", encoding="utf-8") as file:
    answer_list = file.readlines()

with open(OUTPUT_FILE, "w", encoding="utf-8") as file:
    processed = 0
    interval = range(1, LINES)
    for i in interval:
        percentage = int((i / len(interval) * 100))
        line_width = shutil.get_terminal_size().columns
        msg = __process_output_percentage_msg__(percentage, line_width)
        print(f"{MAGENTA}{msg}{" " * (line_width - len(msg))}{RESET}", end="")

        random_index = random.randint(0, len(answer_list) - 1)
        answer_soures = answer_list[random_index]
        file.write(f"{i}. {answer_soures}")
        processed += 1

print(f"\r{GREEN}[{"=" * (shutil.get_terminal_size().columns - 7)}] 100%{RESET}", end="\n")
print(f"{GREEN}Generated {processed} lines complete!{RESET}")
