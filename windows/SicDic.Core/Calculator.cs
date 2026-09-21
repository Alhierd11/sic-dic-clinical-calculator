using System.Globalization;
using System.Text.RegularExpressions;

namespace SicDic.Core;

public sealed record SicResult(int Platelets, int Inr, int Sofa, int Total, bool ThresholdMet)
{
    public string Vector() => $"{Platelets},{Inr},{Sofa},{Total},{ThresholdMet.ToString().ToLowerInvariant()}";
}
public sealed record DicResult(int Platelets, int Dimer, int Pt, int Fibrinogen, int Total, bool ThresholdMet,
    bool NegativePtDelta, decimal DeltaPt, decimal DimerValue, decimal DimerUln)
{
    public string Vector() => $"{Platelets},{Dimer},{Pt},{Fibrinogen},{Total},{ThresholdMet.ToString().ToLowerInvariant()},{NegativePtDelta.ToString().ToLowerInvariant()}";
    public string RatioDisplay() => (DimerValue / DimerUln).ToString("F6", CultureInfo.InvariantCulture);
}
public sealed record SofaResult(int Respiratory, int Cardiovascular, int Hepatic, int Renal, int Total)
{
    public string Vector() => $"{Respiratory},{Cardiovascular},{Hepatic},{Renal},{Total}";
}

public static class Calculator
{
    private static readonly Regex NumberPattern = new("\\A" + Rules.NumberPattern + "\\z", RegexOptions.CultureInvariant, TimeSpan.FromSeconds(1));
    public static decimal Number(string? raw)
    {
        if (raw is null || !NumberPattern.IsMatch(raw.Trim())) throw new ArgumentException("INVALID_NUMBER");
        return decimal.Parse(raw.Trim().Replace(',', '.'), NumberStyles.AllowDecimalPoint, CultureInfo.InvariantCulture);
    }
    private static decimal Positive(string raw)
    {
        decimal value = Number(raw);
        if (value <= 0) throw new ArgumentException("POSITIVE_REQUIRED");
        return value;
    }
    private static bool Flag(string raw) => raw switch { "true" => true, "false" => false, _ => throw new ArgumentException("EXPLICIT_BOOLEAN_REQUIRED") };
    public static SicResult Sic(string platelets, string inr, string sofa)
    {
        decimal s = Number(sofa);
        if (s != decimal.Truncate(s) || s > Rules.SOFA_MAX) throw new ArgumentException("FOUR_COMPONENT_SOFA_INTEGER_REQUIRED");
        int p = Rules.sicPlatelets(Number(platelets), 1), i = Rules.sicInr(Positive(inr), 1), o = Rules.sicSofa(s, 1);
        int total = p + i + o;
        return new(p, i, o, total, total >= Rules.SIC_THRESHOLD && p + i > Rules.SIC_COAG_MIN);
    }
    public static DicResult Dic(string platelets, string dimer, string uln, string patientPt, string controlPt,
        string fibrinogen, string fibrinogenUnit, bool comparable)
    {
        if (!comparable) throw new ArgumentException("DIMER_NOT_COMPARABLE");
        decimal d = Number(dimer), u = Positive(uln), delta = Positive(patientPt) - Positive(controlPt);
        decimal f = fibrinogenUnit switch { "g/L" => Number(fibrinogen), "mg/dL" => Number(fibrinogen) / Rules.fibrinogenMgDlPerGL, _ => throw new ArgumentException("INVALID_UNIT") };
        int p = Rules.dicPlatelets(Number(platelets), 1), dd = Rules.dicDimer(d, u), pt = Rules.dicPt(delta, 1), fib = Rules.dicFibrinogen(f, 1);
        int total = p + dd + pt + fib;
        return new(p, dd, pt, fib, total, total >= Rules.DIC_THRESHOLD, delta < 0, delta, d, u);
    }
    public static SofaResult Sofa(string pf, string supported, string bilirubin, string creatinine, string labUnit,
        string urine24h, string map, string dopamine, string epinephrine, string norepinephrine, string dobutamine, string durationConfirmed)
    {
        if (!Flag(durationConfirmed)) throw new ArgumentException("SOFA_TIME_WINDOW_REQUIRED");
        decimal hs = 1, ks = 1;
        if (labUnit == "umol/L") { hs = Rules.bilirubinUmolPerMgDl; ks = Rules.creatinineUmolPerMgDl; }
        else if (labUnit != "mg/dL") throw new ArgumentException("INVALID_UNIT");
        decimal ratio = Number(pf);
        int r = Flag(supported) ? Rules.respiratorySupported(ratio, 1) : Rules.respiratoryUnsupported(ratio, 1);
        int c = new[] { Rules.map(Positive(map), 1), Rules.dopamine(Number(dopamine), 1), Rules.epinephrine(Number(epinephrine), 1), Rules.epinephrine(Number(norepinephrine), 1), Rules.dobutamine(Number(dobutamine), 1) }.Max();
        int h = Rules.hepaticMgDl(Number(bilirubin), hs), k = Math.Max(Rules.renalMgDl(Number(creatinine), ks), Rules.urine24h(Number(urine24h), 1));
        return new(r, c, h, k, r + c + h + k);
    }
    public static string Convert(string raw, string quantity)
    {
        decimal divisor = quantity switch { "fibrinogen" => Rules.fibrinogenMgDlPerGL, "bilirubin" => Rules.bilirubinUmolPerMgDl, "creatinine" => Rules.creatinineUmolPerMgDl, _ => throw new ArgumentException("INVALID_QUANTITY") };
        return decimal.Round(Number(raw) / divisor, 6, MidpointRounding.AwayFromZero).ToString("0.######", CultureInfo.InvariantCulture);
    }
    public static string EvaluateVector(string op, string[] a)
    {
        try
        {
            return op switch
            {
                "sic" => Sic(a[0], a[1], a[2]).Vector(),
                "dic" => Dic(a[0], a[1], a[2], a[3], a[4], a[5], a[6], Flag(a[7])).Vector(),
                "sofa" => Sofa(a[0], a[1], a[2], a[3], a[4], a[5], a[6], a[7], a[8], a[9], a[10], a[11]).Vector(),
                "convert" => Convert(a[0], a[1]),
                _ => throw new ArgumentException("UNKNOWN_OPERATION")
            };
        }
        catch (ArgumentException) { return "ERROR"; }
    }
}
