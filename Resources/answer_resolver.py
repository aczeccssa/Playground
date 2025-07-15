import re
from abc import ABC
from datetime import datetime
from typing import Dict, Tuple, TypeVar, Generic, Iterable, Iterator
import shutil

T = TypeVar("T")


class StackIterator(Iterator[T]):
    _stack: list[T]
    _index: int

    def __init__(self, stack: list[T]) -> None:
        self._stack = stack
        self._index = 0

    def __next__(self) -> T:
        if self._index < len(self._stack):
            next_item = self._stack[self._index]
            self._index += 1
            return next_item
        else:
            raise StopIteration


class Stack(Generic[T], Iterable[T], ABC):
    _stack: list[T]

    def __init__(self) -> None:
        self._stack: list[T] = []

    def push(self, _item: T) -> None:
        self._stack.append(_item)

    def pop(self) -> T:
        if not self._stack:
            raise IndexError("pop from empty stack")
        return self._stack.pop()

    def peek(self) -> T:
        if not self._stack:
            raise IndexError("peek from empty stack")
        return self._stack[-1]

    def is_empty(self) -> bool:
        return len(self._stack) == 0

    def size(self) -> int:
        return len(self._stack)

    def __iter__(self) -> Iterator[T]:
        return StackIterator(self._stack)

    def __repr__(self) -> str:
        return f"Stack({self._stack})"


class AnswerStruct:
    order: int
    answer: str
    desc: str

    def __init__(self, str_order: int, str_answer: str, str_desc: str):
        self.order = str_order
        self.answer = str_answer
        self.desc = str_desc


type Answer = Tuple[str, str]
type AnswerDict = Dict[int, Answer]
type ErrorTrace = Stack[(float, BaseException)]

LINE_MATCH_PATTERN = r"^\d+\.\s[A-D]:.*$"

RESET = "\033[0m"
BOLD = "\033[1m"
UNDERLINE = "\033[4m"
RED = "\033[31m"
GREEN = "\033[32m"
YELLOW = "\033[33m"
BLUE = "\033[34m"
MAGENTA = "\033[35m"
CYAN = "\033[36m"
WHITE = "\033[37m"

CS = "="  # Leading Symbol
AS = ">"  # Arrow Symbol
TS = " "  # Trailing Symbol

error_trace: ErrorTrace = Stack()


def __process_output_percentage_msg__(percentage: float, line_width: int) -> str:
    show_percentage = f"{percentage:3d}"
    progress_width = line_width - 6 - len(show_percentage) - 1
    completed_length = int((progress_width * percentage) / 100)
    remaining_length = progress_width - completed_length - 1
    return f"\r[{CS * completed_length}{AS}{TS * remaining_length}] {show_percentage}%"


def __error_trace_rest__():
    print(f"{RED}共{error_trace.size()}个异常被盏记录{RESET}")
    for line in error_trace:
        time = datetime.fromtimestamp(line[0])
        exception: BaseException = line[1]
        print(f"{RED}[{time}] {exception}{RESET}")


def __processline__(line: str) -> AnswerStruct | None:
    match = re.match(LINE_MATCH_PATTERN, line)
    if not match:
        return None

    parts = line.split(":", 1)
    if len(parts) < 2:
        return None

    try:
        lead_part, desc_part = parts
        order_str, answer_part = lead_part.split(". ")
        order = int(order_str)
        return AnswerStruct(order, answer_part, desc_part)
    except (ValueError, IndexError) as err:
        error_trace.push((datetime.now().timestamp(), err))
        return None


def __init_answerlist__(filename: str) -> AnswerDict:
    res: AnswerDict = {}
    unmatched_lines: list[str] = []
    try:
        with open(filename, "r", encoding="utf-8") as file:
            lines = file.readlines()
            total_lines = len(lines)

            for i, line in enumerate(lines):
                percentage = int((i / total_lines) * 100)
                line_width = shutil.get_terminal_size().columns
                msg = __process_output_percentage_msg__(percentage, line_width)
                print(f"{MAGENTA}{msg}{" " * (line_width - len(msg))}{RESET}", end="")

                line = line.strip()
                if re.match(LINE_MATCH_PATTERN, line) is None:
                    unmatched_lines.append(line)
                    continue
                new_item = __processline__(line)
                if new_item is None:
                    unmatched_lines.append(line)
                    continue
                res[new_item.order] = (new_item.answer, new_item.desc)

            print(
                f"\r{GREEN}[{CS * (shutil.get_terminal_size().columns - 7)}] 100%{RESET}",
                end="\n",
            )
    except FileNotFoundError as e:
        error_trace.push((datetime.now().timestamp(), e))

    if unmatched_lines:
        msg = f"共{len(unmatched_lines)}行未匹配"
        error_trace.push((datetime.now().timestamp(), Exception(msg)))
        print(f"{RED}{msg}{RESET}")

    return res


input_filename = input(f"{GREEN}输入元数据文件地址: {RESET}")
answer_dict = __init_answerlist__(input_filename)
print(f"{CYAN}读取到 {len(answer_dict)} 条答案{RESET}")

query = input(f"{BOLD}输入要查询的题号: {RESET}")
while query.strip():
    if query.startswith("exit"):
        __error_trace_rest__()
        print(f"{RED}结束进程: {datetime.now()}{RESET}")
        exit(0)
    try:
        index = int(query)
        item = answer_dict[index]
    except ValueError as ve:
        error_trace.push((datetime.now().timestamp(), ve))
        print(
            f'{RED}{UNDERLINE}"{query}"{RESET}{RED} 是非法字符, 不是要求的整数(int){RESET}'
        )
    except KeyError as ke:
        error_trace.push((datetime.now().timestamp(), ke))
        print(f"{RED}数据没有被收录 ==> {query}{RESET}")
    else:
        (answer, desc) = item
        print(f"{GREEN}答案 ==> {BOLD}{UNDERLINE}{answer}{RESET}")
        print(f"解释 ==> {desc}")
    finally:
        query = input(f"{BOLD}输入要查询的题号: {RESET}")
