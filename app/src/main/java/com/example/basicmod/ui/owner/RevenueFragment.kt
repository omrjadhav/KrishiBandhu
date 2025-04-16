package com.example.basicmod.ui.owner

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.basicmod.R
import com.example.basicmod.databinding.FragmentRevenueBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class RevenueFragment : Fragment() {
    private var _binding: FragmentRevenueBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRevenueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRevenueChart()
        setupTopSellingPlants()
        updateRevenueStats()
    }

    private fun setupRevenueChart() {
        val chart = binding.chartRevenue
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.setPinchZoom(false)
        chart.setDrawGridBackground(false)
        chart.axisRight.isEnabled = false
        chart.legend.isEnabled = true

        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)

        // Sample data - replace with actual data
        val entries = listOf(
            Entry(0f, 1000f),
            Entry(1f, 1500f),
            Entry(2f, 1200f),
            Entry(3f, 2000f),
            Entry(4f, 1800f)
        )

        val dataSet = LineDataSet(entries, "Revenue")
        dataSet.color = Color.BLUE
        dataSet.setCircleColor(Color.BLUE)
        dataSet.lineWidth = 2f
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(false)

        chart.data = LineData(dataSet)
        chart.invalidate()
    }

    private fun setupTopSellingPlants() {
        binding.rvTopSellingPlants.layoutManager = LinearLayoutManager(context)
        // TODO: Set up adapter with actual data
        // binding.rvTopSellingPlants.adapter = TopSellingPlantsAdapter()
    }

    private fun updateRevenueStats() {
        // TODO: Update with actual data
        binding.tvTotalRevenue.text = getString(R.string.total_revenue_value, "₹50,000")
        binding.tvTotalOrders.text = getString(R.string.total_orders_value, 100)
        binding.tvAverageOrderValue.text = getString(R.string.average_order_value, "₹500")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 