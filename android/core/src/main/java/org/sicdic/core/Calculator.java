package org.sicdic.core;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/** Pure, offline clinical arithmetic. Numeric threshold flags are not diagnoses. */
public final class Calculator {
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final Pattern NUMBER = Pattern.compile(Rules.NUMBER_PATTERN);
    private Calculator() {}

    public static BigDecimal number(String raw) {
        if (raw == null || !NUMBER.matcher(raw.trim()).matches()) throw new IllegalArgumentException("INVALID_NUMBER");
        return new BigDecimal(raw.trim().replace(',', '.'));
    }
    private static BigDecimal positive(String raw) {
        BigDecimal value = number(raw);
        if (value.signum() <= 0) throw new IllegalArgumentException("POSITIVE_REQUIRED");
        return value;
    }
    private static boolean flag(String raw) {
        if (!"true".equals(raw) && !"false".equals(raw)) throw new IllegalArgumentException("EXPLICIT_BOOLEAN_REQUIRED");
        return Boolean.parseBoolean(raw);
    }
    public record Sic(int platelets, int inr, int sofa, int total, boolean thresholdMet) {
        public String vector() { return platelets+","+inr+","+sofa+","+total+","+thresholdMet; }
    }
    public record Dic(int platelets, int dimer, int pt, int fibrinogen, int total,
                      boolean thresholdMet, boolean negativePtDelta, BigDecimal deltaPt,
                      BigDecimal dimerValue, BigDecimal dimerUln) {
        public String vector() { return platelets+","+dimer+","+pt+","+fibrinogen+","+total+","+thresholdMet+","+negativePtDelta; }
        /** Display only; classification uses exact cross-multiplication. */
        public String ratioDisplay() { return dimerValue.divide(dimerUln, 6, RoundingMode.HALF_UP).toPlainString(); }
    }
    public record Sofa(int respiratory, int cardiovascular, int hepatic, int renal, int total) {
        public String vector() { return respiratory+","+cardiovascular+","+hepatic+","+renal+","+total; }
    }
    public static Sic sic(String platelets, String inr, String sofa) {
        BigDecimal s = number(sofa);
        if (s.stripTrailingZeros().scale() > 0 || s.compareTo(BigDecimal.valueOf(Rules.SOFA_MAX)) > 0)
            throw new IllegalArgumentException("FOUR_COMPONENT_SOFA_INTEGER_REQUIRED");
        int p = Rules.sicPlatelets(number(platelets), ONE);
        int i = Rules.sicInr(positive(inr), ONE);
        int o = Rules.sicSofa(s, ONE);
        int total = p+i+o;
        return new Sic(p,i,o,total,total >= Rules.SIC_THRESHOLD && p+i > Rules.SIC_COAG_MIN);
    }
    public static Dic dic(String platelets, String dimer, String uln, String patientPt, String controlPt,
                          String fibrinogen, String fibrinogenUnit, boolean comparable) {
        if (!comparable) throw new IllegalArgumentException("DIMER_NOT_COMPARABLE");
        BigDecimal d = number(dimer), u = positive(uln);
        BigDecimal delta = positive(patientPt).subtract(positive(controlPt));
        BigDecimal f = number(fibrinogen);
        if ("mg/dL".equals(fibrinogenUnit)) f = f.divide(Rules.fibrinogenMgDlPerGL);
        else if (!"g/L".equals(fibrinogenUnit)) throw new IllegalArgumentException("INVALID_UNIT");
        int p = Rules.dicPlatelets(number(platelets), ONE), dd = Rules.dicDimer(d,u);
        int pt = Rules.dicPt(delta,ONE), fib = Rules.dicFibrinogen(f,ONE);
        int total = p+dd+pt+fib;
        return new Dic(p,dd,pt,fib,total,total >= Rules.DIC_THRESHOLD,delta.signum()<0,delta,d,u);
    }
    /** Development helper policy is explicit in clinical-spec/RULES.md; no SOFA-2 substitution. */
    public static Sofa sofa(String pf, String supported, String bilirubin, String creatinine, String labUnit,
                            String urine24h, String map, String dopamine, String epinephrine,
                            String norepinephrine, String dobutamine, String durationConfirmed) {
        if (!flag(durationConfirmed)) throw new IllegalArgumentException("SOFA_TIME_WINDOW_REQUIRED");
        BigDecimal hepaticScale=ONE, renalScale=ONE;
        if ("umol/L".equals(labUnit)) { hepaticScale=Rules.bilirubinUmolPerMgDl; renalScale=Rules.creatinineUmolPerMgDl; }
        else if (!"mg/dL".equals(labUnit)) throw new IllegalArgumentException("INVALID_UNIT");
        BigDecimal ratio=number(pf);
        int r=flag(supported) ? Rules.respiratorySupported(ratio,ONE) : Rules.respiratoryUnsupported(ratio,ONE);
        int c=Rules.map(positive(map),ONE);
        c=Math.max(c,Rules.dopamine(number(dopamine),ONE));
        c=Math.max(c,Rules.epinephrine(number(epinephrine),ONE));
        c=Math.max(c,Rules.epinephrine(number(norepinephrine),ONE));
        c=Math.max(c,Rules.dobutamine(number(dobutamine),ONE));
        int h=Rules.hepaticMgDl(number(bilirubin),hepaticScale);
        int k=Math.max(Rules.renalMgDl(number(creatinine),renalScale),Rules.urine24h(number(urine24h),ONE));
        return new Sofa(r,c,h,k,r+c+h+k);
    }
    public static String convert(String raw, String quantity) {
        BigDecimal divisor = switch(quantity) {
            case "fibrinogen" -> Rules.fibrinogenMgDlPerGL;
            case "bilirubin" -> Rules.bilirubinUmolPerMgDl;
            case "creatinine" -> Rules.creatinineUmolPerMgDl;
            default -> throw new IllegalArgumentException("INVALID_QUANTITY");
        };
        return number(raw).divide(divisor, 6, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
    public static String evaluateVector(String op, String[] a) {
        try {
            return switch(op) {
                case "sic" -> sic(a[0],a[1],a[2]).vector();
                case "dic" -> dic(a[0],a[1],a[2],a[3],a[4],a[5],a[6],flag(a[7])).vector();
                case "sofa" -> sofa(a[0],a[1],a[2],a[3],a[4],a[5],a[6],a[7],a[8],a[9],a[10],a[11]).vector();
                case "convert" -> convert(a[0],a[1]);
                default -> throw new IllegalArgumentException("UNKNOWN_OPERATION");
            };
        } catch (IllegalArgumentException e) { return "ERROR"; }
    }
}
