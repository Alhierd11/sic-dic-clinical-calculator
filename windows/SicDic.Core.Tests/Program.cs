using SicDic.Core;
using System.Text;

int count = 0;
var output = new StringBuilder();
foreach (string line in File.ReadAllLines(args[0]))
{
    string[] fields = line.Split('\t');
    string actual = Calculator.EvaluateVector(fields[1], fields[2..^1]);
    if (actual != fields[^1]) throw new InvalidOperationException($"{fields[0]}: expected {fields[^1]} but got {actual}");
    output.Append(fields[0]).Append('\t').Append(actual).Append('\n');
    count++;
}
if (args.Length > 1) File.WriteAllText(args[1], output.ToString(), new UTF8Encoding(false));
Console.WriteLine($"PASS: {count} shared golden vectors; rules {Rules.Version}");
