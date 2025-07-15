import pandas as pd
import plotly.graph_objects as go
from plotly.subplots import make_subplots
import dash
from dash import dcc
from dash import html
from statsmodels.tsa.arima.model import ARIMA
import os

# 创建 Dash 应用
app = dash.Dash(__name__)

# 提示用户输入文件路径
file_path = input("Please enter the path of the target file (e.g., /mnt/WaterTracker.csv): ")
# 展开包含 ~ 的路径
file_path = os.path.expanduser(file_path)

try:
    # 加载数据集
    df = pd.read_csv(file_path)

    # 2. Distribution of daily total intake
    daily_total = df[['Date', 'Day Total(ml)']]
    daily_total_distribution = daily_total.groupby('Date')['Day Total(ml)'].sum().describe().round(2)

    # 3. Proportion of different drink types
    drink_type_counts = df['Drink Type'].value_counts()
    total = len(df)
    drink_type_percentages = (drink_type_counts / total * 100).round(2)

    # 4. Trend of target water intake
    df['Date'] = pd.to_datetime(df['Date'])
    average_goal_per_day = df.groupby('Date')['Goal Volume(ml)'].mean()

    # 创建子图
    fig = make_subplots(
        rows=4, cols=1,
        specs=[
            [{"type": "xy"}],
            [{"type": "domain"}],
            [{"type": "xy"}],
            [{"type": "xy"}]
        ],
        subplot_titles=("Boxplot of daily total intake distribution",
                        "Proportion of different drink types",
                        "Trend of target water intake",
                        "Prediction of target water intake")
    )

    # 添加箱线图
    fig.add_trace(
        go.Box(x=daily_total['Date'], y=daily_total['Day Total(ml)'], name='Daily Total Intake'),
        row=1, col=1
    )
    fig.update_xaxes(title_text="Date", row=1, col=1)
    fig.update_yaxes(title_text="Daily total intake (ml)", row=1, col=1)

    # 添加饼图
    fig.add_trace(
        go.Pie(labels=drink_type_percentages.index, values=drink_type_percentages.values, name='Drink Types'),
        row=2, col=1
    )

    # 添加折线图
    fig.add_trace(
        go.Scatter(x=average_goal_per_day.index, y=average_goal_per_day.values, mode='lines+markers',
                   name='Target Water Intake'),
        row=3, col=1
    )
    fig.update_xaxes(title_text="Date", row=3, col=1)
    fig.update_yaxes(title_text="Average target water intake (ml)", row=3, col=1)

    # 数据预测
    try:
        model = ARIMA(average_goal_per_day, order=(1, 1, 1))
        model_fit = model.fit()
        forecast_steps = 5
        forecast = model_fit.get_forecast(steps=forecast_steps)
        forecast_mean = forecast.predicted_mean
        forecast_index = pd.date_range(start=average_goal_per_day.index[-1], periods=forecast_steps + 1, freq='D')[1:]

        # 添加预测折线图
        fig.add_trace(
            go.Scatter(x=forecast_index, y=forecast_mean, mode='lines+markers',
                       name='Predicted Target Water Intake'),
            row=4, col=1
        )
        fig.update_xaxes(title_text="Date", row=4, col=1)
        fig.update_yaxes(title_text="Predicted average target water intake (ml)", row=4, col=1)

    except Exception as e:
        print(f"Prediction error: {e}")

    fig.update_layout(height=1600, width=800, showlegend=False)

except FileNotFoundError:
    print(f"File {file_path} not found. Please check the path.")
    fig = None
    daily_total_distribution = None
    drink_type_percentages = None
    average_goal_per_day = None

# 定义应用布局
app.layout = html.Div([
    html.H1("Water Intake Data Analysis Dashboard"),
    dcc.Graph(
        id='water-intake-graph',
        figure=fig
    ),
    html.Div([
        html.H2("Distribution of daily total intake"),
        html.Pre(str(daily_total_distribution) if daily_total_distribution is not None else "Data not available")
    ]),
    html.Div([
        html.H2("Proportion of different drink types"),
        html.Pre(str(drink_type_percentages) if drink_type_percentages is not None else "Data not available")
    ]),
    html.Div([
        html.H2("Change of average target water intake over time"),
        html.Pre(str(average_goal_per_day) if average_goal_per_day is not None else "Data not available")
    ])
])

if __name__ == '__main__':
    app.run_server(debug=True)