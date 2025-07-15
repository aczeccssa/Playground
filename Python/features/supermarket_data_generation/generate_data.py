import pandas as pd
from datetime import datetime, timedelta
import random

# 定义基础数据
categories = {
    '电子产品': {
        '手机': ['iPhone 14 Pro', 'iPhone 15 Pro', '华为P60', '小米14', 'OPPO Find X', 'vivo X100'],
        '平板电脑': ['iPad Air', 'iPad Pro', '华为MatePad', '小米平板', 'Samsung Tab'],
        '笔记本': ['MacBook Air', 'MacBook Pro', '联想ThinkPad', '华为MateBook', '戴尔XPS'],
        '配件': ['无线耳机', '充电器', '手机壳', '平板保护套', '笔记本支架'],
        '相机': ['索尼A7M4', '佳能R6', '尼康Z6', '富士X-T4', '松下S5'],
        '游戏机': ['PS5', 'Switch OLED', 'Xbox Series X', 'PS4 Pro', 'Switch Lite'],
        '智能手表': ['Apple Watch', 'Garmin', '华为Watch', '小米手环', 'OPPO Watch']
    },
    '家居用品': {
        '家具': ['实木餐桌', '真皮沙发', '书柜', '床架', '办公椅'],
        '厨具': ['炒锅套装', '进口刀具', '料理机', '烤箱', '电饭煲'],
        '家纺': ['床上四件套', '蚕丝被', '羽绒被', '毛巾套装', '地毯'],
        '灯具': ['北欧吊灯', '水晶吊灯', '落地灯', '台灯', '射灯'],
        '收纳': ['收纳柜', '整体衣柜', '收纳盒', '置物架', '储物箱'],
        '卫浴': ['智能马桶', '浴缸套装', '花洒', '浴室柜', '镜子']
    },
    '服装': {
        '女装': ['连衣裙', '羽绒服', '真丝旗袍', '套装', '外套'],
        '男装': ['休闲西装', '商务西服', '夹克', '衬衫', '裤子'],
        '童装': ['儿童套装', '品牌童装', '连衣裙', '外套', '运动服'],
        '运动装': ['耐克运动服', '阿迪达斯', '安德玛', '李宁', '彪马'],
        '配饰': ['奢侈品包包', '钻石项链', '手表', '围巾', '帽子']
    },
    '食品': {
        '零食': ['进口巧克力', '高端坚果', '饼干', '薯片', '糖果'],
        '饮料': ['进口红酒', '茅台酒', '咖啡', '果汁', '矿泉水'],
        '生鲜': ['进口水果', '海鲜礼盒', '牛肉', '三文鱼', '蔬菜'],
        '粮油': ['有机大米', '进口面粉', '橄榄油', '调和油', '面条'],
        '调味品': ['进口橄榄油', '松露油', '酱油', '醋', '调味料']
    }
}

# 生成日期序列
start_date = datetime(2023, 1, 1)
dates = [(start_date + timedelta(days=i)).strftime('%Y-%m-%d') for i in range(365 * 2)]  # 两年的日期

# 创建空列表存储数据
data = []

lines = int(input("输入需要数据条数: "))

# 生成1000条数据
for _ in range(lines):
    # 随机选择日期
    date = random.choice(dates)

    # 随机选择大类
    category = random.choice(list(categories.keys()))
    # 随机选择中类
    subcategory = random.choice(list(categories[category].keys()))
    # 随机选择商品
    product = random.choice(categories[category][subcategory])

    # 根据不同类别设置合理的价格范围和数量范围
    if category == '电子产品':
        base_price = random.uniform(300, 10000)
        quantity = random.randint(1, 100)
    elif category == '家居用品':
        base_price = random.uniform(100, 5000)
        quantity = random.randint(5, 200)
    elif category == '服装':
        base_price = random.uniform(50, 3000)
        quantity = random.randint(10, 300)
    else:  # 食品
        base_price = random.uniform(10, 1000)
        quantity = random.randint(20, 500)

    # 计算各项金额
    sales_amount = base_price * quantity
    return_amount = sales_amount * random.uniform(0.01, 0.05)  # 1-5%的退货率
    discount_amount = sales_amount * random.uniform(0.05, 0.15)  # 5-15%的折扣
    profit_rate = random.uniform(0.15, 0.50)  # 15-50%的利润率
    price_per_customer = sales_amount / quantity

    # 添加数据
    data.append([
        date,
        category,
        subcategory,
        product,
        int(quantity),
        round(sales_amount, 2),
        round(return_amount, 2),
        round(discount_amount, 2),
        round(profit_rate, 2),
        round(price_per_customer, 2)
    ])

headers = [
    '日期', '大类名称', '中类名称', '商品名称', '销售数量', '销售金额', '退货金额', '折扣金额', '利润率', '客单价'
]
# 创建DataFrame
df = pd.DataFrame(data, columns=headers)

# 按日期排序
df = df.sort_values('日期')

# 保存到CSV文件
filename = f'supermarket_data_{lines}.csv'
df.to_csv(filename, index=False)
print(f"数据已生成并保存到 {filename}")
