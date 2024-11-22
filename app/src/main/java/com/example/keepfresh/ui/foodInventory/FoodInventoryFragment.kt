package com.example.keepfresh.ui.foodInventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.keepfresh.data.FoodDatabase
import com.example.keepfresh.data.FoodDatabaseDao
import com.example.keepfresh.data.FoodRepository
import com.example.keepfresh.databinding.FragmentInventoryBinding

class FoodInventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: FoodDatabase
    private lateinit var databaseDao: FoodDatabaseDao
    private lateinit var repository: FoodRepository
    private lateinit var viewModelFactory: FoodInventoryViewModelFactory
    private lateinit var foodInventoryViewModel: FoodInventoryViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        database = FoodDatabase.getInstance(requireContext())
        databaseDao = database.foodDatabaseDao
        repository = FoodRepository(databaseDao)
        viewModelFactory = FoodInventoryViewModelFactory(repository)
        foodInventoryViewModel = ViewModelProvider(this, viewModelFactory).get(FoodInventoryViewModel::class.java)

        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupRecyclerView()
        observeData()

        return root
    }

    private fun setupRecyclerView(){
        binding.foodInventoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeData(){
        foodInventoryViewModel.allFoodItems.observe(viewLifecycleOwner) {foodItems ->
            binding.foodInventoryRecyclerView.adapter = FoodInventoryAdapter(foodItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}