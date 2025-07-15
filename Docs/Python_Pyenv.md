# Pyenv 使用教程

## 目录

- [安装 Pyenv](#安装-pyenv)
  - [通过 Homebrew (macOS/Linux)](#通过-homebrew-macoslinux)
  - [通过包管理器 (Linux)](#通过包管理器-linux)
  - [手动安装 (所有平台)](#手动安装-所有平台)
- [使用 Pyenv](#使用-pyenv)
  - [查看可用版本](#查看可用版本)
  - [安装特定版本](#安装特定版本)
  - [列出已安装的版本](#列出已安装的版本)
  - [设置全局 Python 版本](#设置全局-python-版本)
  - [设置本地 Python 版本](#设置本地-python-版本)
  - [创建虚拟环境](#创建虚拟环境)
  - [更新 Pyenv](#更新-pyenv)
  - [卸载 Pyenv](#卸载-pyenv)
- [结论](#结论)

## 安装 Pyenv

### 通过 Homebrew (macOS/Linux)

如果您正在使用 macOS 或者 Linux，并且已经安装了 [Homebrew](https://brew.sh/)，可以通过以下命令来安装 Pyenv：

```bash
brew update
brew install pyenv
```

### 通过包管理器 (Linux)

对于大多数 Linux 发行版，您可以使用默认的包管理器来安装 Pyenv。例如，在基于 Debian/Ubuntu 的系统上，可以运行：

```bash
sudo apt-get update
sudo apt-get install -y make build-essential libssl-dev zlib1g-dev \
libbz2-dev libreadline-dev libsqlite3-dev wget curl llvm libncurses5-dev \
libncursesw5-dev xz-utils tk-dev libffi-dev liblzma-dev python-openssl git
```

然后下载并安装 Pyenv：

```bash
curl https://pyenv.run | bash
```

### 手动安装 (所有平台)

您也可以手动克隆 Pyenv 仓库到您的计算机：

```bash
git clone https://github.com/pyenv/pyenv.git ~/.pyenv
```

安装后需要更新您的 shell 配置文件（如 `.bashrc`, `.zshrc`）以初始化 Pyenv 环境变量。添加以下两行到配置文件中：

```bash
export PYENV_ROOT="$HOME/.pyenv"
export PATH="$PYENV_ROOT/bin:$PATH"
eval "$(pyenv init --path)"
```

确保将上述内容添加到正确的配置文件中，根据您使用的 shell 类型不同，配置文件可能有所不同。之后，重新加载您的 shell 配置或重启终端。

## 使用 Pyenv

### 查看可用版本

要查看哪些 Python 版本可以安装，可以运行：

```bash
pyenv install --list
```

这会列出所有可安装的 Python 版本。

### 安装特定版本

要安装特定版本的 Python，比如 Python 3.9.0，可以运行：

```bash
pyenv install 3.9.0
```

### 列出已安装的版本

要查看当前系统中安装的所有 Python 版本，可以运行：

```bash
pyenv versions
```

该命令会列出所有已安装的版本，当前活动的版本前会有个星号 `*` 标记。

### 设置全局 Python 版本

要设置系统的全局 Python 版本，可以运行：

```bash
pyenv global 3.9.0
```

这会将 Python 3.9.0 设置为所有新打开的终端窗口的默认 Python 版本。

### 设置本地 Python 版本

要在某个项目目录中设置本地 Python 版本，可以在项目根目录下运行：

```bash
pyenv local 3.8.6
```

这会创建一个 `.python-version` 文件，指定在这个目录及其子目录中使用的 Python 版本。

### 创建虚拟环境

Pyenv 可以与 `pyenv-virtualenv` 插件一起使用来创建虚拟环境。首先需要安装插件：

```bash
git clone https://github.com/pyenv/pyenv-virtualenv.git $(pyenv root)/plugins/pyenv-virtualenv
```

然后在您的 shell 配置文件中添加以下行来初始化插件：

```bash
eval "$(pyenv virtualenv-init -)"
```

安装完成后，可以创建一个新的虚拟环境：

```bash
pyenv virtualenv 3.9.0 my_project_env
```

激活虚拟环境：

```bash
pyenv activate my_project_env
```

停用虚拟环境：

```bash
pyenv deactivate
```

删除虚拟环境：

```bash
pyenv virtualenv-delete my_project_env
```

### 更新 Pyenv

要更新 Pyenv 及其插件，可以使用 `git pull` 命令：

```bash
cd ~/.pyenv
git pull
```

如果您安装了 `pyenv-virtualenv`，也需要更新它：

```bash
cd ~/.pyenv/plugins/pyenv-virtualenv
git pull
```

### 卸载 Pyenv

如果您不再需要 Pyenv，可以从系统中卸载它。首先从 shell 配置文件中移除所有关于 Pyenv 的行，然后删除 Pyenv 目录：

```bash
rm -rf ~/.pyenv
```

此外，您还需要手动删除任何由 Pyenv 创建的虚拟环境。

## 结论

Pyenv 是一个强大且灵活的工具，可以帮助您轻松管理多个 Python 版本。无论您是想在同一台机器上测试不同的 Python 版本，还是想要为每个项目创建独立的虚拟环境，Pyenv 都能提供帮助。希望这份教程能够帮助您开始使用 Pyenv 并提高您的 Python 开发效率。