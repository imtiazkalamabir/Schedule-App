package com.challenge.scheduleapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.challenge.scheduleapp.databinding.ActivityScheduleListBinding
import com.challenge.scheduleapp.databinding.DialogSelectAppBinding
import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.presentation.adapter.AppListAdapter
import com.challenge.scheduleapp.presentation.adapter.ScheduleListAdapter
import com.challenge.scheduleapp.presentation.viewmodel.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class ScheduleListActivity : AppCompatActivity() {

    private val scheduleViewModel: ScheduleViewModel by viewModels()

    private val scheduleListAdapter = ScheduleListAdapter(
        onEditClick = { schedule -> showEditScheduleDialog(schedule) },
        onCancelClick = { schedule -> showCancelConfirmDialog(schedule) },
        onDeleteClick = { schedule -> showDeleteConfirmDialog(schedule) }
    )
    private val binding: ActivityScheduleListBinding by lazy {
        ActivityScheduleListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {

        binding.fabAddSchedule.setOnClickListener {
            showAddScheduleDialog()
        }

        setUpRecycleView()

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setUpRecycleView() {
        binding.recyclerViewSchedules.apply {
            layoutManager = LinearLayoutManager(binding.root.context)
            adapter = scheduleListAdapter
        }
    }

    private fun showAddScheduleDialog() {

        val dialogBinding = DialogSelectAppBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        val appListAdapter = AppListAdapter { app ->
            dialog.dismiss()
        }

        dialogBinding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(this@ScheduleListActivity)
            adapter = appListAdapter
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.progressBar.visibility = View.VISIBLE

        // load apps in the recyclerView
        scheduleViewModel.installedApps.observe(this) { apps ->
            dialogBinding.progressBar.visibility = View.GONE
            appListAdapter.submitList(apps)
        }

        if (scheduleViewModel.installedApps.value.isNullOrEmpty()) {
            scheduleViewModel.loadInstalledApps()
        } else {
            dialogBinding.progressBar.visibility = View.GONE
            appListAdapter.submitList(scheduleViewModel.installedApps.value)
        }

        dialog.show()

    }

    private fun showEditScheduleDialog(schedule: AppSchedule) {

    }

    private fun showCancelConfirmDialog(schedule: AppSchedule) {

    }

    private fun showDeleteConfirmDialog(schedule: AppSchedule) {

    }


}