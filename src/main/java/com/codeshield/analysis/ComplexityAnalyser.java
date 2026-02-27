package com.codeshield.analysis;

import com.codeshield.model.ControlFlowGraph;
import com.codeshield.model.FunctionComplexity;
import com.codeshield.model.ModuleResult;
import com.codeshield.parser.CFGBuilder;
import com.codeshield.parser.SourceReader;

import java.util.List;

public class ComplexityAnalyser {

    public ModuleResult analyse(String filepath) {
        ModuleResult result = new ModuleResult(filepath);

        SourceReader reader = new SourceReader(filepath);
        if (!reader.read()) {
            result.setStatus("skipped/unsupported");
            result.setSkipReason(reader.getError());
            return result;
        }

        int loc = reader.countLoc();
        result.setTotalLoc(loc);

        CFGBuilder builder = new CFGBuilder();
        List<ControlFlowGraph> cfgs;
        try {
            cfgs = builder.buildAll(reader.getLines());
        } catch (Exception e) {
            result.setStatus("skipped/unsupported");
            result.setSkipReason("Parse error: " + e.getMessage());
            return result;
        }

        if (cfgs.isEmpty()) {
            result.setSkipReason("No functions found in module");
            return result;
        }

        int totalCC = 0;
        int maxCC = 0;

        for (ControlFlowGraph cfg : cfgs) {
            FunctionComplexity fc = calculateComplexity(cfg);
            result.addFunction(fc);
            totalCC += fc.getCyclomaticComplexity();
            maxCC = Math.max(maxCC, fc.getCyclomaticComplexity());
        }

        double avgCC = (double) totalCC / cfgs.size();
        result.setTotalComplexity(totalCC);
        result.setMaxComplexity(maxCC);
        result.setAverageComplexity(Math.round(avgCC * 100.0) / 100.0);

        return result;
    }

    public FunctionComplexity calculateComplexity(ControlFlowGraph cfg) {
        int e = cfg.getNumEdges();
        int n = cfg.getNumNodes();
        int p = cfg.getConnectedComponents();

        int m = cfg.getDecisionCount() + 1;

        m = Math.max(m, 1);

        String risk = classifyRisk(m);

        return new FunctionComplexity(
                cfg.getFunctionName(),
                m, e, n, p, risk
        );
    }

    public static String classifyRisk(int complexity) {
        if (complexity <= 10) {
            return "Low Risk";
        } else if (complexity <= 20) {
            return "Moderate Risk";
        } else if (complexity <= 50) {
            return "High Risk";
        } else {
            return "Very High Risk";
        }
    }
}
