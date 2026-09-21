package org.sicdic.app

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.*
import org.sicdic.core.Calculator
import org.sicdic.core.Rules
import java.time.Instant
import java.util.Locale

/** Native Android foundation. Clinical logic lives only in :core. No persisted patient state. */
class MainActivity : Activity() {
    private var language = "ru"
    private var dark = false
    private lateinit var column: LinearLayout
    private lateinit var output: TextView
    private lateinit var copy: Button
    private lateinit var sepsis: Spinner
    private lateinit var condition: Spinner
    private lateinit var basis: Spinner
    private lateinit var fibUnit: Spinner
    private lateinit var comparable: CheckBox
    private lateinit var detailed: CheckBox
    private val fields = linkedMapOf<Int, EditText>()
    private val special = mutableListOf<CheckBox>()
    private var result: String? = null
    private fun t(id: Int): String {
        val config = Configuration(resources.configuration)
        config.setLocale(Locale.forLanguageTag(language))
        return createConfigurationContext(config).getString(id)
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun invalidateResult() {
        result = null
        if (::output.isInitialized) output.text = ""
        if (::copy.isInitialized) copy.isEnabled = false
    }
    private fun label(text: String): TextView = TextView(this).also {
        it.text = text; it.textSize = 16f; it.setTextColor(if (dark) Color.WHITE else Color.rgb(18, 40, 62))
        it.setPadding(0, dp(10), 0, dp(4)); column.addView(it)
    }
    private fun input(id: Int) {
        val caption = label(t(id))
        val entry = EditText(this)
        entry.id = View.generateViewId(); caption.labelFor = entry.id
        entry.minHeight = dp(48); entry.textSize = 18f
        entry.setSingleLine(true)
        entry.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        entry.setTextColor(if (dark) Color.WHITE else Color.rgb(18, 40, 62))
        entry.setBackgroundColor(if (dark) Color.rgb(42, 57, 74) else Color.rgb(233, 239, 246))
        entry.setPadding(dp(12), dp(8), dp(12), dp(8))
        entry.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
        entry.isSaveEnabled = false
        entry.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = invalidateResult()
            override fun afterTextChanged(s: Editable?) = Unit
        })
        fields[id] = entry; column.addView(entry)
    }
    private fun select(id: Int, values: List<String>): Spinner {
        val caption = label(t(id))
        return Spinner(this).also {
            it.id = View.generateViewId(); caption.labelFor = it.id; it.minimumHeight = dp(48)
            it.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, values)
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = invalidateResult()
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = invalidateResult()
            }
            column.addView(it)
        }
    }
    private fun check(id: Int): CheckBox = CheckBox(this).also {
        it.text = t(id); it.minHeight = dp(48); it.setTextColor(if (dark) Color.WHITE else Color.rgb(18,40,62))
        it.setOnCheckedChangeListener { _, _ -> invalidateResult() }; column.addView(it)
    }
    private fun button(id: Int, action: () -> Unit): Button = Button(this).also {
        it.text = t(id); it.minHeight = dp(48); it.setOnClickListener { action() }; column.addView(it)
    }
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); render() }
    private fun render() {
        fields.clear(); special.clear(); result = null
        column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(28), dp(20), dp(28))
            setBackgroundColor(if (dark) Color.rgb(18, 30, 44) else Color.rgb(249, 251, 253))
        }
        val scroll = ScrollView(this).apply { addView(column); isFillViewport = true }
        scroll.setOnApplyWindowInsetsListener { view, insets ->
            view.setPadding(insets.systemWindowInsetLeft, insets.systemWindowInsetTop, insets.systemWindowInsetRight, insets.systemWindowInsetBottom)
            insets
        }
        setContentView(scroll)
        label(t(R.string.app_name)).textSize = 28f
        label(t(R.string.development)).setTextColor(if (dark) Color.rgb(255,210,128) else Color.rgb(134,76,0))
        val lang = Button(this).apply { text = if (language == "ru") "English" else "Русский"; minHeight = dp(48); setOnClickListener { language = if (language == "ru") "en" else "ru"; render() } }
        column.addView(lang)
        button(R.string.dark) { dark = !dark; render() }
        detailed = check(R.string.detailed)
        label(t(R.string.express))
        input(R.string.platelets); input(R.string.inr); input(R.string.sofa); label(t(R.string.sofa_note))
        sepsis = select(R.string.sepsis, listOf(t(R.string.sepsis_no), t(R.string.sepsis_suspected), t(R.string.sepsis_yes)))
        input(R.string.dimer); input(R.string.uln)
        select(R.string.dimer_unit, listOf("mg/L", "µg/L", "ng/mL", "µg/mL"))
        basis = select(R.string.dimer_basis, listOf(t(R.string.unknown), "FEU", "DDU"))
        comparable = check(R.string.comparable)
        input(R.string.pt); input(R.string.control); input(R.string.fibrinogen)
        fibUnit = select(R.string.fibrinogen_unit, listOf("g/L", "mg/dL"))
        condition = select(R.string.condition, listOf(t(R.string.unknown), t(R.string.yes), t(R.string.no)))
        listOf(R.string.pregnancy, R.string.postpartum, R.string.pediatric).forEach { special.add(check(it)) }
        button(R.string.calculate) { calculate() }
        output = label("").apply { textSize = 18f; accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE; setTextIsSelectable(true) }
        copy = button(R.string.copy) {
            result?.let { (getSystemService(CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(t(R.string.app_name), it)) }
        }.apply { isEnabled = false }
        button(R.string.clear) { render() }
        button(R.string.about) { AlertDialog.Builder(this).setTitle(t(R.string.about)).setMessage(t(R.string.about_body)).setPositiveButton(android.R.string.ok, null).show() }
        label("${t(R.string.version)} ${BuildConfig.VERSION_NAME}\n${t(R.string.rules)} ${Rules.VERSION}")
    }
    private fun value(id: Int) = fields.getValue(id).text.toString()
    private fun calculate() {
        invalidateResult()
        val lines = mutableListOf<String>()
        var complete = false
        try {
            val s = Calculator.sic(value(R.string.platelets), value(R.string.inr), value(R.string.sofa))
            lines += "SIC ${s.total()}/${Rules.SIC_MAX}"
            lines += t(if (s.thresholdMet()) R.string.sic_met else R.string.sic_not)
            if (sepsis.selectedItemPosition != 2) lines += t(R.string.sic_context)
            if (detailed.isChecked) lines += "${t(R.string.sic_parts)}: ${s.platelets()} / ${s.inr()} / ${s.sofa()}"
            complete = true
        } catch (_: IllegalArgumentException) { lines += "SIC: ${t(R.string.insufficient)}" }
        try {
            val d = Calculator.dic(value(R.string.platelets), value(R.string.dimer), value(R.string.uln), value(R.string.pt), value(R.string.control), value(R.string.fibrinogen), fibUnit.selectedItem.toString(), comparable.isChecked && basis.selectedItemPosition > 0)
            lines += "Overt DIC ${d.total()}/${Rules.DIC_MAX}"
            lines += t(if (d.thresholdMet()) R.string.dic_met else R.string.dic_not)
            if (condition.selectedItemPosition != 1) lines += t(R.string.dic_context)
            if (d.negativePtDelta()) lines += t(R.string.negative_pt)
            if (detailed.isChecked) lines += "${t(R.string.dic_parts)}: ${d.platelets()} / ${d.dimer()} / ${d.pt()} / ${d.fibrinogen()}\n${t(R.string.ratio)}: ${d.ratioDisplay()}\n${t(R.string.delta)}: ${d.deltaPt()}"
            complete = true
        } catch (_: IllegalArgumentException) { lines += "Overt DIC: ${t(R.string.insufficient)}" }
        special.filter { it.isChecked }.forEach { lines += "${it.text}: ${t(R.string.special_warning)}" }
        lines += "${t(R.string.time)}: ${Instant.now()}\n${t(R.string.rules)}: ${Rules.VERSION}\n${t(R.string.version)}: ${BuildConfig.VERSION_NAME}\n${t(R.string.development)}"
        output.text = lines.joinToString("\n\n")
        if (complete) { result = output.text.toString(); copy.isEnabled = true }
    }
}
