package com.challenge.scheduleapp

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.challenge.scheduleapp.databinding.ActivityScheduleListBinding
import com.challenge.scheduleapp.databinding.DialogSelectAppBinding
import com.challenge.scheduleapp.domain.model.AppSchedule
import com.challenge.scheduleapp.domain.model.InstalledApp
import com.challenge.scheduleapp.domain.model.ProcessResult
import com.challenge.scheduleapp.domain.model.ScheduleStatus
import com.challenge.scheduleapp.presentation.adapter.AppListAdapter
import com.challenge.scheduleapp.presentation.adapter.ScheduleListAdapter
import com.challenge.scheduleapp.presentation.viewmodel.ScheduleViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import kotlin.getValue
import androidx.core.net.toUri

@AndroidEntryPoint
class ScheduleListActivity : AppCompatActivity() {

    private var shouldScrollToTop = false
    private var lastScheduleCount = 0
    private val scheduleViewModel: ScheduleViewModel by viewModels()

    private val scheduleListAdapter = ScheduleListAdapter(
        onEditClick = { schedule -> showEditScheduleDialog(schedule) },
        onCancelClick = { schedule -> showCancelConfirmDialog(schedule) },
        onDeleteClick = { schedule -> showDeleteConfirmDialog(schedule) }
    )
    private val binding: ActivityScheduleListBinding by lazy {
        ActivityScheduleListBinding.inflate(layoutInflater)
    }

    // Permission launcher for notifications
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                getString(R.string.notification_permission_required),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                getString(R.string.notification_permission_granted), Toast.LENGTH_SHORT
            ).show()
        }

        checkAndRequestExactAlarmPermission()
    }

    // Activity launcher for exact alarm permission
    private val exactAlarmPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    getString(R.string.exact_alarm_permission_required),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.exact_alarm_permission_granted), Toast.LENGTH_SHORT
                ).show()
            }
        }

        checkAndRequestBatteryOptimizationPermission()
    }

    // Activity launcher for battery optimization exclusion permission
    private val batteryOptimizationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) {
            Toast.makeText(
                this,
                getString(R.string.battery_optimization_permission_required),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                getString(R.string.battery_optimization_permission_granted), Toast.LENGTH_SHORT
            )
                .show()
        }
        checkAndRequestOverlayPermission()
    }

    // Activity launcher for overlay permission
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, getString(R.string.overlay_permission_granted), Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(
                this,
                getString(R.string.overlay_permission_required),
                Toast.LENGTH_SHORT
            ).show()
        }
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

        // starting all permission checks sequentially from here
        checkAndRequestNotificationPermission()
    }

    private fun checkAndRequestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.overlay_permission_dialog_title))
                .setMessage(getString(R.string.overlay_permission_dialog_message))
                .setPositiveButton(getString(R.string.grant)) { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:$packageName".toUri()
                    )
                    overlayPermissionLauncher.launch(intent)
                }
                .setNegativeButton(getString(R.string.later), null)
                .show()
        }
    }

    private fun checkAndRequestBatteryOptimizationPermission() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.battery_optimization_permission_dialog_title))
                .setMessage(getString(R.string.battery_optimization_permission_dialog_message))
                .setPositiveButton(getString(R.string.disable)) { _, _ ->
                    val intent = Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        "package:$packageName".toUri()
                    )
                    batteryOptimizationPermissionLauncher.launch(intent)
                }
                .setNegativeButton(getString(R.string.later)) { _, _ ->
                    checkAndRequestOverlayPermission()
                }
                .setOnCancelListener {
                    checkAndRequestOverlayPermission()
                }
                .show()
        } else {
            checkAndRequestOverlayPermission()
        }

    }

    private fun checkAndRequestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.exact_alarm_permission_dialog_title))
                    .setMessage(getString(R.string.exact_alarm_permission_dialog_message))
                    .setPositiveButton(getString(R.string.grant)) { _, _ ->
                        val intent = Intent(
                            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                            "package:$packageName".toUri()
                        )
                        exactAlarmPermissionLauncher.launch(intent)
                    }
                    .setNegativeButton(getString(R.string.later)) { _, _ ->
                        checkAndRequestBatteryOptimizationPermission()
                    }
                    .setOnCancelListener {
                        checkAndRequestBatteryOptimizationPermission()
                    }
                    .show()
            } else {
                checkAndRequestBatteryOptimizationPermission()
            }
        } else {
            checkAndRequestBatteryOptimizationPermission()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        == PackageManager.PERMISSION_GRANTED -> {
                    checkAndRequestExactAlarmPermission()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.notification_permission_dialog_title))
                        .setMessage(getString(R.string.notification_permission_dialog_message))
                        .setPositiveButton(getString(R.string.grant)) { _, _ ->
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                            checkAndRequestExactAlarmPermission()
                        }
                        .show()
                }

                else -> {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            checkAndRequestExactAlarmPermission()
        }
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
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            recycledViewPool.setMaxRecycledViews(0, 20)
        }
    }

    private fun observeViewModel() {
        scheduleViewModel.processResult.observe(this) { result ->
            result?.let {
                when (it) {
                    is ProcessResult.Success -> {
                        // Handle success
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        if (it.message.contains("added", ignoreCase = true)) {
                            shouldScrollToTop = true
                        }
                    }

                    is ProcessResult.Error -> {
                        // Handle error
                        if(it.message.equals("Time conflicting with another schedule")){

                        }
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                        shouldScrollToTop = false
                    }
                }
                scheduleViewModel.clearProcessResult()
            }
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

            val wasScheduleAdded = state.schedules.size > lastScheduleCount
            lastScheduleCount = state.schedules.size


            if (state.schedules.isNotEmpty() && !state.isLoading) {
                binding.recyclerViewSchedules.visibility = View.VISIBLE
                binding.tvEmptyState.visibility = View.GONE
            } else if (state.schedules.isEmpty() && !state.isLoading) {
                binding.recyclerViewSchedules.visibility = View.GONE
                binding.tvEmptyState.visibility = View.VISIBLE
            }

            scheduleListAdapter.submitList(state.schedules) {
                if (shouldScrollToTop && wasScheduleAdded) {
                    shouldScrollToTop = false
                    binding.recyclerViewSchedules.post {
                        (binding.recyclerViewSchedules.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(
                            0,
                            0
                        )
                    }
                }
            }
        }
    }

    /**
     * Extension function for observing LiveData only once
     */
    private fun <T> androidx.lifecycle.LiveData<T>.observeOnce(
        lifecycleOwner: androidx.lifecycle.LifecycleOwner,
        observer: (T) -> Unit
    ) {
        observe(lifecycleOwner, object : androidx.lifecycle.Observer<T> {
            override fun onChanged(value: T) {
                observer(value)
                removeObserver(this)
            }
        })
    }


    private fun showSelectAppDialog() {

        val dialogBinding = DialogSelectAppBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(this).setView(dialogBinding.root).create()

        val appListAdapter = AppListAdapter { app ->
            dialog.dismiss()
            showDateTimeSelectDialog(app)
        }

        dialogBinding.recyclerViewApps.apply {
            layoutManager = LinearLayoutManager(binding.root.context)
            adapter = appListAdapter
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.progressBar.visibility = View.VISIBLE

        // load apps in the recyclerView
        scheduleViewModel.installedApps.observeOnce(this) { apps ->
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
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = schedule.scheduledTime

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

                        val newScheduledTime = calendar.timeInMillis
                        scheduleViewModel.updateAppSchedule(
                            schedule.id,
                            newScheduledTime
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

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()

    }

    private fun showCancelConfirmDialog(schedule: AppSchedule) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.cancel_schedule_dialog_title))
            .setMessage(getString(R.string.cancel_schedule_dialog_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                scheduleViewModel.cancelAppSchedule(schedule.id, ScheduleStatus.CANCELLED.name)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun showDeleteConfirmDialog(schedule: AppSchedule) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_schedule_dialog_title))
            .setMessage(getString(R.string.delete_schedule_dialog_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                scheduleViewModel.deleteAppSchedule(schedule.id)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}