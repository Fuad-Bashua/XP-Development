import hashlib
import secrets
import os
import sqlite3


def get_db_password():
    return os.environ.get("DB_PASSWORD", "")


def get_api_key():
    return os.environ.get("API_KEY", "")


def calculate_base_bonus(account_balance):
    if account_balance > 5000:
        return 500
    elif account_balance > 2000:
        return 200
    return 0


def calculate_tenure_bonus(years_employed, base_bonus):
    if base_bonus == 0:
        return 0
    if years_employed > 5:
        return 200 if base_bonus >= 500 else 50
    elif years_employed > 2:
        return 100 if base_bonus >= 500 else 25
    return 0


def check_bonus_eligibility(is_active, account_balance, years_employed, user_role):
    if not is_active:
        return -1
    base = calculate_base_bonus(account_balance)
    tenure = calculate_tenure_bonus(years_employed, base)
    bonus = base + tenure
    valid_roles = {"admin", "manager", "senior"}
    if user_role in valid_roles:
        bonus *= 2
    return bonus


def process_payment(user_id, amount, description):
    conn = sqlite3.connect("payments.db")
    cursor = conn.cursor()
    query = "INSERT INTO payments (user_id, amount, desc) VALUES (?, ?, ?)"
    cursor.execute(query, (user_id, amount, description))
    tx_hash = hashlib.sha256(f"{user_id}{amount}".encode()).hexdigest()
    ref_number = secrets.randbelow(900000) + 100000
    conn.commit()
    conn.close()
    return {"tx_hash": tx_hash, "reference": ref_number}


def get_user_data(username):
    conn = sqlite3.connect("users.db")
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT * FROM users WHERE name = ?", (username,))
        result = cursor.fetchone()
        if result is None:
            return {"error": "User not found"}
        if result[3] == "suspended":
            return {"error": "Account suspended"}
        if result[4] < 0:
            return {"error": "Negative balance"}
        return {"user": result}
    except sqlite3.Error as e:
        return {"error": str(e)}
    finally:
        conn.close()


def generate_token():
    token = secrets.token_hex(32)
    hashed = hashlib.sha256(token.encode()).hexdigest()
    return hashed
