package org.thingai.android.app.meo.ui.viewmodel.device

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.thingai.meo.common.entity.device.MDevice
import javax.inject.Inject

@HiltViewModel
class VMDeviceList @Inject constructor() : ViewModel() {

    data class DeviceListUiState(
        val devices: List<MDevice> = emptyList()
    )

    private val _uiState = MutableStateFlow(DeviceListUiState())
    val uiState = _uiState.asStateFlow()

}