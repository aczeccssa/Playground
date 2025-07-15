import base64
import os
import sys
from io import BytesIO

# Made matplotlib using Qt6 backend
import matplotlib
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from flask import Flask, render_template, request, jsonify
from scipy import signal
from sklearn.linear_model import LinearRegression
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import IsolationForest
from statsmodels.tsa.holtwinters import ExponentialSmoothing
import warnings

warnings.filterwarnings("ignore")

matplotlib.use("Agg")  # 使用非交互式后端

# MacOS特定的字体设置
if sys.platform == "darwin":  # MacOS
    plt.rcParams["font.sans-serif"] = ["Arial Unicode MS"]
else:
    plt.rcParams["font.sans-serif"] = ["SimHei"]
plt.rcParams["axes.unicode_minus"] = False

app = Flask(__name__)


class ECGAnalyzer:
    def __init__(self):
        self.sampling_rate = 510.852  # 从文件中读取的采样率

    def load_data(self, file_path):
        """加载ECG数据文件"""
        try:
            # 读取文件头信息
            with open(file_path, "r", encoding="utf-8") as f:
                header = {}
                for i in range(10):  # 读取前10行作为头信息
                    line = f.readline().strip()
                    if "," in line:
                        key, value = line.split(",", 1)
                        header[key] = value.strip('"')

            # 读取数值数据
            data = pd.read_csv(file_path, skiprows=12, header=None)
            return header, data[0].values
        except Exception as e:
            print(f"读取文件 {file_path} 时出错: {str(e)}")
            return None, None

    def process_signal(self, data):
        """信号处理"""
        # 去基线漂移
        b, a = signal.butter(3, 0.5 / (self.sampling_rate / 2), "highpass")
        data_filtered = signal.filtfilt(b, a, data)

        # 去高频噪声
        b, a = signal.butter(3, 40 / (self.sampling_rate / 2), "lowpass")
        data_filtered = signal.filtfilt(b, a, data_filtered)

        return data_filtered

    def detect_peaks(self, data):
        """检测R峰，使用改进的算法"""
        # 使用Pan-Tompkins算法的改进版本
        # 1. 带通滤波
        lowcut = 5.0
        highcut = 15.0
        nyquist = self.sampling_rate / 2
        low = lowcut / nyquist
        high = highcut / nyquist
        b, a = signal.butter(3, [low, high], btype="band")
        filtered = signal.filtfilt(b, a, data)

        # 2. 求导
        diff = np.diff(filtered)
        squared = diff * diff

        # 3. 移动平均
        window_size = int(0.1 * self.sampling_rate)
        window = np.ones(window_size) / window_size
        smoothed = np.convolve(squared, window, mode="same")

        # 4. 自适应阈值检测
        peaks, _ = signal.find_peaks(
            smoothed,
            height=0.3 * np.max(smoothed),
            distance=int(0.2 * self.sampling_rate),
        )
        return peaks

    def calculate_heart_rate(self, peaks):
        """计算心率"""
        if len(peaks) < 2:  # 如果检测到的峰值少于2个
            return {"mean_hr": 0, "min_hr": 0, "max_hr": 0, "std_hr": 0}

        rr_intervals = np.diff(peaks) / self.sampling_rate  # 转换为秒
        heart_rates = 60 / rr_intervals  # 转换为每分钟心跳次数

        # 移除异常值
        heart_rates = heart_rates[(heart_rates >= 40) & (heart_rates <= 200)]

        if len(heart_rates) == 0:  # 如果所有心率都被过滤掉了
            return {"mean_hr": 0, "min_hr": 0, "max_hr": 0, "std_hr": 0}

        return {
            "mean_hr": np.mean(heart_rates),
            "min_hr": np.min(heart_rates),
            "max_hr": np.max(heart_rates),
            "std_hr": np.std(heart_rates),
        }

    def calculate_hrv_metrics(self, peaks):
        """计算心率变异性指标"""
        if len(peaks) < 2:
            return {"sdnn": 0, "rmssd": 0, "pnn50": 0}

        # 计算RR间期(ms)
        rr_intervals = np.diff(peaks) / self.sampling_rate * 1000

        # 过滤异常值
        rr_intervals = rr_intervals[(rr_intervals >= 300) & (rr_intervals <= 2000)]

        if len(rr_intervals) < 2:
            return {"sdnn": 0, "rmssd": 0, "pnn50": 0}

        # SDNN: RR间期的标准差
        sdnn = np.std(rr_intervals)

        # RMSSD: 相邻RR间期差值的均方根
        rmssd = np.sqrt(np.mean(np.diff(rr_intervals) ** 2))

        # pNN50: 相邻RR间期差值>50ms的比例
        diff_rr = np.abs(np.diff(rr_intervals))
        pnn50 = 100 * np.sum(diff_rr > 50) / len(diff_rr)

        return {"sdnn": sdnn, "rmssd": rmssd, "pnn50": pnn50}

    def detect_arrhythmia(self, peaks):
        """检测可能的心律失常"""
        if len(peaks) < 2:
            return []

        rr_intervals = np.diff(peaks) / self.sampling_rate * 1000
        mean_rr = np.mean(rr_intervals)
        std_rr = np.std(rr_intervals)

        anomalies = []

        # 检测过快心跳
        if mean_rr < 600:  # 心率>100
            anomalies.append("可能存在心动过速")

        # 检测过慢心跳
        if mean_rr > 1000:  # 心率<60
            anomalies.append("可能存在心动过缓")

        # 检测心率不齐
        if std_rr > 150:
            anomalies.append("可能存在心率不齐")

        return anomalies

    def analyze_trend(self, data, peaks):
        """分析ECG信号趋势"""
        if len(peaks) < 2:
            return None

        # 计算每个窗口的心率
        window_size = int(30 * self.sampling_rate)  # 30秒窗口
        n_windows = len(data) // window_size
        heart_rates = []

        for i in range(n_windows):
            start = i * window_size
            end = (i + 1) * window_size
            window_peaks = peaks[(peaks >= start) & (peaks < end)]
            if len(window_peaks) >= 2:
                hr = 60 * len(window_peaks) / (window_size / self.sampling_rate)
                heart_rates.append(hr)

        if len(heart_rates) < 2:
            return None

        # 线性回归分析趋势
        X = np.arange(len(heart_rates)).reshape(-1, 1)
        y = np.array(heart_rates)
        model = LinearRegression()
        model.fit(X, y)

        trend = {
            "slope": model.coef_[0],
            "trend_description": (
                "上升"
                if model.coef_[0] > 0.1
                else "下降" if model.coef_[0] < -0.1 else "平稳"
            ),
            "r2_score": model.score(X, y),
        }

        return trend

    def plot_ecg(self, data, peaks, file_name, save_path):
        """绘制ECG分析的详细可视化图"""
        # 1. ECG原始信号和R峰检测
        ax1 = plt.subplot(3, 1, 1)
        time = np.arange(len(data)) / self.sampling_rate
        ax1.plot(time, data, "b-", label="ECG信号", linewidth=1)
        if len(peaks) > 0:
            ax1.plot(
                peaks / self.sampling_rate, data[peaks], "ro", label="R峰", markersize=4
            )
        ax1.set_xlabel("时间 (秒)")
        ax1.set_ylabel("幅值 (µV)")
        ax1.set_title(f"ECG信号分析 - {file_name}")
        ax1.legend()
        ax1.grid(True)

        # 2. RR间期变化图
        ax2 = plt.subplot(3, 1, 2)
        if len(peaks) >= 2:
            rr_intervals = np.diff(peaks) / self.sampling_rate * 1000  # 转换为毫秒
            rr_times = peaks[1:] / self.sampling_rate
            ax2.plot(rr_times, rr_intervals, "g-", label="RR间期", linewidth=1)
            ax2.axhline(
                y=np.mean(rr_intervals), color="r", linestyle="--", label="平均值"
            )
            ax2.fill_between(
                rr_times,
                np.mean(rr_intervals) - np.std(rr_intervals),
                np.mean(rr_intervals) + np.std(rr_intervals),
                alpha=0.2,
                color="r",
                label="标准差范围",
            )
        ax2.set_xlabel("时间 (秒)")
        ax2.set_ylabel("RR间期 (ms)")
        ax2.set_title("RR间期变化")
        ax2.legend()
        ax2.grid(True)

        # 3. 心率变化趋势
        ax3 = plt.subplot(3, 1, 3)
        if len(peaks) >= 2:
            # 计算每个窗口的心率
            window_size = int(10 * self.sampling_rate)  # 10秒窗口
            n_windows = len(data) // window_size
            heart_rates = []
            times = []

            for i in range(n_windows):
                start = i * window_size
                end = (i + 1) * window_size
                window_peaks = peaks[(peaks >= start) & (peaks < end)]
                if len(window_peaks) >= 2:
                    hr = 60 * len(window_peaks) / (window_size / self.sampling_rate)
                    heart_rates.append(hr)
                    times.append(start / self.sampling_rate)

            if heart_rates:
                # 绘制心率变化
                ax3.plot(times, heart_rates, "b-", label="瞬时心率", linewidth=1)

                # 添加趋势线
                if len(heart_rates) > 1:
                    z = np.polyfit(times, heart_rates, 1)
                    p = np.poly1d(z)
                    ax3.plot(times, p(times), "r--", label=f"趋势线 (斜率: {z[0]:.2f})")

                # 添加正常心率范围
                ax3.axhline(y=60, color="g", linestyle=":", alpha=0.5)
                ax3.axhline(y=100, color="g", linestyle=":", alpha=0.5)
                ax3.fill_between(
                    [times[0], times[-1]],
                    60,
                    100,
                    alpha=0.1,
                    color="g",
                    label="正常心率范围",
                )

        ax3.set_xlabel("时间 (秒)")
        ax3.set_ylabel("心率 (bpm)")
        ax3.set_title("心率变化趋势")
        ax3.legend()
        ax3.grid(True)

        # 调整子图间距
        plt.tight_layout()

        # 保存图像
        plt.savefig(save_path, dpi=300, bbox_inches="tight")
        plt.close()

    def analyze_file(self, file_path):
        """分析单个文件"""
        file_name = os.path.basename(file_path)
        print(f"\n开始分析 {file_name}...")

        # 加载和处理数据
        header, data = self.load_data(file_path)
        if header is None or data is None:
            return None

        processed_data = self.process_signal(data)
        peaks = self.detect_peaks(processed_data)
        hr_stats = self.calculate_heart_rate(peaks)
        hrv_metrics = self.calculate_hrv_metrics(peaks)
        arrhythmia = self.detect_arrhythmia(peaks)
        trend = self.analyze_trend(processed_data, peaks)

        # 计算趋势分析数据
        window_size = int(10 * self.sampling_rate)  # 10秒窗口
        n_windows = len(processed_data) // window_size
        heart_rates = []
        times = []

        for i in range(n_windows):
            start = i * window_size
            end = (i + 1) * window_size
            window_peaks = peaks[(peaks >= start) & (peaks < end)]
            if len(window_peaks) >= 2:
                hr = 60 * len(window_peaks) / (window_size / self.sampling_rate)
                heart_rates.append(hr)
                times.append(start / self.sampling_rate)

        if trend:
            trend["times"] = times
            trend["heart_rates"] = heart_rates

        # 保存图像
        results_dir = "results"
        os.makedirs(results_dir, exist_ok=True)
        plot_path = os.path.join(results_dir, f"{file_name}_analysis.png")
        self.plot_ecg(processed_data, peaks, file_name, plot_path)

        return {
            "file_name": file_name,
            "record_date": header.get("记录日期", "Unknown"),
            "classification": header.get("分类", "Unknown"),
            "heart_rate_stats": hr_stats,
            "hrv_metrics": hrv_metrics,
            "arrhythmia_warnings": arrhythmia,
            "trend_analysis": trend,
            "total_beats": len(peaks),
            "duration": len(data) / self.sampling_rate,
            "processed_data": processed_data,  # 添加处理后的数据
            "peaks": peaks,  # 添加峰值数据
            "raw_data": data,  # 添加原始数据
        }

    def predict_future_trends(self, historical_results, days=7):
        """预测未来趋势"""
        if len(historical_results) < 3:
            return None

        # 提取时间序列数据
        dates = [r["record_date"] for r in historical_results]
        mean_hrs = [r["heart_rate_stats"]["mean_hr"] for r in historical_results]

        # 使用Holt-Winters方法进行预测
        model = ExponentialSmoothing(
            mean_hrs, seasonal_periods=7, trend="add", seasonal="add"
        )
        fitted_model = model.fit()
        forecast = fitted_model.forecast(days)

        return {
            "dates": dates,
            "historical": mean_hrs,
            "forecast": forecast.tolist(),
            "forecast_dates": [f"预测{i + 1}天" for i in range(days)],
        }

    def detect_anomaly_patterns(self, historical_results):
        """检测异常模式"""
        if len(historical_results) < 3:
            return []

        # 准备特征数据
        features = []
        for result in historical_results:
            features.append(
                [
                    result["heart_rate_stats"]["mean_hr"],
                    result["heart_rate_stats"]["std_hr"],
                    result["hrv_metrics"]["sdnn"],
                    result["hrv_metrics"]["rmssd"],
                ]
            )

        # 标准化数据
        scaler = StandardScaler()
        features_scaled = scaler.fit_transform(features)

        # 使用IsolationForest检测异常
        iso_forest = IsolationForest(contamination=0.1, random_state=42)
        predictions = iso_forest.fit_predict(features_scaled)

        # 找出异常记录
        anomalies = []
        for i, pred in enumerate(predictions):
            if pred == -1:
                anomalies.append(
                    {
                        "date": historical_results[i]["record_date"],
                        "metrics": {
                            "mean_hr": historical_results[i]["heart_rate_stats"][
                                "mean_hr"
                            ],
                            "sdnn": historical_results[i]["hrv_metrics"]["sdnn"],
                            "warnings": historical_results[i]["arrhythmia_warnings"],
                        },
                    }
                )

        return anomalies

    def evaluate_health_status(self, result):
        """评估健康状况"""
        score = 100  # 初始满分
        warnings = []

        # 心率评估
        mean_hr = result["heart_rate_stats"]["mean_hr"]
        if mean_hr < 60:
            score -= 10
            warnings.append("心率过低")
        elif mean_hr > 100:
            score -= 10
            warnings.append("心率过高")

        # HRV评估
        sdnn = result["hrv_metrics"]["sdnn"]
        if sdnn < 20:
            score -= 15
            warnings.append("心率变异性过低")
        elif sdnn > 200:
            score -= 10
            warnings.append("心率变异性异常")

        # 心律失常评估
        if result["arrhythmia_warnings"]:
            score -= 5 * len(result["arrhythmia_warnings"])
            warnings.extend(result["arrhythmia_warnings"])

        return {
            "score": max(0, score),  # 确保分数不小于0
            "level": "Good" if score >= 80 else "Fair" if score >= 60 else "Poor",
            "warnings": warnings,
        }


# 创建全局分析器实例
analyzer = ECGAnalyzer()


@app.route("/")
def index():
    """主页"""
    # 获取data目录下的所有CSV文件
    data_dir = "data"
    ecg_files = [f for f in os.listdir(data_dir) if f.endswith(".csv")]
    return render_template("index.html", files=ecg_files)


@app.route("/analyze", methods=["POST"])
def analyze():
    """分析ECG数据"""
    file_name = request.form.get("file")
    if not file_name:
        return jsonify({"error": "未选择文件"})

    file_path = os.path.join("data", file_name)
    try:
        # 分析数据
        result = analyzer.analyze_file(file_path)
        if not result:
            return jsonify({"error": "分析失败"})

        # 生成图表
        fig = create_analysis_plots(result)

        # 将图表转换为base64字符串
        img_data = BytesIO()
        fig.savefig(img_data, format="png", dpi=300, bbox_inches="tight")
        plt.close(fig)
        img_data.seek(0)
        img_base64 = base64.b64encode(img_data.getvalue()).decode()

        # 准备返回数据
        response_data = {
            "plot": img_base64,
            "analysis": {
                "file_name": result["file_name"],
                "record_date": result["record_date"],
                "classification": result["classification"],
                "duration": f"{result['duration']:.2f}秒",
                "total_beats": result["total_beats"],
                "heart_rate": {
                    "mean": f"{result['heart_rate_stats']['mean_hr']:.1f}",
                    "min": f"{result['heart_rate_stats']['min_hr']:.1f}",
                    "max": f"{result['heart_rate_stats']['max_hr']:.1f}",
                    "std": f"{result['heart_rate_stats']['std_hr']:.1f}",
                },
                "hrv_metrics": {
                    "sdnn": f"{result['hrv_metrics']['sdnn']:.1f}",
                    "rmssd": f"{result['hrv_metrics']['rmssd']:.1f}",
                    "pnn50": f"{result['hrv_metrics']['pnn50']:.1f}",
                },
                "warnings": result["arrhythmia_warnings"],
                "trend": (
                    result["trend_analysis"]["trend_description"]
                    if result["trend_analysis"]
                    else None
                ),
            },
        }

        return jsonify(response_data)

    except Exception as e:
        return jsonify({"error": str(e)})


def create_analysis_plots(result):
    """创建分析图表"""
    fig = plt.figure(figsize=(15, 12))

    # 1. ECG信号和R峰
    ax1 = plt.subplot(311)
    time = np.arange(len(result["processed_data"])) / analyzer.sampling_rate
    ax1.plot(time, result["processed_data"], "b-", label="ECG信号", linewidth=1)
    if len(result["peaks"]) > 0:
        ax1.plot(
            result["peaks"] / analyzer.sampling_rate,
            result["processed_data"][result["peaks"]],
            "ro",
            label="R峰",
            markersize=4,
        )
    ax1.set_xlabel("时间 (秒)")
    ax1.set_ylabel("幅值 (µV)")
    ax1.set_title("ECG信号分析")
    ax1.legend()
    ax1.grid(True)

    # 2. RR间期
    ax2 = plt.subplot(312)
    if len(result["peaks"]) >= 2:
        rr_intervals = np.diff(result["peaks"]) / analyzer.sampling_rate * 1000
        rr_times = result["peaks"][1:] / analyzer.sampling_rate
        ax2.plot(rr_times, rr_intervals, "g-", label="RR间期")
        ax2.axhline(y=np.mean(rr_intervals), color="r", linestyle="--", label="平均值")
        ax2.fill_between(
            rr_times,
            np.mean(rr_intervals) - np.std(rr_intervals),
            np.mean(rr_intervals) + np.std(rr_intervals),
            alpha=0.2,
            color="r",
            label="标准差范围",
        )
    ax2.set_xlabel("时间 (秒)")
    ax2.set_ylabel("RR间期 (ms)")
    ax2.set_title("RR间期变化")
    ax2.legend()
    ax2.grid(True)

    # 3. 心率趋势
    ax3 = plt.subplot(313)
    if result["trend_analysis"] and "times" in result["trend_analysis"]:
        times = result["trend_analysis"]["times"]
        heart_rates = result["trend_analysis"]["heart_rates"]
        ax3.plot(times, heart_rates, "b-", label="瞬时心率")

        if len(heart_rates) > 1:
            z = np.polyfit(times, heart_rates, 1)
            p = np.poly1d(z)
            ax3.plot(times, p(times), "r--", label=f"趋势线 (斜率: {z[0]:.2f})")

        ax3.axhline(y=60, color="g", linestyle=":", alpha=0.5)
        ax3.axhline(y=100, color="g", linestyle=":", alpha=0.5)
        ax3.fill_between(
            [times[0], times[-1]], 60, 100, alpha=0.1, color="g", label="正常心率范围"
        )

    ax3.set_xlabel("时间 (秒)")
    ax3.set_ylabel("心率 (bpm)")
    ax3.set_title("心率变化趋势")
    ax3.legend()
    ax3.grid(True)

    plt.tight_layout()
    return fig


def analyze_all_files():
    """分析所有文件并生成比较报告"""
    data_dir = "data"
    ecg_files = [f for f in os.listdir(data_dir) if f.endswith(".csv")]
    all_results = []

    for file_name in ecg_files:
        file_path = os.path.join(data_dir, file_name)
        result = analyzer.analyze_file(file_path)
        if result:
            # 添加健康评估
            result["health_evaluation"] = analyzer.evaluate_health_status(result)
            all_results.append(result)

    # 按日期排序
    all_results.sort(key=lambda x: x["record_date"])

    # 预测未来趋势
    future_prediction = analyzer.predict_future_trends(all_results)

    # 检测异常模式
    anomaly_patterns = analyzer.detect_anomaly_patterns(all_results)

    # 生成比较图表
    comparison_plots = create_comparison_plots(all_results, future_prediction)

    # 将图表转换为base64字符串
    img_data = BytesIO()
    comparison_plots.savefig(img_data, format="png", dpi=300, bbox_inches="tight")
    plt.close(comparison_plots)
    img_data.seek(0)
    comparison_base64 = base64.b64encode(img_data.getvalue()).decode()

    # 准备统计数据
    stats = {
        "total_records": len(all_results),
        "date_range": {
            "start": all_results[0]["record_date"] if all_results else "N/A",
            "end": all_results[-1]["record_date"] if all_results else "N/A",
        },
        "heart_rate_trends": {
            "mean": [r["heart_rate_stats"]["mean_hr"] for r in all_results],
            "dates": [r["record_date"] for r in all_results],
        },
        "hrv_trends": {
            "sdnn": [r["hrv_metrics"]["sdnn"] for r in all_results],
            "rmssd": [r["hrv_metrics"]["rmssd"] for r in all_results],
            "pnn50": [r["hrv_metrics"]["pnn50"] for r in all_results],
        },
        "health_scores": [r["health_evaluation"]["score"] for r in all_results],
        "anomalies": anomaly_patterns,
        "prediction": future_prediction,
    }

    return all_results, comparison_base64, stats


def create_comparison_plots(results, future_prediction):
    """创建比较分析图表"""
    fig = plt.figure(figsize=(15, 15))

    # 1. 心率趋势比较
    ax1 = plt.subplot(411)
    dates = [r["record_date"] for r in results]
    mean_hrs = [r["heart_rate_stats"]["mean_hr"] for r in results]
    min_hrs = [r["heart_rate_stats"]["min_hr"] for r in results]
    max_hrs = [r["heart_rate_stats"]["max_hr"] for r in results]

    ax1.plot(dates, mean_hrs, "b-o", label="平均心率")
    ax1.fill_between(dates, min_hrs, max_hrs, alpha=0.2, color="b", label="心率范围")
    ax1.set_xlabel("日期")
    ax1.set_ylabel("心率 (bpm)")
    ax1.set_title("心率趋势变化")
    ax1.legend()
    ax1.grid(True)
    plt.xticks(rotation=45)

    # 2. HRV指标比较
    ax2 = plt.subplot(412)
    sdnn = [r["hrv_metrics"]["sdnn"] for r in results]
    rmssd = [r["hrv_metrics"]["rmssd"] for r in results]
    pnn50 = [r["hrv_metrics"]["pnn50"] for r in results]

    ax2.plot(dates, sdnn, "r-o", label="SDNN")
    ax2.plot(dates, rmssd, "g-o", label="RMSSD")
    ax2.plot(dates, pnn50, "b-o", label="pNN50")
    ax2.set_xlabel("日期")
    ax2.set_ylabel("值")
    ax2.set_title("心率变异性指标比较")
    ax2.legend()
    ax2.grid(True)
    plt.xticks(rotation=45)

    # 3. 异常检测统计
    ax3 = plt.subplot(413)
    warning_types = set()
    warning_counts = {}

    for r in results:
        for warning in r["arrhythmia_warnings"]:
            if warning not in warning_types:
                warning_types.add(warning)
                warning_counts[warning] = [0] * len(results)
            warning_counts[warning][results.index(r)] += 1

    bottom = np.zeros(len(results))
    for warning in warning_types:
        ax3.bar(dates, warning_counts[warning], bottom=bottom, label=warning)
        bottom += warning_counts[warning]

    ax3.set_xlabel("日期")
    ax3.set_ylabel("异常计数")
    ax3.set_title("异常检测统计")
    ax3.legend()
    plt.xticks(rotation=45)

    # 4. 总体统计
    ax4 = plt.subplot(414)
    total_beats = [r["total_beats"] for r in results]
    durations = [r["duration"] for r in results]

    ax4_twin = ax4.twinx()
    ax4.plot(dates, total_beats, "b-o", label="总心跳数")
    ax4_twin.plot(dates, durations, "r-o", label="记录时长(秒)")

    ax4.set_xlabel("日期")
    ax4.set_ylabel("心跳数", color="b")
    ax4_twin.set_ylabel("时长(秒)", color="r")
    ax4.set_title("记录统计")

    lines1, labels1 = ax4.get_legend_handles_labels()
    lines2, labels2 = ax4_twin.get_legend_handles_labels()
    ax4.legend(lines1 + lines2, labels1 + labels2, loc="upper left")
    plt.xticks(rotation=45)

    # 5. 未来趋势预测
    ax5 = plt.subplot(415)
    ax5.plot(
        future_prediction["dates"],
        future_prediction["forecast"],
        "r-",
        label="预测心率",
    )
    ax5.fill_between(
        future_prediction["dates"],
        future_prediction["forecast"] - np.std(future_prediction["forecast"]),
        future_prediction["forecast"] + np.std(future_prediction["forecast"]),
        alpha=0.2,
        color="r",
        label="预测范围",
    )
    ax5.set_xlabel("日期")
    ax5.set_ylabel("心率 (bpm)")
    ax5.set_title("未来心率趋势预测")
    ax5.legend()
    ax5.grid(True)

    plt.tight_layout()
    return fig


@app.route("/compare")
def compare():
    """比较分析页面"""
    all_results, comparison_plot, stats = analyze_all_files()
    return render_template(
        "compare.html", plot=comparison_plot, stats=stats, results=all_results
    )


def main():
    app.run(debug=True)


if __name__ == "__main__":
    main()
