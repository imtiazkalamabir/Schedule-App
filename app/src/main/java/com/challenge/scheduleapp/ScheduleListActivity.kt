package com.challenge.scheduleapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.challenge.scheduleapp.domain.model.InstalledApp
import com.challenge.scheduleapp.domain.model.ProcessResult
import com.challenge.scheduleapp.presentation.adapter.AppListAdapter
import com.challenge.scheduleapp.presentation.adapter.ScheduleListAdapter
import com.challenge.scheduleapp.presentation.viewmodel.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
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
            showSelectAppDialog()
        }

        setUpRecycleView()
        setUpWindowInsets()
        observeViewModel()
    }

    private fun setUpWindowInsets() {

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

    private fun observeViewModel() {
        scheduleViewModel.processResult.observe(this) { result ->
            when (result) {
                is ProcessResult.Success -> {
                    // Handle success
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                is ProcessResult.Error -> {
                    // Handle error
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
            scheduleViewModel.clearProcessResult()
        }

        scheduleViewModel.appSchedulesUiState.observe(this) { state ->
            if (state.isLoading) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }

            state.error?.let { error ->
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            }

            if (state.schedules.isNotEmpty() && !state.isLoading) {
                binding.recyclerViewSchedules.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
            } else {
                binding.recyclerViewSchedules.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            }

            scheduleListAdapter.submitList(state.schedules)
        }

    }

    private fun showSelectAppDialog() {

        val dialogBinding = DialogSelectAppBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        val appListAdapter = AppListAdapter { app ->
            dialog.dismiss()
            showDateTimeSelectDialog(app)
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

    private fun showDateTimeSelectDialog(app: InstalledApp) {

        val calendar = Calendar.getInstance()

        val datePickerDialog = DatePickerDialog(
            this, { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        val scheduledTime = calendar.timeInMillis
                        scheduleViewModel.addAppSchedule(
                            app.packageName,
                            app.appName,
                            scheduledTime
                        )
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()

            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }


    private fun showEditScheduleDialog(schedule: AppSchedule) {

    }

    private fun showCancelConfirmDialog(schedule: AppSchedule) {

    }

    private fun showDeleteConfirmDialog(schedule: AppSchedule) {

    }


}