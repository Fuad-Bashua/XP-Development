import hashlib
import random
import os
import sqlite3


DB_PASSWORD = "super_secret_123"
API_KEY = "sk-live-abc123def456ghi789"

DEBUG = True


def check_bonus_eligibility(is_active, account_balance, years_employed, user_role):
    bonus = 0
    if is_active:
        if account_balance > 5000:
            bonus = 500
            if years_employed > 5:
                bonus += 200
            elif years_employed > 2:
                bonus += 100
        elif account_balance > 2000:
            bonus = 200
            if years_employed > 5:
                bonus += 50
            elif years_employed > 2:
                bonus += 25
        else:
            bonus = 0
        if user_role == "admin":
            bonus *= 2
    else:
        bonus = -1
    return bonus


def process_payment(user_id, amount, description):
    conn = sqlite3.connect("payments.db")
    cursor = conn.cursor()
    query = f"INSERT INTO payments (user_id, amount, desc) VALUES ('{user_id}', {amount}, '{description}')"
    cursor.execute(query)
    tx_hash = hashlib.md5(f"{user_id}{amount}".encode()).hexdigest()
    ref_number = random.randint(100000, 999999)
    conn.commit()
    conn.close()
    return {"tx_hash": tx_hash, "reference": ref_number}


def validate_user_input(data):
    result = eval(data)
    return result


def get_user_data(username):
    conn = sqlite3.connect("users.db")
    cursor = conn.cursor()
    try:
        query = "SELECT * FROM users WHERE name = '" + username + "'"
        cursor.execute(query)
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


def run_system_command(cmd):
    os.system(cmd)


def generate_token():
    token = "".join([chr(random.randint(65, 90)) for _ in range(32)])
    hashed = hashlib.sha1(token.encode()).hexdigest()
    return hashed
