package com.example.keepfresh.ui.wasteAnalysis

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.keepfresh.data.FoodDatabase
import com.example.keepfresh.data.FoodRepository
import com.example.keepfresh.databinding.FragmentWasteAnalysisBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate

class WasteAnalysisFragment : Fragment() {
    private var _binding: FragmentWasteAnalysisBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: FoodRepository
    private lateinit var viewModelFactory: WasteAnalysisViewModelFactory
    private lateinit var viewModel: WasteAnalysisViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWasteAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = FoodDatabase.getInstance(requireContext())
        repository = FoodRepository(database.foodDatabaseDao)
        viewModelFactory = WasteAnalysisViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(WasteAnalysisViewModel::class.java)

        // Observe and populate charts and table
        viewModel.wasteAnalysis.observe(viewLifecycleOwner) { data ->
            setupCharts(data)
            populateTable(data)
        }
    }

    private fun setupCharts(data: List<WasteData>) {
        val foodWasteEntries = ArrayList<BarEntry>()
        val moneyWasteEntries = ArrayList<BarEntry>()
        val months = ArrayList<String>()

        data.forEachIndexed { index, wasteData ->
            foodWasteEntries.add(BarEntry(index.toFloat(), wasteData.totalFoodWasted.toFloat()))
            moneyWasteEntries.add(BarEntry(index.toFloat(), wasteData.totalMoneyWasted.toFloat()))
            months.add(wasteData.month)
        }

        // Food Waste Chart
        val foodDataSet = BarDataSet(foodWasteEntries, "Food Waste (Items)")
        foodDataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        foodDataSet.valueTextColor = Color.BLACK
        foodDataSet.valueTextSize = 12f
        foodDataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry): String {
                return "${barEntry.y.toInt()} item(s)"
            }
        }

        val foodData = BarData(foodDataSet)
        binding.foodWasteChart.data = foodData
        configureChart(binding.foodWasteChart, months)

        // Money Waste Chart
        val moneyDataSet = BarDataSet(moneyWasteEntries, "Money Waste ($)")
        moneyDataSet.colors = ColorTemplate.VORDIPLOM_COLORS.toList()
        moneyDataSet.valueTextColor = Color.BLACK
        moneyDataSet.valueTextSize = 12f
        moneyDataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry): String {
                return "$${barEntry.y}"
            }
        }

        val moneyData = BarData(moneyDataSet)
        binding.moneyWasteChart.data = moneyData
        configureChart(binding.moneyWasteChart, months)
    }

    private fun configureChart(chart: BarChart, months: List<String>) {
        chart.description.isEnabled = false
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.textColor = Color.BLACK
        chart.axisLeft.textSize = 12f
        chart.axisRight.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(months)
        chart.xAxis.granularity = 1f
        chart.xAxis.textColor = Color.BLACK
        chart.xAxis.labelRotationAngle = -15f
        chart.extraBottomOffset = 15f
        chart.legend.isEnabled = false
        chart.setFitBars(true)
        chart.animateY(1000)
        chart.invalidate()
    }

    private fun populateTable(data: List<WasteData>) {
        val tableLayout = binding.foodWasteTable
        tableLayout.removeAllViews()

        // Add header row
        val headerRow = TableRow(requireContext())
        headerRow.setBackgroundColor(Color.LTGRAY)
        headerRow.addView(createTextView("Month", true))
        headerRow.addView(createTextView("Details", true))
        headerRow.addView(createTextView("Total Cost", true))
        headerRow.addView(createTextView("Most Expensive", true))
        tableLayout.addView(headerRow)

        // Add rows for each WasteData entry
        data.forEachIndexed { index, wasteData ->
            val row = TableRow(requireContext())
            row.setBackgroundColor(if (index % 2 == 0) Color.WHITE else Color.parseColor("#F5F5F5"))

            row.addView(createTextView(wasteData.month))

            val details = wasteData.foodDetails.joinToString("\n") { "${it.name}: $${it.cost}" }
            row.addView(createTextView(details))
            row.addView(createTextView("$${wasteData.totalMoneyWasted}"))
            val mostExpensive = wasteData.mostExpensiveWaste
            row.addView(
                createTextView(
                    mostExpensive?.let { "${it.name}: $${it.cost}" } ?: "N/A"
                )
            )
            tableLayout.addView(row)
        }
    }

    private fun createTextView(text: String, isHeader: Boolean = false): TextView {
        return TextView(requireContext()).apply {
            this.text = text
            textSize = if (isHeader) 16f else 14f
            setPadding(16, 20, 16, 20)
            setTextColor(if (isHeader) Color.BLACK else Color.DKGRAY)
            setTypeface(null, if (isHeader) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL)
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
