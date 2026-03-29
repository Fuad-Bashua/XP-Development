package com.codeshield.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class SecurityScanner {

    private final List<DetectionRule> rules;

    public SecurityScanner() { rules = new ArrayList<>(); loadRules(); }

    private void loadRules() {
        rules.add(new DetectionRule("SEC-001", "Hardcoded Password", "Critical", "Password or secret hardcoded in source code.", "(password|passwd|pwd|secret|api_key|apikey|api_secret|token|auth_token)\\s*=\\s*['\"][^'\"]{3,}['\"]", "CWE-798"));
        rules.add(new DetectionRule("SEC-002", "Hardcoded Connection String", "Critical", "Database connection string with embedded credentials.", "(mysql|postgres|mongodb|redis|sqlite|mssql)://[^\\s'\"]+:[^\\s'\"]+@", "CWE-798"));
        rules.add(new DetectionRule("SEC-003", "AWS/Cloud Key Exposure", "Critical", "Cloud provider access key found in source code.", "(?:AKIA|ABIA|ACCA|ASIA)[0-9A-Z]{16}", "CWE-798"));
        rules.add(new DetectionRule("SEC-004", "Weak Hash Algorithm (MD5)", "High", "MD5 is cryptographically broken. Use SHA-256 or stronger.", "(?:hashlib\\.md5|MD5\\.new|md5\\(|\\.md5\\()", "CWE-328"));
        rules.add(new DetectionRule("SEC-005", "Weak Hash Algorithm (SHA1)", "High", "SHA-1 is deprecated for security use. Use SHA-256 or SHA-3.", "(?:hashlib\\.sha1|SHA\\.new|sha1\\(|\\.sha1\\()", "CWE-328"));
        rules.add(new DetectionRule("SEC-006", "Insecure Random Number Generator", "Medium", "Standard random module is not cryptographically secure. Use secrets module.", "(?:random\\.random|random\\.randint|random\\.choice|random\\.uniform)\\s*\\(", "CWE-338"));
        rules.add(new DetectionRule("SEC-007", "SQL Injection - String Formatting", "Critical", "SQL query built using string formatting. Use parameterised queries.", "(?:execute|cursor\\.execute|query)\\s*\\(\\s*(?:f['\"]|['\"].*%s|['\"].*\\+\\s*|['\"].*\\.format)", "CWE-89"));
        rules.add(new DetectionRule("SEC-008", "SQL Injection - String Concatenation", "Critical", "SQL query constructed via string concatenation. Use parameterised queries.", "(?:SELECT|INSERT|UPDATE|DELETE|DROP)\\s+.*\\+\\s*(?:\\w+|['\"])", "CWE-89"));
        rules.add(new DetectionRule("SEC-009", "Use of eval()", "Critical", "eval() executes arbitrary code. Use ast.literal_eval() or safe alternatives.", "\\beval\\s*\\(", "CWE-95"));
        rules.add(new DetectionRule("SEC-010", "Use of exec()", "Critical", "exec() executes arbitrary code strings. Avoid in production.", "\\bexec\\s*\\(", "CWE-95"));
        rules.add(new DetectionRule("SEC-011", "Unsafe Deserialization (pickle)", "High", "pickle can execute arbitrary code during deserialization. Use JSON instead.", "(?:pickle\\.loads|pickle\\.load|cPickle\\.loads|cPickle\\.load)\\s*\\(", "CWE-502"));
        rules.add(new DetectionRule("SEC-012", "Shell Injection Risk", "High", "subprocess with shell=True allows shell injection. Use shell=False.", "(?:subprocess\\.(?:call|run|Popen|check_output))\\s*\\(.*shell\\s*=\\s*True", "CWE-78"));
        rules.add(new DetectionRule("SEC-013", "os.system() Usage", "High", "os.system() is vulnerable to shell injection. Use subprocess with shell=False.", "os\\.system\\s*\\(", "CWE-78"));
        rules.add(new DetectionRule("SEC-014", "Debug Mode Enabled", "Medium", "Debug mode should be disabled in production to prevent information leakage.", "(?:DEBUG\\s*=\\s*True|debug\\s*=\\s*True|app\\.debug\\s*=\\s*True)", "CWE-215"));
        rules.add(new DetectionRule("SEC-015", "SSL Verification Disabled", "High", "SSL verification disabled, enabling man-in-the-middle attacks.", "verify\\s*=\\s*False", "CWE-295"));
        rules.add(new DetectionRule("SEC-016", "Binding to All Interfaces", "Medium", "Server bound to 0.0.0.0. Restrict to localhost or specific interfaces.", "(?:host\\s*=\\s*['\"]0\\.0\\.0\\.0['\"]|ALLOWED_HOSTS\\s*=\\s*\\[['\"]\\*['\"]\\])", "CWE-668"));
    }

    public SecurityResult scan(String filename, String sourceCode) {
        SecurityResult result = new SecurityResult(filename);
        String[] lines = sourceCode.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i]; String stripped = line.strip();
            if (stripped.startsWith("#")) continue;
            for (DetectionRule rule : rules) {
                Matcher matcher = rule.getPattern().matcher(line);
                if (matcher.find()) {
                    result.addRedFlag(new RedFlag(rule.getRuleId(), rule.getName(), rule.getSeverity(), rule.getDescription(), i + 1, stripped, rule.getCweReference()));
                }
            }
        }
        return result;
    }

    public SecurityResult scan(String filename, List<String> lines) { return scan(filename, String.join("\n", lines)); }
    public List<DetectionRule> getRules() { return rules; }
}
