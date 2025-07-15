import requests
from typing import TypeVar, Generic

T = TypeVar('T')


class Quota:
    limite: int = 0

    def __init__(self, limite: int):
        self.limite = limite

    @staticmethod
    def from_dict(quota_dict: dict):
        return Quota(int(quota_dict.get("limite")))


class CodableException(BaseException):
    code: int
    message: str

    def __init__(self, code: int, msg: str):
        self.code = code
        self.message = msg

    def __str__(self):
        return f"CodableException(code={self.code}, message={self.message})"

    @staticmethod
    def from_dict(codableexception_dict: dict):
        return CodableException(
            code=int(codableexception_dict.get("code")),
            msg=codableexception_dict.get("message")
        )


class ResponseData(Generic[T]):
    status: int
    quota: Quota | None
    error: CodableException | None
    main: T | None

    def __init__(self, status: int, quota: None, error: CodableException, main: T):
        self.status = status
        self.quota = quota
        self.error = error
        self.main = main

    def __str__(self):
        return f"ResponseData(status={self.status}, quota={self.quota}, error={self.error}, main={self.main})"

    @staticmethod
    def from_dict(responsedata_dict: dict):
        exception = None
        if "error" in responsedata_dict and responsedata_dict["error"] is not None:
            exception = CodableException.from_dict(responsedata_dict["error"])
        quota = None
        if "quota" in responsedata_dict and responsedata_dict["quota"] is not None:
            quota = Quota.from_dict(responsedata_dict["quota"])
        return ResponseData(
            status=int(responsedata_dict.get("status", "0")),
            quota=quota,
            error=exception,
            main=responsedata_dict.get("main", None)
        )


def main():
    url = input("Type LifeMark-Foundation-Server target url: ")
    if url is None or url == "":
        url = "http://localhost/api/"
    res = requests.get(url)
    res_dict = res.json()
    body = ResponseData.from_dict(res_dict)
    print(body)


if __name__ == '__main__':
    main()
