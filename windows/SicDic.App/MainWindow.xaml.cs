using System.Reflection;
using System.Text.Json;
using System.Windows;
using System.Windows.Automation;
using System.Windows.Controls;
using System.Windows.Media;
using SicDic.Core;

namespace SicDic.App;

/// <summary>Native foundation shell. No patient persistence or networking.</summary>
public partial class MainWindow : Window
{
    private const string AppVersion = "0.1.0-dev";
    private string language = "ru";
    private bool dark;
    private Dictionary<string, string> text = new();
    private readonly Dictionary<string, TextBox> fields = new();
    private readonly List<CheckBox> special = new();
    private TextBlock output = new();
    private Button copy = new();
    private ComboBox sepsis = new(), condition = new(), basis = new(), fibUnit = new();
    private CheckBox comparable = new(), detailed = new();
    private string? result;
    public MainWindow() { InitializeComponent(); Render(); }
    private string T(string key) => text[key];
    private void InvalidateResult() { result = null; output.Text = ""; copy.IsEnabled = false; }
    private TextBlock Label(string value)
    {
        var label = new TextBlock { Text = value, TextWrapping = TextWrapping.Wrap, Margin = new Thickness(0, 10, 0, 4) };
        Root.Children.Add(label); return label;
    }
    private void Input(string key)
    {
        var box = new TextBox();
        var label = new Label { Content = T(key), Target = box, Padding = new Thickness(0, 8, 0, 4) };
        AutomationProperties.SetName(box, T(key));
        Root.Children.Add(label); Root.Children.Add(box); fields[key] = box;
        box.TextChanged += (_, _) => InvalidateResult();
    }
    private ComboBox Select(string key, params string[] values)
    {
        var box = new ComboBox { ItemsSource = values, SelectedIndex = 0 };
        Root.Children.Add(new Label { Content = T(key), Target = box });
        AutomationProperties.SetName(box, T(key));
        box.SelectionChanged += (_, _) => InvalidateResult(); Root.Children.Add(box); return box;
    }
    private CheckBox Check(string key)
    {
        var box = new CheckBox { Content = new TextBlock { Text = T(key), TextWrapping = TextWrapping.Wrap } };
        AutomationProperties.SetName(box, T(key));
        box.Checked += (_, _) => InvalidateResult(); box.Unchecked += (_, _) => InvalidateResult(); Root.Children.Add(box); return box;
    }
    private Button Button(string caption, Action action)
    {
        var button = new Button { Content = caption, HorizontalAlignment = HorizontalAlignment.Left };
        button.Click += (_, _) => action(); Root.Children.Add(button); return button;
    }
    private void Render()
    {
        using var stream = Assembly.GetExecutingAssembly().GetManifestResourceStream($"SicDic.App.Localization.{language}.json") ?? throw new InvalidOperationException("Missing localization");
        text = JsonSerializer.Deserialize<Dictionary<string, string>>(stream) ?? throw new InvalidOperationException("Invalid localization");
        Root.Children.Clear(); fields.Clear(); special.Clear(); result = null;
        Background = dark ? new SolidColorBrush(Color.FromRgb(18, 30, 44)) : new SolidColorBrush(Color.FromRgb(249, 251, 253));
        Foreground = dark ? Brushes.White : new SolidColorBrush(Color.FromRgb(18, 40, 62));
        Label(T("app_name")).FontSize = 30;
        Label(T("development")).Foreground = dark ? Brushes.Gold : Brushes.SaddleBrown;
        Button(language == "ru" ? "English" : "Русский", () => { language = language == "ru" ? "en" : "ru"; Render(); });
        Button(T("dark"), () => { dark = !dark; Render(); });
        detailed = Check("detailed"); Label(T("express"));
        Input("platelets"); Input("inr"); Input("sofa"); Label(T("sofa_note"));
        sepsis = Select("sepsis", T("sepsis_no"), T("sepsis_suspected"), T("sepsis_yes"));
        Input("dimer"); Input("uln");
        Select("dimer_unit", "mg/L", "µg/L", "ng/mL", "µg/mL"); basis = Select("dimer_basis", T("unknown"), "FEU", "DDU");
        comparable = Check("comparable"); Input("pt"); Input("control"); Input("fibrinogen"); fibUnit = Select("fibrinogen_unit", "g/L", "mg/dL");
        condition = Select("condition", T("unknown"), T("yes"), T("no"));
        foreach (string key in new[] { "pregnancy", "postpartum", "pediatric" }) special.Add(Check(key));
        Button(T("calculate"), Calculate);
        output = Label(""); output.FontSize = 18; AutomationProperties.SetLiveSetting(output, AutomationLiveSetting.Polite);
        copy = Button(T("copy"), () => { if (result is not null) { try { Clipboard.SetText(result); } catch (System.Runtime.InteropServices.COMException) { MessageBox.Show(T("insufficient")); } } }); copy.IsEnabled = false;
        Button(T("clear"), Render);
        Button(T("about"), () => MessageBox.Show(T("about_body"), T("about")));
        Label($"{T("version")} {AppVersion}\n{T("rules")} {Rules.Version}");
    }
    private string V(string key) => fields[key].Text;
    private void Calculate()
    {
        InvalidateResult(); var lines = new List<string>(); bool complete = false;
        try
        {
            var s = Calculator.Sic(V("platelets"), V("inr"), V("sofa"));
            lines.Add($"SIC {s.Total}/{Rules.SIC_MAX}"); lines.Add(T(s.ThresholdMet ? "sic_met" : "sic_not"));
            if (sepsis.SelectedIndex != 2) lines.Add(T("sic_context"));
            if (detailed.IsChecked == true) lines.Add($"{T("sic_parts")}: {s.Platelets} / {s.Inr} / {s.Sofa}");
            complete = true;
        }
        catch (ArgumentException) { lines.Add("SIC: " + T("insufficient")); }
        try
        {
            var d = Calculator.Dic(V("platelets"), V("dimer"), V("uln"), V("pt"), V("control"), V("fibrinogen"), (string)fibUnit.SelectedItem, comparable.IsChecked == true && basis.SelectedIndex > 0);
            lines.Add($"Overt DIC {d.Total}/{Rules.DIC_MAX}"); lines.Add(T(d.ThresholdMet ? "dic_met" : "dic_not"));
            if (condition.SelectedIndex != 1) lines.Add(T("dic_context"));
            if (d.NegativePtDelta) lines.Add(T("negative_pt"));
            if (detailed.IsChecked == true) lines.Add($"{T("dic_parts")}: {d.Platelets} / {d.Dimer} / {d.Pt} / {d.Fibrinogen}\n{T("ratio")}: {d.RatioDisplay()}\n{T("delta")}: {d.DeltaPt}");
            complete = true;
        }
        catch (ArgumentException) { lines.Add("Overt DIC: " + T("insufficient")); }
        foreach (var box in special.Where(x => x.IsChecked == true)) lines.Add($"{((TextBlock)box.Content).Text}: {T("special_warning")}");
        lines.Add($"{T("time")}: {DateTimeOffset.UtcNow:O}\n{T("rules")}: {Rules.Version}\n{T("version")}: {AppVersion}\n{T("development")}");
        output.Text = string.Join("\n\n", lines);
        if (complete) { result = output.Text; copy.IsEnabled = true; }
    }
}
