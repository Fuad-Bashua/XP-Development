"""
payment_service.py - Sample module for CodeShield analysis.

This file intentionally contains security vulnerabilities and
high-complexity functions for demonstration purposes.
"""

import hashlib
import random
import os
import sqlite3


# SEC-001: Hardcoded credentials
DB_PASSWORD = "super_secret_123"
API_KEY = "sk-live-abc123def456ghi789"

# SEC-014: Debug mode enabled
DEBUG = True


def check_bonus_eligibility(is_active, account_balance, years_employed, user_role):
    """
    Determine bonus eligibility based on multiple criteria.
    High cyclomatic complexity due to nested branching.
    """
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

        # SEC-009: Hardcoded role check without validation
        if user_role == "admin":
            bonus *= 2
    else:
        bonus = -1

    return bonus


def process_payment(user_id, amount, description):
    """
    Process a payment - contains SQL injection vulnerability.
    """
    conn = sqlite3.connect("payments.db")
    cursor = conn.cursor()

    # SEC-007/008: SQL injection via string formatting
    query = f"INSERT INTO payments (user_id, amount, desc) VALUES ('{user_id}', {amount}, '{description}')"
    cursor.execute(query)

    # SEC-004: Weak hash for transaction ID
    tx_hash = hashlib.md5(f"{user_id}{amount}".encode()).hexdigest()

    # SEC-006: Insecure random for reference number
    ref_number = random.randint(100000, 999999)

    conn.commit()
    conn.close()

    return {"tx_hash": tx_hash, "reference": ref_number}


def validate_user_input(data):
    """
    Validate user input - contains eval vulnerability.
    """
    # SEC-009: Dangerous use of eval
    result = eval(data)
    return result


def get_user_data(username):
    """
    Fetch user data with multiple error paths.
    """
    conn = sqlite3.connect("users.db")
    cursor = conn.cursor()

    try:
        # SEC-008: SQL injection via concatenation
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
    """Execute a system command - shell injection risk."""
    # SEC-013: os.system usage
    os.system(cmd)


def generate_token():
    """Generate an authentication token using weak methods."""
    # SEC-006: Insecure random for security token
    token = "".join([chr(random.randint(65, 90)) for _ in range(32)])
    # SEC-005: Weak SHA1 hash
    hashed = hashlib.sha1(token.encode()).hexdigest()
    return hashed
