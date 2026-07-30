package com.example.python

data class PythonTemplate(
    val title: String,
    val description: String,
    val script: String
)

object PythonTemplates {
    val templates = listOf(
        PythonTemplate(
            title = "تحلیل جامع نسبت‌های مالی (Financial Ratios)",
            description = "محاسبه نسبت‌های نقدینگی، سودآوری، اهرمی و فعالیت به همراه سنجش سلامت مالی",
            script = """# --- سیستم معاملاتی FIS - تحلیل جامع نسبت‌های مالی ---

# 1. نسبت‌های سودآوری (Profitability Ratios)
gross_margin = (gross_profit / revenue) * 100
operating_margin = (ebit / revenue) * 100
net_margin = (net_income / revenue) * 100

# 2. بازده دارایی‌ها و سرمایه (Return Ratios)
roa = (net_income / total_assets) * 100
roe = (net_income / equity) * 100
roce = (ebit / (total_assets - current_liabilities)) * 100

# 3. نسبت‌های نقدینگی (Liquidity Ratios)
current_ratio = current_assets / current_liabilities
quick_ratio = (cash + receivables) / current_liabilities
cash_ratio = cash / current_liabilities

# 4. نسبت‌های ساختار سرمایه و اهرمی (Solvency & Leverage)
debt_to_equity = total_liabilities / equity
debt_ratio = (total_liabilities / total_assets) * 100
interest_coverage = ebit / (interest_expense if interest_expense > 0 else 1)

# 5. نسبت‌های ارزشی بازار (Valuation & Market)
eps = net_income / (shares_count if shares_count > 0 else 1)
pe_ratio = stock_price / (eps if eps > 0 else 1)
book_value_per_share = equity / (shares_count if shares_count > 0 else 1)
pb_ratio = stock_price / (book_value_per_share if book_value_per_share > 0 else 1)

# 6. ارزیابی سلامت مالی FIS (Financial Health Score)
health_score = 50

if current_ratio >= 1.5:
    health_score += 10
    print("[موجب تقدیر] نسبت جاری فوق‌العاده است.")
elif current_ratio >= 1.0:
    health_score += 5

if net_margin >= 15.0:
    health_score += 15
    print("[بازدهی بالا] حاشیه سود خالص بالاست.")
elif net_margin > 5.0:
    health_score += 8

if debt_to_equity < 1.0:
    health_score += 15
    print("[کم‌ریسک] ساختار بدهی شرکت کاملا ایمن است.")

if operating_cash_flow > net_income:
    health_score += 10
    print("[کیفیت سود] جریان نقد عملیاتی از سود خالص بیشتر است.")

print("تحلیل با موفقیت انجام شد. امتیاز final: ", health_score)
"""
        ),
        PythonTemplate(
            title = "مدل سه مرحله‌ای دوبونت (DuPont Analysis)",
            description = "تجزیه بازده حقوق صاحبان سهام (ROE) به حاشیه سود، گردش دارایی و اهرم مالی",
            script = """# --- سیستم معاملاتی FIS - مدل تجزیه دوبونت ---

# ۱. حاشیه سود خالص (Net Profit Margin)
net_profit_margin = net_income / revenue

# ۲. گردش کل دارایی‌ها (Asset Turnover)
asset_turnover = revenue / total_assets

# ۳. اهرم مالی یا ضریب مالکیت (Equity Multiplier)
equity_multiplier = total_assets / equity

# ۴. محاسبه ROE بر اساس مدل ۳ عاملی
dupont_roe = net_profit_margin * asset_turnover * equity_multiplier * 100

print("حاشیه سود خالص:", round(net_profit_margin * 100, 2), "%")
print("گردش دارایی‌ها:", round(asset_turnover, 2), "مرتبه")
print("اهرم مالی:", round(equity_multiplier, 2), "برابر")
print("بازده حقوق صاحبان سهام (ROE):", round(dupont_roe, 2), "%")

# سنجش سلامت مالی
health_score = 60
if net_profit_margin > 0.15:
    health_score += 15
if equity_multiplier > 3.0:
    health_score -= 10
    print("[هشدار] اهرم مالی بالا ریسک شرکت را افزایش داده است.")
"""
        ),
        PythonTemplate(
            title = "پیش‌بینی جریان وجوه نقد آزاد و ارزش شرکت (DCF Valuation)",
            description = "محاسبه FCF، پیش‌بینی رشد ۳ ساله و ارزش‌گذاری انباشته",
            script = """# --- سیستم معاملاتی FIS - ارزش‌گذاری جریان نقد تنزیل شده (DCF) ---

# جریان نقد آزاد واقعی
calculated_fcf = operating_cash_flow - (cogs * 0.05)

# نرخ تنزیل (WACC) و نرخ رشد بلندمدت
wacc = 0.20 # ۲۰ درصد نرخ تنزیل
growth_rate = 0.15 # ۱۵ درصد رشد سالانه

# پیش‌بینی سال‌های آتی
fcf_year1 = calculated_fcf * (1 + growth_rate)
fcf_year2 = fcf_year1 * (1 + growth_rate)
fcf_year3 = fcf_year2 * (1 + growth_rate)

# ارزش تنزیل شده ۳ سال
pv_year1 = fcf_year1 / (1 + wacc)
pv_year2 = fcf_year2 / ((1 + wacc) ** 2)
pv_year3 = fcf_year3 / ((1 + wacc) ** 3)

total_pv_cashflows = pv_year1 + pv_year2 + pv_year3

# ارزش باقی‌مانده (Terminal Value)
terminal_value = (fcf_year3 * (1 + 0.05)) / (wacc - 0.05)
pv_terminal_value = terminal_value / ((1 + wacc) ** 3)

# ارزش کل دارایی‌های عملیاتی شرکت (Enterprise Value)
estimated_enterprise_value = total_pv_cashflows + pv_terminal_value
estimated_share_price = (estimated_enterprise_value - total_liabilities + cash) / (shares_count if shares_count > 0 else 1)

print("ارزش تخمینی هر سهم بر اساس DCF:", round(estimated_share_price, 0), "ریال/تومان")
print("قیمت فعلی بازار:", stock_price)

margin_of_safety = ((estimated_share_price - stock_price) / estimated_share_price) * 100
print("حاشیه امن سرمایه‌گذاری (Margin of Safety):", round(margin_of_safety, 2), "%")

health_score = 70
if margin_of_safety > 20:
    health_score += 20
    print("[فرصت خرید] سهم کمتر از ارزش ذاتی معامله می‌شود.")
"""
        )
    )
}
